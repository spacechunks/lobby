package space.chunks.lobby.spawn

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import space.chunks.lobby.LobbyModule

class SpawnModule(plugin: Plugin) : LobbyModule(plugin) {
    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(SpawnPlayerListener(), this.plugin)
    }

    override fun onDisable() {
    }
}