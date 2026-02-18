package space.chunks.explorer.lobby

import chunks.space.api.explorer.chunk.v1alpha1.ChunkServiceGrpcKt
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import space.chunks.explorer.lobby.display.DisplayGrid
import space.chunks.explorer.lobby.display.DisplaySession
import space.chunks.explorer.lobby.listener.CancelListener
import space.chunks.explorer.lobby.listener.ControlsListener
import space.chunks.explorer.lobby.listener.PlayerListener
import space.chunks.explorer.lobby.world.VoidWorldGenerator


class Plugin : JavaPlugin() {

    private val sessions = mutableMapOf<Player, DisplaySession>()
    private lateinit var chunkClient: ChunkServiceGrpcKt.ChunkServiceCoroutineStub

    // LIGHT BLUE #7ce8fe
    // A BIT DARKER BLUE #53d0fd

    override fun onEnable() {
//        var cfg: Config
//        try {
//            cfg = parseConfig(this.config)
//        } catch (e: RuntimeException) {
//            this.logger.severe(e.message)
//            this.server.shutdown()
//            return
//        }

//        val channel = ManagedChannelBuilder
//            .forAddress(cfg.controlPlaneEndpointAddr, cfg.controlPlaneEndpointPort)
//            .useTransportSecurity()
//            .build()
//
//        this.chunkClient = ChunkServiceGrpcKt.ChunkServiceCoroutineStub(channel)
//            .withCallCredentials(AuthCredentials(cfg.controlPlaneAPIToken))
//
//        val req = listChunksRequest {}
//
//        runBlocking {
//            val l = chunkClient.listChunks(listChunksRequest {})
//            l.chunksList.forEach {
//                logger.info(it.name)
//            }
//        }

        val spawn = Vector(0.0, 100.0, 0.0)

        Bukkit.getPluginManager().registerEvents(ControlsListener(this, this.sessions), this)
        Bukkit.getPluginManager().registerEvents(PlayerListener(this, this.sessions, spawn), this)
        Bukkit.getPluginManager().registerEvents(CancelListener(), this)
    }


    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return VoidWorldGenerator()
    }
}
