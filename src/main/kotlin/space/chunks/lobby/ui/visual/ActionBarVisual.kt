package space.chunks.lobby.ui.visual

import space.chunks.visual.layout.VisualLayer
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
        var actionBar = VisualLayer()
            .child(x = Position.hotbar, hotbar(health, gravity, chatChannel))

        if (!voiceChatEnabled) {
            actionBar = actionBar.child(x = Position.voiceChatDisabled, ActionBarGlyphs.voiceChatDisabled)
        }

        return actionBar.toText()
    }

    private fun hotbar(
        health: Int,
        gravity: Int,
        chatChannel: ActionBarGlyphs.ChatChannel,
    ): VisualComponent =
        VisualComponent(
            VisualKit.Hud.Hotbar.width,
            VisualLayer(width = VisualKit.Hud.Hotbar.width)
                .child(x = 0, ActionBarGlyphs.health(health))
                .childEnd(ActionBarGlyphs.gravity(gravity))
                .child(x = VisualKit.Hud.Hotbar.width, chatChannel.component)
                .toText()
        )

    private object Position {
        const val hotbar = VisualKit.Hud.Hotbar.start
        const val voiceChatDisabled = hotbar - 35
    }
}
