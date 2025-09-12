package space.chunks.explorer.lobby.world

import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.Random

class VoidWorldGenerator : ChunkGenerator() {

    override fun generateNoise(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        chunkData: ChunkData
    ) {

    }

    // Generate surface features (empty in this case)
    override fun generateSurface(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        chunkData: ChunkData
    ) {

    }

    // Generate bedrock (only at spawn)
    override fun generateBedrock(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        chunkData: ChunkData
    ) {
        // Bedrock generation is handled in generateNoise
    }

    // Define world settings
    override fun shouldGenerateNoise(): Boolean = false
    override fun shouldGenerateSurface(): Boolean = false
    override fun shouldGenerateBedrock(): Boolean = false
    override fun shouldGenerateCaves(): Boolean = false
    override fun shouldGenerateDecorations(): Boolean = false
    override fun shouldGenerateMobs(): Boolean = false
    override fun shouldGenerateStructures(): Boolean = false

    override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider? {
        return super.getDefaultBiomeProvider(worldInfo)
    }
}
