package space.chunks.visual.layout

class VisualRow private constructor(
    private val gap: Int,
    private val items: List<VisualComponent>,
) {
    constructor(gap: Int = 0) : this(gap, emptyList())

    fun item(component: VisualComponent): VisualRow =
        VisualRow(gap, items + component)

    fun items(components: Iterable<VisualComponent>): VisualRow =
        VisualRow(gap, items + components)

    fun toComponent(): VisualComponent {
        var cursor = 0
        var box = VisualBox()

        items.forEachIndexed { index, item ->
            if (index > 0) {
                cursor += gap
            }

            box = box.place(x = cursor, item)
            cursor += item.width
        }

        return VisualComponent.of(cursor, box.render())
    }
}
