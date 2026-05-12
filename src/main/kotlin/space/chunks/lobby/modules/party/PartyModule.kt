package space.chunks.lobby.modules.party

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.plugin.Plugin
import space.chunks.lobby.modules.LobbyModule
import space.chunks.lobby.modules.party.event.PartyDisbandEvent
import space.chunks.lobby.modules.party.event.PartyInviteEvent
import space.chunks.lobby.modules.party.event.PartyInviteStatus
import space.chunks.lobby.ui.Texts

class PartyModule(
    plugin: Plugin,
    private val partyService: PartyService,
    private val texts: Texts,
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
        this.texts.send(
            event.party,
            "party.disband.broadcast",
            mapOf("actor" to this.texts.player(event.party.owner.name))
        )
    }

    @EventHandler
    private fun onPartyInvite(event: PartyInviteEvent) {
        val invitee = Bukkit.getPlayer(event.invitee.id)!!
        val inviter = Bukkit.getPlayer(event.inviter.id)!!

        when (event.status) {

            PartyInviteStatus.ACCEPTED -> {
                this.texts.send(
                    this.partyService.getParty(invitee.uniqueId) ?: return,
                    "party.invite.accepted-broadcast",
                    mapOf("member" to this.texts.player(invitee.name))
                )
            }

            PartyInviteStatus.PENDING -> {
                val party = this.partyService.getParty(inviter.uniqueId)
                val extraPlayers = party?.members?.size ?: 0

                this.texts.send(
                    invitee,
                    "party.invite.received",
                    mapOf(
                        "inviter" to this.texts.player(inviter.name),
                        "invite_id" to event.inviteId,
                        "party_count_line" to if (extraPlayers > 0) {
                            this.texts.raw("party.invite.party-count-line", mapOf("count" to extraPlayers))
                        } else {
                            this.texts.raw("party.invite.party-count-empty")
                        }
                    )
                )
            }

            PartyInviteStatus.DECLINED -> {
                inviter.let {
                    this.texts.send(
                        it,
                        "party.invite.declined-target",
                        mapOf("member" to this.texts.player(invitee.name))
                    )
                }
            }
        }
    }
}
