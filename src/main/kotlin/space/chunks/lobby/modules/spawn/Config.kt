package space.chunks.lobby.modules.spawn

import org.bukkit.configuration.file.FileConfiguration

data class VectorConfig(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    companion object {
        fun parse(config: FileConfiguration): VectorConfig {
            return VectorConfig(
                config.getDouble("spawn.location.x"),
                config.getDouble("spawn.location.y"),
                config.getDouble("spawn.location.z"),
            )
        }
    }

}

data class Config(
    val world: String,
    val spawnLocation: VectorConfig,
) {
    companion object {
        fun parse(config: FileConfiguration): Config {
            return Config(
                config.getString("spawn.world") ?: throw IllegalArgumentException("spawn.world is missing"),
                VectorConfig.parse(config),
            )
        }
    }
}


