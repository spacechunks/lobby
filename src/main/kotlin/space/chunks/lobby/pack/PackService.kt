package space.chunks.lobby.pack

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.headObject
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.writeToFile
import aws.smithy.kotlin.runtime.net.url.Url
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Logger
import java.util.zip.ZipInputStream

class PackService(
    private val logger: Logger,
    private val plugin: Plugin,
    private val cfg: ResourcePackConfig,
) {
    val packDownloadUrl = "https://${this.cfg.s3.bucket}.${this.cfg.s3.endpoint.replace("https://", "")}/${this.cfg.s3.packObjectKey}"
    val packHash = AtomicReference("")

    private val hashPerPlayer = mutableMapOf<UUID, String>()

    fun startPeriodicPull() {
        val s3 = S3Client {
            region = cfg.s3.region
            endpointUrl = Url.parse(cfg.s3.endpoint)
            credentialsProvider = StaticCredentialsProvider {
                accessKeyId = cfg.s3.accessKey
                secretAccessKey = cfg.s3.secretKey
            }
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, { _ ->
            val pack = File(this.plugin.dataFolder, "pack.zip")
            val localHash = if (pack.exists()) sha1(pack) else ""

            runBlocking {
                val headResp = s3.headObject {
                    bucket = cfg.s3.bucket
                    key = cfg.s3.packObjectKey
                }

                val remoteHash = if (headResp.metadata == null) "" else headResp.metadata!!.getOrDefault("sha1", "")

                logger.info("local_pack_hash=$localHash remote_pack_hash=$remoteHash")

                packHash.set(remoteHash)

                if (localHash == remoteHash) {
                    logger.info("hashes are the same skipping pack download")
                    return@runBlocking
                }

                val req = GetObjectRequest {
                    bucket = cfg.s3.bucket
                    key = cfg.s3.packObjectKey
                }

                s3.getObject(req) {
                    it.body?.writeToFile(pack)
                    unzip(pack, "${pack.parent}/pack-files")
                }
            }
        }, 0L, 20 * this.cfg.fetchIntervalSeconds.toLong())
    }

    fun setCurrentPack(playerId: UUID, hash: String) {
        this.hashPerPlayer[playerId] = hash
    }

    fun getCurrentPack(playerId: UUID): String {
        return this.hashPerPlayer[playerId] ?: return ""
    }

    private fun sha1(f: File): String {
        val digest = MessageDigest.getInstance("SHA-1")

        f.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun unzip(f: File, dst: String) {
        val dstDir = File(dst)
        if (!dstDir.exists()) {
            dstDir.mkdirs()
        }

        ZipInputStream(f.inputStream()).use { zipIn ->
            var entry = zipIn.nextEntry

            while (entry != null) {
                val newFile = File(dstDir, entry.name)

                if (entry.isDirectory) {
                    newFile.mkdirs()
                    continue
                }

                newFile.parentFile?.mkdirs()
                FileOutputStream(newFile).use { fos ->
                    zipIn.copyTo(fos)
                }

                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
    }
}