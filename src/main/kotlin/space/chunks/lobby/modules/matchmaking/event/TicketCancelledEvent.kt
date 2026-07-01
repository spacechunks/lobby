package space.chunks.lobby.modules.matchmaking.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

enum class TicketCancelCause {
    NOT_FOUND,
    NO_PLAYABLE_FLAVOR_VERSION,
    SERVICE_UNAVAILABLE,
    REMOVED,
}

class TicketCancelledEvent(val actorId: String, val ticketId: String?, val cause: TicketCancelCause) : Event() {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}