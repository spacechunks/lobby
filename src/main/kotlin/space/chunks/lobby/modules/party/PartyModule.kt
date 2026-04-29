package space.chunks.lobby.modules.party

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.plugin.Plugin
import space.chunks.lobby.modules.LobbyModule
import space.chunks.lobby.modules.party.event.PartyDisbandEvent
import space.chunks.lobby.modules.party.event.PartyInviteEvent
import space.chunks.lobby.modules.party.event.PartyInviteStatus

class PartyModule(
    plugin: Plugin,
    private val partyService: PartyService,
) : LobbyModule(plugin, "party") {

    private val mm = MiniMessage.miniMessage()

    override fun onEnable() {
        PartyCommands.root(this.partyService).forEach {
            this.plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
                commands.registrar().register(it)
            }
        }
        
        Bukkit.getPluginManager().registerEvents(this, plugin)
        Bukkit.getPluginManager().registerEvents(PlayerListener(this.partyService), plugin)
    }

    override fun onDisable() {}

    @EventHandler
    private fun onPartyDisband(event: PartyDisbandEvent) {
        event.party.sendMessage(
            mm.deserialize("<#DC2626>The party has been disbanded.")
        )
    }

    @EventHandler
    private fun onPartyInvite(event: PartyInviteEvent) {
        val invitee = event.player
        val inviter = event.inviter

        when (event.status) {

            PartyInviteStatus.ACCEPTED -> {
                val msg = mm.deserialize(
                    "<white><head:${invitee.name}:true> <#7c3aed>${invitee.name} <#E2E8F0>joined the party."
                )

                this.partyService.getParty(invitee)?.sendMessage(msg)
            }

            PartyInviteStatus.PENDING -> {
                val msg = mm.deserialize(
                    "<#0EA5E9>You received a party invite from " +
                            "<white><head:${inviter?.name}:true> <#F8FAFC>${inviter?.name}<#0EA5E9>." +
                            "<br>   <click:run_command:'/party accept ${event.inviteId}'>" +
                            "<hover:show_text:'<#E2E8F0>Join the player\\'s party.<br><bold><#A3E635>Click to accept'>" +
                            "<#A3E635>[ACCEPT]</hover></click> " +
                            "<click:run_command:'/party decline ${event.inviteId}'>" +
                            "<hover:show_text:'<#E2E8F0>Decline the party invite.<br><bold><#DC2626>Click to decline'>" +
                            "<#DC2626>[DECLINE]</hover></click>"
                )

                invitee.sendMessage(msg)
            }

            PartyInviteStatus.DECLINED -> {
                val msg = mm.deserialize(
                    "<white><head:${invitee.name}:true> <#F8FAFC>${invitee.name} <#F59E0B>declined your party invite."
                )

                inviter?.sendMessage(msg)
            }
        }
    }
}