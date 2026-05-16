package space.chunks.visual.ui

import net.kyori.adventure.text.Component

fun interface UiRenderable {
    fun render(context: UiRenderContext): Component

    companion object {
        fun static(component: Component): UiRenderable =
            UiRenderable { component }
    }
}
