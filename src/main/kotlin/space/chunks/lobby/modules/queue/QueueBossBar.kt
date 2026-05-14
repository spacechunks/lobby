package space.chunks.lobby.modules.queue

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import space.chunks.visual.text.VisualFonts
import space.chunks.visual.text.VisualText

object QueueBossBar {
    private val miniMessage = MiniMessage.miniMessage()

    fun component(name: String, players: Int, maxPlayers: Int): Component =
        miniMessage.deserialize(visual(name, players, maxPlayers).asMiniMessage())

    private fun visual(name: String, players: Int, maxPlayers: Int): VisualText =
        VisualFonts.ChunkExplorer.text.text(
            "Queue: $name  $players/$maxPlayers",
        )
}
