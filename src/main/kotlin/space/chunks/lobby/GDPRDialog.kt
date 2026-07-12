package space.chunks.lobby

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class GDPRDialog {
    fun show(audience: Audience): CompletableFuture<Boolean> {
        val f = CompletableFuture<Boolean>()
        val d = Dialog.create { builder ->
            builder.empty()
                .type(
                    DialogType.notice(
                    ActionButton.builder(Component.text("Ok"))
                        .action(DialogAction.customClick({ view, aud ->
                            val accepted = view.getBoolean("gdpr_accept") ?: return@customClick
                            f.complete(accepted)
                        }, ClickCallback.Options.builder().uses(1).lifetime(30.minutes.toJavaDuration()).build()))
                        .build()
                ))
                .base(
                    DialogBase.builder(Component.text("Privacy Policy"))
                        .body(listOf(
                            DialogBody.plainMessage(
                                Component.text("To play on this server you need to accept our privacy policy.")
                                    .color(NamedTextColor.RED)
                            ),
                            DialogBody.plainMessage(Component.text("-> https://chunks.space/privacy")))
                        )
                        .inputs(listOf(
                            DialogInput.bool("gdpr_accept", Component.text("I have read and accepted the privacy policy."))
                                .build()
                        ))
                        .build())
        }
        audience.showDialog(d)
        return f
    }
}