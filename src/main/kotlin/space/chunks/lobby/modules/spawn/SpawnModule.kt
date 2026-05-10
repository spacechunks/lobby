package space.chunks.lobby.modules.spawn

import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import space.chunks.lobby.modules.LobbyModule
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService
import space.chunks.lobby.ui.ActionBar

class SpawnModule(
    private val sessSvc: DisplaySessionService,
    plugin: Plugin,
) : LobbyModule(plugin, "spawn") {

    override fun onEnable() {
        val cfg = Config.parse(this.plugin.config)
        Bukkit.getPluginManager().registerEvents(
            PlayerListener(this.plugin, cfg, this.sessSvc),
            this.plugin,
        )

        runTask()
    }

    override fun onDisable() {}

    private var currentHealth = 20
    private var currentGravity = 20
    private var voiceChatEnabled = true
    private var chatChannel = ActionBar.ChatChannel.GLOBAL

    private fun runTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, { _ ->
            runBlocking {
                Bukkit.getOnlinePlayers().forEach { player ->
                    ActionBar.send(player, currentHealth, currentGravity, voiceChatEnabled, chatChannel)
                }

                currentHealth = nextValue(currentHealth)
                currentGravity = nextValue(currentGravity)
                voiceChatEnabled = !voiceChatEnabled
                chatChannel =
                    when (chatChannel) {
                        ActionBar.ChatChannel.GLOBAL -> ActionBar.ChatChannel.TEAM
                        ActionBar.ChatChannel.TEAM -> ActionBar.ChatChannel.GLOBAL
                    }
            }
        }, 0, 20)
    }

    private fun nextValue(value: Int): Int {
        return if (value <= 0) 20 else value - 1
    }
}