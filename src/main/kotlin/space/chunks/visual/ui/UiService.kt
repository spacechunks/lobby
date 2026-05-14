package space.chunks.visual.ui

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

class UiService : Listener {
    val bossBars = BossBarRegistry()

    private val players = mutableMapOf<UUID, PlayerUi>()

    fun start() {
        Bukkit.getOnlinePlayers().forEach(::ensure)
    }

    fun set(player: Player, slot: BossBarSlot, content: Component) {
        ensure(player).set(slot, content)
    }

    fun set(players: Iterable<Player>, slot: BossBarSlot, content: Component) {
        players.forEach { set(it, slot, content) }
    }

    fun clear(player: Player, slot: BossBarSlot) {
        ensure(player).clear(slot)
    }

    fun clear(players: Iterable<Player>, slot: BossBarSlot) {
        players.forEach { clear(it, slot) }
    }

    fun ensure(player: Player): PlayerUi =
        players.getOrPut(player.uniqueId) {
            PlayerUi(player, this.bossBars).also { it.sync() }
        }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        ensure(event.player)
    }

    @EventHandler
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        players.remove(event.player.uniqueId)?.dispose()
    }
}
