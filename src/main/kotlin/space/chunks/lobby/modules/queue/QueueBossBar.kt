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
    private const val contentWidth = 200
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

        val content = VisualRow(width = contentWidth, gap = columnGap)
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

    fun debugLines(chunk: String, flavor: String, players: Int, maxPlayers: Int): List<String> {
        val label = VisualFonts.SpaceChunksVisualKit.bossBarSmallLine1
            .formattedComponent("<#dde8f6>QUEUE FOR <#D8E7FF>${escapeMiniMessage(chunk)} (${escapeMiniMessage(flavor)})")
        val count = VisualFonts.SpaceChunksVisualKit.bossBarLine1Half
            .formattedComponent("<#dde8f6>$players/$maxPlayers")
        val action = VisualFonts.SpaceChunksVisualKit.bossBarLine2
            .formattedComponent("<#009cff>${QueueState.WAITING_FOR_PLAYERS.string}")
        val loading = VisualKit.BossBar.LoadingSpinner.frame(0)
        val textWidth = maxOf(label.width, action.width)
        val edgeWidth = maxOf(loading.advance, count.advance)
        val fillWidth = contentWidth - edgeWidth - edgeWidth - columnGap - columnGap
        val textCellStart = edgeWidth + columnGap
        val textStart = textCellStart + ((fillWidth - textWidth).coerceAtLeast(0) / 2)
        val countCellStart = contentWidth - edgeWidth
        val countStart = countCellStart + edgeWidth - count.width

        return listOf(
            "view width=${panelPadding.left + contentWidth + panelPadding.right}, padding=${panelPadding.left}/${panelPadding.right}, content=0..$contentWidth",
            "loading width=${loading.width}, advance=${loading.advance}, visual=0..${loading.width}, edgeCell=0..$edgeWidth",
            "label width=${label.width}, advance=${label.advance}; action width=${action.width}, advance=${action.advance}",
            "text fill=$textCellStart..${textCellStart + fillWidth}, text visual=$textStart..${textStart + textWidth}",
            "count width=${count.width}, advance=${count.advance}, edgeCell=$countCellStart..$contentWidth, visual=$countStart..${countStart + count.width}",
            "left visual padding=0, right visual padding=${contentWidth - (countStart + count.width)}",
        )
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
