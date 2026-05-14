package space.chunks.visual

import space.chunks.visual.layout.VisualBackground
import space.chunks.visual.layout.VisualComponent
import space.chunks.visual.layout.VisualFlexRow
import space.chunks.visual.text.VisualFonts
import space.chunks.visual.text.VisualSpace
import space.chunks.visual.text.VisualText

object VisualKit {
    object Player {
        fun head(name: String, color: String = "#FFFFFF"): VisualComponent =
            VisualComponent(8, VisualText.raw("<!shadow><color:$color><head:$name:true></color>"))
    }

    object Hud {
        object Hotbar {
            const val start = 64
            const val end = 91
            const val width = end - start
        }
    }

    object BossBar {
        object Translucent15 {
            val end: VisualComponent = part(0)
            val start: VisualComponent = part(1)
            val right: VisualComponent = part(1)
            val center: VisualComponent = part(2)
            val left: VisualComponent = part(3)
            val threePartBackground: VisualComponent =
                VisualFlexRow(gap = -1)
                    .children(listOf(left, center, right))
                    .toComponent()
            val stretchBackground: VisualBackground = VisualBackground { width -> background(width) }

            private val parts = listOf(128, 64, 32, 16, 8, 4, 2, 1)

            private fun background(width: Int): VisualComponent {
                var remaining = width
                val elements = mutableListOf<VisualText>()

                for (partWidth in parts) {
                    while (remaining >= partWidth) {
                        if (elements.isNotEmpty()) {
                            elements.add(VisualSpace.pixels(-1))
                        }

                        elements.add(backgroundPart(partWidth).asVisualText())
                        remaining -= partWidth
                    }
                }

                return VisualComponent(width, VisualText.of(*elements.toTypedArray()))
            }

            private fun part(index: Int): VisualComponent =
                VisualComponent.glyph(15, VisualFonts.SpaceChunksVisualKit.bossBarFix, 0xE110 + index)

            private fun backgroundPart(width: Int): VisualComponent =
                VisualComponent.glyph(width, VisualFonts.SpaceChunksVisualKit.bossBarFix, 0xE110 + Integer.numberOfTrailingZeros(width))

            private fun VisualComponent.asVisualText(): VisualText =
                VisualText.of(this)
        }

        object Translucent28 {
            val end: VisualComponent = part(0)
            val start: VisualComponent = part(1)
            val right: VisualComponent = part(1)
            val center: VisualComponent = part(2)
            val left: VisualComponent = part(3)
            val threePartBackground: VisualComponent =
                VisualFlexRow(gap = -1)
                    .children(listOf(left, center, right))
                    .toComponent()
            val stretchBackground: VisualBackground = VisualBackground { width -> background(width) }

            private val parts = listOf(128, 64, 32, 16, 8, 4, 2, 1)

            private fun background(width: Int): VisualComponent {
                var remaining = width
                val elements = mutableListOf<VisualText>()

                for (partWidth in parts) {
                    while (remaining >= partWidth) {
                        if (elements.isNotEmpty()) {
                            elements.add(VisualSpace.pixels(-1))
                        }

                        elements.add(backgroundPart(partWidth).asVisualText())
                        remaining -= partWidth
                    }
                }

                return VisualComponent(width, VisualText.of(*elements.toTypedArray()))
            }

            private fun part(index: Int): VisualComponent =
                VisualComponent.glyph(28, VisualFonts.SpaceChunksVisualKit.bossBarFix, 0xE120 + index)

            private fun backgroundPart(width: Int): VisualComponent =
                VisualComponent.glyph(width, VisualFonts.SpaceChunksVisualKit.bossBarFix, 0xE120 + Integer.numberOfTrailingZeros(width))

            private fun VisualComponent.asVisualText(): VisualText =
                VisualText.of(this)
        }
    }
}
