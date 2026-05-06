package space.chunks.lobby.modules.spawn

import com.google.gson.JsonParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService
import space.chunks.lobby.modules.chunkviewer.event.PlayerIntentLeaveDisplaySessionEvent
import space.chunks.lobby.modules.chunkviewer.event.PlayerSelectFlavorEvent
import space.chunks.lobby.pack.Items
import space.chunks.lobby.ui.Hotbar
import java.time.Duration

class PlayerListener(
    private val plugin: Plugin,
    private val config: Config,
    private val sessionService: DisplaySessionService,
) : Listener {
    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage(Component.text(""))

        val player = event.player
        val mm = MiniMessage.miniMessage()

        player.inventory.clear()
        player.gameMode = GameMode.ADVENTURE

        player.teleport(
            Location(
                Bukkit.getWorld(this.config.world),
                this.config.spawnLocation.x,
                this.config.spawnLocation.y,
                this.config.spawnLocation.z
            )
        )

        player.sendPlayerListHeaderAndFooter(
            mm.deserialize("<br><font:chunkexplorer:tablist>\uE100<br><br><br><br><br><br>"),
            mm.deserialize("<br><gradient:#bcd4f8:#e2ecfd>  ᴄʜᴜɴᴋ ᴇxᴘʟᴏʀᴇʀ ʙʏ sᴘᴀᴄᴇ ᴄʜᴜɴᴋs  <br><gradient:#5cd9fd:#399cf5>  ᴄʜᴜɴᴋs.sᴘᴀᴄᴇ  <br>")
        )

        val title = Title.title(
            mm.deserialize("<black><font:chunkexplorer:title>\uE200"),
            mm.deserialize(""),
            Title.Times.times(
                Duration.ofMillis(0),
                Duration.ofMillis(2000),
                Duration.ofMillis(1000)
            )
        )
        player.showTitle(title)

        player.playerListName(mm.deserialize("<!shadow> <font:chunkexplorer:tablist>\uE101</font></!shadow> <#ff008a>").append(player.name()).color(TextColor.fromHexString("#ff008a")))

        Hotbar.give(player)

        player
            .retrieveCookie(NamespacedKey.fromString("spacechunks:explorer/gateway/pushback")!!)
            .thenAccept {
                if (it == null) {
                    return@thenAccept
                }

                val obj = JsonParser.parseString(it.decodeToString()).asJsonObject
                val reason = obj["reason"].asString

                if (reason == "CONNECTION_ERROR") {
                    player.sendMessage(Component.text("You got pushed back to the lobby, because an error while connecting to the server occurred."))
                    return@thenAccept
                }

                if (reason == "TRANSFER_DATA_INVALID") {
                    player.sendMessage(Component.text("You got pushed back due to invalid transfer data."))
                    return@thenAccept
                }

                if (reason == "TRANSFER_DATA_RECEIVE_TIMEOUT") {
                    player.sendMessage(Component.text("Transfer data could not be received in time."))
                    return@thenAccept
                }
            }
    }

    @EventHandler
    private fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val mm = MiniMessage.miniMessage()
    
        // nexo uses note blocks to display custom blocks.
        // interacting with them will change the block.
        if (event.clickedBlock?.type == Material.NOTE_BLOCK) {
            event.isCancelled = true
            return
        }

        if (event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) return
        if (player.inventory.itemInMainHand.type != Material.PAPER
            && player.inventory.itemInMainHand.itemMeta.itemModel == Items.TELEPORTER) return

        player.addPotionEffect(
            PotionEffectType.DARKNESS
                .createEffect(60, Int.MAX_VALUE)
                .withParticles(false)
        )
        val title = Title.title(
            mm.deserialize("<black><font:chunkexplorer:title>\uE200"),
            mm.deserialize(""),
            Title.Times.times(
                Duration.ofMillis(1000),
                Duration.ofMillis(1000),
                Duration.ofMillis(1000)
            )
        )
        player.showTitle(title)

        // timing is set so that once the darkness almost reaches the player, we teleport them
        Bukkit.getScheduler().runTaskLater(this.plugin, Runnable {
            this.sessionService.startSession(player)
        }, 13)
    }

    @EventHandler
    private fun onEntityDamage(event: EntityDamageEvent) {
        event.isCancelled = true
    }

    @EventHandler
    private fun onDropItem(event: PlayerDropItemEvent) {
        event.isCancelled = true
    }

    @EventHandler
    private fun onPlayerFlavorSelect(event: PlayerSelectFlavorEvent) {
        this.closeSessionAndBackToSpawn(event.player)
    }

    @EventHandler
    private fun onPlayerIntentLeaveDisplaySession(event: PlayerIntentLeaveDisplaySessionEvent) {
        this.closeSessionAndBackToSpawn(event.player)
    }

    private fun closeSessionAndBackToSpawn(player: Player) {
        player.addPotionEffect(
            PotionEffectType.DARKNESS
                .createEffect(25, Int.MAX_VALUE)
                .withParticles(false)
        )
        player.spectatorTarget = null
        player.gameMode = GameMode.ADVENTURE
        player.teleport(
            Location(
                Bukkit.getWorld(this.config.world),
                this.config.spawnLocation.x,
                this.config.spawnLocation.y,
                this.config.spawnLocation.z
            )
        )

        this.sessionService.closeSession(player)
    }
}