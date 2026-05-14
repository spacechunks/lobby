package space.chunks.visual.ui

import org.bukkit.entity.Player

data class UiRenderContext(
    val player: Player,
    val tick: Long,
)
