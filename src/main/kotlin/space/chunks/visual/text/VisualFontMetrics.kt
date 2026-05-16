package space.chunks.visual.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

interface VisualFontMetrics {
    fun charInfo(char: Char): VisualCharInfo

    fun width(content: String): Int

    fun render(content: String): String = content

    fun renderFormatted(content: String): String {
        val rendered = StringBuilder()
        val text = StringBuilder()
        var inTag = false

        fun flushText() {
            if (text.isNotEmpty()) {
                rendered.append(render(text.toString()))
                text.clear()
            }
        }

        for (char in content) {
            when {
                char == '<' && !inTag -> {
                    flushText()
                    inTag = true
                    rendered.append(char)
                }
                char == '>' && inTag -> {
                    inTag = false
                    rendered.append(char)
                }
                inTag -> rendered.append(char)
                else -> text.append(char)
            }
        }

        flushText()
        return rendered.toString()
    }

    fun width(component: Component): Int {
        val text = component as? TextComponent
            ?: throw IllegalArgumentException("Component must be a TextComponent.")

        return width(text.content()) + text.children().sumOf { width(it) }
    }
}
