package space.chunks.explorer.lobby.display

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin

class DisplaySession(
    val player: Player,
    val plugin: Plugin,
    val location: Location
) {
    val center = this.location.clone().add(0.0, 3.0, 10.0)

    private var activeWindow: Window? = null
    private lateinit var background: ItemDisplay

    fun start() {
        this.player.teleport(this.location)

        val fixedEntity = this.location.world
            .spawn(this.location, ArmorStand::class.java) {
                it.setAI(false)
                it.canPickupItems = false
                it.isInvisible = true
                it.isSilent = true
                it.setGravity(false)
            }

        this.player.gameMode = GameMode.SPECTATOR

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            this.player.spectatorTarget = fixedEntity
        }, 10)


        // TODO: fetch chunks
        this.background = spawnWall(this.location.world, 75f, 20, NamespacedKey.fromString("minecraft:black_concrete"))
        this.activeWindow = ChunkSelectWindow(this.plugin, this.center, this)
        this.activeWindow?.render()
    }

    fun stop() {
        this.activeWindow?.close()
        this.background.remove()
    }

    fun switchWindow(new: Window) {
        this.activeWindow?.close()
        this.activeWindow = new
        this.activeWindow?.render()
    }

    fun handleInput(input: Input) {
        this.activeWindow?.handleInput(this.player, input)
    }

    private fun spawnWall(voidWorld: World, scale: Float, z: Int, key: NamespacedKey?): ItemDisplay {
        return voidWorld.spawn(Location(voidWorld, 0.0, 100.0, z.toDouble()), ItemDisplay::class.java) {
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
}