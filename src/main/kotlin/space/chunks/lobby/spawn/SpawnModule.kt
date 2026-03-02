package space.chunks.lobby.spawn

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import space.chunks.lobby.LobbyModule
import space.chunks.lobby.chunkviewer.display.DisplaySessionService

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