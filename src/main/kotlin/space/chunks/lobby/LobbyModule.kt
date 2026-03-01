package space.chunks.lobby

import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

abstract class LobbyModule(protected val plugin: Plugin) : Listener {
    abstract fun onEnable()
    abstract fun onDisable()
}