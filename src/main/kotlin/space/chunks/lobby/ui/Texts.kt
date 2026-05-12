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

class Texts(plugin: Plugin) {
    private val store = TextContentStore(plugin)

    private val miniMessage: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.resolver(
                TagResolver.standard(),
                this.store.tagResolver("tags"),
                playerResolver(),
            )
        )
        .build()

    fun component(key: String, placeholders: Map<String, Any> = emptyMap()): Component =
        parse(this.store.requireString(key), placeholders)

    fun raw(key: String, placeholders: Map<String, Any> = emptyMap()): String =
        applyPlaceholders(this.store.requireString(key), placeholders)

    fun stringOrNull(key: String): String? = this.store.stringOrNull(key)

    fun sectionKeys(path: String): List<String> = this.store.sectionKeys(path)

    fun lines(key: String, placeholders: Map<String, Any> = emptyMap()): List<Component> =
        this.store.requireStringList(key).map { line ->
            parse(line, placeholders)
        }

    fun send(audience: Audience, key: String, placeholders: Map<String, Any> = emptyMap()) {
        this.lines(key, placeholders).forEach(audience::sendMessage)
    }

    fun parse(content: String, placeholders: Map<String, Any> = emptyMap()): Component =
        this.miniMessage.deserialize(content, placeholderResolver(placeholders))

    fun player(name: String, color: String = "#ff008a"): String = "<player:$name:$color>"

    private fun placeholderResolver(placeholders: Map<String, Any>): TagResolver =
        placeholders.entries
            .map { (key, value) ->
                when (value) {
                    is Component -> Placeholder.component(key.toMiniMessageTagName(), value)
                    else -> Placeholder.parsed(key.toMiniMessageTagName(), value.toString())
                }
            }
            .let(TagResolver::resolver)

    private fun applyPlaceholders(content: String, placeholders: Map<String, Any>): String =
        placeholders.entries.fold(content) { acc, (key, value) ->
            require(value !is Component) {
                "Component placeholders are not supported for raw text rendering: $key"
            }

            acc.replace("<${key.toMiniMessageTagName()}>", value.toString())
        }

    private fun String.toMiniMessageTagName(): String =
        this.replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").lowercase()

    private fun playerResolver(): TagResolver = TagResolver.resolver("player") { args, _ ->
        val name = args.popOr("A player name is required").value()
        val color = if (args.hasNext()) args.pop().value() else "#ff008a"
        Tag.preProcessParsed("<white><head:$name:true></white> <color:$color>$name</color>")
    }
}

private class TextContentStore(plugin: Plugin) {
    private val config = YamlConfiguration()

    init {
        val contentConfig = loadYaml(plugin, "content/config.yml")
        merge(contentConfig)

        val sources = contentConfig.getStringList("sources")

        require(sources.isNotEmpty()) {
            "No text content sources configured in content/config.yml"
        }

        sources.forEach { source ->
            merge(loadYaml(plugin, source))
        }
    }

    fun requireString(key: String): String =
        this.config.getString(key) ?: error("Missing text key: $key")

    fun stringOrNull(key: String): String? =
        this.config.getString(key)

    fun requireStringList(key: String): List<String> {
        val lines = this.config.getStringList(key)
        if (lines.isNotEmpty()) {
            return lines
        }

        error("Missing text list key: $key")
    }

    fun sectionKeys(path: String): List<String> =
        this.config.getConfigurationSection(path)?.getKeys(false)?.toList() ?: emptyList()

    fun tagResolver(path: String): TagResolver {
        val section = this.config.getConfigurationSection(path) ?: return TagResolver.empty()
        val builder = TagResolver.builder().resolver(TagResolver.standard())

        section.getKeys(false).forEach { key ->
            val value = section.getString(key) ?: return@forEach
            builder.resolver(Placeholder.parsed(key.toMiniMessageTagName(), value))
        }

        return builder.build()
    }

    private fun merge(source: YamlConfiguration) {
        source.getKeys(true).forEach { key ->
            if (source.isConfigurationSection(key)) {
                return@forEach
            }

            if (this.config.contains(key)) {
                error("Duplicate text key defined more than once: $key")
            }

            this.config.set(key, source.get(key))
        }
    }

    private fun loadYaml(plugin: Plugin, path: String): YamlConfiguration {
        val resource = plugin.getResource(path) ?: error("Missing bundled text resource: $path")
        resource.use { input ->
            return YamlConfiguration.loadConfiguration(
                InputStreamReader(input, StandardCharsets.UTF_8)
            )
        }
    }

    private fun String.toMiniMessageTagName(): String =
        this.replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").lowercase()
}
