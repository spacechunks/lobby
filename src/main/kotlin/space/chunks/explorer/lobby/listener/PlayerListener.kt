package space.chunks.explorer.lobby.listener

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Bat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.event.weather.WeatherEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import space.chunks.explorer.lobby.Plugin

class PlayerListener(
    private val plugin: Plugin,
    private val voidWorld: World
) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        player.teleport(Location(voidWorld, 0.0, 100.0, 0.0))

        val fixedEntity = voidWorld
            .spawn(Location(voidWorld, 0.0, 100.0, 0.0), ArmorStand::class.java) {
                it.setAI(false)
                it.canPickupItems = false
                it.isInvisible = true
                it.isSilent = true
                it.setGravity(false)
            }

        player.gameMode = GameMode.SPECTATOR

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            player.spectatorTarget = fixedEntity
        }, 10)
    }

    @EventHandler
    fun onSpectateUnmount(event: PlayerStopSpectatingEntityEvent) {
//        event.isCancelled = true
    }

    @EventHandler
    fun onSpectateMount(event: PlayerStartSpectatingEntityEvent) {
//        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
//            event.player.hideEntity(plugin, event.newSpectatorTarget)
//        }, 10)
    }

    @EventHandler
    fun on(e: WeatherChangeEvent) {
        e.isCancelled = true
    }
}