package space.chunks.lobby

import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.resource.ResourcePackStatus
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import space.chunks.lobby.modules.chunkviewer.ChunkViewerModule
import space.chunks.lobby.modules.chunkviewer.world.VoidWorldGenerator
import space.chunks.lobby.modules.party.PartyModule
import space.chunks.lobby.modules.party.PartyService
import space.chunks.lobby.modules.queue.QueueModule
import space.chunks.lobby.modules.spawn.SpawnModule
import space.chunks.lobby.pack.PackService
import space.chunks.lobby.pack.ResourcePackConfig
import space.chunks.lobby.ui.ActionBar
import space.chunks.lobby.ui.Texts
import space.chunks.visual.ui.UiService
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class Plugin : JavaPlugin(), Listener {

    private val packConfig = ResourcePackConfig.parse(this.config)
    private val packService = PackService(this.logger, this, packConfig)
    private val partyService = PartyService()
    private val uiService = UiService(ActionBar::send)
    private val texts = Texts(this)

    // modules
    private val chunkViewerMod = ChunkViewerModule(this, packConfig, partyService, texts)
    private val spawnMod = SpawnModule(this.chunkViewerMod.sessionService, this, texts, this.uiService)
    private val partyMod = PartyModule(this, partyService, texts, uiService)
    private val queueMod = QueueModule(this, uiService)

    override fun onEnable() {
        val modules = listOf(
            chunkViewerMod,
            spawnMod,
            partyMod,
            queueMod,
        )

        Bukkit.getPluginManager().registerEvents(this.uiService, this)
        this.uiService.start(this)
        this.packService.startPeriodicPull()
        Bukkit.getPluginManager().registerEvents(this, this)

        modules.forEach {
            it.onEnable()
        }
    }

    @EventHandler
    fun onAsyncConfigure(event: AsyncPlayerConnectionConfigureEvent) {
        val conn = event.connection
        val future = CompletableFuture<ResourcePackStatus>()
        val hash = this.packService.packHash.get()

        val info = ResourcePackInfo.resourcePackInfo(
            UUID.fromString("92de217b-8b2b-403b-86a5-fe26fa3a9b5f"),
            URI.create(this.packService.packDownloadUrl),
            hash
        )

        val request = ResourcePackRequest.resourcePackRequest()
            .packs(info)
            .required(true)
            .callback { _, status, _ ->
                future.complete(status)
            }
            .build()

        conn.audience.sendResourcePacks(request)

        val status = try {
            future.get(30, TimeUnit.SECONDS)
        } catch (_: Throwable) {
            null
        }

        when (status) {
            ResourcePackStatus.ACCEPTED,
            ResourcePackStatus.SUCCESSFULLY_LOADED,
            ResourcePackStatus.DOWNLOADED -> {
                this.packService.setCurrentPack(conn.profile.id!!, hash)
                return
            }

            ResourcePackStatus.DECLINED -> {
                conn.disconnect(this.texts.component("common.resource-pack.declined"))
            }

            ResourcePackStatus.FAILED_DOWNLOAD,
            ResourcePackStatus.FAILED_RELOAD,
            ResourcePackStatus.INVALID_URL,
            ResourcePackStatus.DISCARDED,
            null -> {
                conn.disconnect(this.texts.component("common.resource-pack.failed"))
            }
        }
    }


    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return VoidWorldGenerator()
    }
}
