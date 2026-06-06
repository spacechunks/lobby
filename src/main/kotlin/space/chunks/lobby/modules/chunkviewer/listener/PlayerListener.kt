package space.chunks.lobby.modules.chunkviewer.listener

import chunks.space.api.explorer.instance.v1alpha1.Api
import chunks.space.api.explorer.instance.v1alpha1.InstanceServiceGrpcKt
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.grpc.StatusException
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
import space.chunks.lobby.ui.Texts
import space.chunks.lobby.ui.bossbar.BossBars
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
    private val texts: Texts,
    private val bossbars: BossBars,
) : Listener {
    private val playerTasks = mutableMapOf<UUID, BukkitTask>()

    @EventHandler
    private fun onPlayerFlavorSelect(event: PlayerSelectFlavorEvent) {
        val player = event.player
        val flavor = event.flavor
        val ver = flavor.versionsList.first()
        val chunk = event.chunk

        val party = this.partyService.getParty(player.uniqueId)
        if (party != null) {
            if (party.owner.id != player.uniqueId) {
                player.sendMessage(this.texts.component("chunkviewer.instance.owner-required"))
                return
            }

            val arr = JsonArray()
            party.members
                .map { it.id.toString() }
                .toList()
                .forEach { arr.add(it) }

            val obj = JsonObject()
            obj.add("members", arr)

            party.owner.asPlayer()!!.storeCookie(
                NamespacedKey.fromString("spacechunks:party/members")!!,
                obj.toString().toByteArray()
            )
        }

        val players =
            listOf(player, *party?.members?.map { it.asPlayer() }?.filterNotNull()?.toTypedArray() ?: arrayOf())

        this.bossbars.sendLoadingBar(players, chunk.name, flavor.name, players.count())

        this.logger.info("flavor selected. playerId=${player.uniqueId} flavorId=${flavor.id} flavorVersionId=${ver.id}")

        this.runFlavorVersion(player.uniqueId.toString(), chunk.id, ver.id)
            .exceptionally { e ->
                players.forEach { player ->
                    player.sendMessage(
                        Component.text("Failed to run instance: ${e.message}").color(NamedTextColor.RED)
                    )
                    this.bossbars.clearLoadingBar(players)
                }
                return@exceptionally null
            }
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
                it.sendMessage(this.texts.component("chunkviewer.instance.creation-failed", mapOf("state" to state)))
            }
            this.bossbars.clearLoadingBar(players)
            return
        }

        players.forEach {
            val data = "{\"id\":\"${instance.id}\",\"addr\":\"${instance.ip}:${instance.port}\"}".toByteArray()
            it.storeCookie(NamespacedKey.fromString("spacechunks:explorer/gateway/transfer")!!, data)

            // as usual, we have to wait before transferring the player to the
            // instance, otherwise the resource pack won't unload
            Bukkit.getScheduler().runTaskLater(this.plugin, Runnable {
                it.transfer(this.config.gatewayHost, this.config.gatewayPort)
            }, 10L)
        }
    }

    private fun runFlavorVersion(
        orderedBy: String,
        chunkId: String,
        versionId: String
    ): CompletableFuture<InstanceTypes.Instance> {
        val f = CompletableFuture<InstanceTypes.Instance>()
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            runBlocking {
                var resp: Api.RunFlavorVersionResponse?

                try {
                    val req = Api.RunFlavorVersionRequest
                        .newBuilder()
                        .setFlavorVersionId(versionId)
                        .setChunkId(chunkId)
                        .setOrderedBy(orderedBy)
                        .build()
                    resp = instanceClient.runFlavorVersion(req)
                } catch (e: StatusException) {
                    f.completeExceptionally(e)
                    return@runBlocking
                }

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
