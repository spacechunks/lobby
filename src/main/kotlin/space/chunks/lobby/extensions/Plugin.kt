package space.chunks.lobby.extensions

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.plugin.Plugin

fun Plugin.callSyncEvent(event: Event) {
    Bukkit.getScheduler().callSyncMethod(this, {
        Bukkit.getPluginManager().callEvent(event)
    })
}