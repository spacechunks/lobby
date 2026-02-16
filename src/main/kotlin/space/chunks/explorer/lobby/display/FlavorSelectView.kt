package space.chunks.explorer.lobby.display

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

class FlavorSelectView(
    plugin: Plugin,
    center: Location,
    session: DisplaySession,
    private val flavors: PaginatedList<String>,
) : View(plugin, center, session) {
    private val mini = MiniMessage.miniMessage()
    private val texts = mutableMapOf<Int, TextDisplay>()

    private var pageIndicator: TextDisplay? = null
    private var selectMarker: ItemDisplay? = null
    private var currPage = 0
    private var currIdx = 0

    private val elements = mutableListOf<Entity>()

    private fun xOff(biggest: Int): Double {
        // dont ask lmao
        return mapOf(
            1 to 4.5,
            2 to 4.5,
            3 to 4.3,
            4 to 4.1,
            5 to 3.9,
            6 to 3.6,
            7 to 3.5,
            8 to 3.3,
            9 to 3.1,
            10 to 2.7,
            11 to 2.9,
            12 to 2.7,
            13 to 2.5,
            14 to 2.3,
            15 to 2.1,
            16 to 1.9,
            17 to 1.7,
            18 to 1.6,
            19 to 1.4,
            20 to 1.2,
            21 to 1.0,
            22 to 0.8,
            23 to 0.6,
            24 to 0.4,
            25 to 0.2,
        )[biggest] ?: 1.0
    }

    override fun render() {
        center.world.spawn(center.clone().add(-0.05, 3.8, 0.0), ItemDisplay::class.java) { d ->
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

        this.spawnTextElement(
            this.mini.deserialize("<gradient:#E3ECFD:#C1D7F9>Choose your flavor!</gradient>"),
            this.center.clone().add(0.0, 1.0, 0.0), 3.5f,
        )

        this.spawnUiElement(
            this.center.clone().add(-6.5, 1.5, 0.0),
            Vector3f(4.0f, 4.0f, 4.0f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/satellite"),
            true,
        )

        this.spawnUiElement(
            this.center.clone().add(-9.5, 3.0, 0.0),
            Vector3f(1.0f, 1.0f, 1.0f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone2"),
            true,
        )

        this.spawnUiElement(
            this.center.clone().add(-7.5, -1.5, 0.0),
            Vector3f(1.0f, 1.0f, 1.0f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone3"),
            true,
        )

//        /stack 10.5 -6.0 1.5 spacechunks:explorer/chunk_select/stone1 2.5 true
        this.spawnUiElement(
            this.center.clone().add(8.5, -5.0, 0.0),
            Vector3f(2.5f, 2.5f, 2.5f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone1"),
            true,
        )

//        /stack 10.5 -8.0 1.5 spacechunks:explorer/chunk_select/stone2 .5 true
        this.spawnUiElement(
            this.center.clone().add(8.5, -7.0, 0.0),
            Vector3f(0.5f, 0.5f, 0.5f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone2"),
            true,
        )

//        /stack 10.0 -5.0 1.5 spacechunks:explorer/chunk_select/stone3 .7 true
        this.spawnUiElement(
            this.center.clone().add(8.5, -3.8, 0.0),
            Vector3f(0.7f, 0.7f, 0.7f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone3"),
            true,
        )

        this.renderTexts()
        this.updateSelectionMark()
        this.updatePageIndicator()
    }

    override fun close() {
        this.elements.forEach { it.remove() }
    }

    override fun handleInput(player: Player, input: Input) {
        when (input) {
            Input.W -> this.currIdx--
            Input.A -> this.currPage--
            Input.S -> this.currIdx++
            Input.D -> this.currPage++
            Input.SPACE -> {
                // TODO: run selected chunk flavor
            }
            Input.SNEAK -> {
                player.playSound(player.location, "spacechunks.explorer.chunk_select.click", 0.5f, 1f)
                this.session.switchWindow(ChunkSelectView(this.plugin, this.center, this.session, this.session.grid))
            }
        }

        if (this.currPage > this.flavors.totalPages - 1) {
            // revert our increment we did before, because we would
            // exceed the pages list
            this.currPage--
            player.playSound(player.location, "spacechunks.explorer.chunk_select.click_err", 0.5f, 1f)
            return
        }

        if (this.currPage < 0) {
            // revert our decrement we did before, because we would
            // fall below the available pages list
            this.currPage++
            player.playSound(player.location, "spacechunks.explorer.chunk_select.click_err", 0.5f, 1f)
            return
        }

        if (input == Input.W || input == Input.A || input == Input.S || input == Input.D) {
            player.playSound(player.location, "spacechunks.explorer.chunk_select.click", 0.5f, 1f)
        }

        val pageItemsCount = this.flavors.getPage(this.currPage).size

        if (this.currIdx > pageItemsCount - 1) {
            this.currIdx = 0 // jump to first entry
        }

        if (this.currIdx < 0) {
            this.currIdx = pageItemsCount - 1 // jump to last entry
        }

        this.renderTexts()
        this.updateSelectionMark()
        this.updatePageIndicator()
    }

    // this is called everytime we receive an input
    private fun renderTexts() {
        val pageItems = this.flavors.getPage(this.currPage)
        val d = pageItems.sortedByDescending { it.length }
        val xOff = this.xOff(d.first().length)
        val start = this.center.clone().subtract(0.0, 0.0, 0.0)

        for ((idx, f) in pageItems.withIndex()) {
            val fill = 25 - f.length
            var str = f
            if (fill > 0) {
                str += " ".repeat(fill)
            }

            val loc = start.clone().subtract(0.0, 0.5, 0.0).subtract(xOff, 1.0 * idx, 0.0)
            var txt = Component.text(str)
            val td = this.texts[idx]

            if (this.currIdx == idx) {
                txt = txt.color(TextColor.fromHexString("#52cefd"))
            }

            if (td == null) {
                this.texts[idx] = this.spawnTextElement(txt, loc, 3f)
                continue
            }

            td.text(txt)
            td.teleport(loc)
        }

        // clear the rest of the displays, so we don't display
        // the previous page texts.
        // to give a concrete example why we need this: if the
        // first page has 5 items, and we switch to a page that
        // only has 2 items, the last 3 items would still display
        // the text of the previous page.
        for (idx in (pageItems.size..<this.flavors.pageSize)) {
            this.texts[idx]?.text(Component.text(""))
        }
    }

    // this is called everytime we receive an input
    private fun updatePageIndicator() {
        val txt =
            this.mini.deserialize("<color:#53d0fd><font:spacechunks:ui>\uE102</font> <white>${this.currPage + 1}/${this.flavors.totalPages} <color:#53d0fd><font:spacechunks:ui>\uE101</font>")
        if (this.pageIndicator == null) {
            this.pageIndicator =
                this.spawnTextElement(
                    txt,
                    this.center.clone().subtract(0.0, 6.0, 0.0),
                    2f,
                )
            return
        }
        this.pageIndicator?.text(txt)
    }

    // this is called everytime we receive an input
    private fun updateSelectionMark() {
        val loc = this.texts[this.currIdx]!!.location.clone().subtract(-5.0, -0.32, 0.0)
        if (this.selectMarker == null) {
            this.selectMarker = this.spawnUiElement(
                loc,
                Vector3f(0.5f, 0.5f, .5f),
                NamespacedKey.fromString("spacechunks:explorer/chunk_select/arrow_right"),
                false,
            )
            return
        }

        this.selectMarker?.teleport(loc)
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

    private fun spawnTextElement(txt: Component, loc: Location, scale: Float): TextDisplay {
        return loc.world.spawn(loc, TextDisplay::class.java) { d ->
            this.elements.add(d)
            d.text(txt)
            d.setTransformationMatrix(
                Matrix4f().scale(scale).rotate(AxisAngle4f(Math.toRadians(-180.0).toFloat(), 0f, 1f, 0f))
            )
            d.alignment = TextDisplay.TextAlignment.LEFT
            d.billboard = Display.Billboard.FIXED
            d.backgroundColor = Color.fromARGB(0, 0, 0, 0)
            d.brightness = Display.Brightness(15, 15)
        }
    }
}