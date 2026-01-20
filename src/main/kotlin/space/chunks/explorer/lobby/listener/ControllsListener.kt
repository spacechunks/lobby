package space.chunks.explorer.lobby.listener

import net.kyori.adventure.text.Component
import org.bukkit.Input
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.plugin.Plugin
import space.chunks.explorer.lobby.display.DisplayGrid
import space.chunks.explorer.lobby.display.GameDisplay

/**
 * Listener for player input events to navigate the display grid
 */
class ControllsListener(
    private val plugin: Plugin,
    private val displayGrid: DisplayGrid
) : Listener {

    private val lastInputState = mutableMapOf<String, Boolean>()

    @EventHandler
    fun onPlayerInput(event: PlayerInputEvent) {
        val player = event.player
        val input = event.input

        if (displayGrid.getFocusedIndex() == -1) {
            displayGrid.setInitialFocus()
        }

        handleDirectionalInput(player, "forward", input.isForward()) { displayGrid.moveFocusUp() }
        handleDirectionalInput(player, "backward", input.isBackward()) { displayGrid.moveFocusDown() }
        handleDirectionalInput(player, "left", input.isLeft()) { displayGrid.moveFocusLeft() }
        handleDirectionalInput(player, "right", input.isRight()) { displayGrid.moveFocusRight() }

        if (player.isSneaking) {
            handleDirectionalInput(player, "sneak_left", input.isLeft()) { displayGrid.previousPage() }
            handleDirectionalInput(player, "sneak_right", input.isRight()) { displayGrid.nextPage() }
        }

        if (displayGrid.getFocusedDisplay() != null) {
            val gameItem = displayGrid.getFocusedGameItem()
            if (gameItem != null) {
                player.sendActionBar(Component.text(
                    "Page ${displayGrid.getCurrentPage() + 1}/${displayGrid.getTotalPages()} - " +
                    "Game: ${gameItem.gameId} - " +
                    "Players: ${gameItem.playerCount}/${gameItem.maxPlayers} - " +
                    "Status: ${gameItem.status}"
                ))
            }
        }
    }

    private fun handleDirectionalInput(
        p: Player,
        inputName: String, 
        currentState: Boolean, 
        action: () -> Boolean
    ) {
        val inputKey = "${p.uniqueId}:$inputName"
        val previousState = lastInputState[inputKey] ?: false

        if (currentState && !previousState) {
            p.playSound(p.location, "spacechunks.explorer.chunk_select.click", 0.5f, 1f)
            action()
        }

        lastInputState[inputKey] = currentState
    }
}
