package space.chunks.visual.layout

data class VisualPadding(
    val left: Int = 0,
    val right: Int = 0,
) {
    init {
        require(left >= 0) { "left padding must be zero or positive." }
        require(right >= 0) { "right padding must be zero or positive." }
    }

    companion object {
        val none = VisualPadding()

        fun horizontal(value: Int): VisualPadding =
            VisualPadding(left = value, right = value)
    }
}
