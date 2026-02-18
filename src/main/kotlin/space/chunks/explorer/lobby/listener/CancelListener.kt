package space.chunks.explorer.lobby.listener

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.weather.WeatherChangeEvent

class CancelListener : Listener {

    @EventHandler
    fun onPlayerStopSpectatingEntity(event: PlayerStopSpectatingEntityEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onWeatherChange(e: WeatherChangeEvent) {
        e.isCancelled = true
    }
}