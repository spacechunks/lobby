package space.chunks.explorer.lobby.display

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.TextDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

class GameDisplay(
    private val world: World,
    private val location: Location,
    private val icon: Material?,
    private val title: Component,
    private val backgroundColor: Material = Material.LIGHT_GRAY_CONCRETE
) {
    private var backgroundDisplay: ItemDisplay? = null
    private var highlightDisplay: ItemDisplay? = null
    private var itemDisplay: ItemDisplay? = null
    private var textDisplay: TextDisplay? = null
    private var isFocused: Boolean = false
    private var currentAnimationTaskId: Int = -1

    companion object {
        private const val ICON_SCALE = 0.7f
        private const val TEXT_OFFSET = 1.0
        private const val TEXT_SCALE = 1.5f
        private const val BACKGROUND_SCALE = 1.2f
        private const val BACKGROUND_THICKNESS = 0.01f
        private const val ICON_OFFSET = 0.1
        private const val FOCUS_SCALE = 1.3f
        private const val NORMAL_SCALE = 1.2f

        private const val HIGHLIGHT_NORMAL_SCALE = 1.1f
        private const val HIGHLIGHT_FOCUS_SCALE = 1.6f
        private const val HIGHLIGHT_THICKNESS = 0.05f
        private val HIGHLIGHT_COLOR = Material.LIME_CONCRETE
        private const val HIGHLIGHT_OFFSET = 0.05
    }

    fun spawn() {
        val scale = if (isFocused) FOCUS_SCALE else NORMAL_SCALE
        backgroundDisplay = world.spawn(location, ItemDisplay::class.java) { display ->
            display.setItemStack(ItemStack(backgroundColor))
            display.transformation = Transformation(
                Vector3f(0f, 0f, 0f),
                AxisAngle4f(0f, 0f, 0f, 0f),
                Vector3f(scale, scale, BACKGROUND_THICKNESS),
                AxisAngle4f(0f, 0f, 0f, 0f)
            )
            display.billboard = Display.Billboard.CENTER
        }

        val highlightScale = if (isFocused) HIGHLIGHT_FOCUS_SCALE else HIGHLIGHT_NORMAL_SCALE
        val highlightLocation = location.clone().add(0.0, 0.0, HIGHLIGHT_OFFSET)
        highlightDisplay = world.spawn(highlightLocation, ItemDisplay::class.java) { display ->
            display.setItemStack(ItemStack(HIGHLIGHT_COLOR))
            display.transformation = Transformation(
                Vector3f(0f, 0f, 0f),
                AxisAngle4f(0f, 0f, 0f, 0f),
                Vector3f(highlightScale, highlightScale, HIGHLIGHT_THICKNESS),
                AxisAngle4f(0f, 0f, 0f, 0f)
            )
            display.billboard = Display.Billboard.CENTER
        }

        if (icon != null) {
            val iconLocation = location.clone().add(0.0, 0.0, ICON_OFFSET)
            itemDisplay = world.spawn(iconLocation, ItemDisplay::class.java) { display ->
                display.setItemStack(ItemStack(icon))
                display.transformation = Transformation(
                    Vector3f(0f, 0f, 0f),
                    AxisAngle4f(0f, 0f, 0f, 0f),
                    Vector3f(ICON_SCALE, ICON_SCALE, ICON_SCALE),
                    AxisAngle4f(0f, 0f, 0f, 0f)
                )
                display.billboard = Display.Billboard.CENTER
            }
        }

        val textLocation = location.clone().add(0.0, TEXT_OFFSET, 0.0)
        textLocation.yaw += 180
        textDisplay = world.spawn(textLocation, TextDisplay::class.java) { display ->
            display.text(title)
            display.isShadowed = true
            display.billboard = Display.Billboard.CENTER
            display.alignment = TextDisplay.TextAlignment.CENTER
            display.transformation = Transformation(
                Vector3f(0f, 0f, 0f),
                AxisAngle4f(0f, 0f, 0f, 0f),
                Vector3f(TEXT_SCALE, TEXT_SCALE, TEXT_SCALE),
                AxisAngle4f(0f, 0f, 0f, 0f)
            )
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

    fun setLocation(newLocation: Location) {
        backgroundDisplay?.teleport(newLocation)
        val highlightLocation = newLocation.clone().add(0.0, 0.0, -HIGHLIGHT_OFFSET)
        highlightDisplay?.teleport(highlightLocation)
        if (icon != null) {
            val iconLocation = newLocation.clone().add(0.0, 0.0, ICON_OFFSET)
            itemDisplay?.teleport(iconLocation)
        }
        textDisplay?.teleport(newLocation.clone().add(0.0, TEXT_OFFSET, 0.0))
    }

    fun isSpawned(): Boolean {
        return backgroundDisplay != null && highlightDisplay != null && textDisplay != null && (icon == null || itemDisplay != null)
    }

    fun setFocus(focused: Boolean, animate: Boolean = false, plugin: org.bukkit.plugin.Plugin? = null): Boolean {
        if (isFocused == focused) return false

        if (currentAnimationTaskId != -1) {
            org.bukkit.Bukkit.getScheduler().cancelTask(currentAnimationTaskId)
            currentAnimationTaskId = -1
        }

        if (animate && plugin != null) {
            animateBounce(plugin, isEntryAnimation = focused) {
                isFocused = focused
                updateBackgroundFocus()
            }
        } else {
            isFocused = focused
            updateFocus()
        }

        return true
    }

    private fun updateBackgroundFocus() {
        if (!isSpawned()) return

        val scale = if (isFocused) FOCUS_SCALE else NORMAL_SCALE
        backgroundDisplay?.let { display ->
            display.transformation = Transformation(
                display.transformation.translation,
                display.transformation.leftRotation,
                Vector3f(scale, scale, BACKGROUND_THICKNESS),
                display.transformation.rightRotation
            )
        }
    }

    fun updateFocus() {
        if (!isSpawned()) return

        val scale = if (isFocused) FOCUS_SCALE else NORMAL_SCALE
        backgroundDisplay?.let { display ->
            display.transformation = Transformation(
                display.transformation.translation,
                display.transformation.leftRotation,
                Vector3f(scale, scale, BACKGROUND_THICKNESS),
                display.transformation.rightRotation
            )
        }

        val highlightScale = if (isFocused) HIGHLIGHT_FOCUS_SCALE else HIGHLIGHT_NORMAL_SCALE
        highlightDisplay?.let { display ->
            display.transformation = Transformation(
                display.transformation.translation,
                display.transformation.leftRotation,
                Vector3f(highlightScale, highlightScale, HIGHLIGHT_THICKNESS),
                display.transformation.rightRotation
            )
        }
    }
    fun isFocused(): Boolean {
        return isFocused
    }

    fun animateScale(
        plugin: org.bukkit.plugin.Plugin,
        targetScale: Float,
        durationTicks: Int,
        onComplete: (() -> Unit)? = null
    ) {
        if (!isSpawned()) return

        if (currentAnimationTaskId != -1) {
            org.bukkit.Bukkit.getScheduler().cancelTask(currentAnimationTaskId)
            currentAnimationTaskId = -1
        }

        highlightDisplay?.let { display ->
            val startScale = display.transformation.scale
            val steps = durationTicks.coerceAtLeast(1)
            val scaleStep = Vector3f(
                (targetScale - startScale.x) / steps,
                (targetScale - startScale.y) / steps,
                0f
            )

            var currentStep = 0

            currentAnimationTaskId = org.bukkit.Bukkit.getScheduler().scheduleSyncRepeatingTask(
                plugin,
                {
                    currentStep++
                    if (currentStep <= steps) {
                        val progress = currentStep.toFloat() / steps
                        val smoothProgress = if (progress < 0.5f) 
                            2 * progress * progress 
                        else 
                            1 - Math.pow((-2 * progress + 2).toDouble(), 2.0).toFloat() / 2

                        val newScale = Vector3f(
                            startScale.x + (targetScale - startScale.x) * smoothProgress,
                            startScale.y + (targetScale - startScale.y) * smoothProgress,
                            startScale.z
                        )
                        display.transformation = Transformation(
                            display.transformation.translation,
                            display.transformation.leftRotation,
                            newScale,
                            display.transformation.rightRotation
                        )
                    } else {
                        org.bukkit.Bukkit.getScheduler().cancelTask(currentAnimationTaskId)
                        currentAnimationTaskId = -1

                        onComplete?.invoke()

                        if (currentAnimationTaskId == -1) {
                            updateFocus()
                        }
                    }
                },
                0L, 1L
            )
        }
    }

    fun animatePosition(
        plugin: org.bukkit.plugin.Plugin,
        targetLocation: Location,
        durationTicks: Int,
        onComplete: (() -> Unit)? = null
    ) {
        if (!isSpawned()) return

        backgroundDisplay?.let { display ->
            val startLoc = display.location
            val steps = durationTicks.coerceAtLeast(1)
            val xStep = (targetLocation.x - startLoc.x) / steps
            val yStep = (targetLocation.y - startLoc.y) / steps
            val zStep = (targetLocation.z - startLoc.z) / steps

            var currentStep = 0
            var taskId = -1

            taskId = org.bukkit.Bukkit.getScheduler().scheduleSyncRepeatingTask(
                plugin,
                {
                    currentStep++
                    if (currentStep <= steps) {
                        val progress = currentStep.toFloat() / steps
                        val smoothProgress = if (progress < 0.5f) 
                            2 * progress * progress 
                        else 
                            1 - Math.pow((-2 * progress + 2).toDouble(), 2.0).toFloat() / 2

                        val newLoc = startLoc.clone().add(
                            (targetLocation.x - startLoc.x) * smoothProgress,
                            (targetLocation.y - startLoc.y) * smoothProgress,
                            (targetLocation.z - startLoc.z) * smoothProgress
                        )
                        setLocation(newLoc)
                    } else {
                        org.bukkit.Bukkit.getScheduler().cancelTask(taskId)
                        onComplete?.invoke()
                    }
                },
                0L, 1L
            )
        }
    }
    fun animateBounce(
        plugin: org.bukkit.plugin.Plugin,
        isEntryAnimation: Boolean = true,
        intensity: Float = 0.1f,
        durationTicks: Int = 20,
        onComplete: (() -> Unit)? = null
    ) {
        if (!isSpawned()) return

        highlightDisplay?.let { display ->
            if (isEntryAnimation) {
                val normalScale = HIGHLIGHT_NORMAL_SCALE
                val expandedScale = HIGHLIGHT_FOCUS_SCALE + intensity
                val focusScale = HIGHLIGHT_FOCUS_SCALE

                display.transformation = Transformation(
                    display.transformation.translation,
                    display.transformation.leftRotation,
                    Vector3f(normalScale, normalScale, HIGHLIGHT_THICKNESS),
                    display.transformation.rightRotation
                )

                animateScale(plugin, expandedScale, durationTicks / 2) {
                    animateScale(plugin, focusScale, durationTicks / 2, onComplete)
                }
            } else {
                val focusScale = display.transformation.scale.x
                val expandedScale = focusScale
                val normalScale = HIGHLIGHT_NORMAL_SCALE

                display.transformation = Transformation(
                    display.transformation.translation,
                    display.transformation.leftRotation,
                    Vector3f(focusScale, focusScale, HIGHLIGHT_THICKNESS),
                    display.transformation.rightRotation
                )

                animateScale(plugin, normalScale, durationTicks)
            }
        }
    }

}
