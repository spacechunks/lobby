package space.chunks.visual.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import space.chunks.visual.layout.VisualComponent

data class VisualText(
    private val content: String,
) : VisualElement {
    override fun asMiniMessage(): String = content

    fun asComponent(miniMessage: MiniMessage = MiniMessage.miniMessage()): Component =
        miniMessage.deserialize(content)

    operator fun plus(other: VisualElement): VisualText =
        VisualText(content + other.asMiniMessage())

    companion object {
        val empty = VisualText("")

        fun raw(miniMessage: String): VisualText =
            VisualText(miniMessage)

        fun of(vararg elements: VisualElement): VisualText =
            VisualText(elements.joinToString("") { it.asMiniMessage() })

        fun of(component: VisualComponent): VisualText =
            VisualText(component.asMiniMessage())

        fun join(separator: VisualElement, elements: Iterable<VisualElement>): VisualText =
            VisualText(elements.joinToString(separator.asMiniMessage()) { it.asMiniMessage() })
    }
}
