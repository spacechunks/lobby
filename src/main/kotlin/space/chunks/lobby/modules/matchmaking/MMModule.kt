package space.chunks.lobby.modules.matchmaking

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import space.chunks.lobby.controlplane.instance.InstanceService
import space.chunks.lobby.modules.LobbyModule
import space.chunks.lobby.modules.matchmaking.listener.PartyListener
import space.chunks.lobby.modules.matchmaking.listener.PlayerListener
import space.chunks.lobby.modules.matchmaking.listener.TicketListener
import space.chunks.lobby.modules.party.PartyService
import space.chunks.lobby.ui.Texts
import space.chunks.lobby.ui.bossbar.BossBars

class MMModule(
    plugin: Plugin,
    private val partyService: PartyService,
    private val mmService: MMService,
    private val instanceService: InstanceService,
    private val texts: Texts,
    private val bossbars: BossBars,
    private val cfg: Config,
) : LobbyModule(plugin, "matchmaking") {

    override fun onEnable() {
        this.mmService.removeAllTickets()

        Bukkit.getPluginManager().registerEvents(
            PlayerListener(
                this.logger,
                this.plugin,
                mmService,
                this.partyService,
                this.texts,
                this.bossbars,
                this.cfg,
                instanceService,
            ),
            this.plugin,
        )

        Bukkit.getPluginManager().registerEvents(
            PartyListener(
                this.logger,
                this.mmService,
                this.instanceService,
                this.bossbars
            ),
            this.plugin,
        )

        Bukkit.getPluginManager().registerEvents(
            TicketListener(
                this.logger,
                cfg,
                mmService,
                this.instanceService,
                this.partyService,
                this.bossbars,
                this.texts,
            ),
            this.plugin,
        )

    }

    override fun onDisable() {
        this.mmService.cancelScope()
        this.instanceService.cancelScope()
    }
}