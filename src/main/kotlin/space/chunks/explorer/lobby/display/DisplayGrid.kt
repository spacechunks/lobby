package space.chunks.explorer.lobby.display

import org.bukkit.Bukkit
import org.bukkit.Location
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class DisplayGrid(
    private val centerLocation: Location,
    private val itemsPerRow: Int,
    private val plugin: org.bukkit.plugin.Plugin,
    private val itemsPerPage: Int = itemsPerRow * 4,
    private val spacing: Double = 3.8
) {
    private val displays = mutableListOf<ChunkDisplay>()
    private var focusedIndex: Int = -1

    private var currentPage: Int = 0
    private var totalItems: Int = 0
    private var allGameItems = mutableListOf<ChunkDisplay>()

    fun getTotalPages(): Int {
        return max(1, ceil(totalItems.toDouble() / itemsPerPage).toInt())
    }

    fun getCurrentPage(): Int {
        return currentPage + 1
    }

    fun setAllItems(items: List<ChunkDisplay>) {
        allGameItems.clear()
        allGameItems.addAll(items)
        totalItems = items.size
        refreshCurrentPage()
    }

    fun addItem(item: ChunkDisplay, refreshDisplay: Boolean = true) {
        allGameItems.add(item)
        totalItems++
        if (refreshDisplay) {
            refreshCurrentPage()
        }
    }

    fun refreshCurrentPage() {
        val startIndex = currentPage * itemsPerPage
        val endIndex = min(startIndex + itemsPerPage, totalItems)

        val new = mutableListOf<ChunkDisplay>()

        for (i in startIndex until endIndex) {
            val item = allGameItems[i]
            val position = calculatePositionInPage(i - startIndex)

            item.location = position
            item.center = this.centerLocation
            item.spawn()
            new.add(item)
        }

        Bukkit.getScheduler().runTaskLater(this.plugin, { _ ->
            clear()
            this.displays.addAll(new)
            if (displays.isNotEmpty()) {
                setInitialFocus()
            }

        }, 2L)
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

    fun clear() {
        displays.forEach {
            it.setFocus(false)
            it.remove()
        }
        displays.clear()
        focusedIndex = -1
    }

//    fun clearAll() {
//        clear()
//        allGameItems.clear()
//        totalItems = 0
//        currentPage = 0
//    }

    fun setInitialFocus(): Boolean {
        return setFocus(0)
    }

    fun setFocus(index: Int): Boolean {
        if (index < 0 || index >= displays.size) return false
        if (focusedIndex == index) return true

        if (focusedIndex >= 0 && focusedIndex < displays.size) {
            displays[focusedIndex].setFocus(false, plugin = this.plugin)
        }

        focusedIndex = index
        displays[focusedIndex].setFocus(true, plugin = this.plugin)
        return true
    }

    fun moveFocusUp(): Boolean {
        if (focusedIndex < 0) return setInitialFocus()

        val currentRow = focusedIndex / itemsPerRow
        if (currentRow <= 0) {
            return previousPage()
        }

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

//        val currentRow = focusedIndex / itemsPerRow
//        if (currentRow <= 0) {
//            return previousPage()
//        }

        return setFocus(focusedIndex - 1)
    }

    fun moveFocusRight(): Boolean {
        if (focusedIndex < 0) return setInitialFocus()

//        val currentRow = focusedIndex / itemsPerRow
//        if (currentRow >= itemsPerPage / itemsPerRow) {
//            return nextPage()
//        }

        val newIndex = focusedIndex + 1
        return if (newIndex < displays.size) setFocus(newIndex) else false
    }

    fun getFocusedDisplay(): ChunkDisplay? {
        return if (focusedIndex >= 0 && focusedIndex < displays.size) displays[focusedIndex] else null
    }

    fun getFocusedIndex(): Int {
        return focusedIndex
    }

    fun getFocusedGameItem(): ChunkDisplay? {
        if (focusedIndex < 0 || displays.isEmpty()) return null

        val globalIndex = currentPage * itemsPerPage + focusedIndex
        return if (globalIndex < totalItems) allGameItems[globalIndex] else null
    }
}
