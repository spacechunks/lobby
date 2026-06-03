package space.chunks.lobby.modules.chunkviewer.display

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffectType
import org.joml.Matrix4f
import org.joml.Vector3f
import space.chunks.lobby.ui.Texts

class DisplaySession(
    val player: Player,
    val plugin: Plugin,
    val location: Location,
    val chunks: List<ChunkDisplay>,
    private val texts: Texts,
) {
    val center = this.location.clone().add(0.0, 3.0, 10.0)
    val grid = DisplayGrid(
        center,
        4,
        plugin,
        8
    )

    private var activeView: View? = null
    private lateinit var background: TextDisplay
    private lateinit var behind: TextDisplay
    private lateinit var camera: ArmorStand

    fun start() {
        player.addPotionEffect(PotionEffectType.DARKNESS.createEffect(40, Int.MAX_VALUE))
        this.player.teleport(this.location)

        // place player 1.5 blocks further away from the view, to make room
        // for descriptions longer than 100 chars.
        this.camera = this.location.world
            .spawn(this.location.clone().subtract(0.0, 0.0, 1.0), ArmorStand::class.java) {
                it.setAI(false)
                it.canPickupItems = false
                it.isInvisible = true
                it.isSilent = true
                it.setGravity(false)
            }

        this.player.gameMode = GameMode.SPECTATOR

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            this.player.spectatorTarget = camera
        }, 10)

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            this.player.hideEntity(this.plugin, this.camera)
        }, 20)

        // TODO: fetch chunks
        this.background = spawnWall(
            this.location.clone().add(0.0, 0.0, 20.0).addRotation(180f,0f),
            2f,
        )

        this.behind = spawnWall(
            this.location.clone().subtract(0.0, 0.0, 20.0),
            2f,
        )

        this.activeView = ChunkSelectView(this.plugin, this.center, this, this.grid, this.texts)
        this.activeView?.render()
    }

    fun stop() {
        this.activeView?.close()
        this.background.remove()
        this.behind.remove()
        this.camera.remove()
    }

    fun switchWindow(new: View) {
        this.activeView?.close()
        this.activeView = new
        this.activeView?.render()
    }

    fun handleInput(input: Input) {
        this.activeView?.handleInput(this.player, input)
    }

    private fun spawnWall(location: Location, scale: Float): TextDisplay {
        return location.world.spawn(location, TextDisplay::class.java) {
            it.brightness = Display.Brightness(0, 0)
            it.text(this.texts.component("chunkviewer.display.background"))
            it.billboard = Display.Billboard.FIXED
            it.lineWidth = 1
            it.isDefaultBackground = false
            it.backgroundColor = Color.fromARGB(255, 0, 0, 0)
            it.setTransformationMatrix(Matrix4f().scale(Vector3f(scale, scale, 0.1f)))
        }
    }
}
