package space.chunks.lobby.modules.chunkviewer.listener

import chunks.space.api.explorer.instance.v1alpha1.Api
import chunks.space.api.explorer.instance.v1alpha1.InstanceServiceGrpcKt
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import space.chunks.lobby.modules.chunkviewer.Config
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService
import space.chunks.lobby.modules.chunkviewer.event.PlayerSelectFlavorEvent
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger
import chunks.space.api.explorer.instance.v1alpha1.Types as InstanceTypes

class PlayerListener(
    private val logger: Logger,
    private val plugin: Plugin,
    private val sessionService: DisplaySessionService,
    private val instanceClient: InstanceServiceGrpcKt.InstanceServiceCoroutineStub,
    private val config: Config
) : Listener {
    private val playerTasks = mutableMapOf<UUID, BukkitTask>()

    @EventHandler
    private fun onPlayerFlavorSelect(event: PlayerSelectFlavorEvent) {
        val player = event.player
        val flavor = event.flavor
        val ver = flavor.versionsList.first()

        this.logger.info("flavor selected. playerId=${player.uniqueId} flavorId=${flavor.id} flavorVersionId=${ver.id}")

        this.runFlavorVersion(event.chunk.id, ver.id)
            .thenCompose {
                return@thenCompose this.waitForInstance(player.uniqueId, it.id)
            }.thenAccept {
                this.instanceCreationCompleted(player, it)
            }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        this.sessionService.closeSession(player)

        this.playerTasks[player.uniqueId]?.cancel()
        this.playerTasks.remove(player.uniqueId)
    }

    private fun instanceCreationCompleted(player: Player, instance: InstanceTypes.Instance) {
        val state = instance.state

        if (state == InstanceTypes.InstanceState.CREATION_FAILED
            || state == InstanceTypes.InstanceState.DELETING
            || state == InstanceTypes.InstanceState.DELETED
        ) {
            player.sendMessage(
                Component.text("Instanced failed to be created: REASON: $state", NamedTextColor.RED)
            )
            return
        }

        val data = "{\"addr\":\"${instance.ip}:${instance.port}\"}".toByteArray()
        player.storeCookie(NamespacedKey.fromString("spacechunks:explorer/gateway/transfer")!!, data)

        player.transfer(this.config.gatewayHost, this.config.gatewayPort)
    }

    private fun runFlavorVersion(chunkId: String, versionId: String): CompletableFuture<InstanceTypes.Instance> {
        val f = CompletableFuture<InstanceTypes.Instance>()
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            runBlocking {
                val req = Api.RunFlavorVersionRequest
                    .newBuilder()
                    .setFlavorVersionId(versionId)
                    .setChunkId(chunkId)
                    .build()
                val resp = instanceClient.runFlavorVersion(req)
                f.complete(resp.instance)
            }
        })
        return f
    }

    private fun waitForInstance(playerId: UUID, instanceId: String): CompletableFuture<InstanceTypes.Instance> {
        val f = CompletableFuture<InstanceTypes.Instance>()
        this.logger.info("waiting for instance to be ready. playerId=${playerId} instanceId=$instanceId")

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, { t ->
            this.playerTasks[playerId] = t
            runBlocking {
                val req = Api.GetInstanceRequest
                    .newBuilder()
                    .setId(instanceId)
                    .build()

                val resp = instanceClient.getInstance(req)

                when (val state = resp.instance.state) {
                    InstanceTypes.InstanceState.CREATION_FAILED,
                    InstanceTypes.InstanceState.DELETING,
                    InstanceTypes.InstanceState.DELETED -> {
                        logger.info("instance creation failed. playerId=${playerId} instanceId=$instanceId instanceState=${state}")
                        t.cancel()
                        playerTasks.remove(playerId)
                        f.complete(resp.instance)
                    }

                    InstanceTypes.InstanceState.PENDING, InstanceTypes.InstanceState.CREATING -> {
                        return@runBlocking
                    }

                    InstanceTypes.InstanceState.RUNNING -> {
                        logger.info("instance running. playerId=${playerId} instanceId=$instanceId instanceState=${state} instancePort=${resp.instance.port} instanceIp=${resp.instance.ip}")
                        t.cancel()
                        playerTasks.remove(playerId)
                        f.complete(resp.instance)
                    }

                    else -> {
                        // we continue just in case the system heals at some point.
                        // if the player disconnects, we stop the task anyway.
                        logger.warning("instance in unknown state. playerId=${playerId} instanceId=$instanceId instanceState=${state}")
                        return@runBlocking
                    }
                }
            }
        }, 0L, 20 * this.config.instancePollIntervalSeconds.toLong())
        return f
    }
}