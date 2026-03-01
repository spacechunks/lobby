package space.chunks

import chunks.space.api.explorer.chunk.v1alpha1.ChunkServiceGrpcKt
import chunks.space.api.explorer.chunk.v1alpha1.listChunksRequest
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import space.chunks.lobby.chunkviewer.Config
import space.chunks.lobby.chunkviewer.display.ChunkDisplay
import space.chunks.lobby.chunkviewer.display.DisplaySession
import space.chunks.lobby.chunkviewer.grpc.AuthCredentials
import space.chunks.lobby.chunkviewer.listener.CancelListener
import space.chunks.lobby.chunkviewer.listener.ControlsListener
import space.chunks.lobby.chunkviewer.listener.PlayerListener
import space.chunks.lobby.chunkviewer.pack.PackService
import space.chunks.lobby.chunkviewer.parseConfig
import space.chunks.lobby.chunkviewer.world.VoidWorldGenerator
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.logging.Level

class Plugin : JavaPlugin() {
    val chunks = CopyOnWriteArrayList<ChunkDisplay>()

    private val sessions = mutableMapOf<Player, DisplaySession>()

    // LIGHT BLUE #7ce8fe
    // A BIT DARKER BLUE #53d0fd

    override fun onEnable() {
        var cfg: Config
        try {
            cfg = parseConfig(this.config)
        } catch (e: RuntimeException) {
            this.logger.log(Level.SEVERE, e.message, e)
            this.server.shutdown()
            return
        }

        val packService = PackService(this, cfg.resourcePack)

        packService.startPeriodicPull()

        val channel = ManagedChannelBuilder
            .forAddress(cfg.controlPlane.addr, cfg.controlPlane.port)
            .useTransportSecurity()
            .build()

        val chunkClient = ChunkServiceGrpcKt.ChunkServiceCoroutineStub(channel)
            .withCallCredentials(AuthCredentials(cfg.controlPlane.apiToken))

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, { _ ->
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

        Bukkit.getPluginManager().registerEvents(ControlsListener(this, this.sessions), this)
        Bukkit.getPluginManager().registerEvents(
            PlayerListener(this, packService, this.sessions, spawn),
            this
        )
        Bukkit.getPluginManager().registerEvents(CancelListener(), this)


        // lobby
        Bukkit.getPluginManager().registerEvents(space.chunks.lobby.spawn.PlayerListener(), this)
    }


    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return VoidWorldGenerator()
    }
}