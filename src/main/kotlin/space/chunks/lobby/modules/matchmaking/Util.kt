package space.chunks.lobby.modules.matchmaking

import chunks.space.api.explorer.instance.v1alpha1.Types
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import space.chunks.lobby.controlplane.instance.InstanceService
import space.chunks.lobby.modules.party.PartyService
import space.chunks.lobby.ui.Texts
import space.chunks.lobby.ui.bossbar.BossBars
import space.chunks.lobby.ui.bossbar.LoadingBossBar
import java.util.*
import java.util.logging.Logger


fun waitForInstance(
    logger: Logger,
    config: Config,
    instanceService: InstanceService,
    partyService: PartyService,
    mmService: MMService,
    bossbars: BossBars,
    texts: Texts,
    actorId: String,
    instanceId: String,
    onComplete: () -> Unit = {}
) {
    val players = getPlayersByActorId(partyService, actorId)

    instanceService.getInstance(instanceId).thenAccept { instance ->
        bossbars.sendLoadingBar(
            players,
            players.size,
            LoadingBossBar.LoadingState.STARTING,
            instance.chunk.name,
            instance.flavor.name,
            instance.flavorVersion.maxPlayers,
        )
    }

    instanceService.waitForInstance(actorId, instanceId) { instance ->
        when (val state = instance.state) {
            Types.InstanceState.CREATION_FAILED,
            Types.InstanceState.DELETING,
            Types.InstanceState.DELETED -> {
                players.forEach {
                    it.sendMessage(
                        texts.component(
                            "chunkviewer.instance.creation-failed", // TODO: make matchmaking text
                            mapOf("state" to state)
                        )
                    )
                }

                onComplete()

                bossbars.clearLoadingBar(players)
                logger.info("instance creation failed, removing ticket. actorId=$actorId instanceId=${instance.id} instanceState=$state")
                mmService.removeTicketByActor(actorId)
            }

            Types.InstanceState.PENDING, Types.InstanceState.CREATING -> {
                // debug log?
                return@waitForInstance
            }

            Types.InstanceState.RUNNING -> {
                logger.info("instance running. actorId=$actorId instanceId=${instance.id} instanceState=${state} instancePort=${instance.port} instanceIp=${instance.ip}")
                players.forEach {
                    val data = "{\"id\":\"${instance.id}\",\"addr\":\"${instance.ip}:${instance.port}\"}".toByteArray()
                    it.storeCookie(NamespacedKey.fromString("spacechunks:explorer/gateway/transfer")!!, data)

                    // as usual, we have to wait before transferring the player to the
                    // instance, otherwise the resource pack won't unload
//                    Bukkit.getScheduler().runTaskLater(this.plugin, Runnable {
                    onComplete()
                    it.transfer(config.gatewayHost, config.gatewayPort)
//                    }, 10L)
                }
            }

            else -> {
                // we continue just in case the system heals at some point.
                // if the player disconnects, we stop the task anyway.
                logger.warning("instance in unknown state. instanceId=${instance.id} instanceState=${state}")
            }
        }
    }
}

fun getPlayersByActorId(partyService: PartyService, actorId: String): List<Player> {
    val party = partyService.getPartyById(actorId)
    return party?.onlinePlayers()
        ?: listOf(
            Bukkit.getPlayer(UUID.fromString(actorId))
        ).mapNotNull { it }
}