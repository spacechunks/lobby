package space.chunks.lobby.modules.chunkviewer.display

import chunks.space.api.explorer.chunk.v1alpha1.Types
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.plugin.Plugin
import org.joml.Vector3f
import space.chunks.lobby.modules.chunkviewer.event.PlayerSelectFlavorEvent
import space.chunks.lobby.pack.Fonts
import space.chunks.lobby.pack.Sounds
import space.chunks.lobby.pack.Textures
import space.chunks.lobby.ui.Texts

class ModeSelectView(
    plugin: Plugin,
    center: Location,
    session: DisplaySession,
    private val chunk: Types.Chunk,
    private val flavor: Types.Flavor,
    private val textsContent: Texts,
): View(plugin, center, session) {
    private val leftPos = this.center.clone().add(7.8, -2.55, 0.0)
    private val rightPos = this.center.clone().add(-1.75, -2.55, 0.0)
    private val selectionMarker = this.spawnItemDisplay(
        leftPos,
        Vector3f(0.5f, 0.5f, .5f),
        Textures.ARROW_RIGHT,
        false,
    )

    private lateinit var publicMatch: TextDisplay
    private lateinit var privateMatch: TextDisplay

    override fun render() {
        this.spawnItemDisplay(
            ChunkViewerLayout.logoLocation(this.center),
            ChunkViewerLayout.LOGO_SCALE,
            Textures.LOGO_WIDE,
            false,
        )

        this.spawnTextElement(
            this.textsContent.component("chunkviewer.mode-select.title") .font(Fonts.CHUNK_VIEWER),
            this.center.clone().add(0.0, -1.0, 0.0), 3.5f,
        )

        // left
        this.publicMatch = this.spawnTextElement(
            this.textsContent
                .component("chunkviewer.mode-select.matchmaking")
                .color(TextColor.fromHexString("#52cefd"))
                .font(Fonts.CHUNK_VIEWER),
            this.center.clone().add(5.0, -3.0, 0.0), 3.5f,
        )

        // right
        this.privateMatch = this.spawnTextElement(
            this.textsContent
                .component("chunkviewer.mode-select.private")
                .font(Fonts.CHUNK_VIEWER),
            this.center.clone().add(-5.0, -3.0, 0.0), 3.5f,
        )

        this.spawnItemDisplay(
            center.clone().add(-3.5, 3.5, 0.0),
            Vector3f(1f, 1f, 1f),
            Textures.STONE_1,
            true,
        )

        this.spawnItemDisplay(
            center.clone().add(-3.0, 5.0, 0.0),
            Vector3f(.6f, .6f, .6f),
            Textures.STONE_3,
            true,
        )

        this.spawnItemDisplay(
            center.clone().add(3.6, 4.5, 0.0),
            Vector3f(1f, 1f, 1f),
            Textures.STONE_2,
            true,
        )

        this.spawnItemDisplay(
            center.clone().add(3.5, 2.5, 0.0),
            Vector3f(.8f, .8f, .8f),
            Textures.STONE_4,
            true,
        )
    }

    override fun close() {
        this.closeElements()
    }

    override fun handleInput(
        player: Player,
        input: Input
    ) {
        if (input == Input.A) {
            if (this.selectionMarker.location == leftPos) {
                player.playSound(player.location, Sounds.CLICK_ERR, 0.5f, 1f)
                return
            }

            this.publicMatch.text(this.publicMatch.text().color(TextColor.fromHexString("#52cefd")))
            this.privateMatch.text(this.privateMatch.text().color(NamedTextColor.WHITE))

            this.selectionMarker.teleport(leftPos)

            player.playSound(player.location, Sounds.CLICK, 0.5f, 1f)
            return
        }

        if (input == Input.D) {
            if (this.selectionMarker.location == rightPos) {
                player.playSound(player.location, Sounds.CLICK_ERR, 0.5f, 1f)
                return
            }

            this.privateMatch.text(this.privateMatch.text().color(TextColor.fromHexString("#52cefd")))
            this.publicMatch.text(this.publicMatch.text().color(NamedTextColor.WHITE))

            this.selectionMarker.teleport(rightPos)

            player.playSound(player.location, Sounds.CLICK, 0.5f, 1f)
            return
        }

        if (input == Input.SPACE) {
            // leftPos is public match
            val mmMode = this.selectionMarker.location == leftPos

            player.playSound(player.location, Sounds.CLICK, 0.5f, 1f)

            Bukkit.getPluginManager().callEvent(
                PlayerSelectFlavorEvent(this.chunk, this.flavor, mmMode, player)
            )
            return
        }

        if (input == Input.SNEAK) {
            player.playSound(player.location, Sounds.CLICK, 0.5f, 1f)
            this.session.switchWindow(
                FlavorSelectView(
                    this.plugin,
                    this.center,
                    this.session,
                    this.chunk,
                    PaginatedList(this.chunk.flavorsList.toList(), 5),
                    this.textsContent
                )
            )
            return
        }

        player.playSound(player.location, Sounds.CLICK_ERR, 0.5f, 1f)
    }
}