package space.chunks.lobby.ui.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class Space {

    companion object {
        private val positiveAdvances =
            listOf(
                1024 to "\uF82F",
                512 to "\uF82E",
                256 to "\uF82D",
                128 to "\uF82C",
                64 to "\uF82B",
                32 to "\uF82A",
                16 to "\uF829",
                8 to "\uF828",
                4 to "\uF824",
                2 to "\uF822",
                1 to "\uF821",
            )

        private val negativeAdvances =
            listOf(
                1024 to "\uF80F",
                512 to "\uF80E",
                256 to "\uF80D",
                128 to "\uF80C",
                64 to "\uF80B",
                32 to "\uF80A",
                16 to "\uF809",
                8 to "\uF808",
                4 to "\uF804",
                2 to "\uF802",
                1 to "\uF801",
            )

        fun asString(pixel: Int, tag: Boolean): String {
            if (pixel == 0) {
                return ""
            }

            val advances = if (pixel > 0) positiveAdvances else negativeAdvances
            var remaining = if (pixel < 0) pixel * -1 else pixel
            val content = StringBuilder()

            println("pixel " + pixel)

            for ((advance, glyph) in advances) {
                while (
                    (remaining > 0 && remaining >= advance)
                ) {
                    content.append(glyph)
                    remaining -= advance
                    println("remain " + remaining)
                }
            }

            println("content $content")
            println("====")

            check(remaining == 0) {
                "Could not map pixel offset $pixel to space font content."
            }

            if (tag) {
                return "<font:spacechunks-visualkit:space>$content</font>"
            } else {
                return content.toString()
            }
        }
        fun asComponent(pixel: Int): Component {
            if (pixel == 0) {
                return MiniMessage.miniMessage().deserialize("")
            }
            return MiniMessage.miniMessage().deserialize(asString(pixel, true))
        }
    }
}