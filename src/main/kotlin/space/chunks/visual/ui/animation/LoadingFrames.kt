package space.chunks.visual.ui.animation

object LoadingFrames {
    fun frameIndex(tick: Long, frameCount: Int, ticksPerFrame: Int = 2): Int {
        require(frameCount > 0) { "frameCount must be positive." }
        require(ticksPerFrame > 0) { "ticksPerFrame must be positive." }

        return ((tick / ticksPerFrame) % frameCount).toInt()
    }
}
