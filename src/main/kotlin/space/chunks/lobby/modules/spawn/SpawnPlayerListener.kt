package space.chunks.lobby.modules.spawn

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService

class SpawnPlayerListener(
    private val config: Config,
    private val sessionService: DisplaySessionService,
) : Listener {
    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage(Component.text(""))

        val player = event.player
        player.gameMode = GameMode.ADVENTURE
        player.inventory.setItem(4, ItemStack(Material.NETHER_STAR))

        player.teleport(
            Location(
                Bukkit.getWorld(this.config.world),
                this.config.spawnLocation.x,
                this.config.spawnLocation.y,
                this.config.spawnLocation.z
            )
        )
    }

    @EventHandler
    private fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
    
        // nexo uses note blocks to display custom blocks.
        // interacting with them will change the block.
        if (event.clickedBlock?.type == Material.NOTE_BLOCK) {
            event.isCancelled = true
            return
        }

        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        if (player.inventory.itemInMainHand.type != Material.NETHER_STAR) return

        this.sessionService.startSession(player)
    }
}