package space.chunks.visual.layout

import space.chunks.visual.text.VisualText

class VisualView(
    private val background: VisualBackground,
    private val content: VisualComponent? = null,
    private val width: Int? = null,
    private val minWidth: Int = 0,
    private val padding: VisualPadding = VisualPadding.none,
    private val align: VisualAlignment = VisualAlignment.START,
) {
    constructor(
        background: VisualComponent,
        content: VisualComponent? = null,
        width: Int? = null,
        minWidth: Int = 0,
        padding: VisualPadding = VisualPadding.none,
        align: VisualAlignment = VisualAlignment.START,
    ) : this(VisualBackground.fixed(background), content, width, minWidth, padding, align)

    init {
        require(width == null || width >= 0) { "width must be zero or positive." }
        require(minWidth >= 0) { "minWidth must be zero or positive." }
    }

    fun toComponent(): VisualComponent {
        val contentWidth = content?.let { padding.left + it.width + padding.right } ?: 0
        val viewWidth = width ?: maxOf(minWidth, contentWidth)
        val renderedBackground = background.render(viewWidth)
        var layer = VisualLayer(width = viewWidth)
            .child(x = 0, renderedBackground)

        content?.let {
            layer = layer.child(x = contentX(viewWidth, it.width), it)
        }

        return VisualComponent(viewWidth, layer.toText())
    }

    fun toText(): VisualText =
        VisualText.of(toComponent())

    private fun contentX(viewWidth: Int, contentWidth: Int): Int {
        val innerWidth = viewWidth - padding.left - padding.right
        if (contentWidth >= innerWidth) {
            return padding.left
        }

        return when (align) {
            VisualAlignment.START -> padding.left
            VisualAlignment.CENTER -> padding.left + ((innerWidth - contentWidth) / 2)
            VisualAlignment.END -> viewWidth - padding.right - contentWidth
        }
    }
}
