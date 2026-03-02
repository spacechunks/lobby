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
                config.getString("chunkViewer.resourcePack.s3.accessKey")
                    ?: throw RuntimeException("chunkViewer.resourcePack.s3.accessKey is missing")

            val secretKey =
                config.getString("chunkViewer.resourcePack.s3.secretKey")
                    ?: throw RuntimeException("chunkViewer.resourcePack.s3.secretKey is missing")

            val bucket = config.getString("chunkViewer.resourcePack.s3.bucket")
                ?: throw RuntimeException("chunkViewer.resourcePack.s3.bucket is missing")

            val region = config.getString("chunkViewer.resourcePack.s3.region")
                ?: throw RuntimeException("chunkViewer.resourcePack.s3.region is missing")

            val endpoint =
                config.getString("chunkViewer.resourcePack.s3.endpoint")
                    ?: throw RuntimeException("chunkViewer.resourcePack.s3.endpoint is missing")

            val objKey =
                config.getString("chunkViewer.resourcePack.s3.packObjectKey")
                    ?: throw RuntimeException("chunkViewer.resourcePack.s3.packObjectKey is missing")

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
            val fetchIntervalSeconds = config.getInt("chunkViewer.resourcePack.fetchIntervalSeconds")

            val thumbnailsLocation = config.getString("chunkViewer.resourcePack.thumbnailsLocation")
                ?: throw RuntimeException("chunkViewer.resourcePack.thumbnailsLocation is missing")

            val thumbnailMissingKey = config.getString("chunkViewer.resourcePack.thumbnailMissingKey")
                ?: throw RuntimeException("chunkViewer.resourcePack.thumbnailMissingKey is missing")

            val thumbnailKeyPrefix = config.getString("chunkViewer.resourcePack.thumbnailKeyPrefix")
                ?: throw RuntimeException("chunkViewer.resourcePack.thumbnailKeyPrefix is missing")

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