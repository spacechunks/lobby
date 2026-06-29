package space.chunks.lobby.extensions

import net.kyori.adventure.audience.Audience
import org.bukkit.entity.Player

fun List<Player>.toAudience(): Audience = Audience.audience(map { it as Audience })