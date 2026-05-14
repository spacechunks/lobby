package space.chunks.lobby.modules.queue

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import space.chunks.visual.VisualKit
import space.chunks.visual.layout.VisualAlignment
import space.chunks.visual.layout.VisualLayer
import space.chunks.visual.layout.VisualPadding
import space.chunks.visual.layout.VisualView
import space.chunks.visual.text.VisualFonts
import space.chunks.visual.text.VisualText

object QueueBossBar {
    private val miniMessage = MiniMessage.miniMessage()
    private const val contentWidth = 200
    private val panelPadding = VisualPadding.horizontal(6)

    fun component(chunk: String, flavor: String, queueState: QueueState, players: Int, maxPlayers: Int): Component =
        miniMessage.deserialize(visual(chunk, flavor, queueState, players, maxPlayers).asMiniMessage())

    private fun visual(chunk: String, flavor: String, queueState: QueueState, players: Int, maxPlayers: Int): VisualText {
        val label = VisualFonts.SpaceChunksVisualKit.bossBarSmallLine1
            .formattedComponent("<#dde8f6>QUEUE FOR <#D8E7FF>$chunk ($flavor)")

        val count = VisualFonts.SpaceChunksVisualKit.bossBarLine1Half
            .formattedComponent("<#dde8f6>$players/$maxPlayers")

        val action = VisualFonts.SpaceChunksVisualKit.bossBarLine2
            .formattedComponent("<#009cff>${queueState.string}.")

        val content = VisualLayer(width = contentWidth)
            .child(0, label)
            .child(0, action)
            .childEnd(count)
            .toComponent()

        return VisualView(
            background = VisualKit.BossBar.Translucent28.stretchBackground,
            content = content,
            padding = panelPadding,
            align = VisualAlignment.START,
        ).toText()
    }

    enum class QueueState(val string: String) {
        WAITING_FOR_PLAYERS("Waiting for players"),
        COUNTDONW("in 10s"),
        STARTING("Starting"),
    }
}
