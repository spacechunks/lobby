package space.chunks.explorer.lobby

import chunks.space.api.explorer.chunk.v1alpha1.ChunkServiceGrpcKt
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import space.chunks.explorer.lobby.display.DisplayGrid
import space.chunks.explorer.lobby.display.GameDisplay
import space.chunks.explorer.lobby.display.GameItem
import space.chunks.explorer.lobby.listener.ControllsListener
import space.chunks.explorer.lobby.listener.PlayerListener
import space.chunks.explorer.lobby.world.VoidWorldGenerator
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


class Plugin : JavaPlugin() {

    private lateinit var displayGrid: DisplayGrid
    private lateinit var chunkClient: ChunkServiceGrpcKt.ChunkServiceCoroutineStub

    // LIGHT BLUE #7ce8fe
    // A BIT DARKER BLUE #53d0fd

    override fun onEnable() {
//        var cfg: Config
//        try {
//            cfg = parseConfig(this.config)
//        } catch (e: RuntimeException) {
//            this.logger.severe(e.message)
//            this.server.shutdown()
//            return
//        }

//        val channel = ManagedChannelBuilder
//            .forAddress(cfg.controlPlaneEndpointAddr, cfg.controlPlaneEndpointPort)
//            .useTransportSecurity()
//            .build()
//
//        this.chunkClient = ChunkServiceGrpcKt.ChunkServiceCoroutineStub(channel)
//            .withCallCredentials(AuthCredentials(cfg.controlPlaneAPIToken))
//
//        val req = listChunksRequest {}
//
//        runBlocking {
//            val l = chunkClient.listChunks(listChunksRequest {})
//            l.chunksList.forEach {
//                logger.info(it.name)
//            }
//        }

        val voidWorld = WorldCreator.name(UUID.randomUUID().toString())
            .generator(VoidWorldGenerator())
            .createWorld()?: throw IllegalStateException("Failed to create void world")

        prepareWorld(voidWorld)


        Bukkit.getPluginManager().registerEvents(PlayerListener(this, voidWorld), this)
        Bukkit.getPluginManager().registerEvents(ControllsListener(this, displayGrid), this)
    }

    private fun prepareWorld(voidWorld: World) {
        listOf(*voidWorld.entities.toTypedArray()).forEach { it.remove() }
        voidWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        voidWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        voidWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        voidWorld.setGameRule(GameRule.DO_FIRE_TICK, false)
        voidWorld.setGameRule(GameRule.DO_MOB_LOOT, false)
        voidWorld.setGameRule(GameRule.DO_TILE_DROPS, false)
        voidWorld.time = 1000
        voidWorld.clearWeatherDuration = -1

        displayGrid = DisplayGrid(
            voidWorld,
            Location(voidWorld, 0.0, 103.0, 10.0),
            4,
            this,
            itemsPerPage = 8
        )

        // Removed all game display elements as requested
        spawnWall(voidWorld, 200f, 20, NamespacedKey.fromString("minecraft:black_concrete"))
        spawnWall(voidWorld, 100f, -10, NamespacedKey.fromString("minecraft:black_concrete"))

        val center = Location(voidWorld, 0.0, 103.0, 10.0)

        center.world.spawn(center.clone().add(0.0, 3.5, 0.0), ItemDisplay::class.java) { d ->
            val stack = ItemStack(Material.PAPER)
            stack.editMeta { m ->0
                m.itemModel = NamespacedKey.fromString("spacechunks:explorer/chunk_select/logo")
            }

            d.setItemStack(stack)

            d.billboard = Display.Billboard.CENTER

            d.transformation = Transformation(
                d.transformation.translation,
                d.transformation.leftRotation,
                Vector3f(7f, 3.5f, 1f),
                d.transformation.rightRotation
            )
        }

//        spawnUiElement(
//            center.clone().add(0.0, 3.0, 0.0),
//            3.0f,
//            NamespacedKey.fromString("spacechunks:explorer/chunk_select/logo"),
//            false
//        )
//
//        spawnUiElement(
//            center.clone().add(-7.0, 4.0, 0.0),
//            1.0f,
//            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone2")
//        )

        spawnUiElement(
            center.clone().add(-3.5, 3.5, 0.0),
            Vector3f(1f, 1f, 1f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone1"),
            true,
//            20
        )

        spawnUiElement(
            center.clone().add(-3.0, 5.0, 0.0),
            Vector3f(.6f, .6f, .6f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone3"),
            true,
//            3
        )


        spawnUiElement(
            center.clone().add(3.6, 4.5, 0.0),
            Vector3f(1f, 1f, 1f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone2"),
            true,
//            2
        )

        val lol = spawnUiElement(
            center.clone().add(3.5, 2.5, 0.0),
            Vector3f(.8f, .8f, .8f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/stone4"),
            true,
//            1
        )

//        lol.setTransformationMatrix(Matrix4f().rotation(AxisAngle4f(Math.toRadians(-180.0).toFloat(), 0f, 1f, 0f)))

        val arrowUpLoc = center.clone().add(8.0, -1.55, 0.0)
        val arrowUp = spawnUiElement(
            arrowUpLoc,
            Vector3f(.8f, .8f, 0.5f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/arrow_up"),
            false,
//            1
        )

        val arrowDonw = spawnUiElement(
            arrowUpLoc.clone().subtract(0.0, .6, 0.0),
            Vector3f(.8f, .8f, 0.5f),
            NamespacedKey.fromString("spacechunks:explorer/chunk_select/arrow_down"),
            false,
//            1
        )

        val gameItems = mutableListOf<GameItem>()
        for (i in 0..7) {
            gameItems.add(GameItem(
                icon = Material.PAPER,
                title = Component.text("Game ${i}"),
                key = NamespacedKey.fromString("spacechunks:explorer/chunk_select/chunk_thumbnail")!!,
                backgroundColor = Material.LIGHT_GRAY_CONCRETE,
                gameId = "game-${i}",
                playerCount = i % 10,
                maxPlayers = 10,
                status = if (i % 5 == 0) "Running" else "Waiting",
                center = center
            ))
        }

        for (i in 0..7) {
            gameItems.add(GameItem(
                icon = Material.PAPER,
                title = Component.text("Game ${i}"),
                key = NamespacedKey.fromString("spacechunks:explorer/chunk_select/chunk_thumbnail2")!!,
                backgroundColor = Material.LIGHT_GRAY_CONCRETE,
                gameId = "game-${i}",
                playerCount = i % 10,
                maxPlayers = 10,
                status = if (i % 5 == 0) "Running" else "Waiting",
                center = center
            ))
        }

        for (i in 0..7) {
            gameItems.add(GameItem(
                icon = Material.PAPER,
                title = Component.text("Game ${i}"),
                key = NamespacedKey.fromString("spacechunks:explorer/chunk_select/chunk_thumbnail3")!!,
                backgroundColor = Material.LIGHT_GRAY_CONCRETE,
                gameId = "game-${i}",
                playerCount = i % 10,
                maxPlayers = 10,
                status = if (i % 5 == 0) "Running" else "Waiting",
                center = center
            ))
        }

        val textLocation = Location(voidWorld, 0.0, 107.0, 10.0).clone()
        textLocation.yaw += 180

        displayGrid.setAllItems(gameItems)
        displayGrid.setInitialFocus()
    }

    private fun spawnWall(voidWorld: World, scale: Float, z: Int, key: NamespacedKey?) {
        voidWorld.spawn(Location(voidWorld, 0.0, 100.0, z.toDouble()), ItemDisplay::class.java) {
            val stack = ItemStack(Material.PAPER)
            stack.editMeta { m ->
                m.itemModel = key
            }

            it.brightness = Display.Brightness(15, 15)

            it.setItemStack(stack)

            it.transformation = Transformation(
                it.transformation.translation,
                it.transformation.leftRotation,
                Vector3f(scale, scale, 0.1f),
                it.transformation.rightRotation
            )
        }
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
            }.runTaskTimer(this, 0L, 1)
        }
    }
}
