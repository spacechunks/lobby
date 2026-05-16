package space.chunks.visual.layout

import space.chunks.visual.text.VisualElement
import space.chunks.visual.text.VisualSpace
import space.chunks.visual.text.VisualText

class VisualLayer private constructor(
    private val width: Int?,
    private val children: List<Child> = emptyList(),
) {
    constructor(width: Int? = null) : this(width, emptyList())

    fun child(x: Int, element: VisualElement, width: Int = 0): VisualLayer {
        require(width >= 0) { "width must be zero or positive." }
        return VisualLayer(this.width, children + Child(x, width, element))
    }

    fun child(x: Int, component: VisualComponent): VisualLayer =
        child(x, component, component.advance)

    fun childEnd(x: Int, component: VisualComponent): VisualLayer =
        child(x - component.width, component)

    fun childEnd(component: VisualComponent): VisualLayer {
        require(width != null) { "Cannot place at the end of a layer without a width." }
        return childEnd(width, component)
    }

    fun toComponent(): VisualComponent =
        VisualComponent(measuredWidth(), element = toText())

    fun toText(): VisualText {
        val ordered = children.sortedBy { it.x }
        var cursor = 0
        val content = mutableListOf<VisualElement>()

        ordered.forEach { child ->
            content.add(VisualSpace.pixels(child.x - cursor))
            content.add(child.element)
            cursor = child.x + child.width
        }

        if (width != null) {
            content.add(VisualSpace.pixels(width - cursor))
        }

        return VisualText.of(*content.toTypedArray())
    }

    private fun measuredWidth(): Int =
        width ?: children.maxOfOrNull { it.x + it.width } ?: 0

    private data class Child(
        val x: Int,
        val width: Int,
        val element: VisualElement,
    )
}
