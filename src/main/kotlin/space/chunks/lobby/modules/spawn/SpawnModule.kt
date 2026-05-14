package space.chunks.lobby.modules.spawn

import org.bukkit.Bukkit
import org.bukkit.GameRules
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import space.chunks.lobby.modules.LobbyModule
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService
import space.chunks.lobby.ui.ActionBar
import space.chunks.lobby.ui.Texts

class SpawnModule(
    private val sessSvc: DisplaySessionService,
    plugin: Plugin,
    private val texts: Texts,
) : LobbyModule(plugin, "spawn") {
    private var actionBarTask: BukkitTask? = null

    override fun onEnable() {
        val cfg = Config.parse(this.plugin.config)
        Bukkit.getPluginManager().registerEvents(
            PlayerListener(this.plugin, cfg, this.sessSvc, this.texts),
            this.plugin,
        )

        Bukkit.getServer().getWorld(cfg.world)?.setGameRule(GameRules.LOCATOR_BAR, false)

        this.actionBarTask = Bukkit.getScheduler().runTaskTimer(this.plugin, Runnable {
            Bukkit.getOnlinePlayers()
                .filter { this.sessSvc.getSession(it) == null }
                .forEach(ActionBar::send)
        }, 0L, 20L)
    }

    override fun onDisable() {
        this.actionBarTask?.cancel()
        this.actionBarTask = null
    }
}
