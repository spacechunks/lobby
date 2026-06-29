package space.chunks.lobby.modules.matchmaking.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.chunks.lobby.modules.matchmaking.MMData

class MatchCancelledEvent(val data: MMData): Event() {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}