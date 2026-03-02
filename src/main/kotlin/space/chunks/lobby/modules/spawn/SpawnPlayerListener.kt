package space.chunks.lobby.modules.spawn

import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService

class SpawnPlayerListener(
    private val world: World,
    private val sessionService: DisplaySessionService,
) : Listener {
    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage(Component.text(""))

        val player = event.player
        player.gameMode = GameMode.ADVENTURE
        player.inventory.setItem(4, ItemStack(Material.NETHER_STAR))
        player.teleport(Location(world, 0.5, -42.0, 0.5))
    }

    @EventHandler
    private fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player

        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        if (player.inventory.itemInMainHand.type != Material.NETHER_STAR) return

        this.sessionService.startSession(player)
    }
}