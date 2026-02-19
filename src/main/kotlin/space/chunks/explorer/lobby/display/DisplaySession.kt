package space.chunks.explorer.lobby.display

import chunks.space.api.explorer.chunk.v1alpha1.Types
import org.bukkit.*
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.joml.Matrix4f
import org.joml.Vector3f

class DisplaySession(
    val player: Player,
    val plugin: Plugin,
    val location: Location,
    val chunks: List<Types.Chunk>
) {
    val center = this.location.clone().add(0.0, 3.0, 10.0)
    val grid = DisplayGrid(
        center,
        4,
        plugin,
        8
    )

    private var activeView: View? = null
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
        this.background = spawnWall(
            this.location.clone().add(0.0, 0.0, 20.0),
            75f,
            NamespacedKey.fromString("minecraft:black_concrete"),
        )
        this.activeView = ChunkSelectView(this.plugin, this.center, this, this.grid)
        this.activeView?.render()
    }

    fun stop() {
        this.activeView?.close()
        this.background.remove()
    }

    fun switchWindow(new: View) {
        this.activeView?.close()
        this.activeView = new
        this.activeView?.render()
    }

    fun handleInput(input: Input) {
        this.activeView?.handleInput(this.player, input)
    }

    private fun spawnWall(location: Location, scale: Float, key: NamespacedKey?): ItemDisplay {
        return location.world.spawn(location, ItemDisplay::class.java) {
            val stack = ItemStack(Material.PAPER)
            stack.editMeta { m ->
                m.itemModel = key
            }

            it.brightness = Display.Brightness(15, 15)
            it.setItemStack(stack)
            it.setTransformationMatrix(Matrix4f().scale(Vector3f(scale, scale, 0.1f)))
        }
    }
}