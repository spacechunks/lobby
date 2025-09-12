package space.chunks.explorer.lobby

import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.generator.ChunkGenerator
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3f
import space.chunks.explorer.lobby.display.DisplayGrid
import space.chunks.explorer.lobby.display.GameItem
import space.chunks.explorer.lobby.listener.ControllsListener
import space.chunks.explorer.lobby.listener.PlayerListener
import space.chunks.explorer.lobby.world.VoidWorldGenerator

class ExplorerLobbyPlugin : JavaPlugin() {

    private lateinit var displayGrid: DisplayGrid

    override fun onEnable() {
        val voidWorld = WorldCreator.name("void")
            .generator(VoidWorldGenerator())
            .createWorld()?: throw IllegalStateException("Failed to create void world")
        prepareWorld(voidWorld)

        Bukkit.getPluginManager().registerEvents(PlayerListener(this, voidWorld), this)
//        Bukkit.getPluginManager().registerEvents(ControllsListener(this, displayGrid), this)
    }

    private fun prepareWorld(voidWorld: World) {
        listOf(*voidWorld.entities.toTypedArray()).forEach { it.remove() }
        voidWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        voidWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        voidWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        voidWorld.setGameRule(GameRule.DO_FIRE_TICK, false)
        voidWorld.setGameRule(GameRule.DO_MOB_LOOT, false)
        voidWorld.setGameRule(GameRule.DO_TILE_DROPS, false)
        voidWorld.time = 0

        // Removed all game display elements as requested
//        spawnWall(voidWorld, 200f, 20, Material.PURPLE_CONCRETE)
//        spawnWall(voidWorld, 100f, -10, Material.PURPLE_CONCRETE)
//        spawnColoredBox(
//            world = voidWorld,
//            location = Location(voidWorld, 0.0, 75.0, 19.0),
//            color = Color.fromARGB(100, 0, 0, 0),
//            scale = 600.0,
//        )

//        displayGrid = DisplayGrid(voidWorld, Location(voidWorld, 0.0, 105.0, 10.0), 5)

        val gameItems = mutableListOf<GameItem>()
        for (i in 0..30) {
            gameItems.add(GameItem(
                icon = if (i % 3 == 0) Material.DIAMOND else null,
                title = Component.text("Game ${i}"),
                backgroundColor = Material.LIGHT_GRAY_CONCRETE,
                gameId = "game-${i}",
                playerCount = i % 10,
                maxPlayers = 10,
                status = if (i % 5 == 0) "Running" else "Waiting"
            ))
        }

        val textLocation = Location(voidWorld, 0.0, 105.0, 10.0).clone()
        textLocation.yaw += 180
        val box = spawnColoredBox(
            world = voidWorld,
            location = textLocation,
            color = Color.RED,
            scale = 5.0,
        )

        spawnColoredBox(
            world = voidWorld,
            location = textLocation.clone().add(0.0, 0.0, -0.1),
            color = Color.YELLOW,
            scale = 2.0,
        )


        spawnColoredBox(
            world = voidWorld,
            location = textLocation.clone().add(0.0, 0.0, -0.2),
            color = Color.BLUE,
            scale = 1.0,
        )

//        displayGrid.setAllItems(gameItems)
//
//        displayGrid.setInitialFocus()
    }

    fun spawnColoredBox(
        world: World,
        location: Location,
        color: Color,
        scale: Double = 1.0,
        aspectRatio: Double = 1.8,
    ): TextDisplay {
        return world.spawn(location, TextDisplay::class.java) { display ->
            display.text(Component.text(" "))
            display.isShadowed = false
            display.billboard = Display.Billboard.FIXED
            display.alignment = TextDisplay.TextAlignment.CENTER

            val originalPos = display.transformation.translation

            val scaledTransform = Transformation(
                Vector3f(0f, 0f, 0f),
                Quaternionf(),
                Vector3f(
                    (scale * aspectRatio).toFloat(),
                    scale.toFloat(),
                    1f
                ),
                Quaternionf()
            )
            display.transformation = scaledTransform

            val width = scale * aspectRatio
            val height = scale
            val offsetX = -width / 2
            val offsetY = -height / 2

            val finalTransform = Transformation(
                Vector3f(
                    (originalPos.x + offsetX).toFloat(),
                    (originalPos.y + offsetY).toFloat(),
                    originalPos.z
                ),
                Quaternionf(),
                Vector3f(
                    (scale * aspectRatio).toFloat(),
                    scale.toFloat(),
                    1f
                ),
                Quaternionf()
            )
            display.transformation = finalTransform

            display.backgroundColor = color
        }
    }

    private fun spawnWall(voidWorld: World, scale: Float, z: Int, material: Material) {
        voidWorld.spawn(Location(voidWorld, 0.0, 100.0, z.toDouble()), ItemDisplay::class.java) {
            it.setItemStack(ItemStack.of(material))

        it.transformation = Transformation(
                it.transformation.translation,
                it.transformation.leftRotation,
                Vector3f(scale, scale, 0.1f),
                it.transformation.rightRotation
            )
        }
    }

}
