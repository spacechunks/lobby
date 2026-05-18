package space.chunks.lobby.modules.chunkviewer.display

import org.bukkit.Location
import org.joml.Vector3f

object ChunkViewerLayout {
    val LOGO_SCALE = Vector3f(7f, 3.5f, 1f)

    fun logoLocation(center: Location): Location {
        return center.clone().add(0.0, 3.5, 0.0)
    }
}
