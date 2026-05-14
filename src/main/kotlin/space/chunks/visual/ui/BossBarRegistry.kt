package space.chunks.visual.ui

class BossBarRegistry {
    private val slots = mutableListOf<BossBarSlot>()

    fun register(key: String, order: Int): BossBarSlot {
        require(slots.none { it.key == key }) {
            "Bossbar slot '$key' is already registered."
        }

        return BossBarSlot(key, order).also {
            slots += it
            slots.sortBy(BossBarSlot::order)
        }
    }

    fun all(): List<BossBarSlot> =
        slots.toList()
}
