package space.chunks.lobby.modules.party.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.entity.Player
import space.chunks.lobby.modules.party.Party

class PartyUpdateEvent(
    val party: Party,
    val clearPlayers: List<Player> = emptyList(),
) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
