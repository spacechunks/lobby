package space.chunks.lobby.extensions

fun String.parseAddress(): Pair<String, Int> {
    val parts = this.split(":")

    if (parts.size < 2) {
        throw RuntimeException("$this is not a invalid address")
    }

    return Pair(parts[0], parts[1].toInt())
}