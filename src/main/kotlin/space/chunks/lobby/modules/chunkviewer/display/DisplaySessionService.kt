package space.chunks.lobby.modules.chunkviewer.display

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import space.chunks.lobby.ui.Texts
import java.util.concurrent.locks.ReentrantLock

class DisplaySessionService(
    private val plugin: Plugin,
    private val chunks: List<ChunkDisplay>,
    private val spawn: Vector,
    private val worldName: String,
    private val texts: Texts,
) {
    private val sessions = mutableMapOf<Player, DisplaySession>()
    private val slots = mutableListOf(this.spawn)
    private val slotsPerPlayer = mutableMapOf<Player, Vector>()
    private val lock = ReentrantLock()

    /**
     * starts the display session for a player with a 10-tick delay.
     */
    fun startSession(player: Player) {
        this.lock.lock()
        try {
            // TODO: check if the player has the latest pack installed, if not -> re-send

            val w = Bukkit.getWorld(this.worldName) ?: throw IllegalStateException("world $worldName does not exist")

            // this should prevent display sessions from being spawned at the same location
            val slot = this.slots
                .last()
                .clone()
                .add(Vector(100.0, 0.0, 0.0))

            this.slots.add(slot)
            this.slotsPerPlayer[player] = slot

            val loc = slot.toLocation(w)

            // we have to clone the chunk displays otherwise they will be re-used
            // which can lead to displays overlapping. to reproduce this issue people
            // have to simultaneously start the session. we still want to have the
            // locking in place to make sure we are thread-safe.
            val clones = mutableListOf<ChunkDisplay>()
            this.chunks.forEach {
                clones.add(ChunkDisplay(it.title, it.chunk, it.thumbnailKey))
            }

            val sess = DisplaySession(player, this.plugin, loc.clone(), clones, this.texts)
            this.sessions[player] = sess

            loc.chunk.load()

            // not sure if loading chunk is async or not, but waiting a bit solves
            // the problem of the spectator target not being set consistently, thus
            // letting the player fly around in the chunk_viewer world.
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                sess.start()
            }, 10)
        } finally {
            this.lock.unlock()
        }
    }

    fun getSession(player: Player): DisplaySession? {
        return sessions[player]
    }

    fun closeSession(player: Player) {
        this.lock.lock()
        try {
            this.sessions[player]?.stop()
            this.sessions.remove(player)

            this.slotsPerPlayer[player]?.let {
                this.slots.remove(it)
                this.slotsPerPlayer.remove(player)
            }
        } finally {
            this.lock.unlock()
        }
    }
}
