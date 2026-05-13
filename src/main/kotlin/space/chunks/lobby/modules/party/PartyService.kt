package space.chunks.lobby.modules.party

import com.google.common.cache.CacheBuilder
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import space.chunks.lobby.modules.party.event.PartyDisbandEvent
import space.chunks.lobby.modules.party.event.PartyInviteEvent
import space.chunks.lobby.modules.party.event.PartyInviteStatus
import space.chunks.lobby.ui.PartyBossBar
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Party @OptIn(ExperimentalUuidApi::class) constructor(
    val owner: PartyPlayer,
    val members: MutableSet<PartyPlayer> = mutableSetOf(),
    val id: String = Uuid.generateV7().toHexDashString()
) : ForwardingAudience {
    private val mm = MiniMessage.miniMessage()

    private val bb: BossBar = BossBar.bossBar(
        Component.text(),
        0f,
        BossBar.Color.BLUE,
        BossBar.Overlay.PROGRESS
    )

    override fun audiences(): Iterable<Audience?> {
        val l = mutableSetOf<Audience?>()
        l.addAll(members.map { it.asPlayer() }.toSet())
        l.add(owner.asPlayer())
        return l
    }

    fun updateBossBar() {
        bb.name(PartyBossBar.buildString(this))
        bb.addViewer(this)
    }
}

data class PartyInvite @OptIn(ExperimentalUuidApi::class) constructor(
    val partyId: String,
    val player: PartyPlayer,
    val id: String = Uuid.generateV7().toHexDashString()
)

data class PartyPlayer(val id: UUID, val name: String) {
    fun asPlayer() = Bukkit.getPlayer(name)
}

class PartyService {
    private val parties = CacheBuilder<String, Party>
        .newBuilder()
        // the main reason for this is, that in order to have a very simple
        // party implementation we just keep the party alive for x minutes,
        // so that once the party leaves the server to join a chunk, the party
        // will still be present when they return to the lobby. bummer, if they
        // play for more than x minutes. the party will be gone, and they have to
        // invite all people again.
        //
        // at some point we will implement a better system, but right now this suffices.
        .expireAfterAccess(60, TimeUnit.MINUTES)
        .removalListener<String, Party> {
            this.partyByPlayer.remove(it.value?.owner?.id)
            it.value?.members?.forEach { member ->
                this.partyByPlayer.remove(member.id)
            }
        }
        .build<String, Party>()

    private val invites = CacheBuilder<String, PartyInvite>
        .newBuilder()
        .expireAfterAccess(30, TimeUnit.SECONDS)
        .build<String, PartyInvite>()

    private val partyByPlayer = mutableMapOf<UUID, Party>()

    fun invitePlayer(inviter: PartyPlayer, invitee: PartyPlayer) {
        val inviterParty = this.getParty(inviter.id)
        if (inviter.id == invitee.id) {
            throw PartyException(PartyExceptionReason.INVITER_IS_INVITEE)
        }

        if (inviterParty == null) {
            val party = Party(inviter)

            this.parties.put(party.id, party)
            this.partyByPlayer[inviter.id] = party

            val invite = PartyInvite(party.id, invitee)
            this.invites.put(invite.id, invite)

            Bukkit.getPluginManager().callEvent(
                PartyInviteEvent(invitee, inviter, invite.id, PartyInviteStatus.PENDING, party)
            )
            return
        }

        // the inviter must be the owner of the party to invite players
        if (inviterParty.owner.id != inviter.id) {
            throw PartyException(PartyExceptionReason.NOT_OWNER)
        }

        val invite = PartyInvite(inviterParty.id, invitee)
        this.invites.put(invite.id, invite)

        Bukkit.getPluginManager().callEvent(
            PartyInviteEvent(invitee, inviter, invite.id, PartyInviteStatus.PENDING, inviterParty)
        )
    }

    fun acceptInvite(inviteId: String) {
        val invite = this.invites.getIfPresent(inviteId) ?: throw PartyException(PartyExceptionReason.INVITE_GONE)
        val party = this.parties.getIfPresent(invite.partyId) ?: throw PartyException(PartyExceptionReason.PARTY_GONE)

        // leave the party the player is currently part of, if they accept another invite
        this.getParty(invite.player.id)?.let {
            this.leaveParty(it.id, invite.player.id, invite.player.id)
        }

        party.members.add(invite.player)
        this.partyByPlayer[invite.player.id] = party
        this.invites.invalidate(inviteId)

        Bukkit.getPluginManager().callEvent(
            PartyInviteEvent(invite.player, party.owner, invite.id, PartyInviteStatus.ACCEPTED, party)
        )
    }

    fun declineInvite(inviteId: String) {
        val invite = this.invites.getIfPresent(inviteId) ?: throw PartyException(PartyExceptionReason.INVITE_GONE)

        // since the party is gone and the player declined the invite, there is no need to inform anyone
        val party = this.parties.getIfPresent(invite.partyId) ?: throw PartyException(PartyExceptionReason.PARTY_GONE)

        this.invites.invalidate(inviteId)
        Bukkit.getPluginManager().callEvent(
            PartyInviteEvent(invite.player, party.owner, invite.id, PartyInviteStatus.DECLINED, party)
        )
    }

    fun getParty(playerId: UUID): Party? {
        val p1 = partyByPlayer[playerId] ?: return null

        // the cache does not call the removal listener just because the entry is expired.
        // we'd need to call parties.cleanUp() for this. but since we try to avoid having
        // a repeating task for this, we try to check if the party is still present in the
        // cache before returning it.
        val p2 = this.parties.getIfPresent(p1.id)
        if (p2 == null) {
            // the party is gone so clean up
            this.partyByPlayer.remove(playerId)
            return null
        }
        return p2
    }

    fun disbandParty(partyId: String, actorId: UUID) {
        val party = this.parties.getIfPresent(partyId) ?: throw PartyException(PartyExceptionReason.PARTY_GONE)

        if (party.owner.id != actorId) {
            throw PartyException(PartyExceptionReason.NOT_OWNER)
        }

        this.partyByPlayer.remove(party.owner.id)
        party.members.forEach {
            this.partyByPlayer.remove(it.id)
        }

        this.parties.invalidate(partyId)
        Bukkit.getPluginManager().callEvent(PartyDisbandEvent(party))
    }

    fun leaveParty(partyId: String, actorId: UUID, toKick: UUID) {
        val party = this.parties.getIfPresent(partyId) ?: throw PartyException(PartyExceptionReason.PARTY_GONE)

        // we only want to check if the actor is able to kick someone
        // if the actor and the person to kick are different.
        // if they are the same, the person wants to remove itself, so
        // that is fine.
        if (actorId != toKick && actorId != party.owner.id) {
            throw PartyException(PartyExceptionReason.NOT_OWNER)
        }

        if (party.owner.id == toKick) {
            this.disbandParty(partyId, actorId)
            return
        }

        party.members.removeIf { it.id == toKick }
        this.partyByPlayer.remove(toKick)

        if (party.members.isEmpty()) {
            this.disbandParty(partyId, party.owner.id)
        }
    }
}