package space.chunks.lobby.modules.spawn

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration

data class VectorConfig(
    val x: Double,
    val y: Double,
    val z: Double,
) {
    companion object {
        fun parse(config: ConfigurationSection): VectorConfig {
            return VectorConfig(
                config.getDouble("x"),
                config.getDouble("y"),
                config.getDouble("z"),
            )
        }
    }

}

data class Config(
    val world: String,
    val spawnLocation: VectorConfig,
    val roboSpawnLocation: VectorConfig,
    val postgresDSN: String
) {
    companion object {
        fun parse(config: FileConfiguration): Config {
            val spawnLoc =
                config.getConfigurationSection("spawn.location")
                    ?: throw IllegalArgumentException("spawn.location is missing")

            val roboLoc =
                config.getConfigurationSection("spawn.roboLocation")
                    ?: throw IllegalArgumentException("spawn.roboLocation is missing")

            val postgresDSN =
                config.getString("spawn.postgresDSN") ?: throw IllegalArgumentException("postgresDSN is missing")

            return Config(
                config.getString("spawn.world") ?: throw IllegalArgumentException("spawn.world is missing"),
                VectorConfig.parse(spawnLoc),
                VectorConfig.parse(roboLoc),
                postgresDSN,
            )
        }
    }
}


