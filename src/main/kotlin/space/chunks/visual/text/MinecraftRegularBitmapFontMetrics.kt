package space.chunks.visual.text

object MinecraftRegularBitmapFontMetrics : VisualFontMetrics by MinecraftNormalFontMetrics {
    private val overrides = mapOf(
        '(' to VisualCharInfo('(', 3),
        ')' to VisualCharInfo(')', 3),
        '*' to VisualCharInfo('*', 3),
        'l' to VisualCharInfo('l', 2),
        '{' to VisualCharInfo('{', 3),
        '}' to VisualCharInfo('}', 3),
        '~' to VisualCharInfo('~', 6),
    )

    override fun charInfo(char: Char): VisualCharInfo =
        overrides[char] ?: MinecraftNormalFontMetrics.charInfo(char)

    override fun width(content: String): Int {
        if (content.isEmpty()) {
            return 0
        }

        return content.sumOf { charInfo(it).effectiveWidth + 1 } - 1
    }
}
