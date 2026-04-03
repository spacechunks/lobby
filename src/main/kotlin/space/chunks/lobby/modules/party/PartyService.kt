package space.chunks.lobby.modules.party

import com.google.common.cache.CacheBuilder
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import space.chunks.lobby.modules.party.event.PartyDisbandEvent
import space.chunks.lobby.modules.party.event.PartyInviteEvent
import space.chunks.lobby.modules.party.event.PartyInviteStatus
import java.util.concurrent.TimeUnit
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Party @OptIn(ExperimentalUuidApi::class) constructor(
    val owner: Player,
    val members: MutableSet<Player> = mutableSetOf(),
    val id: String = Uuid.generateV7().toHexDashString()
) : ForwardingAudience {
    override fun audiences(): Iterable<Audience> {
        val l = mutableSetOf<Audience>()
        l.addAll(members)
        l.add(owner)
        return l
    }
}

data class PartyInvite @OptIn(ExperimentalUuidApi::class) constructor(
    val partyId: String,
    val player: Player,
    val id: String = Uuid.generateV7().toHexDashString()
)

class PartyService {
    private val parties = CacheBuilder<String, Party>
        .newBuilder()
        .expireAfterAccess(60, TimeUnit.MINUTES)
        .removalListener<String, Party> {
            this.partyByPlayer.remove(it.value?.owner)
            it.value?.members?.forEach { member ->
                this.partyByPlayer.remove(member)
            }
        }
        .build<String, Party>()

    private val invites = CacheBuilder<String, PartyInvite>
        .newBuilder()
        .expireAfterAccess(30, TimeUnit.SECONDS)
        .build<String, PartyInvite>()

    private val partyByPlayer = mutableMapOf<Player, Party>()

    fun invitePlayer(inviter: Player, invitee: Player) {
        val inviterParty = this.getParty(inviter)
        if (inviter == invitee) {
            throw PartyException(PartyExceptionReason.INVITER_IS_INVITEE)
        }

        if (inviterParty == null) {
            val party = Party(inviter)

            this.parties.put(party.id, party)
            this.partyByPlayer[inviter] = party

            val invite = PartyInvite(party.id, invitee)
            this.invites.put(invite.id, invite)

            Bukkit.getPluginManager().callEvent(
                PartyInviteEvent(invitee, inviter, invite.id, PartyInviteStatus.PENDING)
            )
            return
        }

        // the inviter must be the owner of the party to invite players
        if (inviterParty.owner != inviter) {
            throw PartyException(PartyExceptionReason.NOT_OWNER)
        }

        val invite = PartyInvite(inviterParty.id, invitee)
        this.invites.put(invite.id, invite)

        Bukkit.getPluginManager().callEvent(
            PartyInviteEvent(invitee, inviter, invite.id, PartyInviteStatus.PENDING)
        )
    }

    fun acceptInvite(inviteId: String) {
        val invite = this.invites.getIfPresent(inviteId) ?: throw PartyException(PartyExceptionReason.INVITE_GONE)
        val party = this.parties.getIfPresent(invite.partyId) ?: throw PartyException(PartyExceptionReason.PARTY_GONE)

        // leave the party the player is currently part of, if they accept another invite
        this.getParty(invite.player)?.let {
            this.leaveParty(it.id, invite.player, invite.player)
        }

        party.members.add(invite.player)
        this.partyByPlayer[invite.player] = party
        this.invites.invalidate(inviteId)

        Bukkit.getPluginManager().callEvent(
            PartyInviteEvent(invite.player, party.owner, invite.id, PartyInviteStatus.ACCEPTED)
        )
    }

    fun declineInvite(inviteId: String) {
        val invite = this.invites.getIfPresent(inviteId) ?: throw PartyException(PartyExceptionReason.INVITE_GONE)

        // since the party is gone and the player declined the invite, there is no need to inform anyone
        val party = this.parties.getIfPresent(invite.partyId) ?: throw PartyException(PartyExceptionReason.PARTY_GONE)

        this.invites.invalidate(inviteId)
        Bukkit.getPluginManager().callEvent(
            PartyInviteEvent(invite.player, party.owner, invite.id, PartyInviteStatus.DECLINED)
        )
    }

    fun getParty(player: Player): Party? {
        val p1 = partyByPlayer[player] ?: return null

        // the cache does not call the removal listener just because the entry is expired.
        // we'd need to call parties.cleanUp() for this. but since we try to avoid having
        // a repeating task for this, we try to check if the party is still present in the
        // cache before returning it.
        val p2 = this.parties.getIfPresent(p1.id)
        if (p2 == null) {
            // the party is gone so clean up
            this.partyByPlayer.remove(player)
            return null
        }
        return p2
    }

    fun disbandParty(partyId: String, actor: Player) {
        val party = this.parties.getIfPresent(partyId) ?: throw PartyException(PartyExceptionReason.PARTY_GONE)

        if (party.owner != actor) {
            throw PartyException(PartyExceptionReason.NOT_OWNER)
        }

        this.partyByPlayer.remove(party.owner)
        party.members.forEach {
            this.partyByPlayer.remove(it.player)
        }

        this.parties.invalidate(partyId)
        Bukkit.getPluginManager().callEvent(PartyDisbandEvent(party))
    }

    fun leaveParty(partyId: String, actor: Player, toKick: Player) {
        val party = this.parties.getIfPresent(partyId) ?: throw PartyException(PartyExceptionReason.PARTY_GONE)

        // we only want to check if the actor is able to kick someone
        // if the actor and the person to kick are different.
        // if they are the same, the person wants to remove itself, so
        // that is fine.
        if (actor.uniqueId != toKick.uniqueId && actor != party.owner) {
            throw PartyException(PartyExceptionReason.NOT_OWNER)
        }

        if (party.owner == toKick) {
            this.disbandParty(partyId, actor)
            return
        }

        party.members.remove(toKick)
        this.partyByPlayer.remove(toKick)

        if (party.members.isEmpty()) {
            this.disbandParty(partyId, party.owner)
        }
    }
}