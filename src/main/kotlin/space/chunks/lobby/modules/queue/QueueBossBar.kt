package space.chunks.lobby.modules.queue

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import space.chunks.visual.VisualKit
import space.chunks.visual.layout.VisualLayer
import space.chunks.visual.layout.VisualPadding
import space.chunks.visual.layout.VisualRow
import space.chunks.visual.layout.VisualView
import space.chunks.visual.text.VisualFonts
import space.chunks.visual.text.VisualText
import space.chunks.visual.ui.UiRenderable
import space.chunks.visual.ui.animation.LoadingFrames

object QueueBossBar {
    private val miniMessage = MiniMessage.miniMessage()
    private const val minimumContentWidth = 200
    private const val columnGap = 8
    private val panelPadding = VisualPadding.horizontal(6)

    fun component(
        chunk: String,
        flavor: String,
        queueState: QueueState,
        players: Int,
        maxPlayers: Int,
        loadingFrame: Int? = null,
    ): Component =
        miniMessage.deserialize(visual(chunk, flavor, queueState, players, maxPlayers, loadingFrame).asMiniMessage())

    fun waitingForPlayers(chunk: String, flavor: String, players: Int, maxPlayers: Int): UiRenderable =
        UiRenderable { context ->
            component(
                chunk = chunk,
                flavor = flavor,
                queueState = QueueState.WAITING_FOR_PLAYERS,
                players = players,
                maxPlayers = maxPlayers,
                loadingFrame = LoadingFrames.frameIndex(
                    context.tick,
                    VisualKit.BossBar.LoadingSpinner.frameCount,
                    ticksPerFrame = 2,
                ),
            )
        }

    private fun visual(
        chunk: String,
        flavor: String,
        queueState: QueueState,
        players: Int,
        maxPlayers: Int,
        loadingFrame: Int?,
    ): VisualText {
        val label = VisualFonts.SpaceChunksVisualKit.bossBarSmallLine1
            .formattedComponent("<#dde8f6>QUEUE FOR <#D8E7FF>${escapeMiniMessage(chunk)} (${escapeMiniMessage(flavor)})")

        val count = VisualFonts.SpaceChunksVisualKit.bossBarLine1Half
            .formattedComponent("<#dde8f6>$players/$maxPlayers")

        val action = VisualFonts.SpaceChunksVisualKit.bossBarLine2
            .formattedComponent("<#009cff>${queueState.string}")

        val loadingIndicator = loadingFrame?.let(VisualKit.BossBar.LoadingSpinner::frame)
        val textColumn = VisualLayer(width = maxOf(label.width, action.width))
            .child(0, label)
            .child(0, action)
            .toComponent()

        val content = VisualRow(minWidth = minimumContentWidth, gap = columnGap)
            .edgeStart(loadingIndicator)
            .center(textColumn)
            .edgeEnd(count)
            .toComponent()

        return VisualView(
            background = VisualKit.BossBar.Translucent28.stretchBackground,
            content = content,
            padding = panelPadding,
        ).toText()
    }

    private fun escapeMiniMessage(content: String): String =
        content
            .replace("\\", "\\\\")
            .replace("<", "\\<")

    enum class QueueState(val string: String) {
        WAITING_FOR_PLAYERS("Waiting for players"),
        COUNTDONW("in 10s"),
        STARTING("Starting"),
    }
}
