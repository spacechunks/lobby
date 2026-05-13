package space.chunks.lobby.ui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import space.chunks.lobby.modules.party.Party
import space.chunks.lobby.modules.party.PartyPlayer
import space.chunks.lobby.ui.util.Space

object PartyBossBar {
    private val miniMessage = MiniMessage.miniMessage()

    fun buildString(p: Party): Component {
        val content = StringBuilder()

        val padding = "<!shadow><font:spacechunks-visualkit:bossbar/bossbar-fix>\uE131" + Space.asString(1, true) + "</font>"

        content.append(padding)
        content.append(Space.asString(-1, true))
        content.append(this.head(p.owner, true))

        p.members.forEach {
            content.append(Space.asString(-1, true))
            content.append(this.head(it, false))
        }

        content.append(Space.asString(-1, true))
        content.append(padding)

        return this.miniMessage.deserialize(content.toString())
    }

    private fun head(player: PartyPlayer, leader: Boolean): String {
        val background = "<!shadow><font:spacechunks-visualkit:bossbar/bossbar-fix>\uE133" + Space.asString(1, true) +  "<font:spacechunks-visualkit:bossbar/bossbar-fix>\uE132" + Space.asString(1, true) + "<font:spacechunks-visualkit:bossbar/bossbar-fix>\uE131" + Space.asString(1, true) + "</font>"
        val head = "<!shadow><color:#FF1234><head:${player.name}:true></color>"
        val statusOnline = "<!shadow><font:chunkexplorer:bossbar/party>\uE110</font>"
        val statusPending = "<!shadow><font:chunkexplorer:bossbar/party>\uE111</font>"
        val statusAFK = "<!shadow><font:chunkexplorer:bossbar/party>\uE112</font>"
        val frame = "<!shadow><font:chunkexplorer:bossbar/party>\uE120</font>"

        val content = StringBuilder()
        content.append(background)
        content.append(Space.asString(-12, true))

        if (leader) {
            content.append(Space.asString(-1, true))
            content.append(frame)
            content.append(Space.asString(-10, true))
        }

        content.append(head)
        content.append(Space.asString(-3, true))
        content.append(statusOnline) // TODO: based on actual status

        return content.toString()
    }
}