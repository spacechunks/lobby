package space.chunks.lobby.spawn

import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerListener : Listener {

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        val spawn = Location(player.world, 0.5, -42.0, 0.5)

        player.teleport(spawn)
    }
}