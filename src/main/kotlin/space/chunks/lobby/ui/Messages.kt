package space.chunks.lobby.ui

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class Messages(plugin: Plugin) {
    private val config = plugin.getResource("messages.yml")!!.use { input ->
        YamlConfiguration.loadConfiguration(InputStreamReader(input, StandardCharsets.UTF_8))
    }

    private val miniMessage: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.resolver(
                TagResolver.standard(),
                yamlTagResolver(),
                playerResolver(),
            )
        )
        .build()

    fun component(key: String, placeholders: Map<String, String> = emptyMap()): Component =
        this.miniMessage.deserialize(
            this.config.getString(key) ?: error("Missing message key: $key"),
            placeholderResolver(placeholders),
        )

    fun lines(key: String, placeholders: Map<String, String> = emptyMap()): List<Component> =
        this.config.getStringList(key).map { line ->
            this.miniMessage.deserialize(line, placeholderResolver(placeholders))
        }

    fun send(audience: Audience, key: String, placeholders: Map<String, String> = emptyMap()) {
        this.lines(key, placeholders).forEach(audience::sendMessage)
    }

    fun player(name: String, color: String = "#ff008a"): String = "<player:$name:$color>"

    private fun placeholderResolver(placeholders: Map<String, String>): TagResolver =
        placeholders.entries
            .map { Placeholder.parsed(it.key.toMiniMessageTagName(), it.value) }
            .let(TagResolver::resolver)

    private fun yamlTagResolver(): TagResolver {
        val section = this.config.getConfigurationSection("tags") ?: return TagResolver.empty()
        val builder = TagResolver.builder().resolver(TagResolver.standard())

        section.getKeys(false).forEach { key ->
            section.getString(key)?.let { builder.resolver(Placeholder.parsed(key.toMiniMessageTagName(), it)) }
        }

        return builder.build()
    }

    private fun String.toMiniMessageTagName(): String =
        this.replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").lowercase()

    private fun playerResolver(): TagResolver = TagResolver.resolver("player") { args, _ ->
        val name = args.popOr("A player name is required").value()
        val color = if (args.hasNext()) args.pop().value() else "#ff008a"
        Tag.preProcessParsed("<white><head:$name:true></white> <color:$color>$name</color>")
    }
}
