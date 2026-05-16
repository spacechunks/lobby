package space.chunks.visual.text

data class VisualCharInfo(
    val char: Char,
    val width: Int,
    val effectiveWidth: Int = width,
)
