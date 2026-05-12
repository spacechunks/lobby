package space.chunks.lobby.modules.spawn

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import space.chunks.lobby.modules.LobbyModule
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService
import space.chunks.lobby.ui.Texts

class SpawnModule(
    private val sessSvc: DisplaySessionService,
    plugin: Plugin,
    private val texts: Texts,
) : LobbyModule(plugin, "spawn") {

    override fun onEnable() {
        val cfg = Config.parse(this.plugin.config)
        Bukkit.getPluginManager().registerEvents(
            PlayerListener(this.plugin, cfg, this.sessSvc, this.texts),
            this.plugin,
        )
    }

    override fun onDisable() {}
}
