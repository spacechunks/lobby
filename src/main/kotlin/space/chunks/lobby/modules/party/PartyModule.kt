package space.chunks.lobby.modules.party

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.plugin.Plugin
import space.chunks.lobby.ui.Messages
import space.chunks.lobby.modules.LobbyModule
import space.chunks.lobby.modules.party.event.PartyDisbandEvent
import space.chunks.lobby.modules.party.event.PartyInviteEvent
import space.chunks.lobby.modules.party.event.PartyInviteStatus

class PartyModule(
    plugin: Plugin,
    private val partyService: PartyService,
) : LobbyModule(plugin, "party") {

    private val messages = Messages(plugin)

    override fun onEnable() {
        PartyCommands.root(this.partyService).forEach {
            this.plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
                commands.registrar().register(PartyCommands.root(this.partyService, this.messages)
            }
        }

        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    override fun onDisable() {}

    @EventHandler
    private fun onPartyDisband(event: PartyDisbandEvent) {
        this.messages.send(
            event.party,
            "messages.party.disband.broadcast",
            mapOf("actor" to this.messages.player(event.party.owner.name))
        )
    }

    @EventHandler
    private fun onPartyInvite(event: PartyInviteEvent) {
        val invitee = Bukkit.getPlayer(event.invitee.id)!!
        val inviter = Bukkit.getPlayer(event.inviter.id)!!

        when (event.status) {

            PartyInviteStatus.ACCEPTED -> {
                this.messages.send(
                    this.partyService.getParty(invitee) ?: return,
                    "messages.party.invite.accepted-broadcast",
                    mapOf("member" to this.messages.player(invitee.name))
                )

                this.partyService.getParty(invitee.uniqueId)?.sendMessage(msg)
            }

            PartyInviteStatus.PENDING -> {
                val party = inviter?.let(this.partyService::getParty)
                val extraPlayers = party?.members?.size ?: 0

                this.messages.send(
                    invitee,
                    "messages.party.invite.received",
                    mapOf(
                        "inviter" to this.messages.player(inviter?.name ?: "Unknown"),
                        "invite_id" to event.inviteId,
                        "party_count_line" to if (extraPlayers > 0) {
                            "<prefix2> <subtle>... with $extraPlayers more players!"
                        } else {
                            "<prefix2>"
                        }
                    )
                )
            }

            PartyInviteStatus.DECLINED -> {
                inviter?.let {
                    this.messages.send(
                        it,
                        "messages.party.invite.declined-target",
                        mapOf("member" to this.messages.player(invitee.name))
                    )
                }
            }
        }
    }
}
