package space.chunks.lobby.modules.party

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import space.chunks.lobby.modules.LobbyModule
import space.chunks.lobby.modules.party.event.PartyDisbandEvent
import space.chunks.lobby.modules.party.event.PartyInviteEvent
import space.chunks.lobby.modules.party.event.PartyInviteStatus
import space.chunks.lobby.modules.party.event.PartyUpdateEvent
import space.chunks.lobby.ui.PartyBossBar
import space.chunks.lobby.ui.Texts
import space.chunks.visual.ui.BossBarSlot
import space.chunks.visual.ui.UiService

class PartyModule(
    plugin: Plugin,
    private val partyService: PartyService,
    private val texts: Texts,
    private val uiService: UiService,
) : LobbyModule(plugin, "party") {
    private val partyBar: BossBarSlot = this.uiService.bossBars.register("party", order = 0)

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
        this.texts.send(
            event.party,
            "party.disband.broadcast",
            mapOf("actor" to this.texts.player(event.party.owner.name))
        )
        this.uiService.clear(event.party.onlinePlayers(), this.partyBar)
    }

    @EventHandler
    private fun onPartyUpdate(event: PartyUpdateEvent) {
        this.uiService.clear(event.clearPlayers, this.partyBar)
        this.uiService.set(
            event.party.onlinePlayers(),
            this.partyBar,
            PartyBossBar.component(event.party),
        )
    }

    @EventHandler
    private fun onPartyInvite(event: PartyInviteEvent) {
        when (event.status) {
            PartyInviteStatus.ACCEPTED -> {
                this.texts.send(
                    this.partyService.getParty(event.invitee.id) ?: return,
                    "party.invite.accepted-broadcast",
                    mapOf("member" to this.texts.player(event.invitee.name))
                )
            }

            PartyInviteStatus.PENDING -> {
                Bukkit.getPlayer(event.invitee.id)?.let {
                    this.sendPendingInvite(it, event.inviter, event.inviteId)
                }
            }

            PartyInviteStatus.DECLINED -> {
                Bukkit.getPlayer(event.inviter.id)?.let {
                    this.texts.send(
                        it,
                        "party.invite.declined-target",
                        mapOf("member" to this.texts.player(event.invitee.name))
                    )
                }
            }
        }
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        this.partyService.getParty(event.player.uniqueId)?.let {
            this.uiService.set(event.player, this.partyBar, PartyBossBar.component(it))
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
