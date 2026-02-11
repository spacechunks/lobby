package space.chunks.explorer.lobby.display

class PaginatedList<T>(
    val items: List<T>,
    val pageSize: Int
) {
    init {
        require(pageSize > 0) { "pageSize must be greater than 0" }
    }

    val totalPages: Int get() = (items.size + pageSize - 1) / pageSize

    fun getPage(pageIndex: Int): List<T> {
        require(pageIndex in 0 until totalPages) {
            "pageIndex must be between 0 and ${totalPages - 1}"
        }

        val fromIndex = pageIndex * pageSize
        val toIndex = minOf(fromIndex + pageSize, items.size)

        return items.subList(fromIndex, toIndex)
    }
}