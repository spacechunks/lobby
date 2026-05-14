package space.chunks.visual.layout

import space.chunks.visual.text.VisualText

fun interface VisualBackground {
    fun render(width: Int): VisualComponent

    companion object {
        fun fixed(component: VisualComponent): VisualBackground =
            VisualBackground { component }

        fun repeat(tile: VisualComponent, gap: Int = 0): VisualBackground =
            VisualBackground { width ->
                if (width == 0 || tile.width == 0) {
                    return@VisualBackground VisualComponent(width, element = VisualText.empty)
                }

                val step = tile.width + gap
                require(step > 0) { "tile width plus gap must be positive." }

                val count = generateSequence(1) { it + 1 }
                    .first { it * tile.width + (it - 1) * gap >= width }

                var row = VisualFlexRow(gap = gap)
                kotlin.repeat(count) {
                    row = row.child(tile)
                }

                VisualComponent(width, element = VisualText.of(row.toComponent()))
            }
    }
}
