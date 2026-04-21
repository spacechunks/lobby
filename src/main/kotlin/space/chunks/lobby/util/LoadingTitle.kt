package space.chunks.lobby.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

class LoadingTitle(private val plugin: Plugin, private val subtitle: Component) {
    private val list = listOf(
        "\uE100",
        "\uE101",
        "\uE102",
        "\uE103",
        "\uE104",
        "\uE105",
        "\uE106",
        "\uE107"
    )

    private val tasks = mutableMapOf<Player, BukkitTask>()

    fun run(player: Player) {
        var iter = list.iterator()
        val task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val mm = MiniMessage.miniMessage()
            if (!iter.hasNext()) {
                iter = list.iterator()
            }

            if (!player.isOnline) {
                this.tasks[player]?.cancel()
                this.tasks.remove(player)
                return@Runnable
            }

            player.showTitle(
                Title.title(
                    mm.deserialize("<!shadow><font:spacechunks-visualkit:title>" + iter.next()),
                    subtitle,
                    0,
                    20,
                    0
                )
            )
        }, 0, 2)
        this.tasks[player] = task
    }

    fun stop(player: Player) {
        this.tasks[player]?.cancel()
    }
}