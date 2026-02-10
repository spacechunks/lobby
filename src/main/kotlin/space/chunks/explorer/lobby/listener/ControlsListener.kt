package space.chunks.explorer.lobby.listener

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.plugin.Plugin
import space.chunks.explorer.lobby.display.DisplaySession
import space.chunks.explorer.lobby.display.Input

/**
 * Listener for player input events to navigate the display grid
 */
class ControlsListener(
    private val plugin: Plugin,
    private val sessions: MutableMap<Player, DisplaySession>,
) : Listener {

    private val lastInputState = mutableMapOf<String, Boolean>()

    @EventHandler
    fun onPlayerInput(event: PlayerInputEvent) {
        val player = event.player
        val input = event.input

        handleDirectionalInput(player, "forward", input.isForward()) {
            this.sessions[player]?.handleInput(Input.W)
        }

        handleDirectionalInput(player, "backward", input.isBackward()) {
            this.sessions[player]?.handleInput(Input.S)
        }

        handleDirectionalInput(player, "left", input.isLeft()) {
            this.sessions[player]?.handleInput(Input.A)
        }

        handleDirectionalInput(player, "right", input.isRight()) {
            this.sessions[player]?.handleInput(Input.D)
        }

        handleDirectionalInput(player, "jump", input.isJump()) {
            this.sessions[player]?.handleInput(Input.SPACE)
        }

//        handleDirectionalInput(player, "forward", input.isForward()) { displayGrid.moveFocusUp() }
//        handleDirectionalInput(player, "backward", input.isBackward()) { displayGrid.moveFocusDown() }
//        handleDirectionalInput(player, "left", input.isLeft()) { displayGrid.moveFocusLeft() }
//        handleDirectionalInput(player, "right", input.isRight()) { displayGrid.moveFocusRight() }
//
//        if (player.isSneaking) {
//            handleDirectionalInput(player, "sneak_left", input.isLeft()) { displayGrid.previousPage() }
//            handleDirectionalInput(player, "sneak_right", input.isRight()) { displayGrid.nextPage() }
//        }
    }

    private fun handleDirectionalInput(
        p: Player,
        inputName: String,
        currentState: Boolean,
        action: () -> Unit
    ) {
        val inputKey = "${p.uniqueId}:$inputName"
        val previousState = lastInputState[inputKey] ?: false

        if (currentState && !previousState) {
            action()
        }

        lastInputState[inputKey] = currentState
    }
}
