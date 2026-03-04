package space.chunks.lobby.modules.chunkviewer.display

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

class DisplaySessionService(
    private val plugin: Plugin,
    private val chunks: List<ChunkDisplay>,
    private val spawn: Vector,
    private val worldName: String,
) {
    private val sessions = mutableMapOf<Player, DisplaySession>()
    private val slots = mutableListOf(this.spawn)
    private val slotsPerPlayer = mutableMapOf<Player, Vector>()

    /**
     * starts the display session for a player with a 1-second delay.
     */
    fun startSession(player: Player) {
        val w = Bukkit.getWorld(this.worldName) ?: throw IllegalStateException("world $worldName does not exist")

        // this should prevent display sessions from being spawned at the same location
        val slot = this.slots
            .last()
            .clone()
            .add(Vector(100.0, 0.0, 0.0))

        this.slots.add(slot)
        this.slotsPerPlayer[player] = slot

        val loc = slot.toLocation(w)

        val sess = DisplaySession(player, this.plugin, loc, this.chunks)
        this.sessions[player] = sess

        loc.chunk.load()

        // not sure if loading chunk is async or not, but waiting a bit solves
        // the problem of the spectator target not being set consistently, thus
        // letting the player fly around in the chunk_viewer world.
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            sess.start()
        }, 10)
    }

    fun getSession(player: Player): DisplaySession? {
        return sessions[player]
    }

    fun closeSession(player: Player) {
        this.sessions[player]?.stop()
        this.sessions.remove(player)

        this.slotsPerPlayer[player]?.let {
            this.slots.remove(it)
            this.slotsPerPlayer.remove(player)
        }
    }
}