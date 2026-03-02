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

    /**
     * starts the display session for a player with a 1-second delay.
     */
    fun startSession(player: Player) {
        val w = Bukkit.getWorld(this.worldName) ?: throw IllegalStateException("world $worldName does not exist")

        val loc = this.spawn.toLocation(w).add(
            Vector(Bukkit.getOnlinePlayers().size * 100.0, 0.0, 0.0),
        )

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
    }
}