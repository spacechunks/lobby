package space.chunks.lobby.player

import org.bukkit.NamespacedKey

object PlayerMetadataKeys {
    val MM_SEARCH_ONGOING = NamespacedKey.fromString("spacechunks:explorer/lobby/playerdata/mm_search_ongoing")!!
    val MM_PRIVATE_ONGOING = NamespacedKey.fromString("spacechunks:explorer/lobby/playerdata/mm_private_ongoing")!!
}