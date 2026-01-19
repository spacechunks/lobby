package space.chunks.explorer.lobby

import org.bukkit.configuration.file.FileConfiguration
import kotlin.text.split

data class Config(
    val controlPlaneEndpointAddr: String,
    val controlPlaneEndpointPort: Int,
    val controlPlaneAPIToken: String,
)

fun parseConfig(config: FileConfiguration): Config {
    val explorerEndpoint =
        config.getString("controlPlaneEndpoint")
            ?: throw RuntimeException("controlPlaneEndpoint is missing")

    val parts = explorerEndpoint.split(":")

    if (parts.size < 2) {
        throw RuntimeException("controlPlaneEndpoint is invalid")
    }

    val controlPlaneAPIToken  = config.getString("controlPlaneAPIToken")
        ?: throw RuntimeException("controlPlaneAPIToken is missing")

    return Config(
        parts[0],
        parts[1].toInt(),
        controlPlaneAPIToken
    )
}

