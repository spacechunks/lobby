package space.chunks.lobby.modules.spawn

import com.google.gson.JsonParser
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
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
import space.chunks.lobby.ui.Texts
import java.time.Duration

class PlayerListener(
    private val plugin: Plugin,
    private val config: Config,
    private val sessionService: DisplaySessionService,
    private val texts: Texts,
) : Listener {
    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage(Component.empty())

        val player = event.player

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
            this.texts.component("spawn.tablist.header"),
            this.texts.component("spawn.tablist.footer")
        )

        val title = Title.title(
            this.texts.component("spawn.title.logo"),
            Component.empty(),
            Title.Times.times(
                Duration.ofMillis(0),
                Duration.ofMillis(2000),
                Duration.ofMillis(1000)
            )
        )
        player.showTitle(title)

        val playerName = this.playerName(player)
        player.playerListName(playerName)

        Hotbar.give(player, this.texts)

        player
            .retrieveCookie(NamespacedKey.fromString("spacechunks:explorer/gateway/pushback")!!)
            .thenAccept {
                if (it == null) {
                    return@thenAccept
                }

                val obj = JsonParser.parseString(it.decodeToString()).asJsonObject
                val reason = obj["reason"].asString

                if (reason == "CONNECTION_ERROR") {
                    this.texts.send(player, "spawn.pushback.connection-error")
                    return@thenAccept
                }

                if (reason == "TRANSFER_DATA_INVALID") {
                    this.texts.send(player, "spawn.pushback.transfer-data-invalid")
                    return@thenAccept
                }

                if (reason == "TRANSFER_DATA_RECEIVE_TIMEOUT") {
                    this.texts.send(player, "spawn.pushback.transfer-data-timeout")
                    return@thenAccept
                }
            }
    }

    @EventHandler
    private fun onAsyncChat(event: AsyncChatEvent) {
        event.renderer { source, _, message, _ ->
            this.texts.component(
                "${this.playerTextPath(source)}.chat-format",
                mapOf(
                    "name" to source.name,
                    "playerName" to event.player.name,
                    "message" to message,
                )
            )
        }
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
        if (player.inventory.itemInMainHand.type != Material.PAPER
            && player.inventory.itemInMainHand.itemMeta.itemModel == Items.TELEPORTER) return

        player.addPotionEffect(
            PotionEffectType.DARKNESS
                .createEffect(60, Int.MAX_VALUE)
                .withParticles(false)
        )
        val title = Title.title(
            this.texts.component("spawn.title.logo"),
            Component.empty(),
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

    private fun playerName(player: Player): Component =
        this.texts.component(
            "${this.playerTextPath(player)}.player-name",
            mapOf("name" to player.name)
        )

    private fun playerTextPath(player: Player): String {
        val sections = this.texts.sectionKeys("spawn.player")

        sections.forEach { section ->
            if (section == "default") {
                return@forEach
            }

            val permission = this.texts.stringOrNull("spawn.player.$section.permission") ?: return@forEach
            if (permission.isNotBlank() && player.hasPermission(permission)) {
                return "spawn.player.$section"
            }
        }

        return "spawn.player.default"
    }
}
