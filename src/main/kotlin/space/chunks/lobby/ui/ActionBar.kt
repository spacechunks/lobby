package space.chunks.lobby.ui

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import space.chunks.lobby.ui.visual.ActionBarGlyphs
import space.chunks.lobby.ui.visual.ActionBarVisual
import kotlin.math.roundToInt

object ActionBar {
    private val miniMessage = MiniMessage.miniMessage()

    enum class ChatChannel(val visual: ActionBarGlyphs.ChatChannel) {
        GLOBAL(ActionBarGlyphs.ChatChannel.GLOBAL),
        TEAM(ActionBarGlyphs.ChatChannel.TEAM),
    }

    fun send(player: Player) {
        this.send(
            player = player,
            health = player.health.roundToInt().coerceIn(0, 20),
            gravity = if (player.hasGravity()) 20 else 0,
            voiceChatEnabled = true,
            chatChannel = ChatChannel.GLOBAL,
        )
    }

    fun send(
        player: Player,
        health: Int,
        gravity: Int,
        voiceChatEnabled: Boolean,
        chatChannel: ChatChannel,
    ) {
        require(health in 0..20) { "health must be between 0 and 20." }
        require(gravity in 0..20) { "gravity must be between 0 and 20." }

        val content = ActionBarVisual.bar(
            health = health,
            gravity = gravity,
            voiceChatEnabled = voiceChatEnabled,
            chatChannel = chatChannel.visual,
        )

        player.sendActionBar(miniMessage.deserialize(content.asMiniMessage()))
    }
}
