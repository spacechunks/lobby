package space.chunks.lobby.modules.party

import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener(private val partyService: PartyService) : Listener {
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        this.partyService.getParty(player)?.let {
            this.partyService.leaveParty(it.id, player, player)
        }
    }
}