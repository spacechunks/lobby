package space.chunks.lobby.ui

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import space.chunks.lobby.ui.util.Space

object ActionBar {
    private val miniMessage = MiniMessage.miniMessage()

    enum class ChatChannel(val unicode: String) {
        GLOBAL("\uE140"),
        TEAM("\uE141"),
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

        val content = StringBuilder()

        // Left padding to align the actionbar group.
        content.append(Space.asString(64, true))

        // Health icon, one dedicated unicode per value from 0 to 20.
        content.append(healthUnicode(health))
        content.append(Space.asString(25, true))

        // Gravity icon, one dedicated unicode per value from 0 to 20.
        content.append(gravityUnicode(gravity))
        content.append(Space.asString(2, true))

        // Active chat channel icon.
        content.append(chatChannelUnicode(chatChannel))

        // Voice chat is only shown when it is disabled.
        if (!voiceChatEnabled) {
            content.append(Space.asString(-62, true))
            content.append(voiceChatDisabledUnicode())
        }

        player.sendActionBar(miniMessage.deserialize(content.toString()))
    }

    private fun healthUnicode(health: Int): String {
        return actionBarIcon(0xE100 + health)
    }

    private fun gravityUnicode(gravity: Int): String {
        return actionBarIcon(0xE120 + gravity)
    }

    private fun chatChannelUnicode(chatChannel: ChatChannel): String {
        return actionBarIcon(chatChannel.unicode)
    }

    private fun voiceChatDisabledUnicode(): String {
        return actionBarIcon(0xE150)
    }

    private fun actionBarIcon(codePoint: Int): String {
        return actionBarIcon(String(Character.toChars(codePoint)))
    }

    private fun actionBarIcon(unicode: String): String {
        return "<!shadow><font:chunkexplorer:actionbar>$unicode</font>"
    }
}
