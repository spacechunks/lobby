package space.chunks.visual.text

data class VisualFont(
    val namespace: String,
    val path: String,
) {
    val key: String = "$namespace:$path"

    fun glyph(codePoint: Int, shadow: Boolean = false): VisualGlyph =
        VisualGlyph(this, codePoint, shadow)

    fun text(content: String, shadow: Boolean = false): VisualText =
        VisualText(wrap(content, shadow))

    fun wrap(content: String, shadow: Boolean = false): String {
        val shadowPrefix = if (shadow) "" else "<!shadow>"
        return "$shadowPrefix<font:$key>$content</font>"
    }
}
