package space.chunks.explorer.lobby.listener

import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.util.Vector
import space.chunks.explorer.lobby.Plugin
import space.chunks.explorer.lobby.display.DisplaySession

class PlayerListener(
    private val plugin: Plugin,
    private val sessions: MutableMap<Player, DisplaySession>,
    private val spawn: Vector
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

        val loc = this.spawn.toLocation(player.world).add(
            Vector(Bukkit.getOnlinePlayers().size * 100.0, 0.0, 0.0),
        )

        val sess = DisplaySession(player, this.plugin, loc)
        this.sessions[player] = sess

        sess.start()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        this.sessions[event.player]?.stop()
        this.sessions.remove(event.player)
    }
}