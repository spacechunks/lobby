package space.chunks.lobby

import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import space.chunks.lobby.chunkviewer.ChunkViewerModule
import space.chunks.lobby.chunkviewer.world.VoidWorldGenerator
import space.chunks.lobby.spawn.SpawnModule

class Plugin : JavaPlugin() {

    private val chunkViewerMod = ChunkViewerModule(this)
    private val spawnMod = SpawnModule(this.chunkViewerMod.sessionService, this)

    override fun onEnable() {
        val modules = listOf(
            chunkViewerMod,
            spawnMod,
        )

        modules.forEach {
            it.onEnable()
        }
    }


    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return VoidWorldGenerator()
    }
}