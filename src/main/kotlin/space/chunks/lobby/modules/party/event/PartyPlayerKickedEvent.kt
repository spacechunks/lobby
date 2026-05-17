package space.chunks.lobby.modules.party.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.chunks.lobby.modules.party.Party
import space.chunks.lobby.modules.party.PartyPlayer

class PartyPlayerKickedEvent(val party: Party, val player: PartyPlayer): Event() {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}