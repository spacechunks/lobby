package space.chunks.explorer.lobby

import chunks.space.api.explorer.chunk.v1alpha1.ChunkServiceGrpcKt
import chunks.space.api.explorer.chunk.v1alpha1.Types
import chunks.space.api.explorer.chunk.v1alpha1.listChunksRequest
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import space.chunks.explorer.lobby.display.DisplaySession
import space.chunks.explorer.lobby.grpc.AuthCredentials
import space.chunks.explorer.lobby.listener.CancelListener
import space.chunks.explorer.lobby.listener.ControlsListener
import space.chunks.explorer.lobby.listener.PlayerListener
import space.chunks.explorer.lobby.world.VoidWorldGenerator
import java.util.concurrent.CopyOnWriteArrayList
import java.util.logging.Level


class Plugin : JavaPlugin() {

    val chunks = CopyOnWriteArrayList<Types.Chunk>()

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

        val channel = ManagedChannelBuilder
            .forAddress(cfg.controlPlaneEndpointAddr, cfg.controlPlaneEndpointPort)
            .useTransportSecurity()
            .build()

        val chunkClient = ChunkServiceGrpcKt.ChunkServiceCoroutineStub(channel)
            .withCallCredentials(AuthCredentials(cfg.controlPlaneAPIToken))

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, { _ ->
            runBlocking {
                val resp = chunkClient.listChunks(listChunksRequest {})
                logger.info("got ${resp.chunksList.size} chunks from control plane")

                chunks.clear()
                chunks.addAll(resp.chunksList)
            }
        }, 0, 20 * 5)

        val spawn = Vector(0.0, 100.0, 0.0)

        Bukkit.getPluginManager().registerEvents(ControlsListener(this, this.sessions), this)
        Bukkit.getPluginManager().registerEvents(PlayerListener(this, this.sessions, spawn), this)
        Bukkit.getPluginManager().registerEvents(CancelListener(), this)
    }


    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return VoidWorldGenerator()
    }
}
