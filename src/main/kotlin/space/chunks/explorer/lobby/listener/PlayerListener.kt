package space.chunks.explorer.lobby.listener

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.weather.WeatherChangeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.Vector3f
import space.chunks.explorer.lobby.Plugin
import space.chunks.explorer.lobby.display.DisplaySession
import kotlin.collections.set
import kotlin.math.cos
import kotlin.math.sin

class PlayerListener(
    private val plugin: Plugin,
    private val sessions: MutableMap<Player, DisplaySession>,
) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        prepareWorld(player.location.world)

        val loc = Location(player.location.world, 0.0, 100.0, 0.0)

        Bukkit.getScheduler().runTaskLater(this.plugin, { _ ->
            val sess = DisplaySession(player, this.plugin, loc)
            this.sessions[player] = sess

            Bukkit.getPluginManager().registerEvents(ControlsListener(this.plugin, this.sessions), this.plugin)
            sess.start()
        }, 15L)


//        this.plugin.getCommand("stack")!!.setExecutor { sender, command, label, args ->
//            if (!label.equals("stack", true)) {
//                return@setExecutor false
//            }
//
//            d[args[3]]?.remove()
//            d[args[3]] = spawnUiElement(
//                sess.center.clone().add(args[0].toDouble(), args[1].toDouble(), args[2].toDouble()),
//                Vector3f(args[4].toFloat(), args[4].toFloat(), args[4].toFloat()),
//                NamespacedKey.fromString(args[3]),
//                args[5].toBoolean(),
//            )
//
//            return@setExecutor false
//        }



//            fun spawnTextElement(txt: Component, loc: Location, scale: Float): TextDisplay {
//                return loc.world.spawn(loc, TextDisplay::class.java) { d ->
//                    d.text(txt)
//                    d.setTransformationMatrix(
//                        Matrix4f().scale(scale).rotate(AxisAngle4f(Math.toRadians(-180.0).toFloat(), 0f, 1f, 0f))
//                    )
//                    d.alignment = TextDisplay.TextAlignment.LEFT
//                    d.billboard = Display.Billboard.FIXED
//                    d.backgroundColor = Color.fromARGB(0, 0, 0, 0)
//                    d.brightness = Display.Brightness(15, 15)
//                }
//            }
//
//                d.forEach { it.remove() }
//                d.clear()
//
//
//            val loc = sess.center.clone().subtract(0.0, args[1].toDouble(), 0.0)
//
//            if (d.size == 0) {
//                var c = 0.0
//                for (i in (0..args[0].toInt())) {
//                    c += 1.0
//                    val f = spawnTextElement(Component.text("abdcfdeghegdd"), loc.clone().subtract(0.0, c, 0.0), 3f)
//                    d.add(f)
//                }
//            }
//
//            return@setExecutor false
//        }

//        this.plugin.getCommand("len")!!.setExecutor { sender, command, label, args ->
//            if (!label.equals("len", true)) {
//                return@setExecutor false
//            }
//
//            fun spawnTextElement(txt: Component, loc: Location, scale: Float): TextDisplay {
//                return loc.world.spawn(loc, TextDisplay::class.java) { d ->
//                    d.text(txt)
//                    d.setTransformationMatrix(
//                        Matrix4f().scale(scale).rotate(AxisAngle4f(Math.toRadians(-180.0).toFloat(), 0f, 1f, 0f))
//                    )
//                    d.alignment = TextDisplay.TextAlignment.LEFT
//                    d.billboard = Display.Billboard.FIXED
//                    d.backgroundColor = Color.fromARGB(0, 0, 0, 0)
//                    d.brightness = Display.Brightness(15, 15)
//                }
//            }
//
//            val fill = 25 - args[0].length
//            var str = args[0]
//            if (fill > 0) {
//                str += " ".repeat(fill)
//            }
//
//            if (d == null)
//                d = spawnTextElement(Component.text(str), sess.center, 3f)
//
//            d?.text(Component.text(str))
//            d?.teleport(sess.center.clone().subtract(args[1].toDouble(), args[2].toDouble(), args[3].toDouble()))
//
//            return@setExecutor false
//        }
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        this.sessions[event.player]?.stop()
        this.sessions.remove(event.player)
    }

    @EventHandler
    fun onSpectateUnmount(event: PlayerStopSpectatingEntityEvent) {
//        event.isCancelled = true
    }

    @EventHandler
    fun onSpectateMount(event: PlayerStartSpectatingEntityEvent) {
//        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
//            event.player.hideEntity(plugin, event.newSpectatorTarget)
//        }, 10)
    }

    private fun prepareWorld(voidWorld: World) {
        voidWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
        voidWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
        voidWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false)
        voidWorld.setGameRule(GameRule.DO_FIRE_TICK, false)
        voidWorld.setGameRule(GameRule.DO_MOB_LOOT, false)
        voidWorld.setGameRule(GameRule.DO_TILE_DROPS, false)
        voidWorld.time = 1000
        voidWorld.clearWeatherDuration = -1
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
            }.runTaskTimer(this.plugin, 0L, 1)
        }
    }

    @EventHandler
    fun on(e: WeatherChangeEvent) {
        e.isCancelled = true
    }
}