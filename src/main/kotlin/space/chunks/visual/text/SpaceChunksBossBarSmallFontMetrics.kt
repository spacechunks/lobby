package space.chunks.visual.text

object SpaceChunksBossBarSmallFontMetrics : VisualFontMetrics {
    private const val glyphWidth = 5

    private val mappedChars: Map<Char, VisualCharInfo> = buildMap {
        ('A'..'Z').forEachIndexed { index, char ->
            put(char, VisualCharInfo(0xF800.plus(index).toChar(), glyphWidth))
        }

        ('a'..'z').forEach { char ->
            put(char, checkNotNull(get(char.uppercaseChar())))
        }

        put('Ä', VisualCharInfo('\uF81A', glyphWidth))
        put('Ö', VisualCharInfo('\uF81B', glyphWidth))
        put('Ü', VisualCharInfo('\uF81C', glyphWidth))
        put('ä', checkNotNull(get('Ä')))
        put('ö', checkNotNull(get('Ö')))
        put('ü', checkNotNull(get('Ü')))
    }

    override fun charInfo(char: Char): VisualCharInfo =
        mappedChars[char] ?: MinecraftRegularBitmapFontMetrics.charInfo(char)

    override fun width(content: String): Int {
        if (content.isEmpty()) {
            return 0
        }

        return content.sumOf { charInfo(it).effectiveWidth + 1 } - 1
    }

    override fun render(content: String): String =
        content.map { mappedChars[it]?.char ?: it }.joinToString("")
}
