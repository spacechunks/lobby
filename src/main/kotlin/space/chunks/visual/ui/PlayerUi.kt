package space.chunks.visual.ui

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class PlayerUi(
    private val player: Player,
    private val registry: BossBarRegistry,
) {
    private val bossBars = mutableMapOf<BossBarSlot, BossBar>()
    private val bossBarContent = mutableMapOf<BossBarSlot, UiRenderable>()
    private var visible = true

    fun sync() {
        registry.all().forEach { slot ->
            bossBars.getOrPut(slot) {
                bossBar().also {
                    if (this.visible) {
                        player.showBossBar(it)
                    }
                }
            }
        }
    }

    fun set(slot: BossBarSlot, content: Component) {
        set(slot, UiRenderable.static(content))
    }

    fun set(slot: BossBarSlot, content: UiRenderable) {
        sync()
        bossBarContent[slot] = content
        bossBars.getValue(slot).name(content.render(UiRenderContext(this.player, 0L)))
    }

    fun clear(slot: BossBarSlot) {
        sync()
        bossBarContent.remove(slot)
        bossBars.getValue(slot).name(Component.empty())
    }

    fun show() {
        this.visible = true
        sync()
        bossBars.values.forEach(player::showBossBar)
    }

    fun hide() {
        this.visible = false
        bossBars.values.forEach(player::hideBossBar)
    }

    fun isVisible(): Boolean =
        this.visible

    fun render(tick: Long) {
        if (!this.visible) {
            return
        }

        sync()
        val context = UiRenderContext(this.player, tick)
        bossBarContent.forEach { (slot, content) ->
            bossBars.getValue(slot).name(content.render(context))
        }
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
