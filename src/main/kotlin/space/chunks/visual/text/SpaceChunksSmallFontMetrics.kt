package space.chunks.visual.text

object SpaceChunksSmallFontMetrics : VisualFontMetrics {
    private val charMap: Map<Char, VisualCharInfo> =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜ".associateWith { VisualCharInfo(it, 5) }

    private val defaultChar = VisualCharInfo('\u0000', 0)

    override fun charInfo(char: Char): VisualCharInfo =
        charMap[char] ?: defaultChar

    override fun width(content: String): Int {
        if (content.isEmpty()) {
            return 0
        }

        return content.sumOf { charInfo(it).effectiveWidth + 1 } - 1
    }
}
