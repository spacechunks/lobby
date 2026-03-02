package space.chunks.lobby.modules

import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.util.logging.Logger

abstract class LobbyModule(
    val plugin: Plugin,
    moduleName: String,
) : Listener {
    protected val logger by lazy {
        val l = Logger.getLogger(moduleName)
        l.parent = this.plugin.logger
        return@lazy l
    }

    // FIXME: at some point provide some sort of isolation
    // i.e. a separate config file and subfolder of this.plugin.dataFolder
    // used as a data folder of the module.
    val config = this.plugin.config
    val dataFolder = this.plugin.dataFolder

    abstract fun onEnable()
    abstract fun onDisable()
}