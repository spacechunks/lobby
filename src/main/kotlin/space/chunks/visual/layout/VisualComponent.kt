package space.chunks.visual.layout

import space.chunks.visual.text.VisualElement
import space.chunks.visual.text.VisualFont

data class VisualComponent(
    val width: Int,
    private val element: VisualElement,
) : VisualElement {
    init {
        require(width >= 0) { "width must be zero or positive." }
    }

    override fun asMiniMessage(): String =
        element.asMiniMessage()

    companion object {
        fun of(width: Int, element: VisualElement): VisualComponent =
            VisualComponent(width, element)

        fun glyph(width: Int, font: VisualFont, codePoint: Int, shadow: Boolean = false): VisualComponent =
            of(width, font.glyph(codePoint, shadow))

        fun row(gap: Int = 0, vararg components: VisualComponent): VisualComponent =
            VisualRow(gap = gap)
                .items(components.asIterable())
                .toComponent()

        fun panel(
            background: VisualComponent,
            content: VisualComponent,
            width: Int? = null,
            minWidth: Int = 0,
            paddingLeft: Int = 0,
            paddingRight: Int = 0,
            align: VisualAlignment = VisualAlignment.START,
        ): VisualComponent =
            VisualPanel(
                background = background,
                width = width,
                minWidth = minWidth,
                paddingLeft = paddingLeft,
                paddingRight = paddingRight,
                align = align,
            ).content(content).toComponent()

        fun panel(
            background: VisualBackground,
            content: VisualComponent,
            width: Int? = null,
            minWidth: Int = 0,
            paddingLeft: Int = 0,
            paddingRight: Int = 0,
            align: VisualAlignment = VisualAlignment.START,
        ): VisualComponent =
            VisualPanel(
                background = background,
                width = width,
                minWidth = minWidth,
                paddingLeft = paddingLeft,
                paddingRight = paddingRight,
                align = align,
            ).content(content).toComponent()
    }
}
