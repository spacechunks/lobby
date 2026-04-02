package space.chunks.lobby.modules.party.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.chunks.lobby.modules.party.Party

class PartyDisbandEvent(val party: Party): Event() {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}