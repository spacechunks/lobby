package space.chunks.explorer.lobby

import chunks.space.api.explorer.chunk.v1alpha1.ChunkServiceGrpcKt
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import space.chunks.explorer.lobby.display.DisplayGrid
import space.chunks.explorer.lobby.display.DisplaySession
import space.chunks.explorer.lobby.listener.PlayerListener
import space.chunks.explorer.lobby.world.VoidWorldGenerator
import java.util.*


class Plugin : JavaPlugin() {

    private lateinit var displayGrid: DisplayGrid
    private lateinit var chunkClient: ChunkServiceGrpcKt.ChunkServiceCoroutineStub

    private val sessions = mutableMapOf<Player, DisplaySession>()

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

        val voidWorld = WorldCreator.name(UUID.randomUUID().toString())
            .generator(VoidWorldGenerator())
            .createWorld() ?: throw IllegalStateException("Failed to create void world")

        prepareWorld(voidWorld)


        Bukkit.getPluginManager().registerEvents(PlayerListener(this, voidWorld, this.sessions), this)
    }

    private fun prepareWorld(voidWorld: World) {
        listOf(*voidWorld.entities.toTypedArray()).forEach { it.remove() }
        voidWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        voidWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        voidWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        voidWorld.setGameRule(GameRule.DO_FIRE_TICK, false)
        voidWorld.setGameRule(GameRule.DO_MOB_LOOT, false)
        voidWorld.setGameRule(GameRule.DO_TILE_DROPS, false)
        voidWorld.time = 1000
        voidWorld.clearWeatherDuration = -1
    }
}
