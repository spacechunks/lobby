package space.chunks.lobby.ui.bossbar

import org.bukkit.entity.Player
import space.chunks.lobby.modules.party.Party
import space.chunks.visual.ui.BossBarSlot
import space.chunks.visual.ui.UiService

class BossBars(private val uiService: UiService) {
    val partySlot: BossBarSlot = this.uiService.bossBars.register("party", order = 0)
    val loadingSlot: BossBarSlot = this.uiService.bossBars.register("loading", order = 1)

    fun clearPartyBar(player: Player) {
        this.uiService.clear(player, this.partySlot)
    }

    fun clearPartyBar(players: List<Player>) {
        this.uiService.clear(players, this.partySlot)
    }

    fun partyBar(player: Player, party: Party) {
        this.uiService.set(
            player,
            this.partySlot,
            PartyBossBar.create(party),
        )
    }

    fun partyBar(players: List<Player>, party: Party) {
        this.uiService.set(
            players,
            this.partySlot,
            PartyBossBar.create(party),
        )
    }

    fun clearLoadingBar(players: List<Player>) {
        this.uiService.clear(players, loadingSlot)
    }

    fun loadingBar(players: List<Player>, chunk: String, flavor: String, maxPlayers: Int) {
        uiService.set(
            players,
            loadingSlot,
            LoadingBossBar.create(
                chunk,
                flavor,
                LoadingBossBar.LoadingState.STARTING,
                players.count(),
                maxPlayers
            ),
        )
    }
}