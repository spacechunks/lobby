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

            val parts = parseAddress(explorerEndpoint)

            val controlPlaneAPIToken = config.getString("chunkViewer.controlPlane.apiToken")
                ?: throw RuntimeException("chunkViewer.controlPlane.apiToken is missing")

            return ControlPlaneConfig(parts.first, parts.second, controlPlaneAPIToken)
        }
    }
}

data class Config(
    val instancePollIntervalSeconds: Int,
    val gatewayHost: String,
    val gatewayPort: Int,
    val controlPlane: ControlPlaneConfig,
)

fun parseConfig(config: FileConfiguration): Config {
    val addr = config.getString("chunkViewer.gatewayAddress")
        ?: throw RuntimeException("chunkViewer.gatewayAddress is missing")

    val parts = parseAddress(addr)

    return Config(
        config.getInt("chunkViewer.instancePollIntervalSeconds", 1),
        parts.first,
        parts.second,
        ControlPlaneConfig.parse(config)
    )
}


fun parseAddress(addr: String): Pair<String, Int> {
    val parts = addr.split(":")

    if (parts.size < 2) {
        throw RuntimeException("$addr is not a invalid address")
    }

    return Pair(parts[0], parts[1].toInt())
}
