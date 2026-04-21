package space.chunks.lobby.modules.chunkviewer.listener

import chunks.space.api.explorer.instance.v1alpha1.Api
import chunks.space.api.explorer.instance.v1alpha1.InstanceServiceGrpcKt
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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
import space.chunks.lobby.modules.party.PartyService
import space.chunks.lobby.util.LoadingTitle
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger
import chunks.space.api.explorer.instance.v1alpha1.Types as InstanceTypes

class PlayerListener(
    private val logger: Logger,
    private val plugin: Plugin,
    private val sessionService: DisplaySessionService,
    private val instanceClient: InstanceServiceGrpcKt.InstanceServiceCoroutineStub,
    private val config: Config,
    private val partyService: PartyService,
) : Listener {
    private val playerTasks = mutableMapOf<UUID, BukkitTask>()
    private val loadingTitle = LoadingTitle(this.plugin, Component.text("Instance is loading..."))

    @EventHandler
    private fun onPlayerFlavorSelect(event: PlayerSelectFlavorEvent) {
        val player = event.player
        val flavor = event.flavor
        val ver = flavor.versionsList.first()

        val party = this.partyService.getParty(player)
        if (party != null) {
            if (party.owner != player) {
                player.sendMessage("You have to be party owner to start a game")
                return
            }

            val arr = JsonArray()
            party.members
                .map { it.uniqueId.toString() }
                .toList()
                .forEach { arr.add(it) }

            val obj = JsonObject()
            obj.add("members", arr)

            party.owner.storeCookie(
                NamespacedKey.fromString("spacechunks:party/members")!!,
                obj.toString().toByteArray()
            )
        }

        val players = listOf(player, *party?.members?.toTypedArray() ?: arrayOf())

        players.forEach {
            this.loadingTitle.run(it)
        }

        this.logger.info("flavor selected. playerId=${player.uniqueId} flavorId=${flavor.id} flavorVersionId=${ver.id}")

        player.sendMessage(Component.text("Starting instance for Chunk ").color(NamedTextColor.GRAY).append(Component.text(event.chunk.name).color(NamedTextColor.WHITE)))

        this.runFlavorVersion(event.chunk.id, ver.id)
            .thenCompose {
                return@thenCompose this.waitForInstance(player.uniqueId, it.id)
            }.thenAccept {
                this.instanceCreationCompleted(players, it)
            }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        this.sessionService.closeSession(player)

        this.playerTasks[player.uniqueId]?.cancel()
        this.playerTasks.remove(player.uniqueId)
    }

    private fun instanceCreationCompleted(players: List<Player>, instance: InstanceTypes.Instance) {
        val state = instance.state

        if (state == InstanceTypes.InstanceState.CREATION_FAILED
            || state == InstanceTypes.InstanceState.DELETING
            || state == InstanceTypes.InstanceState.DELETED
        ) {
            players.forEach {
                it.sendMessage(
                    Component.text("Instanced failed to be created: REASON: $state", NamedTextColor.RED)
                )
                this.loadingTitle.stop(it)
            }
            return
        }

        players.forEach {
            val data = "{\"addr\":\"${instance.ip}:${instance.port}\"}".toByteArray()
            it.storeCookie(NamespacedKey.fromString("spacechunks:explorer/gateway/transfer")!!, data)
            it.transfer(this.config.gatewayHost, this.config.gatewayPort)
            this.loadingTitle.stop(it)
        }
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