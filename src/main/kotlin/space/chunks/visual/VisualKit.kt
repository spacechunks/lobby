package space.chunks.visual

import space.chunks.visual.layout.VisualComponent
import space.chunks.visual.text.VisualFonts
import space.chunks.visual.text.VisualText

object VisualKit {
    object Player {
        fun head(name: String, color: String = "#FFFFFF"): VisualComponent =
            VisualComponent.of(8, VisualText.raw("<!shadow><color:$color><head:$name:true></color>"))
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
            val threePartBackground: VisualComponent = VisualComponent.row(
                gap = -1,
                left,
                center,
                right,
            )

            private fun part(index: Int): VisualComponent =
                VisualComponent.glyph(15, VisualFonts.SpaceChunksVisualKit.bossBarFix, 0xE110 + index)
        }
    }
}
