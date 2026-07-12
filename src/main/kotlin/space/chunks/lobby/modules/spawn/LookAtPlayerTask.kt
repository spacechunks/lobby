package space.chunks.lobby.modules.spawn

import kr.toxicity.model.api.bukkit.platform.BukkitAdapter
import kr.toxicity.model.api.tracker.DummyTracker
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.atan2

class LookAtPlayerTask(private val tracker: DummyTracker, private val player: Player, origin: Location) : BukkitRunnable() {
    private val origin = origin.clone()

    private var facingDefault = true // starts true since it spawns already facing origin's rotation

    companion object {
        private const val RANGE = 10.0
        private const val RANGE_SQ = RANGE * RANGE

        /**
         * Quake III fast inverse square root.
         * Returns an approximation of 1 / sqrt(number).
         */
        private fun fastInvSqrt(number: Float): Float {
            val threehalfs = 1.5f
            val x2 = number * 0.5f
            var y = number

            var i = java.lang.Float.floatToIntBits(y)
            i = 0x5f3759df - (i shr 1)
            y = java.lang.Float.intBitsToFloat(i)

            y *= (threehalfs - (x2 * y * y)) // 1st Newton iteration
            y *= (threehalfs - (x2 * y * y)) // 2nd iteration for extra precision

            return y
        }

        /**
         * Fast sqrt(number) derived from fastInvSqrt: sqrt(x) = x * (1/sqrt(x))
         */
        private fun fastSqrt(number: Float): Float {
            if (number <= 0f) return 0f
            return number * fastInvSqrt(number)
        }
    }

    override fun run() {
        val targetLoc = this.player.location

        if (targetLoc.world.name != this.origin.world.name) {
            return
        }

        val dx = (targetLoc.x - origin.x).toFloat()
        val dy = (targetLoc.y - origin.y).toFloat()
        val dz = (targetLoc.z - origin.z).toFloat()

        val distSq = dx * dx + dy * dy + dz * dz

        if (distSq > RANGE_SQ) {
            if (!facingDefault) {
                tracker.location(BukkitAdapter.adapt(origin.clone())) // snap back to original facing
                facingDefault = true
            }
            return
        }

        // if player is initially entering the radius wave to them
        if (this.facingDefault) {
            this.tracker.animate("hand_wave")
        }

        val yaw = Math.toDegrees(atan2(-dx, dz).toDouble()).toFloat()

        val newLoc = origin.clone()
        newLoc.yaw = yaw

        val eyeDy = (targetLoc.y + 1.6) - origin.y
        val horizontalDistSq = dx * dx + dz * dz
        val horizontalDist = fastSqrt(horizontalDistSq)

        newLoc.pitch = -Math.toDegrees(atan2(eyeDy, horizontalDist.toDouble())).toFloat()
        tracker.location(BukkitAdapter.adapt(newLoc))
        facingDefault = false
    }
}