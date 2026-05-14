package space.chunks.lobby.ui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import space.chunks.lobby.modules.party.Party
import space.chunks.lobby.ui.visual.PartyBossBarVisual

object PartyBossBar {
    private val miniMessage = MiniMessage.miniMessage()

    fun component(p: Party): Component {
        return this.miniMessage.deserialize(
            PartyBossBarVisual.party(
                ownerName = p.owner.name,
                memberNames = p.members.map { it.name },
            ).asMiniMessage()
        )
    }
}
