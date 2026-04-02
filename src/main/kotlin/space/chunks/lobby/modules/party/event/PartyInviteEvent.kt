package space.chunks.lobby.modules.party.event

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

enum class PartyInviteStatus {
    ACCEPTED,
    PENDING,
    DECLINED,
}

class PartyInviteEvent(
    player: Player,
    val inviter: Player?,
    val inviteId: String,
    val status: PartyInviteStatus,
): PlayerEvent(player) {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}