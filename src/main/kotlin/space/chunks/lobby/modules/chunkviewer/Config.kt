package space.chunks.lobby.modules.chunkviewer

import org.bukkit.configuration.file.FileConfiguration

data class ControlPlaneConfig(
    val addr: String,
    val port: Int,
    val apiToken: String,
) {
    companion object {
        fun parse(config: FileConfiguration): ControlPlaneConfig {
            val explorerEndpoint =
                config.getString("chunkViewer.controlPlane.endpoint")
                    ?: throw RuntimeException("chunkViewer.controlPlane.endpoint is missing")

            val parts = explorerEndpoint.split(":")

            if (parts.size < 2) {
                throw RuntimeException("chunkViewer.controlPlaneEndpoint is invalid")
            }

            val controlPlaneAPIToken = config.getString("chunkViewer.controlPlane.apiToken")
                ?: throw RuntimeException("chunkViewer.controlPlane.apiToken is missing")

            return ControlPlaneConfig(parts[0], parts[1].toInt(), controlPlaneAPIToken)
        }
    }
}

data class Config(
    val controlPlane: ControlPlaneConfig,
)

fun parseConfig(config: FileConfiguration): Config {
    return Config(ControlPlaneConfig.parse(config))
}

