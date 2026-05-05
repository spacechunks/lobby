package space.chunks.lobby.modules.party.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.chunks.lobby.modules.party.PartyPlayer

enum class PartyInviteStatus {
    ACCEPTED,
    PENDING,
    DECLINED,
}

class PartyInviteEvent(
    val invitee: PartyPlayer,
    val inviter: PartyPlayer,
    val inviteId: String,
    val status: PartyInviteStatus,
) : Event() {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}