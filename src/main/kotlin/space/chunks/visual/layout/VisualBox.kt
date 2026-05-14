package space.chunks.visual.layout

import space.chunks.visual.text.VisualElement
import space.chunks.visual.text.VisualSpace
import space.chunks.visual.text.VisualText

class VisualBox private constructor(
    private val width: Int?,
    private val children: List<Child> = emptyList(),
) {
    constructor(width: Int? = null) : this(width, emptyList())

    fun place(x: Int, element: VisualElement, width: Int = 0): VisualBox {
        require(width >= 0) { "width must be zero or positive." }
        return VisualBox(this.width, children + Child(x, width, element))
    }

    fun place(x: Int, component: VisualComponent): VisualBox =
        place(x, component, component.width)

    fun placeEnd(x: Int, component: VisualComponent): VisualBox =
        place(x - component.width, component)

    fun placeEnd(component: VisualComponent): VisualBox {
        require(width != null) { "Cannot place at the end of a box without a width." }
        return placeEnd(width, component)
    }

    fun render(): VisualText {
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

    private data class Child(
        val x: Int,
        val width: Int,
        val element: VisualElement,
    )
}
