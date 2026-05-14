package space.chunks.lobby.ui.visual

import space.chunks.visual.text.VisualElement
import space.chunks.visual.layout.VisualComponent
import space.chunks.visual.text.VisualFonts
import space.chunks.visual.text.VisualGlyph

object ActionBarGlyphs {
    enum class ChatChannel(private val glyph: VisualGlyph) : VisualElement {
        GLOBAL(VisualFonts.ChunkExplorer.actionBar.glyph(0xE140)),
        TEAM(VisualFonts.ChunkExplorer.actionBar.glyph(0xE141));

        override fun asMiniMessage(): String =
            glyph.asMiniMessage()

        val component: VisualComponent =
            VisualComponent.of(0, glyph)
    }

    fun health(value: Int): VisualComponent {
        require(value in 0..20) { "health must be between 0 and 20." }
        return VisualComponent.of(0, VisualFonts.ChunkExplorer.actionBar.glyph(0xE100 + value))
    }

    fun gravity(value: Int): VisualComponent {
        require(value in 0..20) { "gravity must be between 0 and 20." }
        return VisualComponent.of(2, VisualFonts.ChunkExplorer.actionBar.glyph(0xE120 + value))
    }

    val voiceChatDisabled: VisualComponent =
        VisualComponent.of(0, VisualFonts.ChunkExplorer.actionBar.glyph(0xE150))
}
