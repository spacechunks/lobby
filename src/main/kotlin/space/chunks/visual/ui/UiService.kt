package space.chunks.visual.ui

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

class UiService(
    private val renderActionBar: (Player) -> Unit,
) : Listener {
    val bossBars = BossBarRegistry()

    private val players = mutableMapOf<UUID, PlayerUi>()
    private var actionBarTask: BukkitTask? = null
    private var tick = 0L

    fun start(plugin: Plugin) {
        Bukkit.getOnlinePlayers().forEach(::ensure)

        this.actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            this.tick++
            Bukkit.getOnlinePlayers()
                .filter(::isVisible)
                .forEach { player ->
                    this.renderActionBar(player)
                    ensure(player).render(this.tick)
                }
        }, 0L, 1L)
    }

    fun stop() {
        this.actionBarTask?.cancel()
        this.actionBarTask = null
    }

    fun set(player: Player, slot: BossBarSlot, content: Component) {
        ensure(player).set(slot, content)
    }

    fun set(player: Player, slot: BossBarSlot, content: UiRenderable) {
        ensure(player).set(slot, content)
    }

    fun set(players: Iterable<Player>, slot: BossBarSlot, content: Component) {
        players.forEach { set(it, slot, content) }
    }

    fun set(players: Iterable<Player>, slot: BossBarSlot, content: UiRenderable) {
        players.forEach { set(it, slot, content) }
    }

    fun clear(player: Player, slot: BossBarSlot) {
        ensure(player).clear(slot)
    }

    fun clear(players: Iterable<Player>, slot: BossBarSlot) {
        players.forEach { clear(it, slot) }
    }

    fun show(player: Player) {
        ensure(player).show()
    }

    fun hide(player: Player) {
        ensure(player).hide()
    }

    fun isVisible(player: Player): Boolean =
        ensure(player).isVisible()

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
