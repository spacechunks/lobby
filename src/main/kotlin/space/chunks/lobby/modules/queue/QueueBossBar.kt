package space.chunks.lobby.modules.queue

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import space.chunks.visual.VisualKit
import space.chunks.visual.layout.VisualAlignment
import space.chunks.visual.layout.VisualBox
import space.chunks.visual.layout.VisualComponent
import space.chunks.visual.text.VisualFonts
import space.chunks.visual.text.VisualText

object QueueBossBar {
    private val miniMessage = MiniMessage.miniMessage()
    private const val paddingLeft = 6
    private const val paddingRight = 6
    private const val countGap = 6
    fun component(chunk: String, flavor: String, queueState: QueueState, players: Int, maxPlayers: Int): Component =
        miniMessage.deserialize(visual(chunk, flavor, queueState, players, maxPlayers).asMiniMessage())

    private fun visual(chunk: String, flavor: String, queueState: QueueState, players: Int, maxPlayers: Int): VisualText {
        val label = VisualFonts.SpaceChunksVisualKit.bossBarSmallLine1
            .formattedComponent("<#dde8f6>QUEUE FOR <#D8E7FF>$chunk ($flavor)")

        val count = VisualFonts.SpaceChunksVisualKit.bossBarLine1Half
            .formattedComponent("<#dde8f6>$players/$maxPlayers")

        val action = VisualFonts.SpaceChunksVisualKit.bossBarLine2
            .formattedComponent("<#009cff>${queueState.string}.")

        val text2 = VisualBox().place(0, label)
            .place(0, action)
            .placeEnd(200, count).toComponent()

        val text = VisualComponent.row(
            gap = countGap,
            label,
            count,
            action,
        )

        return VisualText.of(
            VisualComponent.panel(
                background = VisualKit.BossBar.Translucent28.stretchBackground,
                content = text2,
                minWidth = VisualKit.BossBar.Translucent28.threePartBackground.width,
                paddingLeft = paddingLeft,
                paddingRight = paddingRight,
                align = VisualAlignment.START,
            )
        )
    }

    enum class QueueState(val string: String) {
        WAITING_FOR_PLAYERS("Waiting for players"),
        COUNTDONW("in 10s"),
        STARTING("Starting"),
    }
}
