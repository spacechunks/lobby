package space.chunks.explorer.lobby.display

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

abstract class Window(protected val plugin: Plugin, protected val center: Location) {
    abstract fun render()
    abstract fun close()
    abstract fun handleInput(player: Player, input: Input)
}