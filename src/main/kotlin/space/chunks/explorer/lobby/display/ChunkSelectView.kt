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
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

class ChunkSelectView(
    plugin: Plugin,
    center: Location,
    session: DisplaySession,
    val grid: DisplayGrid
) : View(plugin, center, session) {
    private val elements = mutableListOf<ItemDisplay>()

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

        spawnUiElement(
            center.clone().add(-3.5, 3.5, 0.0),
            Vector3f(1f, 1f, 1f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone1"),
            true,
        )

        spawnUiElement(
            center.clone().add(-3.0, 5.0, 0.0),
            Vector3f(.6f, .6f, .6f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone3"),
            true,
        )


        spawnUiElement(
            center.clone().add(3.6, 4.5, 0.0),
            Vector3f(1f, 1f, 1f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone2"),
            true,
        )

        val lol = spawnUiElement(
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

        if (input == Input.W || input == Input.A || input == Input.S || input == Input.D || input == Input.SPACE) {
            player.playSound(player.location, "spacechunks.explorer.chunk_select.click", 0.5f, 1f)
        }

        when (input) {
            Input.W -> {
                this.grid.moveFocusUp()
                this.renderArrows()
            }
            Input.A -> this.grid.moveFocusLeft()
            Input.S -> {
                this.grid.moveFocusDown()
                this.renderArrows()
            }
            Input.D -> this.grid.moveFocusRight()
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
            this.arrowUp = spawnUiElement(
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
            this.arrowDown = spawnUiElement(
                loc,
                Vector3f(.8f, .8f, 0.5f),
                NamespacedKey.fromString("spacechunks:explorer/chunk_select/arrow_down"),
                false,
            )
            return
        }

        this.arrowDown?.teleport(loc)
    }

    private fun spawnUiElement(
        location: Location,
        scale: Vector3f,
        key: NamespacedKey?,
        animate: Boolean,
//        tick: Long
    ): ItemDisplay {
        return location.world.spawn(location, ItemDisplay::class.java) { d ->
            val stack = ItemStack(Material.PAPER)
            stack.editMeta { m ->
                m.itemModel = key
            }

            this.elements.add(d)

            d.setItemStack(stack)

            d.billboard = Display.Billboard.CENTER

            d.transformation = Transformation(
                d.transformation.translation,
                d.transformation.leftRotation,
                scale,
                d.transformation.rightRotation
            )

            d.brightness = Display.Brightness(15, 15)


            if (!animate) return@spawn

            // AI
            val base: Transformation = d.getTransformation()
            val baseTranslation = Vector3f(base.getTranslation())

            val rand = java.util.Random()
            val step = rand.nextFloat(0.02f, 0.04f)

            object : BukkitRunnable() {
                var time: Double = Math.random() * Math.PI * 2

                override fun run() {
//                    time += 0.02
                    time += step

                    val x = (sin(time) * 0.15).toFloat()
                    val y = (sin(time * 1.5) * 0.10).toFloat()
                    val z = (cos(time * 1.2) * 0.15).toFloat()

                    val translation = Vector3f(
                        baseTranslation.x + x,
                        baseTranslation.y + y,
                        baseTranslation.z + z
                    )

                    val t: Transformation = Transformation(
                        translation,
                        base.getLeftRotation(),
                        base.getScale(),
                        base.getRightRotation()
                    )

                    d.setTransformation(t)
                    d.interpolationDelay = 0
                }
            }.runTaskTimer(this.plugin, 0L, 1)
        }
    }
}