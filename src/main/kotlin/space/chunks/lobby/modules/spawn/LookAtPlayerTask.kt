package space.chunks.lobby.modules.spawn

import kr.toxicity.model.api.bukkit.platform.BukkitAdapter
import kr.toxicity.model.api.tracker.DummyTracker
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.atan2
import kotlin.math.sqrt

class LookAtPlayerTask(private val tracker: DummyTracker, private val player: Player, origin: Location) : BukkitRunnable() {
    private val origin = origin.clone()

    private var facingDefault = true // starts true since it spawns already facing origin's rotation

    companion object {
        private const val RANGE = 10.0
    }

    override fun run() {
        val targetLoc = this.player.location

        if (targetLoc.world.name != this.origin.world.name) {
            return
        }

        if (targetLoc.distance(origin) > RANGE) {
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

        val dx = targetLoc.x - origin.x
        val dz = targetLoc.z - origin.z
        val yaw = Math.toDegrees(atan2(-dx, dz)).toFloat()

        val newLoc = origin.clone()
        newLoc.yaw = yaw

         val dy = (targetLoc.y + 1.6) - origin.y;
         val horizontalDist = sqrt(dx * dx + dz * dz);
         newLoc.setPitch(-Math.toDegrees(atan2(dy, horizontalDist)).toFloat());
        tracker.location(BukkitAdapter.adapt(newLoc))
        facingDefault = false
    }
}