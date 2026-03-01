package space.chunks

import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import space.chunks.lobby.chunkviewer.ChunkViewerModule
import space.chunks.lobby.chunkviewer.world.VoidWorldGenerator
import space.chunks.lobby.spawn.SpawnModule

class Plugin : JavaPlugin() {

    override fun onEnable() {
        val modules = listOf(
            ChunkViewerModule(this),
            SpawnModule(this)
        )

        modules.forEach {
            it.onEnable()
        }
    }


    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return VoidWorldGenerator()
    }
}