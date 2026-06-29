package space.chunks.lobby.controlplane

import org.bukkit.configuration.file.FileConfiguration
import space.chunks.lobby.extensions.parseAddress

data class Config(
    val addr: String,
    val port: Int,
    val apiToken: String,
    val instancePollIntervalSeconds: Int,
) {
    companion object {
        fun parse(config: FileConfiguration): Config {
            val explorerEndpoint =
                config.getString("controlPlane.endpoint")
                    ?: throw RuntimeException("controlPlane.endpoint is missing")

            val parts = explorerEndpoint.parseAddress()

            val controlPlaneAPIToken = config.getString("controlPlane.apiToken")
                ?: throw RuntimeException("controlPlane.apiToken is missing")

            val instancePollIntervalSeconds = config.getInt("controlPlane.instancePollIntervalSeconds", 1)

            return Config(parts.first, parts.second, controlPlaneAPIToken, instancePollIntervalSeconds)
        }
    }
}