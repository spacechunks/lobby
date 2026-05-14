package space.chunks.visual.layout

/**
 * Horizontal layout for a known-width visual area.
 *
 * Use [edgeStart]/[edgeEnd] for balanced side content like an icon and a counter,
 * [center] or [fill] for the middle content, and [fixed] only when a cell really
 * needs an explicit width.
 */
class VisualRow private constructor(
    private val width: Int?,
    private val gap: Int,
    private val cells: List<Cell>,
) {
    constructor(width: Int? = null, gap: Int = 0) : this(width, gap, emptyList())

    init {
        require(width == null || width >= 0) { "width must be zero or positive." }
        require(gap >= 0) { "gap must be zero or positive." }
    }

    fun fixed(
        component: VisualComponent,
        width: Int = component.advance,
        align: VisualAlignment = VisualAlignment.START,
        padding: VisualPadding = VisualPadding.none,
    ): VisualRow {
        require(width >= 0) { "width must be zero or positive." }
        return VisualRow(this.width, this.gap, this.cells + Cell.Fixed(component, width, align, padding))
    }

    /**
     * Adds a leading edge cell. All edge cells reserve the same width, based on
     * the widest edge component, so callers do not need to calculate side columns.
     */
    fun edgeStart(component: VisualComponent?): VisualRow =
        if (component == null) this else edge(component, VisualAlignment.START)

    /**
     * Adds a trailing edge cell. It shares the same reserved width as [edgeStart]
     * and aligns the component to the outside edge.
     */
    fun edgeEnd(component: VisualComponent?): VisualRow =
        if (component == null) this else edge(component, VisualAlignment.END)

    fun fill(
        component: VisualComponent,
        weight: Int = 1,
        align: VisualAlignment = VisualAlignment.START,
    ): VisualRow {
        require(weight > 0) { "weight must be positive." }
        return VisualRow(this.width, this.gap, this.cells + Cell.Fill(component, weight, align))
    }

    /**
     * Adds a weighted middle cell and centers the component inside it.
     */
    fun center(component: VisualComponent, weight: Int = 1): VisualRow =
        fill(component, weight, VisualAlignment.CENTER)

    fun toComponent(): VisualComponent {
        if (cells.isEmpty()) {
            return VisualComponent(0, element = VisualLayer().toText())
        }

        val gapWidth = gap * (cells.size - 1)
        val edgeWidth = cells.maxOfOrNull {
            if (it is Cell.Edge) it.component.advance else 0
        } ?: 0
        val fixedWidth = cells.sumOf {
            when (it) {
                is Cell.Fixed -> it.outerWidth
                is Cell.Edge -> edgeWidth
                is Cell.Fill -> if (width == null) it.component.width else 0
            }
        }
        val rowWidth = width ?: fixedWidth + gapWidth
        val fillWidth = (rowWidth - fixedWidth - gapWidth).coerceAtLeast(0)
        val fillWeight = cells.sumOf { if (it is Cell.Fill && width != null) it.weight else 0 }

        var remainingFillWidth = fillWidth
        var remainingFillWeight = fillWeight
        var cursor = 0
        var layer = VisualLayer(width = rowWidth)

        cells.forEachIndexed { index, cell ->
            if (index > 0) {
                cursor += gap
            }

            val cellWidth = when (cell) {
                is Cell.Fixed -> cell.outerWidth
                is Cell.Edge -> edgeWidth
                is Cell.Fill -> {
                    if (width == null) {
                        cell.component.width
                    } else {
                        val allocated = if (remainingFillWeight == cell.weight) {
                            remainingFillWidth
                        } else {
                            (remainingFillWidth * cell.weight) / remainingFillWeight
                        }
                        remainingFillWidth -= allocated
                        remainingFillWeight -= cell.weight
                        allocated
                    }
                }
            }

            layer = layer.child(
                x = cursor + contentX(cell, cellWidth),
                component = cell.component,
            )
            cursor += cellWidth
        }

        return VisualComponent(rowWidth, element = layer.toText())
    }

    private fun alignedOffset(width: Int, contentWidth: Int, align: VisualAlignment): Int {
        if (contentWidth >= width) {
            return 0
        }

        return when (align) {
            VisualAlignment.START -> 0
            VisualAlignment.CENTER -> (width - contentWidth) / 2
            VisualAlignment.END -> width - contentWidth
        }
    }

    private fun contentX(cell: Cell, cellWidth: Int): Int =
        when (cell) {
            is Cell.Fixed -> cell.padding.left + alignedOffset(cell.width, cell.component.width, cell.align)
            is Cell.Edge -> alignedOffset(cellWidth, cell.component.width, cell.align)
            is Cell.Fill -> alignedOffset(cellWidth, cell.component.width, cell.align)
        }

    private fun edge(component: VisualComponent, align: VisualAlignment): VisualRow =
        VisualRow(this.width, this.gap, this.cells + Cell.Edge(component, align))

    private sealed class Cell {
        abstract val component: VisualComponent
        abstract val align: VisualAlignment

        data class Fixed(
            override val component: VisualComponent,
            val width: Int,
            override val align: VisualAlignment,
            val padding: VisualPadding,
        ) : Cell() {
            val outerWidth: Int = padding.left + width + padding.right
        }

        data class Edge(
            override val component: VisualComponent,
            override val align: VisualAlignment,
        ) : Cell()

        data class Fill(
            override val component: VisualComponent,
            val weight: Int,
            override val align: VisualAlignment,
        ) : Cell()
    }
}
