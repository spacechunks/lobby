package space.chunks.explorer.lobby.display

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Matrix4f
import org.joml.Vector3f

class GameDisplay(
    private val world: World,
    private val location: Location,
    private val icon: Material?,
    private val title: Component,
) {
    private var backgroundDisplay: ItemDisplay? = null
    private var highlightDisplay: ItemDisplay? = null
    private var itemDisplay: ItemDisplay? = null
    private var textDisplay: TextDisplay? = null
    private var isFocused: Boolean = false
    private var breathingTaskId: Int = -1
    private var stopBreathing: Boolean = false
    private var currScale: Float = -1.0f
    private var currHighlightScale: Float = -1.0f

    companion object {
        private const val ICON_SCALE = 3.5f
        private const val ICON_FOCUS_SCALE = 3.39f

        private const val TEXT_OFFSET = 1.0
        private const val TEXT_SCALE = 1.5f
        private const val BACKGROUND_SCALE = 1.2f
        private const val BACKGROUND_THICKNESS = 0.05f
        private const val ICON_OFFSET = 2.0
        private const val FOCUS_SCALE = 1.3f
        private const val NORMAL_SCALE = 1.2f

        private const val HIGHLIGHT_NORMAL_SCALE = 1.1f
        private const val HIGHLIGHT_FOCUS_SCALE = 3.6f
        private const val HIGHLIGHT_OFFSET = 0.5
    }

    fun spawn(key: NamespacedKey) {
        val iconLocation = location.clone().add(0.0, 0.0, ICON_OFFSET)
        if (icon != null) {
            this.itemDisplay = world.spawn(iconLocation, ItemDisplay::class.java) { display ->
                val iconItem = ItemStack(icon)
                iconItem.editMeta { ii ->
                    ii.itemModel = key
                }

                display.setItemStack(iconItem)

                display.transformation = Transformation(
                    Vector3f(0f, 0f, 0f),
                    AxisAngle4f(0f, 0f, 0f, 0f),
                    Vector3f(ICON_SCALE, ICON_SCALE, ICON_SCALE),
                    AxisAngle4f(0f, 0f, 0f, 0f)
                )

                display.billboard = Display.Billboard.CENTER
                display.brightness = Display.Brightness(15, 15)
            }
        }
    }

    fun remove() {
        backgroundDisplay?.remove()
        highlightDisplay?.remove()
        itemDisplay?.remove()
        textDisplay?.remove()
        backgroundDisplay = null
        highlightDisplay = null
        itemDisplay = null
        textDisplay = null
    }


    fun isSpawned(): Boolean {
        return backgroundDisplay != null && highlightDisplay != null && textDisplay != null && (icon == null || itemDisplay != null)
    }

    fun setFocus(focused: Boolean, animate: Boolean = false, plugin: org.bukkit.plugin.Plugin? = null): Boolean {
        if (isFocused == focused) return false

        if (focused) {
            this.stopBreathing = false
            this.currScale = ICON_SCALE
            this.currHighlightScale = HIGHLIGHT_NORMAL_SCALE
            Bukkit.getScheduler().runTaskTimer(plugin!!, { t ->
                if (this.stopBreathing) {
                    t.cancel()
                    return@runTaskTimer
                }
                this.breathingTaskId = t.taskId
                val scale = if (currScale == ICON_FOCUS_SCALE) ICON_SCALE else ICON_FOCUS_SCALE
                this.currScale = scale
                this.itemDisplay?.setTransformationMatrix(Matrix4f().scale(scale))
                this.itemDisplay?.interpolationDuration = 11
                this.itemDisplay?.interpolationDelay = 0
            }, 0L, 11)
        } else {
            Bukkit.getScheduler().cancelTask(this.breathingTaskId)
            this.stopBreathing = true
        }

        this.isFocused = focused
        updateFocus()

        return true
    }

    fun updateFocus() {
        val iconScale = if (isFocused) ICON_FOCUS_SCALE else ICON_SCALE
        this.itemDisplay?.let { d ->
            d.setTransformationMatrix(Matrix4f().scale(iconScale))
            d.interpolationDuration = 0
            d.interpolationDelay = 0
        }
    }
}
