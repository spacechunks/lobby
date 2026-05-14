package space.chunks.visual.layout

class VisualPanel private constructor(
    private val background: VisualBackground,
    private val width: Int?,
    private val minWidth: Int,
    private val paddingLeft: Int,
    private val paddingRight: Int,
    private val align: VisualAlignment,
    private val content: VisualComponent?,
) {
    constructor(
        background: VisualComponent,
        width: Int? = null,
        minWidth: Int = 0,
        paddingLeft: Int = 0,
        paddingRight: Int = 0,
        align: VisualAlignment = VisualAlignment.START,
    ) : this(VisualBackground.fixed(background), width, minWidth, paddingLeft, paddingRight, align, null)

    constructor(
        background: VisualBackground,
        width: Int? = null,
        minWidth: Int = 0,
        paddingLeft: Int = 0,
        paddingRight: Int = 0,
        align: VisualAlignment = VisualAlignment.START,
    ) : this(background, width, minWidth, paddingLeft, paddingRight, align, null)

    init {
        require(width == null || width >= 0) { "width must be zero or positive." }
        require(minWidth >= 0) { "minWidth must be zero or positive." }
        require(paddingLeft >= 0) { "paddingLeft must be zero or positive." }
        require(paddingRight >= 0) { "paddingRight must be zero or positive." }
    }

    fun content(component: VisualComponent): VisualPanel =
        VisualPanel(background, width, minWidth, paddingLeft, paddingRight, align, component)

    fun toComponent(): VisualComponent {
        val contentWidth = content?.let { paddingLeft + it.width + paddingRight } ?: 0
        val panelWidth = width ?: maxOf(minWidth, contentWidth)
        val renderedBackground = background.render(panelWidth)
        var box = VisualBox(width = panelWidth)
            .place(x = 0, renderedBackground)

        content?.let {
            box = box.place(x = contentX(panelWidth, it.width), it)
        }

        return VisualComponent.of(panelWidth, box.render())
    }

    private fun contentX(panelWidth: Int, contentWidth: Int): Int {
        val innerWidth = panelWidth - paddingLeft - paddingRight
        if (contentWidth >= innerWidth) {
            return paddingLeft
        }

        return when (align) {
            VisualAlignment.START -> paddingLeft
            VisualAlignment.CENTER -> paddingLeft + ((innerWidth - contentWidth) / 2)
            VisualAlignment.END -> panelWidth - paddingRight - contentWidth
        }
    }
}
