package space.chunks.lobby.modules.spawn

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import space.chunks.lobby.modules.LobbyModule
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService

class SpawnModule(
    private val sessSvc: DisplaySessionService,
    plugin: Plugin,
) : LobbyModule(plugin, "spawn") {
    override fun onEnable() {
        val w = Bukkit.getWorld("devlobby")!!

        Bukkit.getPluginManager().registerEvents(
            SpawnPlayerListener(w, this.sessSvc),
            this.plugin,
        )
    }

    override fun onDisable() {}
}