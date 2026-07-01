package space.chunks.lobby.extensions

import net.kyori.adventure.audience.Audience
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

// PLAYER LIST

fun List<Player>.toAudience(): Audience = Audience.audience(map { it as Audience })

// PLAYER

fun Player.getBool(key: NamespacedKey, default: Boolean): Boolean {
    return this.persistentDataContainer.getOrDefault(key, PersistentDataType.BOOLEAN, default)
}

fun Player.setBool(key: NamespacedKey, value: Boolean) {
    this.persistentDataContainer.set(key, PersistentDataType.BOOLEAN, value)
}

fun Player.removeMetadata(key: NamespacedKey) {
    this.persistentDataContainer.remove(key)
}