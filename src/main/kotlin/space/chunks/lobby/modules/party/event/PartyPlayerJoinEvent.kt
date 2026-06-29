package space.chunks.lobby.modules.party.event

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.chunks.lobby.modules.party.Party
import space.chunks.lobby.modules.party.PartyPlayer

class PartyPlayerJoinEvent(
    val party: Party,
    val player: PartyPlayer,
) : Event(), Cancellable {
    private var cancelled: Boolean = false

    override fun getHandlers(): HandlerList = HANDLERS

    override fun isCancelled(): Boolean {
        return this.cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancelled = cancel
    }

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}