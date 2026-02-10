package space.chunks.explorer.lobby.display

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

class FlavorSelectWindow(
    plugin: Plugin,
    center: Location,
    private val flavors: List<String>
) : Window(plugin, center) {

    private val elements = mutableListOf<Entity>()

    private fun xOff(biggest: Int): Double {
        return mapOf(
            1 to 4.7,
            2 to 4.5,
            3 to 4.3,
            4 to 4.2,
            5 to 4.0,
            6 to 3.8,
            7 to 3.6,
            8 to 3.4,
            9 to 3.2,
            10 to 2.7, // 3.0
            11 to 2.8,
            12 to 2.5,
            13 to 2.3,
            14 to 2.2,
            15 to 1.9,
            16 to 1.8,
            17 to 1.4,
            18 to 1.4,
            19 to 1.2,
            20 to 1.0,
            21 to 0.8,
            22 to 0.8,
            23 to 0.4,
            24 to 0.1,
            25 to 0.2,
        )[biggest] ?: 1.0
    }

    private fun yOff(n: Int): Double {
        return mapOf(
            1 to 0.5,
            2 to -0.5,
            3 to -1.0,
            4 to -1.5,
            5 to -1.7,
        )[n] ?: 1.0
    }

    override fun render() {
        val textLocs = mutableListOf<Location>()

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
            Component.text("Choose your Flavor!").color(TextColor.fromHexString("#52cefd")),
            this.center.clone().add(0.0, 1.0, 0.0), 3.5f,
        )

        this.spawnUiElement(
            this.center.clone().add(-7.5, 1.5, 0.0),
            Vector3f(4.0f, 4.0f, 4.0f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/satellite"),
            true,
        )

        this.spawnUiElement(
            this.center.clone().add(-10.5, 3.0, 0.0),
            Vector3f(1.0f, 1.0f, 1.0f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone2"),
            true,
        )

        this.spawnUiElement(
            this.center.clone().add(-8.5, -1.5, 0.0),
            Vector3f(1.0f, 1.0f, 1.0f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone3"),
            true,
        )

//        /stack 10.5 -6.0 1.5 spacechunks:explorer/chunk_select/stone1 2.5 true
        this.spawnUiElement(
            this.center.clone().add(9.5, -5.0, 0.0),
            Vector3f(2.5f, 2.5f, 2.5f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone1"),
            true,
        )

//        /stack 10.5 -8.0 1.5 spacechunks:explorer/chunk_select/stone2 .5 true
        this.spawnUiElement(
            this.center.clone().add(9.5, -7.0, 0.0),
            Vector3f(0.5f, 0.5f, 0.5f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone2"),
            true,
        )

//        /stack 10.0 -5.0 1.5 spacechunks:explorer/chunk_select/stone3 .7 true
        this.spawnUiElement(
            this.center.clone().add(9.5, -3.8, 0.0),
            Vector3f(0.7f, 0.7f, 0.7f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone3"),
            true,
        )
        var space = 0.0

        val d = this.flavors.sortedByDescending { it.length }

        val start = this.center.clone().subtract(0.0, this.yOff(this.flavors.size), 0.0)

        for (f in this.flavors) {
            space += 1.0
            val fill = 25 - f.length
            var str = f
            if (fill > 0) {
                str += " ".repeat(fill)
            }


            val d = this.spawnTextElement(
                Component.text(str),
                start.clone().subtract(0.0, 0.5, 0.0) .subtract(this.xOff(d.first().length), space, 0.0),
                3f,
            )
            textLocs.add(d.location)
        }

        this.spawnUiElement(
            textLocs.first().clone().subtract(-5.0, -0.32, 0.0),
            Vector3f(0.5f, 0.5f, .5f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/arrow_right"),
            false,
        )
    }

    override fun close() {
        this.elements.forEach { it.remove() }
    }

    override fun handleInput(player: Player, input: Input) {
        TODO("Not yet implemented")
    }

    private fun renderArrow() {

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