package space.chunks.visual.text

object VisualSpace {
    private val positiveAdvances =
        listOf(
            1024 to 0xF82F,
            512 to 0xF82E,
            256 to 0xF82D,
            128 to 0xF82C,
            64 to 0xF82B,
            32 to 0xF82A,
            16 to 0xF829,
            8 to 0xF828,
            4 to 0xF824,
            2 to 0xF822,
            1 to 0xF821,
        )

    private val negativeAdvances =
        listOf(
            1024 to 0xF80F,
            512 to 0xF80E,
            256 to 0xF80D,
            128 to 0xF80C,
            64 to 0xF80B,
            32 to 0xF80A,
            16 to 0xF809,
            8 to 0xF808,
            4 to 0xF804,
            2 to 0xF802,
            1 to 0xF801,
        )

    fun pixels(amount: Int): VisualText {
        if (amount == 0) {
            return VisualText.empty
        }

        val advances = if (amount > 0) positiveAdvances else negativeAdvances
        var remaining = kotlin.math.abs(amount)
        val raw = StringBuilder()

        for ((advance, codePoint) in advances) {
            while (remaining >= advance) {
                raw.appendCodePoint(codePoint)
                remaining -= advance
            }
        }

        check(remaining == 0) {
            "Could not map pixel offset $amount to space font content."
        }

        return VisualFonts.SpaceChunksVisualKit.space.text(raw.toString(), shadow = true)
    }

    fun rawPixels(amount: Int): String {
        val content = pixels(amount).asMiniMessage()
        val prefix = "<font:${VisualFonts.SpaceChunksVisualKit.space.key}>"
        val suffix = "</font>"
        return content.removePrefix(prefix).removeSuffix(suffix)
    }
}
