package space.chunks.visual.layout

class VisualFlexRow private constructor(
    private val gap: Int,
    private val children: List<VisualComponent>,
) {
    constructor(gap: Int = 0) : this(gap, emptyList())

    fun child(component: VisualComponent): VisualFlexRow =
        VisualFlexRow(gap, children + component)

    fun children(components: Iterable<VisualComponent>): VisualFlexRow =
        VisualFlexRow(gap, children + components)

    fun toComponent(): VisualComponent {
        var cursor = 0
        var layer = VisualLayer()

        children.forEachIndexed { index, child ->
            if (index > 0) {
                cursor += gap
            }

            layer = layer.child(x = cursor, child)
            cursor += child.width
        }

        return VisualComponent(cursor, layer.toText())
    }
}
