package space.chunks.lobby.modules.party

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
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
    override fun onEnable() {
        this.plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, {
            commands -> commands.registrar().register(PartyCommands.root(this.partyService))
        })
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    override fun onDisable() {}

    @EventHandler
    private fun onPartyDisband(event: PartyDisbandEvent) {
        event.party.sendMessage(Component.text("The party has been disbanded.", NamedTextColor.RED))
    }

    @EventHandler
    private fun onPartyInvite(event: PartyInviteEvent) {
        val invitee = event.player
        val inviter = event.inviter

        when (event.status) {
            PartyInviteStatus.ACCEPTED -> {
                val msg = Component.text("${invitee.name} joined the party.")
                this.partyService.getParty(invitee)?.sendMessage(msg)
            }
            PartyInviteStatus.PENDING -> {
                val msg = Component
                    .text("${inviter?.name} has invited you to their party")
                    .append(Component.text(" "))
                    .append(
                        Component.text(" [ACCEPT] ")
                            .color(NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.runCommand("/party accept ${event.inviteId}"))
                    )
                .append(
                    Component.text("[DECLINE]")
                        .color(NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/party decline ${event.inviteId}"))
                )

                invitee.sendMessage(msg)
            }
            PartyInviteStatus.DECLINED -> {
                val msg = Component.text("${invitee.name} does not want to join your party :(")
                inviter?.sendMessage(msg)
            }
        }

    }
}