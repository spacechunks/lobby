package space.chunks.explorer.lobby.display

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Transformation
import org.joml.Vector3f

class ChunkSelectView(
    plugin: Plugin,
    center: Location,
    session: DisplaySession,
    val grid: DisplayGrid
) : View(plugin, center, session) {
    private val arrowUpLoc = center.clone().add(8.0, -1.55, 0.0)
    private val arrowDownLoc = this.arrowUpLoc.clone().subtract(0.0, .6, 0.0)
    private val middleArrowLoc = center.clone().add(8.0, -1.7, 0.0)

    private var arrowDown: ItemDisplay? = null
    private var arrowUp: ItemDisplay? = null

    override fun render() {
        center.world.spawn(center.clone().add(0.0, 3.5, 0.0), ItemDisplay::class.java) { d ->
            val stack = ItemStack(Material.PAPER)
            stack.editMeta { m ->
                m.itemModel = NamespacedKey.fromString("spacechunks:explorer/chunk_select/logo")
            }

            this.elements.add(d)

            d.setItemStack(stack)

            d.billboard = Display.Billboard.CENTER

            d.transformation = Transformation(
                d.transformation.translation,
                d.transformation.leftRotation,
                Vector3f(7f, 3.5f, 1f),
                d.transformation.rightRotation
            )
        }

        this.spawnItemDisplay(
            center.clone().add(-3.5, 3.5, 0.0),
            Vector3f(1f, 1f, 1f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone1"),
            true,
        )

        this.spawnItemDisplay(
            center.clone().add(-3.0, 5.0, 0.0),
            Vector3f(.6f, .6f, .6f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone3"),
            true,
        )


        this.spawnItemDisplay(
            center.clone().add(3.6, 4.5, 0.0),
            Vector3f(1f, 1f, 1f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone2"),
            true,
        )

        this.spawnItemDisplay(
            center.clone().add(3.5, 2.5, 0.0),
            Vector3f(.8f, .8f, .8f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone4"),
            true,
        )

        this.spawnArrowUp(this.arrowUpLoc)
        this.spawnArrowDown(this.arrowDownLoc)

        val gameItems = mutableListOf<ChunkDisplay>()
        for (i in 0..7) {
            gameItems.add(ChunkDisplay(
                title = Component.text("Game ${i}"),
                thumbnailTextureKey = NamespacedKey.fromString("spacechunks:explorer/chunk_select/chunk_thumbnail")!!
            ))
        }

        for (i in 0..7) {
            gameItems.add(ChunkDisplay(
                title = Component.text("Game ${i}"),
                thumbnailTextureKey = NamespacedKey.fromString("spacechunks:explorer/chunk_select/chunk_thumbnail2")!!,
            ))
        }

        for (i in 0..7) {
            gameItems.add(ChunkDisplay(
                title = Component.text("Game ${i}"),
                thumbnailTextureKey = NamespacedKey.fromString("spacechunks:explorer/chunk_select/chunk_thumbnail3")!!,
            ))
        }

        this.grid.setAllItems(gameItems)
        this.renderArrows()
        this.grid.setInitialFocus()
    }

    override fun close() {
        this.elements.forEach { it.remove() }
        this.grid.clear()
    }

    override fun handleInput(player: Player, input: Input) {
        if (this.grid.getFocusedIndex() == -1) {
            this.grid.setInitialFocus()
        }

        var hasNext = false

        when (input) {
            Input.W -> {
                hasNext = this.grid.moveFocusUp()
                this.renderArrows()
            }

            Input.A -> {
                hasNext = this.grid.moveFocusLeft()
            }
            Input.S -> {
                hasNext = this.grid.moveFocusDown()
                this.renderArrows()
            }

            Input.D -> {
                hasNext = this.grid.moveFocusRight()
            }
            Input.SPACE -> {
                val m = PaginatedList(
                    listOf(
                        "Flavor ABC",
                        "abcdefghijii",
                        "abcdefghijklmnoprst",
                        "abcde",
                        "abcdefghijklmno",
                        "abcdefghijklmnopqrstuvwxy",
                        "abcdefghijklmnopqrstuvwxy",
                    ),
                    5
                )
                player.playSound(player.location, "spacechunks.explorer.chunk_select.click", 0.5f, 1f)
                this.session.switchWindow(FlavorSelectView(this.plugin, this.center, this.session, m))
            }
            Input.SNEAK -> {
                player.playSound(player.location, "spacechunks.explorer.chunk_select.click_err", 0.5f, 1f)
            }
        }

        if (!hasNext) {
            player.playSound(player.location, "spacechunks.explorer.chunk_select.click_err", 0.5f, 1f)
            return
        }

        player.playSound(player.location, "spacechunks.explorer.chunk_select.click", 0.5f, 1f)
    }

    private fun renderArrows() {
        // there is only one page, so don't display any arrows
        if (this.grid.getTotalPages() == 1) {
            this.arrowUp?.remove()
            this.arrowDown?.remove()
            this.arrowUp = null
            this.arrowDown = null
            return
        }

        // we are on the first page and there are multiple pages ahead,
        // so only display the arrow down
        if (this.grid.getCurrentPage() == 1 && this.grid.getTotalPages() >= 1) {
            this.arrowUp?.remove()
            this.arrowUp = null
            this.spawnArrowDown(this.middleArrowLoc)
            return
        }

        // we are on the last page, so only display arrow up
        if (this.grid.getCurrentPage() == this.grid.getTotalPages()) {
            this.arrowDown?.remove()
            this.arrowDown = null
            this.spawnArrowUp(this.middleArrowLoc)
            return
        }

        // we are in the middle pages, meaning we can navigate
        // up or down, so display both arrows.

        this.spawnArrowUp(this.arrowUpLoc)
        this.spawnArrowDown(this.arrowDownLoc)
    }

    private fun spawnArrowUp(loc: Location) {
        if (this.arrowUp == null) {
            this.arrowUp = this.spawnItemDisplay(
                loc,
                Vector3f(.8f, .8f, 0.5f),
                NamespacedKey.fromString("spacechunks:explorer/chunk_select/arrow_up"),
                false,
            )
            return
        }

        this.arrowUp?.teleport(loc)
    }

    private fun spawnArrowDown(loc: Location) {
        if (this.arrowDown == null) {
            this.arrowDown = this.spawnItemDisplay(
                loc,
                Vector3f(.8f, .8f, 0.5f),
                NamespacedKey.fromString("spacechunks:explorer/chunk_select/arrow_down"),
                false,
            )
            return
        }

        this.arrowDown?.teleport(loc)
    }
}