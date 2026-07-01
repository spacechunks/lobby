package space.chunks.lobby

import chunks.space.api.explorer.chunk.v1alpha1.ChunkServiceGrpcKt
import chunks.space.api.explorer.instance.v1alpha1.InstanceServiceGrpcKt
import chunks.space.api.matchmaking.v1alpha1.MatchmakingServiceGrpcKt
import com.noxcrew.interfaces.InterfacesListeners
import io.grpc.ManagedChannelBuilder
import io.grpc.netty.NettyChannelBuilder
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.resource.ResourcePackStatus
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import space.chunks.lobby.controlplane.instance.InstanceService
import space.chunks.lobby.modules.chunkviewer.ChunkViewerModule
import space.chunks.lobby.modules.chunkviewer.grpc.AuthCredentials
import space.chunks.lobby.modules.chunkviewer.world.VoidWorldGenerator
import space.chunks.lobby.modules.matchmaking.MMModule
import space.chunks.lobby.modules.matchmaking.MMService
import space.chunks.lobby.modules.party.PartyModule
import space.chunks.lobby.modules.party.PartyService
import space.chunks.lobby.modules.spawn.SpawnModule
import space.chunks.lobby.pack.PackService
import space.chunks.lobby.pack.ResourcePackConfig
import space.chunks.lobby.ui.ActionBar
import space.chunks.lobby.ui.Texts
import space.chunks.lobby.ui.bossbar.BossBars
import space.chunks.visual.ui.UiService
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import space.chunks.lobby.controlplane.Config as ControlPlaneConfig
import space.chunks.lobby.modules.matchmaking.Config as MMConfig

class Plugin : JavaPlugin(), Listener {
    private val cpConfig = ControlPlaneConfig.parse(this.config)
    private val mmConfig = MMConfig.parse(this.config)

    private val cpChannel = ManagedChannelBuilder
        .forAddress(this.cpConfig.addr, this.cpConfig.port)
        .keepAliveTime(30, TimeUnit.SECONDS)      // ping server every 30s
        .keepAliveTimeout(10, TimeUnit.SECONDS)   // wait 10s for pong
        .useTransportSecurity()
        .build()

    private val mmChannel = NettyChannelBuilder
        .forAddress(this.mmConfig.addr, this.mmConfig.port)
        .usePlaintext()
        .keepAliveTime(30, TimeUnit.SECONDS)
        .keepAliveTimeout(10, TimeUnit.SECONDS)
        .keepAliveWithoutCalls(true)
        .build()

    private val mmClient = MatchmakingServiceGrpcKt.MatchmakingServiceCoroutineStub(mmChannel)
    private val instanceClient = InstanceServiceGrpcKt.InstanceServiceCoroutineStub(cpChannel)
        .withCallCredentials(AuthCredentials(this.cpConfig.apiToken))
    private val chunkClient = ChunkServiceGrpcKt.ChunkServiceCoroutineStub(cpChannel)
        .withCallCredentials(AuthCredentials(cpConfig.apiToken))

    private val packConfig = ResourcePackConfig.parse(this.config)
    private val packService = PackService(this.logger, this, packConfig)
    private val partyService = PartyService()
    private val mmService = MMService(
        this.logger,
        this.mmClient,
        this.mmConfig.ticketPollInterval.seconds,
        this,
    )
    private val instanceService = InstanceService(
        this.logger,
        this.instanceClient,
        this.cpConfig.instancePollIntervalSeconds.seconds,
    )
    private val uiService = UiService(ActionBar::send)
    private val texts = Texts(this)
    private val bossbars = BossBars(this.uiService)

    // modules
    private val chunkViewerMod = ChunkViewerModule(this, this.packConfig, this.chunkClient, this.texts)
    private val spawnMod = SpawnModule(
        this.chunkViewerMod.sessionService,
        this,
        this.texts,
        this.uiService,
        this.partyService,
        this.mmService,
    )
    private val partyMod = PartyModule(this, this.partyService, this.texts, this.bossbars)
    private val mmMod = MMModule(
        this,
        this.partyService,
        this.mmService,
        this.instanceService,
        this.texts,
        this.bossbars,
        this.mmConfig,
    )

    private val modules = listOf(
        chunkViewerMod,
        spawnMod,
        partyMod,
        mmMod,
    )

    override fun onEnable() {
        installInterfaces()
        Bukkit.getPluginManager().registerEvents(this.uiService, this)
        this.uiService.start(this)
        this.packService.startPeriodicPull()
        Bukkit.getPluginManager().registerEvents(this, this)

        modules.forEach {
            it.onEnable()
        }
    }

    override fun onDisable() {
        this.modules.forEach {
            it.onDisable()
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!label.equals("mm", ignoreCase = true)) {
            return false
        }

        this.chunkViewerMod.sessionService.startSession(sender as Player, true)
        return true
    }

    private fun installInterfaces() {
        runCatching {
            InterfacesListeners.install(this)
        }.onFailure { ex ->
            if (ex !is IllegalArgumentException) {
                throw ex
            }
            logger.info("Noxcrew interfaces are already installed; reusing the existing listener.")
        }
    }

    @EventHandler
    fun onAsyncConfigure(event: AsyncPlayerConnectionConfigureEvent) {
        val conn = event.connection
        val future = CompletableFuture<ResourcePackStatus>()
        val hash = this.packService.packHash.get()

        val info = ResourcePackInfo.resourcePackInfo(
            UUID.fromString("92de217b-8b2b-403b-86a5-fe26fa3a9b5f"),
            URI.create(this.packService.packDownloadUrl),
            hash
        )

        val request = ResourcePackRequest.resourcePackRequest()
            .packs(info)
            .required(true)
            .callback { _, status, _ ->
                future.complete(status)
            }
            .build()

        conn.audience.sendResourcePacks(request)

        val status = try {
            future.get(30, TimeUnit.SECONDS)
        } catch (_: Throwable) {
            null
        }

        when (status) {
            ResourcePackStatus.ACCEPTED,
            ResourcePackStatus.SUCCESSFULLY_LOADED,
            ResourcePackStatus.DOWNLOADED -> {
                this.packService.setCurrentPack(conn.profile.id!!, hash)
                return
            }

            ResourcePackStatus.DECLINED -> {
                conn.disconnect(this.texts.component("common.resource-pack.declined"))
            }

            ResourcePackStatus.FAILED_DOWNLOAD,
            ResourcePackStatus.FAILED_RELOAD,
            ResourcePackStatus.INVALID_URL,
            ResourcePackStatus.DISCARDED,
            null -> {
                conn.disconnect(this.texts.component("common.resource-pack.failed"))
            }
        }
    }

    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return VoidWorldGenerator()
    }
}
