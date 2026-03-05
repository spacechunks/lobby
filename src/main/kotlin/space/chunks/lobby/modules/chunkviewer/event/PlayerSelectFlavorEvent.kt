package space.chunks.lobby.modules.chunkviewer.event

import chunks.space.api.explorer.chunk.v1alpha1.Types
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerSelectFlavorEvent(
    val chunk: Types.Chunk,
    val flavor: Types.Flavor,
    player: Player,
) : PlayerEvent(player) {
    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}