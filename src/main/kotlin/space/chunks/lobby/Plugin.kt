package space.chunks.lobby

import io.papermc.paper.event.connection.configuration.PlayerConnectionInitialConfigureEvent
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import space.chunks.lobby.modules.chunkviewer.ChunkViewerModule
import space.chunks.lobby.modules.chunkviewer.world.VoidWorldGenerator
import space.chunks.lobby.modules.spawn.SpawnModule
import space.chunks.lobby.pack.PackService
import space.chunks.lobby.pack.ResourcePackConfig
import java.net.URI
import java.util.*

class Plugin : JavaPlugin(), Listener {

    private val packConfig = ResourcePackConfig.parse(this.config)
    private val packService = PackService(this.logger, this, packConfig)

    // modules
    private val chunkViewerMod = ChunkViewerModule(this, packConfig)
    private val spawnMod = SpawnModule(this.chunkViewerMod.sessionService, this)

    override fun onEnable() {
        val modules = listOf(
            chunkViewerMod,
            spawnMod,
        )

        modules.forEach {
            it.onEnable()
        }

        this.packService.startPeriodicPull()
        Bukkit.getPluginManager().registerEvents(this, this)
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

    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return VoidWorldGenerator()
    }
}