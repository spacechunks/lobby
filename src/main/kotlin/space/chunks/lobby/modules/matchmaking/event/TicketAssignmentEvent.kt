package space.chunks.lobby.modules.matchmaking.event

import chunks.space.api.matchmaking.v1alpha1.Api
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TicketAssignmentEvent(
    val actorId: String,
    val ticket: Api.Ticket,
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

