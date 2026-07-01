package space.chunks.lobby.modules.matchmaking

import chunks.space.api.explorer.chunk.v1alpha1.Types
import chunks.space.api.matchmaking.v1alpha1.MatchmakingServiceGrpcKt
import chunks.space.api.matchmaking.v1alpha1.activateTicketRequest
import chunks.space.api.matchmaking.v1alpha1.removeAllTicketsRequest
import chunks.space.api.matchmaking.v1alpha1.removeTicketRequest
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.*
import org.bukkit.plugin.Plugin
import space.chunks.lobby.extensions.callSyncEvent
import space.chunks.lobby.modules.matchmaking.event.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.time.Duration
import chunks.space.api.matchmaking.v1alpha1.Api as mmv1alphaApi

class MMService(
    private val logger: Logger,
    private val client: MatchmakingServiceGrpcKt.MatchmakingServiceCoroutineStub,
    private val ticketPollInterval: Duration,
    private val plugin: Plugin,
) {
    private val ticketsByActor = ConcurrentHashMap<String, MMData>()
    private val actorJobs = mutableMapOf<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun createAndPollTicket(
        actorId: String,
        chunk: Types.Chunk,
        flavor: Types.Flavor,
        playerCount: Int,
    ) {
        val job = this.scope.launch {
           try {
               val req = mmv1alphaApi.CreateTicketRequest
                   .newBuilder()
                   .setFlavorId(flavor.id)
                   .setPlayerCount(playerCount)
                   .build()

               val resp = client.createTicket(req)
               val data = MMData(
                   resp.ticket,
                   chunk,
                   flavor,
                   actorId,
               )

               ticketsByActor[actorId] = data

               client.activateTicket(activateTicketRequest {
                   ticketId = resp.ticket.id
               })

               plugin.callSyncEvent(TicketActivatedEvent(data))

               pollTicket(actorId, resp.ticket.id)
           } catch (e: StatusException) {
               logger.log(Level.SEVERE, "error creating and activating ticket. actorId=$actorId", e)
               plugin.callSyncEvent(
                   TicketCancelledEvent(actorId, null, TicketCancelCause.SERVICE_UNAVAILABLE)
               )
           }
        }

        this.actorJobs[actorId] = job
    }

    fun cancelScope() {
        this.scope.cancel()
    }

    fun removeTicketByActor(actorId: String) {
        try {
            this.ticketsByActor[actorId]?.let { data ->
                this.logger.info("removing ticket for actor. actorId=$actorId ticketId=${data.ticket.id}")
                this.scope.launch {
                    client.removeTicket(removeTicketRequest {
                        ticketId = data.ticket.id
                    })
                    
                    plugin.callSyncEvent(TicketCancelledEvent(actorId, null, TicketCancelCause.REMOVED))
                }
            }
        } catch (e: StatusException) {
            throw e
        } finally {
            this.ticketsByActor.remove(actorId)
            this.actorJobs[actorId]?.cancel()
            this.actorJobs.remove(actorId)
        }
    }

    fun removeAllTickets() {
        this.logger.info("removing all tickets")
        this.scope.launch {
            client.removeAllTickets(removeAllTicketsRequest {})
        }
        this.ticketsByActor.clear()
        this.actorJobs.forEach { (_, job) -> job.cancel() }
        this.actorJobs.clear()
    }

    private suspend fun pollTicket(
        actorId: String,
        ticketId: String,
    ) {
        while (true) {
            val req = mmv1alphaApi.GetTicketRequest
                .newBuilder()
                .setTicketId(ticketId)
                .build()

            try {
                val resp = this.client.getTicket(req)
                val newTicket = resp.ticket

                if (newTicket.status == mmv1alphaApi.TicketStatus.NO_PLAYABLE_FLAVOR_VERSION) {
                    this.plugin.callSyncEvent(
                        TicketCancelledEvent(actorId, ticketId, TicketCancelCause.NO_PLAYABLE_FLAVOR_VERSION)
                    )
                    return
                }

                val data = this.ticketsByActor[actorId] ?: continue

                if (newTicket.hasMatch()) {
                    this.plugin.callSyncEvent(MatchUpdateEvent(data))
                }

                if (newTicket.hasAssignment()) {
                    this.plugin.callSyncEvent(TicketAssignmentEvent(actorId, newTicket.id, newTicket.assignment.instanceId))
                    return
                }

                // the old ticket (data.ticket) did have a match, but the new updated one (newTicket)
                // does not have one, it means that the match was cancelled
                if (data.ticket.hasMatch() && !newTicket.hasMatch()) {
                    this.plugin.callSyncEvent(MatchCancelledEvent(data))
                }

                this.ticketsByActor[actorId] = MMData(
                    newTicket,
                    data.chunk,
                    data.flavor,
                    data.actorId,
                )
            } catch (e: StatusException) {
                if (e.status.code == Status.Code.NOT_FOUND) {
                    this.plugin.callSyncEvent(
                        TicketCancelledEvent(actorId, ticketId, TicketCancelCause.NOT_FOUND)
                    )
                    return
                }
                logger.log(Level.WARNING, "error polling ticket. ticketId=$ticketId", e)
            }

            delay(this.ticketPollInterval)
        }
    }
}