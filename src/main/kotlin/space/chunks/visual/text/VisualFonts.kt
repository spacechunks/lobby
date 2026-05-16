package space.chunks.visual.text

object VisualFonts {
    object Minecraft {
        val default = VisualFont("minecraft", "default", MinecraftNormalFontMetrics)
        val normal = default
    }

    val normal = Minecraft.normal

    object ChunkExplorer {
        val actionBar = VisualFont("chunkexplorer", "actionbar")
        val bossBarParty = VisualFont("chunkexplorer", "bossbar/party")
        val tabList = VisualFont("chunkexplorer", "tablist")
        val text = VisualFont("chunkexplorer", "text")
        val title = VisualFont("chunkexplorer", "title")
    }

    object SpaceChunksVisualKit {
        val space = VisualFont("spacechunks-visualkit", "space")
        val smallText = VisualFont("spacechunks-visualkit", "text/small-font", SpaceChunksSmallFontMetrics)
        val bossBarFix = VisualFont("spacechunks-visualkit", "bossbar/bossbar-fix")
        val bossBarLine1 = VisualFont("spacechunks-visualkit", "bossbar/bossbar-1", MinecraftRegularBitmapFontMetrics)
        val bossBarLine1Half = VisualFont("spacechunks-visualkit", "bossbar/bossbar-1-5", MinecraftRegularBitmapFontMetrics)
        val bossBarLine2 = VisualFont("spacechunks-visualkit", "bossbar/bossbar-2", MinecraftRegularBitmapFontMetrics)
        val bossBarSmallLine1 = VisualFont("spacechunks-visualkit", "bossbar/bossbar-1", SpaceChunksBossBarSmallFontMetrics)
        val bossBarSmallLine1Half = VisualFont("spacechunks-visualkit", "bossbar/bossbar-1-5", SpaceChunksBossBarSmallFontMetrics)
        val bossBarSmallLine2 = VisualFont("spacechunks-visualkit", "bossbar/bossbar-2", SpaceChunksBossBarSmallFontMetrics)
    }
}
