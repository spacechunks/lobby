package space.chunks.explorer.lobby.display

import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

data class GameItem(
    val icon: Material?,
    val title: Component,
    val backgroundColor: Material = Material.LIGHT_GRAY_CONCRETE,
    val gameId: String = "",
    val playerCount: Int = 0,
    val maxPlayers: Int = 0,
    val status: String = ""
)

class DisplayGrid(
    private val world: World,
    private val centerLocation: Location,
    private val itemsPerRow: Int,
    private val itemsPerPage: Int = itemsPerRow * 4,
    private val spacing: Double = 3.0
) {
    private val displays = mutableListOf<GameDisplay>()
    private var focusedIndex: Int = -1

    private var currentPage: Int = 0
    private var totalItems: Int = 0
    private var allGameItems = mutableListOf<GameItem>()

    fun getTotalPages(): Int {
        return max(1, ceil(totalItems.toDouble() / itemsPerPage).toInt())
    }

    fun getCurrentPage(): Int {
        return currentPage
    }
    fun setAllItems(items: List<GameItem>) {
        allGameItems.clear()
        allGameItems.addAll(items)
        totalItems = items.size
        refreshCurrentPage()
    }

    fun addItem(item: GameItem, refreshDisplay: Boolean = true) {
        allGameItems.add(item)
        totalItems++
        if (refreshDisplay) {
            refreshCurrentPage()
        }
    }

    fun addItems(items: List<GameItem>) {
        allGameItems.addAll(items)
        totalItems += items.size
        refreshCurrentPage()
    }
    fun refreshCurrentPage() {
        clear()

        val startIndex = currentPage * itemsPerPage
        val endIndex = min(startIndex + itemsPerPage, totalItems)

        for (i in startIndex until endIndex) {
            val item = allGameItems[i]
            val position = calculatePositionInPage(i - startIndex)
            val display = GameDisplay(
                world = world,
                location = position,
                icon = item.icon,
                title = item.title,
                backgroundColor = item.backgroundColor
            )
            display.spawn()
            displays.add(display)
        }

        if (displays.isNotEmpty()) {
            setInitialFocus()
        }
    }

    private fun calculatePositionInPage(indexInPage: Int): Location {
        val row = indexInPage / itemsPerRow
        val column = itemsPerRow - 1 - (indexInPage % itemsPerRow)

        val totalWidth = (itemsPerRow - 1) * spacing
        val xOffset = column * spacing - (totalWidth / 2)
        val yOffset = -row * spacing

        return centerLocation.clone().add(
            xOffset,
            yOffset,
            0.0
        )
    }

    fun nextPage(): Boolean {
        if (currentPage >= getTotalPages() - 1) return false

        currentPage++
        refreshCurrentPage()
        return true
    }

    fun previousPage(): Boolean {
        if (currentPage <= 0) return false

        currentPage--
        refreshCurrentPage()
        return true
    }

    fun goToPage(page: Int): Boolean {
        if (page < 0 || page >= getTotalPages()) return false
        if (page == currentPage) return true

        currentPage = page
        refreshCurrentPage()
        return true
    }
    fun add(
        icon: Material?,
        title: Component,
        backgroundColor: Material = Material.LIGHT_GRAY_CONCRETE
    ) {
        addItem(GameItem(icon, title, backgroundColor), true)
    }

    fun clear() {
        displays.forEach { it.remove() }
        displays.clear()
        focusedIndex = -1
    }

    fun clearAll() {
        clear()
        allGameItems.clear()
        totalItems = 0
        currentPage = 0
    }

    fun setInitialFocus(): Boolean {
        if (displays.isEmpty()) return false
        return setFocus(0)
    }

    fun setFocus(index: Int): Boolean {
        if (index < 0 || index >= displays.size) return false
        if (focusedIndex == index) return true

        if (focusedIndex >= 0 && focusedIndex < displays.size) {
            displays[focusedIndex].setFocus(false)
        }

        focusedIndex = index
        displays[focusedIndex].setFocus(true)
        return true
    }

    fun moveFocusUp(): Boolean {
        if (focusedIndex < 0) return setInitialFocus()

        val currentRow = focusedIndex / itemsPerRow
        if (currentRow <= 0) return false

        val newIndex = focusedIndex - itemsPerRow
        return if (newIndex >= 0) setFocus(newIndex) else false
    }

    fun moveFocusDown(): Boolean {
        if (focusedIndex < 0) return setInitialFocus()

        val newIndex = focusedIndex + itemsPerRow
        if (newIndex < displays.size) {
            return setFocus(newIndex)
        } else {
            return nextPage()
        }
    }

    fun moveFocusLeft(): Boolean {
        if (focusedIndex < 0) return setInitialFocus()

        val currentColumn = focusedIndex % itemsPerRow
        if (currentColumn <= 0) {
            return previousPage()
        }

        return setFocus(focusedIndex - 1)
    }

    fun moveFocusRight(): Boolean {
        if (focusedIndex < 0) return setInitialFocus()

        val currentColumn = focusedIndex % itemsPerRow
        if (currentColumn >= itemsPerRow - 1) {
            return nextPage()
        }

        val newIndex = focusedIndex + 1
        return if (newIndex < displays.size) setFocus(newIndex) else false
    }

    fun getFocusedDisplay(): GameDisplay? {
        return if (focusedIndex >= 0 && focusedIndex < displays.size) displays[focusedIndex] else null
    }

    fun getFocusedIndex(): Int {
        return focusedIndex
    }

    fun getFocusedGameItem(): GameItem? {
        if (focusedIndex < 0 || displays.isEmpty()) return null

        val globalIndex = currentPage * itemsPerPage + focusedIndex
        return if (globalIndex < totalItems) allGameItems[globalIndex] else null
    }
}
