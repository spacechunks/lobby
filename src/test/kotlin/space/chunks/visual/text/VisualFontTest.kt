package space.chunks.visual.text

import kotlin.test.Test
import kotlin.test.assertEquals

class VisualFontTest {
    @Test
    fun `creates measured normal text components`() {
        val component = VisualFonts.normal.component("Queue")

        assertEquals(29, component.width)
        assertEquals("<!shadow><font:minecraft:default>Queue</font>", component.asMiniMessage())
    }

    @Test
    fun `maps bossbar small text to existing private glyphs`() {
        val component = VisualFonts.SpaceChunksVisualKit.bossBarSmallLine1.component("QUEUE")

        assertEquals(29, component.width)
        assertEquals(
            "<!shadow><font:spacechunks-visualkit:bossbar/bossbar-1>\uF810\uF814\uF804\uF814\uF804</font>",
            component.asMiniMessage(),
        )
    }

    @Test
    fun `formatted text preserves minimessage tags without measuring them`() {
        val component = VisualFonts.SpaceChunksVisualKit.bossBarSmallLine1
            .formattedComponent("<white>QUEUE <#D8E7FF>EXAMPLE")

        assertEquals(75, component.width)
        assertEquals(
            "<!shadow><font:spacechunks-visualkit:bossbar/bossbar-1><white>\uF810\uF814\uF804\uF814\uF804 <#D8E7FF>\uF804\uF817\uF800\uF80C\uF80F\uF80B\uF804</font>",
            component.asMiniMessage(),
        )
    }
}
