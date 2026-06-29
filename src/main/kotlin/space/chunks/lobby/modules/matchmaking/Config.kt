package space.chunks.lobby.modules.matchmaking

import org.bukkit.configuration.file.FileConfiguration
import space.chunks.lobby.extensions.parseAddress

data class Config(
    val addr: String,
    val port: Int,
    val ticketPollInterval: Int,
    val gatewayHost: String,
    val gatewayPort: Int,
) {
    companion object {
        fun parse(config: FileConfiguration): Config {
            val mmEndpoint =
                config.getString("matchmaking.endpoint")
                    ?: throw RuntimeException("matchmaking.endpoint is missing")

            val mmParts = mmEndpoint.parseAddress()

            val ticketPollInterval = config.getInt("matchmaking.ticketPollInterval", 1)

            val gwAddr = config.getString("matchmaking.gatewayAddress")
                ?: throw RuntimeException("chunkViewer.gatewayAddress is missing")

            val gwParts = gwAddr.parseAddress()

            return Config(
                mmParts.first,
                mmParts.second,
                ticketPollInterval,
                gwParts.first,
                gwParts.second,
            )
        }
    }
}