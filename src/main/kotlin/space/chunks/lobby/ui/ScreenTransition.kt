package space.chunks.lobby.ui

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType
import java.time.Duration

class ScreenTransition(
    private val plugin: Plugin,
    private val texts: Texts,
) {

    fun fadeToBlack(
        player: Player,
        onBlack: (Player) -> Unit,
    ) {
        player.addPotionEffect(
            PotionEffectType.DARKNESS
                .createEffect(DARKNESS_DURATION_TICKS, Int.MAX_VALUE)
                .withParticles(false)
        )

        player.showTitle(
            Title.title(
                this.texts.component("spawn.title.logo"),
                Component.empty(),
                TITLE_TIMES
            )
        )

        Bukkit.getScheduler().runTaskLater(this.plugin, Runnable {
            onBlack(player)
        }, BLACK_SCREEN_DELAY_TICKS)
    }

    private companion object {
        const val DARKNESS_DURATION_TICKS = 60
        const val BLACK_SCREEN_DELAY_TICKS = 13L

        val TITLE_TIMES: Title.Times = Title.Times.times(
            Duration.ofMillis(1000),
            Duration.ofMillis(1000),
            Duration.ofMillis(1000)
        )
    }
}
