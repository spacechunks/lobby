package space.chunks.lobby.modules.matchmaking.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TicketAssignmentEvent(
    val actorId: String,
    val ticketId: String,
    val instanceId: String,
): Event() {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}

