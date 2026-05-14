package space.chunks.lobby.modules.queue

import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import space.chunks.lobby.modules.LobbyModule
import space.chunks.visual.ui.BossBarSlot
import space.chunks.visual.ui.UiService
import java.util.UUID

class QueueModule(
    plugin: Plugin,
    private val uiService: UiService,
) : LobbyModule(plugin, "queue") {
    private val queueBar: BossBarSlot = this.uiService.bossBars.register("queue", order = 1)
    private val enabledPlayers = mutableSetOf<UUID>()

    override fun onEnable() {
        this.plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
            commands.registrar().register(command())
        }
    }

    override fun onDisable() {}

    private fun command() =
        Commands.literal("queuebar")
            .executes { ctx ->
                val player = ctx.source.sender as Player

                if (enabledPlayers.remove(player.uniqueId)) {
                    uiService.clear(player, queueBar)
                    player.sendMessage(Component.text("Queue bossbar disabled."))
                } else {
                    enabledPlayers.add(player.uniqueId)
                    uiService.set(
                        player,
                        queueBar,
                        QueueBossBar.component("Chefs", "default", QueueBossBar.QueueState.WAITING_FOR_PLAYERS, players = 3, maxPlayers = 8),
                    )
                    player.sendMessage(Component.text("Queue bossbar enabled."))
                }

                Command.SINGLE_SUCCESS
            }
            .build()
}
