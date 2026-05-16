package space.chunks.visual.layout

import space.chunks.visual.text.VisualElement
import space.chunks.visual.text.VisualFont

data class VisualComponent(
    val width: Int,
    val advance: Int = width,
    private val element: VisualElement,
) : VisualElement {
    init {
        require(width >= 0) { "width must be zero or positive." }
        require(advance >= 0) { "advance must be zero or positive." }
    }

    override fun asMiniMessage(): String =
        element.asMiniMessage()

    companion object {
        fun glyph(width: Int, font: VisualFont, codePoint: Int, shadow: Boolean = false): VisualComponent =
            VisualComponent(width, element = font.glyph(codePoint, shadow))
    }
}
