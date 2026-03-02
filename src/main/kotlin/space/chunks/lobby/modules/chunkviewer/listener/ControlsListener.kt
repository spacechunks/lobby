package space.chunks.lobby.modules.chunkviewer.listener

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInputEvent
import space.chunks.lobby.modules.chunkviewer.display.DisplaySessionService
import space.chunks.lobby.modules.chunkviewer.display.Input

/**
 * Listener for player input events to navigate the display grid
 */
class ControlsListener(private val sessionService: DisplaySessionService) : Listener {

    private val lastInputState = mutableMapOf<String, Boolean>()

    @EventHandler
    fun onPlayerInput(event: PlayerInputEvent) {
        val player = event.player
        val input = event.input

        // TODO: clean map entries when player leaves

        handleDirectionalInput(player, "forward", input.isForward()) {
            this.sessionService.getSession(player)?.handleInput(Input.W)
        }

        handleDirectionalInput(player, "backward", input.isBackward()) {
            this.sessionService.getSession(player)?.handleInput(Input.S)
        }

        handleDirectionalInput(player, "left", input.isLeft()) {
            this.sessionService.getSession(player)?.handleInput(Input.A)
        }

        handleDirectionalInput(player, "right", input.isRight()) {
            this.sessionService.getSession(player)?.handleInput(Input.D)
        }

        handleDirectionalInput(player, "jump", input.isJump()) {
            this.sessionService.getSession(player)?.handleInput(Input.SPACE)
        }

        handleDirectionalInput(player, "sneak", input.isSneak()) {
            this.sessionService.getSession(player)?.handleInput(Input.SNEAK)
        }
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
