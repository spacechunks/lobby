package space.chunks.lobby.modules.spawn

import com.noxcrew.interfaces.InterfacesConstants
import com.noxcrew.interfaces.InterfacesListeners
import com.noxcrew.interfaces.click.ClickHandler
import com.noxcrew.interfaces.drawable.Drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildPlayerInterface
import kotlinx.coroutines.launch
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService
import space.chunks.lobby.pack.Items
import space.chunks.lobby.ui.ActionBar
import space.chunks.lobby.ui.ScreenTransition
import space.chunks.lobby.ui.Texts
import space.chunks.visual.ui.UiService

class Hotbar(
    private val sessionService: DisplaySessionService,
    private val texts: Texts,
    private val uiService: UiService,
    private val transition: ScreenTransition,
) {

    private val hotbarInterface = buildPlayerInterface {
        withTransform { pane, _ ->
            pane.hotbar[0] = StaticElement(
                Drawable.drawable(
                    ItemStack(Material.PAPER).apply {
                        editMeta {
                            it.itemModel = Items.TELEPORTER
                            it.displayName(texts.component("spawn.hotbar.teleporter"))
                        }
                    }
                ),
                clickHandler = ClickHandler { context ->
                    completingLater = true
                    this@Hotbar.openTeleporter(context.player) {
                        complete()
                    }
                }
            )

            pane.hotbar[1] = StaticElement(
                Drawable.drawable(
                    ItemStack(Material.PAPER).apply {
                        editMeta {
                            it.itemModel = Items.MAP
                            it.displayName(texts.component("spawn.hotbar.warps"))
                        }
                    }
                )
            )

            pane.hotbar[2] = StaticElement(
                Drawable.drawable(
                    ItemStack(Material.PAPER).apply {
                        editMeta {
                            it.itemModel = Items.INVENTORY
                            it.displayName(texts.component("spawn.hotbar.inventory"))
                        }
                    }
                )
            )

            pane.hotbar[3] = StaticElement(
                Drawable.drawable(
                    ItemStack(Material.PAPER).apply {
                        editMeta {
                            it.itemModel = Items.PARTY
                            it.displayName(texts.component("spawn.hotbar.party"))
                        }
                    }
                )
            )

            pane.hotbar[4] = StaticElement(
                Drawable.drawable(
                    ItemStack(Material.PAPER).apply {
                        editMeta {
                            it.itemModel = Items.SETTINGS
                            it.displayName(texts.component("spawn.hotbar.settings"))
                        }
                    }
                )
            )
        }
    }

    fun open(player: Player) {
        InterfacesConstants.SCOPE.launch {
            hotbarInterface.open(player)
        }
    }

    fun close(player: Player) {
        InterfacesListeners.INSTANCE.getOpenPlayerInterface(player.uniqueId)
            ?.close(InterfacesConstants.SCOPE, InventoryCloseEvent.Reason.PLUGIN)
    }

    private fun openTeleporter(player: Player, onComplete: () -> Unit) {
        this.transition.fadeToBlack(player) {
            this.close(player)
            onComplete()

            this.uiService.hide(player)
            ActionBar.clear(player)

            this.sessionService.startSession(player)
        }
    }
}
