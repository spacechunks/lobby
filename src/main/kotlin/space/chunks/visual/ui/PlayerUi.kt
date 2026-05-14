package space.chunks.visual.ui

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class PlayerUi(
    private val player: Player,
    private val registry: BossBarRegistry,
) {
    private val bossBars = mutableMapOf<BossBarSlot, BossBar>()

    fun sync() {
        registry.all().forEach { slot ->
            bossBars.getOrPut(slot) {
                bossBar().also(player::showBossBar)
            }
        }
    }

    fun set(slot: BossBarSlot, content: Component) {
        sync()
        bossBars.getValue(slot).name(content)
    }

    fun clear(slot: BossBarSlot) {
        sync()
        bossBars.getValue(slot).name(Component.empty())
    }

    fun dispose() {
        bossBars.values.forEach(player::hideBossBar)
    }

    private fun bossBar(): BossBar =
        BossBar.bossBar(
            Component.empty(),
            0f,
            BossBar.Color.BLUE,
            BossBar.Overlay.PROGRESS,
        )
}
