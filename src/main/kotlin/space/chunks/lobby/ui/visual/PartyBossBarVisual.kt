package space.chunks.lobby.ui.visual

import space.chunks.visual.layout.VisualBox
import space.chunks.visual.layout.VisualComponent
import space.chunks.visual.VisualKit
import space.chunks.visual.layout.VisualRow
import space.chunks.visual.text.VisualText

object PartyBossBarVisual {
    private const val maxSlots = 8
    private const val slotGap = -1
    private const val playerSlotWidth = 40
    private const val placeholderSlotWidth = 38
    private const val slotPaddingLeft = 30
    private const val slotContentWidth = 8
    private const val statusGap = -2
    private const val leaderFramePadding = 1
    private const val statusX = 6

    private val barStart = VisualKit.BossBar.Translucent15.start
    private val barEnd = VisualKit.BossBar.Translucent15.end
    private val slotBackground = VisualKit.BossBar.Translucent15.threePartBackground

    fun party(ownerName: String, memberNames: Iterable<String>): VisualText =
        VisualText.of(
            VisualRow(gap = slotGap)
                .item(barStart)
                .items(slots(ownerName, memberNames))
                .item(barEnd)
                .toComponent()
        )

    private fun slots(ownerName: String, memberNames: Iterable<String>): List<VisualComponent> {
        val players = listOf(playerSlot(ownerName, leader = true)) +
            memberNames
                .take(maxSlots - 1)
                .map { playerSlot(it) }

        return players + List(maxSlots - players.size) { placeholderSlot() }
    }

    fun playerSlot(
        playerName: String,
        leader: Boolean = false,
        status: PartyBossBarGlyphs.Status = PartyBossBarGlyphs.Status.ONLINE,
        headColor: String = "#FF1234",
    ): VisualComponent {
        val head = VisualKit.Player.head(playerName, headColor)

        val content =
            if (leader) leaderContent(head, status)
            else memberContent(head, status)

        return VisualComponent.panel(
            background = slotBackground,
            content = content,
            minWidth = playerSlotWidth,
            paddingLeft = slotPaddingLeft,
        )
    }

    fun placeholderSlot(): VisualComponent =
        VisualComponent.panel(
            background = slotBackground,
            content = PartyBossBarGlyphs.emptySlot,
            minWidth = placeholderSlotWidth,
            paddingLeft = slotPaddingLeft,
        )

    private fun memberContent(
        head: VisualComponent,
        status: PartyBossBarGlyphs.Status,
    ): VisualComponent =
        VisualComponent.row(
            gap = statusGap,
            head,
            status.component,
        )

    private fun leaderContent(
        head: VisualComponent,
        status: PartyBossBarGlyphs.Status,
    ): VisualComponent =
        VisualComponent.of(
            slotContentWidth,
            VisualBox(width = slotContentWidth)
                .place(x = 0, PartyBossBarGlyphs.leaderFrame)
                .place(x = leaderFramePadding, head)
                .place(x = statusX, status.component)
                .render()
        )

}
