package space.chunks.lobby.ui.visual

import space.chunks.visual.layout.VisualComponent
import space.chunks.visual.text.VisualElement
import space.chunks.visual.text.VisualFonts
import space.chunks.visual.text.VisualGlyph

object PartyBossBarGlyphs {
    enum class Status(private val glyph: VisualGlyph) : VisualElement {
        ONLINE(VisualFonts.ChunkExplorer.bossBarParty.glyph(0xE110)),
        PENDING(VisualFonts.ChunkExplorer.bossBarParty.glyph(0xE111)),
        AFK(VisualFonts.ChunkExplorer.bossBarParty.glyph(0xE112));

        val component: VisualComponent =
            VisualComponent(2, glyph)

        override fun asMiniMessage(): String =
            glyph.asMiniMessage()
    }

    val leaderFrame: VisualComponent =
        VisualComponent(11, VisualFonts.ChunkExplorer.bossBarParty.glyph(0xE120))

    val emptySlot: VisualComponent =
        VisualComponent(4, VisualFonts.ChunkExplorer.bossBarParty.glyph(0xE130))
}
