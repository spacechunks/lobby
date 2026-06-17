package space.chunks.lobby.modules.chunkviewer.listener

import chunks.space.api.explorer.instance.v1alpha1.InstanceServiceGrpcKt
import chunks.space.api.matchmaking.v1alpha1.MatchmakingServiceGrpcKt
import chunks.space.api.matchmaking.v1alpha1.removeTicketRequest
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.grpc.StatusException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import space.chunks.lobby.modules.chunkviewer.Config
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService
import space.chunks.lobby.modules.chunkviewer.event.PlayerSelectFlavorEvent
import space.chunks.lobby.modules.party.PartyService
import space.chunks.lobby.ui.Texts
import space.chunks.lobby.ui.bossbar.BossBars
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.time.Duration.Companion.milliseconds
import chunks.space.api.explorer.instance.v1alpha1.Api as instancev1alpha1Api
import chunks.space.api.explorer.instance.v1alpha1.Types as InstanceTypes
import chunks.space.api.matchmaking.v1alpha1.Api as mmv1alphaApi

class PlayerListener(
    private val logger: Logger,
    private val plugin: Plugin,
    private val sessionService: DisplaySessionService,
    private val instanceClient: InstanceServiceGrpcKt.InstanceServiceCoroutineStub,
    private val mmClient: MatchmakingServiceGrpcKt.MatchmakingServiceCoroutineStub,
    private val config: Config,
    private val partyService: PartyService,
    private val texts: Texts,
    private val bossbars: BossBars,
    private val scope: CoroutineScope,
) : Listener {
    private val playerJobs = mutableMapOf<UUID, Job>()

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

        val job = this.scope.launch {
            if (event.mmMode) {
                var ticket: mmv1alphaApi.Ticket? = null
                try {
                    ticket = createTicket(players, flavor.id)
                    val instanceId = pollTicket(ticket.id)
                    val ins = waitForInstance(player.uniqueId, instanceId)
                    instanceCreationCompleted(players, ins)
                } catch (e: Exception) {
                    logger.log(Level.WARNING, "error while creating and waiting for ticket", e)
                    if (ticket != null) {
                        mmClient.removeTicket(removeTicketRequest { ticketId = ticket.id })
                    }
                }
                return@launch
            }

            var ins = runFlavorVersion(player.uniqueId.toString(), ver.id)
            ins = waitForInstance(player.uniqueId, ins.id)

            instanceCreationCompleted(players, ins)
        }

        this.playerJobs[player.uniqueId] = job
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        this.sessionService.closeSession(player)

        this.playerJobs[player.uniqueId]?.cancel()
        this.playerJobs.remove(player.uniqueId)
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

    private suspend fun waitForInstance(playerId: UUID, instanceId: String): InstanceTypes.Instance {
        this.logger.info("waiting for instance to be ready. playerId=${playerId} instanceId=$instanceId")
        while (true) {
            try {

                val req = instancev1alpha1Api.GetInstanceRequest.newBuilder().setId(instanceId).build()
                val resp = instanceClient.getInstance(req)
                when (val state = resp.instance.state) {
                    InstanceTypes.InstanceState.RUNNING,
                    InstanceTypes.InstanceState.CREATION_FAILED,
                    InstanceTypes.InstanceState.DELETING,
                    InstanceTypes.InstanceState.DELETED -> return resp.instance

                    else -> {
                        logger.info("instance not ready yet, state=$state")
                        delay((config.instancePollIntervalSeconds * 1000L).milliseconds)
                    }
                }
            } catch (e: Exception) {
                logger.log(Level.WARNING, "error polling instance", e)
            }
        }
    }

    private suspend fun pollTicket(ticketId: String): String {
        while (true) {
            val req = mmv1alphaApi.GetTicketRequest.newBuilder().setTicketId(ticketId).build()

            try {
                val resp = mmClient.getTicket(req)
                when (resp.ticket.status) {
                    mmv1alphaApi.TicketStatus.NO_PLAYABLE_FLAVOR_VERSION -> {
                        throw RuntimeException("NO_PLAYABLE_FLAVOR_VERSION")
                    }

                    else -> {
                        if (resp.ticket.hasAssignment()) {
                            return resp.ticket.assignment.instanceId
                        }
                        delay((config.instancePollIntervalSeconds * 1000L).milliseconds)
                    }
                }
            } catch (e: StatusException) {
                logger.log(Level.WARNING, "error polling ticket", e)
            }
        }
    }

    private suspend fun runFlavorVersion(orderedBy: String, versionId: String): InstanceTypes.Instance {
        val req = instancev1alpha1Api.RunFlavorVersionRequest.newBuilder()
            .setFlavorVersionId(versionId)
            .setOrderedBy(orderedBy)
            .build()
        return instanceClient.runFlavorVersion(req).instance
    }

    private suspend fun createTicket(players: List<Player>, flavorId: String): mmv1alphaApi.Ticket {
        logger.info("creating ticket. flavorId=$flavorId players=${players.joinToString(",") { it.name }}")
        val createReq = mmv1alphaApi.CreateTicketRequest.newBuilder()
            .setFlavorId(flavorId)
            .setPlayerCount(players.size)
            .build()
        val resp = mmClient.createTicket(createReq)
        val activateReq = mmv1alphaApi.ActivateTicketRequest.newBuilder()
            .setTicketId(resp.ticket.id)
            .build()
        mmClient.activateTicket(activateReq)
        return resp.ticket
    }
}
