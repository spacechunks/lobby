package space.chunks.lobby.modules.queue

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import space.chunks.visual.VisualKit
import space.chunks.visual.layout.VisualAlignment
import space.chunks.visual.layout.VisualComponent
import space.chunks.visual.text.VisualFonts
import space.chunks.visual.text.VisualText

object QueueBossBar {
    private val miniMessage = MiniMessage.miniMessage()
    private const val paddingLeft = 6
    private const val paddingRight = 6
    private const val countGap = 6
    fun component(name: String, players: Int, maxPlayers: Int): Component =
        miniMessage.deserialize(visual(name, players, maxPlayers).asMiniMessage())

    private fun visual(name: String, players: Int, maxPlayers: Int): VisualText {
        val label = VisualFonts.SpaceChunksVisualKit.bossBarSmallLine1
            .formattedComponent("<white>QUEUE <#D8E7FF>$name")
        val count = VisualFonts.SpaceChunksVisualKit.bossBarLine1
            .formattedComponent("<#FFFFFF>$players<#9DB4D8>/$maxPlayers")
        val text = VisualComponent.row(
            gap = countGap,
            label,
            count,
        )

        return VisualText.of(
            VisualComponent.panel(
                background = VisualKit.BossBar.Translucent15.stretchBackground,
                content = text,
                minWidth = VisualKit.BossBar.Translucent15.threePartBackground.width,
                paddingLeft = paddingLeft,
                paddingRight = paddingRight,
                align = VisualAlignment.START,
            )
        )
    }
}
