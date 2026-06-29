package space.chunks.lobby.modules.matchmaking.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import space.chunks.lobby.controlplane.instance.InstanceService
import space.chunks.lobby.modules.matchmaking.MMService
import space.chunks.lobby.modules.party.event.PartyDisbandEvent
import space.chunks.lobby.modules.party.event.PartyPlayerJoinEvent
import space.chunks.lobby.modules.party.event.PartyPlayerKickedEvent
import space.chunks.lobby.ui.Texts
import space.chunks.lobby.ui.bossbar.BossBars
import java.util.logging.Level
import java.util.logging.Logger

class PartyListener(
    private val logger: Logger,
    private val mmService: MMService,
    private val instanceService: InstanceService,
    private val bossbars: BossBars,
    private val texts: Texts,
) : Listener {
    @EventHandler
    fun onPartyPlayerKicked(event: PartyPlayerKickedEvent) {
        val party = event.party
        val player = event.player
        this.logger.info("player kicked from party, removing ticket. partyId=${party.id} playerName=${player.name} playerId=${player.id}")
        this.mmService.removeTicketByActor(party.id)
        this.instanceService.cancelJob(party.id)
        this.bossbars.clearLoadingBar(party.onlinePlayers())
        player.asPlayer()?.let { player ->
            this.bossbars.clearLoadingBar(listOf(player))
        }

        this.texts.send(party, "matchmaking.ticket.cancelled-player-left-party")
    }

    @EventHandler
    fun onPartyDisband(event: PartyDisbandEvent) {
        val party = event.party
        this.logger.info("party disbanded, removing ticket. partyId=${party.id}")
        this.mmService.removeTicketByActor(event.party.id)
        this.instanceService.cancelJob(party.id)
        this.bossbars.clearLoadingBar(party.onlinePlayers())
    }

    @EventHandler
    fun onPartyPlayerJoin(event: PartyPlayerJoinEvent) {
        val party = event.party
        val player = event.player
        this.logger.info("player joined party, removing ticket. partyId=${party.id} playerName=${event.player.name} playerId=${event.player.id}")
        this.mmService.removeTicketByActor(event.party.id)
        this.instanceService.cancelJob(event.party.id)
        this.bossbars.clearLoadingBar(party.onlinePlayers())

        try {
            // if there is a ticket active for the player that joined the party, we want to remove it,
            // so it will not be matched anymore.
            logger.info("removing ticket for player joining party. playerName=${player.name} playerId=${player.id}")
            this.mmService.removeTicketByActor(event.player.id.toString())
            this.texts.send(party, "matchmaking.ticket.cancelled-player-joined-party")
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "error removing ticket for player joining party", e)
            event.isCancelled = true
        }
    }
}