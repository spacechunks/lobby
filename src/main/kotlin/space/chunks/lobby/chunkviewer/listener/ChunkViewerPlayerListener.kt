package space.chunks.lobby.chunkviewer.listener

import io.papermc.paper.event.connection.configuration.PlayerConnectionInitialConfigureEvent
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.text.Component
import org.bukkit.GameRule
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import space.chunks.lobby.chunkviewer.display.ChunkDisplay
import space.chunks.lobby.chunkviewer.display.DisplaySessionService
import space.chunks.lobby.chunkviewer.pack.PackService
import java.net.URI
import java.util.*

// 0.5 -42 0.5


class ChunkViewerPlayerListener(
    private val plugin: Plugin,
    private val packService: PackService,
    private val sessionService: DisplaySessionService,
    private val spawn: Vector,
    private val chunks: List<ChunkDisplay>
) : Listener {

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        val w = event.world
        w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        w.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        w.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        w.setGameRule(GameRule.DO_FIRE_TICK, false)
        w.setGameRule(GameRule.DO_MOB_LOOT, false)
        w.setGameRule(GameRule.DO_TILE_DROPS, false)
        w.time = 1000
        w.clearWeatherDuration = -1
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.joinMessage(Component.text(""))
        val player = event.player
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        this.sessionService.closeSession(event.player)
    }


    @EventHandler
    fun onConfigure(event: PlayerConnectionInitialConfigureEvent) {
        val info = ResourcePackInfo.resourcePackInfo(
            UUID.randomUUID(),
            URI.create(this.packService.packDownloadUrl),
            this.packService.packHash.get()
        )

        val request = ResourcePackRequest.resourcePackRequest()
            .packs(info)
            .required(true)
            .build()

        event.connection.audience.sendResourcePacks(request)
    }
}