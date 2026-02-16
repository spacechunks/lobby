package space.chunks.explorer.lobby.listener

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.event.world.WorldLoadEvent
import space.chunks.explorer.lobby.Plugin
import space.chunks.explorer.lobby.display.DisplaySession

class PlayerListener(
    private val plugin: Plugin,
    private val sessions: MutableMap<Player, DisplaySession>,
) : Listener {

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        event.world.entities.forEach { it.remove() }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        prepareWorld(player.location.world)

        val loc = Location(player.location.world, 0.0, 100.0, 0.0)

        val sess = DisplaySession(player, this.plugin, loc)
        this.sessions[player] = sess

        sess.start()
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        this.sessions[event.player]?.stop()
        this.sessions.remove(event.player)
    }

    @EventHandler
    fun onSpectateUnmount(event: PlayerStopSpectatingEntityEvent) {
        event.isCancelled = true
    }

    private fun prepareWorld(voidWorld: World) {
        voidWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        voidWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        voidWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        voidWorld.setGameRule(GameRule.DO_FIRE_TICK, false)
        voidWorld.setGameRule(GameRule.DO_MOB_LOOT, false)
        voidWorld.setGameRule(GameRule.DO_TILE_DROPS, false)
        voidWorld.time = 1000
        voidWorld.clearWeatherDuration = -1
    }

    @EventHandler
    fun on(e: WeatherChangeEvent) {
        e.isCancelled = true
    }
}