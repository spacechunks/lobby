package space.chunks.visual.text

import net.kyori.adventure.text.Component
import space.chunks.visual.layout.VisualComponent

data class VisualFont(
    val namespace: String,
    val path: String,
    val metrics: VisualFontMetrics? = null,
) {
    val key: String = "$namespace:$path"

    fun glyph(codePoint: Int, shadow: Boolean = false): VisualGlyph =
        VisualGlyph(this, codePoint, shadow)

    fun text(content: String, shadow: Boolean = false): VisualText =
        VisualText(wrap(content, shadow))

    fun formattedText(content: String, shadow: Boolean = false): VisualText =
        VisualText(wrapFormatted(content, shadow))

    fun component(content: String, shadow: Boolean = false): VisualComponent =
        VisualComponent.of(width(content), text(content, shadow))

    fun formattedComponent(content: String, shadow: Boolean = false): VisualComponent =
        VisualComponent.of(formattedWidth(content), formattedText(content, shadow))

    fun width(content: String): Int =
        requireNotNull(metrics) { "Font $key does not have metrics configured." }
            .width(content)

    fun formattedWidth(content: String): Int =
        width(stripMiniMessageTags(content))

    fun width(component: Component): Int =
        requireNotNull(metrics) { "Font $key does not have metrics configured." }
            .width(component)

    fun wrap(content: String, shadow: Boolean = false): String {
        val shadowPrefix = if (shadow) "" else "<!shadow>"
        val renderedContent = metrics?.render(content) ?: content
        return "$shadowPrefix<font:$key>$renderedContent</font>"
    }

    fun wrapFormatted(content: String, shadow: Boolean = false): String {
        val shadowPrefix = if (shadow) "" else "<!shadow>"
        val renderedContent = metrics?.renderFormatted(content) ?: content
        return "$shadowPrefix<font:$key>$renderedContent</font>"
    }

    private fun stripMiniMessageTags(content: String): String =
        buildString {
            var inTag = false

            for (char in content) {
                when {
                    char == '<' -> inTag = true
                    char == '>' && inTag -> inTag = false
                    !inTag -> append(char)
                }
            }
        }
}
