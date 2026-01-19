package space.chunks.explorer.lobby.listener

import net.kyori.adventure.text.Component
import org.bukkit.Input
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
        val playerId = player.uniqueId.toString()

        if (displayGrid.getFocusedIndex() == -1) {
            displayGrid.setInitialFocus()
        }

        handleDirectionalInput(playerId, "forward", input.isForward()) { displayGrid.moveFocusUp() }
        handleDirectionalInput(playerId, "backward", input.isBackward()) { displayGrid.moveFocusDown() }
        handleDirectionalInput(playerId, "left", input.isLeft()) { displayGrid.moveFocusLeft() }
        handleDirectionalInput(playerId, "right", input.isRight()) { displayGrid.moveFocusRight() }

        if (player.isSneaking) {
            handleDirectionalInput(playerId, "sneak_left", input.isLeft()) { displayGrid.previousPage() }
            handleDirectionalInput(playerId, "sneak_right", input.isRight()) { displayGrid.nextPage() }
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
        playerId: String, 
        inputName: String, 
        currentState: Boolean, 
        action: () -> Boolean
    ) {
        val inputKey = "$playerId:$inputName"
        val previousState = lastInputState[inputKey] ?: false

        if (currentState && !previousState) {
            action()
        }

        lastInputState[inputKey] = currentState
    }
}
