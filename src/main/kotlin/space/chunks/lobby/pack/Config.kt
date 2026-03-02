package space.chunks.lobby.pack

import org.bukkit.configuration.file.FileConfiguration

data class S3Config(
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
    val region: String,
    val endpoint: String,
    val packObjectKey: String
) {
    companion object {
        fun parse(config: FileConfiguration): S3Config {
            val accessKey =
                config.getString("resourcePack.s3.accessKey")
                    ?: throw RuntimeException("resourcePack.s3.accessKey is missing")

            val secretKey =
                config.getString("resourcePack.s3.secretKey")
                    ?: throw RuntimeException("resourcePack.s3.secretKey is missing")

            val bucket = config.getString("resourcePack.s3.bucket")
                ?: throw RuntimeException("resourcePack.s3.bucket is missing")

            val region = config.getString("resourcePack.s3.region")
                ?: throw RuntimeException("resourcePack.s3.region is missing")

            val endpoint =
                config.getString("resourcePack.s3.endpoint")
                    ?: throw RuntimeException("resourcePack.s3.endpoint is missing")

            val objKey =
                config.getString("resourcePack.s3.packObjectKey")
                    ?: throw RuntimeException("resourcePack.s3.packObjectKey is missing")

            return S3Config(accessKey, secretKey, bucket, region, endpoint, objKey)
        }
    }
}

data class ResourcePackConfig(
    val fetchIntervalSeconds: Int,
    val thumbnailsLocation: String,
    val thumbnailMissingKey: String,
    val thumbnailKeyPrefix: String,
    val s3: S3Config,
) {
    companion object {
        fun parse(config: FileConfiguration): ResourcePackConfig {
            val fetchIntervalSeconds = config.getInt("resourcePack.fetchIntervalSeconds")

            val thumbnailsLocation = config.getString("resourcePack.thumbnailsLocation")
                ?: throw RuntimeException("resourcePack.thumbnailsLocation is missing")

            val thumbnailMissingKey = config.getString("resourcePack.thumbnailMissingKey")
                ?: throw RuntimeException("resourcePack.thumbnailMissingKey is missing")

            val thumbnailKeyPrefix = config.getString("resourcePack.thumbnailKeyPrefix")
                ?: throw RuntimeException("resourcePack.thumbnailKeyPrefix is missing")

            return ResourcePackConfig(
                fetchIntervalSeconds,
                thumbnailsLocation,
                thumbnailMissingKey,
                thumbnailKeyPrefix,
                S3Config.parse(config)
            )
        }
    }
}