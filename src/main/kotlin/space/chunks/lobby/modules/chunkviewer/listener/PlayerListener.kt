package space.chunks.lobby.modules.chunkviewer.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService

class PlayerListener(
    private val sessionService: DisplaySessionService,
) : Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        this.sessionService.closeSession(player)
    }
}
