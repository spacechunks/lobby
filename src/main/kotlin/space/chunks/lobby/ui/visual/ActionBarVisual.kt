package space.chunks.lobby.ui.visual

import space.chunks.visual.layout.VisualBox
import space.chunks.visual.layout.VisualComponent
import space.chunks.visual.VisualKit
import space.chunks.visual.text.VisualText

object ActionBarVisual {
    fun bar(
        health: Int,
        gravity: Int,
        voiceChatEnabled: Boolean,
        chatChannel: ActionBarGlyphs.ChatChannel,
    ): VisualText {
        var actionBar = VisualBox()
            .place(x = Position.hotbar, hotbar(health, gravity, chatChannel))

        if (!voiceChatEnabled) {
            actionBar = actionBar.place(x = Position.voiceChatDisabled, ActionBarGlyphs.voiceChatDisabled)
        }

        return actionBar.render()
    }

    private fun hotbar(
        health: Int,
        gravity: Int,
        chatChannel: ActionBarGlyphs.ChatChannel,
    ): VisualComponent =
        VisualComponent.of(
            VisualKit.Hud.Hotbar.width,
            VisualBox(width = VisualKit.Hud.Hotbar.width)
                .place(x = 0, ActionBarGlyphs.health(health))
                .placeEnd(ActionBarGlyphs.gravity(gravity))
                .place(x = VisualKit.Hud.Hotbar.width, chatChannel.component)
                .render()
        )

    private object Position {
        const val hotbar = VisualKit.Hud.Hotbar.start
        const val voiceChatDisabled = hotbar - 35
    }
}
