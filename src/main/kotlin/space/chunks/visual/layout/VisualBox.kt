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

    fun toComponent(): VisualComponent =
        VisualComponent.of(measuredWidth(), render())

    fun render(): VisualText {
        val ordered = children.sortedBy { it.x }
        var cursor = 0
        val content = mutableListOf<VisualElement>()

        ordered.forEach { child ->
            val advanceCorrection = if (content.isEmpty()) 0 else 1
            content.add(VisualSpace.pixels(child.x - cursor - advanceCorrection))
            content.add(child.element)
            cursor = child.x + child.width
        }

        if (width != null) {
            val advanceCorrection = if (content.isEmpty()) 0 else 1
            content.add(VisualSpace.pixels(width - cursor - advanceCorrection))
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
