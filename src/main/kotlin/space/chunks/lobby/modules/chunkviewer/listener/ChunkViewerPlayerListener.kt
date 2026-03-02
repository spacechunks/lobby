package space.chunks.lobby.modules.chunkviewer.listener

import org.bukkit.GameRule
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import space.chunks.lobby.modules.chunkviewer.display.ChunkDisplay
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService

class ChunkViewerPlayerListener(
    private val plugin: Plugin,
    private val sessionService: DisplaySessionService,
    private val spawn: Vector,
    private val chunks: List<ChunkDisplay>
) : Listener {

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        val w = event.world
        w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        w.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        w.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        w.setGameRule(GameRule.DO_FIRE_TICK, false)
        w.setGameRule(GameRule.DO_MOB_LOOT, false)
        w.setGameRule(GameRule.DO_TILE_DROPS, false)
        w.time = 1000
        w.clearWeatherDuration = -1
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        this.sessionService.closeSession(event.player)
    }
}