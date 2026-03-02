package space.chunks.lobby.chunkviewer

import chunks.space.api.explorer.chunk.v1alpha1.ChunkServiceGrpcKt
import chunks.space.api.explorer.chunk.v1alpha1.listChunksRequest
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.WorldCreator
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import space.chunks.lobby.LobbyModule
import space.chunks.lobby.chunkviewer.display.ChunkDisplay
import space.chunks.lobby.chunkviewer.display.DisplaySessionService
import space.chunks.lobby.chunkviewer.grpc.AuthCredentials
import space.chunks.lobby.chunkviewer.listener.CancelListener
import space.chunks.lobby.chunkviewer.listener.ChunkViewerPlayerListener
import space.chunks.lobby.chunkviewer.listener.ControlsListener
import space.chunks.lobby.chunkviewer.pack.PackService
import space.chunks.lobby.chunkviewer.world.VoidWorldGenerator
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

class ChunkViewerModule(plugin: Plugin) : LobbyModule(plugin, "chunk-viewer") {
    val chunks = CopyOnWriteArrayList<ChunkDisplay>()
    val worldName = "chunk_viewer"
    val sessionService = DisplaySessionService(
        this.plugin,
        this.chunks,
        Vector(0.0, 100.0, 0.0),
        this.worldName,
    )

    // LIGHT BLUE #7ce8fe
    // A BIT DARKER BLUE #53d0fd

    override fun onEnable() {
        Bukkit.createWorld(
            WorldCreator(this.worldName)
                .generator(VoidWorldGenerator())
                .generateStructures(false)
        )


        val cfg = parseConfig(this.config)
        val packService = PackService(this.logger, this.plugin, cfg.resourcePack)

        packService.startPeriodicPull()

        val channel = ManagedChannelBuilder
            .forAddress(cfg.controlPlane.addr, cfg.controlPlane.port)
            .useTransportSecurity()
            .build()

        val chunkClient = ChunkServiceGrpcKt.ChunkServiceCoroutineStub(channel)
            .withCallCredentials(AuthCredentials(cfg.controlPlane.apiToken))

        Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, { _ ->
            runBlocking {
                val resp = chunkClient.listChunks(listChunksRequest {})
                logger.info("got ${resp.chunksList.size} chunks from control plane")

                val d = resp.chunksList.map { c ->
                    val textureFile = File(
                        dataFolder,
                        "pack-files/${cfg.resourcePack.thumbnailsLocation}/${c.id}.png"
                    )

                    val key =
                        if (textureFile.exists())
                            NamespacedKey.fromString("${cfg.resourcePack.thumbnailKeyPrefix}/${c.id}")!!
                        else
                            NamespacedKey.fromString(cfg.resourcePack.thumbnailMissingKey)!!

                    ChunkDisplay(Component.text(c.name), c, key)
                }.toList()

                chunks.clear()
                chunks.addAll(d)
            }
        }, 0, 20 * 5)

        val spawn = Vector(0.0, 100.0, 0.0)

        Bukkit.getPluginManager().registerEvents(ControlsListener(this.sessionService), this.plugin)
        Bukkit.getPluginManager().registerEvents(
            ChunkViewerPlayerListener(this.plugin, packService, this.sessionService, spawn, this.chunks),
            this.plugin,
        )

        Bukkit.getPluginManager().registerEvents(CancelListener(), this.plugin)
    }

    override fun onDisable() {}
}