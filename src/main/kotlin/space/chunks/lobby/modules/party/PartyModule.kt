package space.chunks.lobby.modules.party

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import space.chunks.lobby.modules.LobbyModule
import space.chunks.lobby.modules.party.event.PartyDisbandEvent
import space.chunks.lobby.modules.party.event.PartyInviteEvent
import space.chunks.lobby.modules.party.event.PartyInviteStatus
import space.chunks.lobby.modules.party.event.PartyPlayerKickedEvent
import space.chunks.lobby.ui.Texts
import space.chunks.lobby.ui.bossbar.BossBars

class PartyModule(
    plugin: Plugin,
    private val partyService: PartyService,
    private val texts: Texts,
    private val bossBars: BossBars,
) : LobbyModule(plugin, "party") {

    override fun onEnable() {
        PartyCommands.root(this.partyService, this.texts).forEach {
            this.plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
                commands.registrar().register(it)
            }
        }

        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    override fun onDisable() {}

    @EventHandler
    private fun onPartyDisband(event: PartyDisbandEvent) {
        val party = event.party
        this.texts.send(
            party,
            "party.disband.broadcast",
            mapOf("actor" to this.texts.player(party.owner.name))
        )

        this.bossBars.clearPartyBar(party.onlinePlayers())
    }

    @EventHandler
    private fun onPartyUpdate(event: PartyPlayerKickedEvent) {
        event.player.asPlayer()?.let {
            bossBars.clearPartyBar(it)
        }
    }

    @EventHandler
    private fun onPartyInvite(event: PartyInviteEvent) {
        val party = event.party
        val invitee = event.invitee
        val inviter = event.invitee


        when (event.status) {
            PartyInviteStatus.ACCEPTED -> {
                this.texts.send(
                    this.partyService.getParty(invitee.id) ?: return,
                    "party.invite.accepted-broadcast",
                    mapOf("member" to this.texts.player(invitee.name))
                )
                this.bossBars.partyBar(party.onlinePlayers(), party)
            }

            PartyInviteStatus.PENDING -> {
                Bukkit.getPlayer(event.invitee.id)?.let {
                    this.sendPendingInvite(it, inviter, event.inviteId)
                }
            }

            PartyInviteStatus.DECLINED -> {
                Bukkit.getPlayer(inviter.id)?.let {
                    this.texts.send(
                        it,
                        "party.invite.declined-target",
                        mapOf("member" to this.texts.player(invitee.name))
                    )
                }
            }
        }
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        this.partyService.getParty(player.uniqueId)?.let {
            this.bossBars.partyBar(player, it)
        }

        this.partyService.getPendingInvites(event.player.uniqueId).forEach { invite ->
            val party = this.partyService.getPartyById(invite.partyId) ?: return@forEach
            this.sendPendingInvite(event.player, party.owner, invite.id)
        }
    }

    private fun sendPendingInvite(invitee: Player, inviter: PartyPlayer, inviteId: String) {
        val party = this.partyService.getParty(inviter.id)
        val extraPlayers = party?.members?.size ?: 0

        this.texts.send(
            invitee,
            "party.invite.received",
            mapOf(
                "inviter" to this.texts.player(inviter.name),
                "invite_id" to inviteId,
                "party_count_line" to if (extraPlayers > 0) {
                    this.texts.raw("party.invite.party-count-line", mapOf("count" to extraPlayers))
                } else {
                    this.texts.raw("party.invite.party-count-empty")
                }
            )
        )
    }
}
