package space.chunks.lobby.modules.party

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import space.chunks.lobby.ui.Messages

class PartyCommands {
    companion object {
        fun root(partyService: PartyService, messages: Messages): List<LiteralCommandNode<CommandSourceStack>> {
            val root = Commands.literal("party").executes { ctx ->
                messages.send(ctx.getSource().sender as Player, "messages.party.help")
                Command.SINGLE_SUCCESS
            }

            val invite = Commands.literal("invite").then(
                playerArg().executes { ctx ->
                    val invitees = ctx.getArgument("players", PlayerSelectorArgumentResolver::class.java)
                        .resolve(ctx.getSource())
                    val inviter = ctx.getSource().sender as Player

                    try {
                        invitees.forEach {
                            partyService.invitePlayer(
                                PartyPlayer(inviter.uniqueId, inviter.name),
                                PartyPlayer(it.uniqueId, it.name)
                            )

                            messages.send(
                                inviter,
                                "messages.party.invite.sent",
                                mapOf("target" to messages.player(it.name))
                            )
                        }
                    } catch (ex: PartyException) {
                        if (ex.reason == PartyExceptionReason.INVITER_IS_INVITEE) {
                            messages.send(inviter, "messages.party.invite.self-error")
                        }
                        if (ex.reason == PartyExceptionReason.NOT_OWNER) {
                            messages.send(inviter, "messages.party.invite.not-owner-error")
                        }
                        return@executes Command.SINGLE_SUCCESS
                    }
                    return@executes Command.SINGLE_SUCCESS
                }
            )

            val accept = Commands.literal("accept").then(
                Commands.argument("inviteId", StringArgumentType.string()).executes { ctx ->
                    val id = ctx.getArgument("inviteId", String::class.java)
                    val player = ctx.getSource().sender as Player

                    try {
                        partyService.acceptInvite(id)
                        val party = partyService.getParty(player)
                        if (party == null) {
                            messages.send(player, "messages.party.invite.party-gone-error")
                            return@executes Command.SINGLE_SUCCESS
                        }

                        messages.send(
                            player,
                            "messages.party.invite.accepted",
                            mapOf("owner" to messages.player(party.owner.name))
                        )
                        messages.send(
                            player,
                            "messages.party.list.leader",
                            mapOf("member" to messages.player(party.owner.name))
                        )
                        party.members.forEach { member ->
                            messages.send(
                                player,
                                "messages.party.list.member",
                                mapOf("member" to messages.player(member.name))
                            )
                        }
                    } catch (ex: PartyException) {
                        if (ex.reason == PartyExceptionReason.INVITE_GONE) {
                            messages.send(player, "messages.party.invite.expired-error")
                        }
                        if (ex.reason == PartyExceptionReason.PARTY_GONE) {
                            messages.send(player, "messages.party.invite.party-gone-error")
                        }
                    }

                    return@executes Command.SINGLE_SUCCESS
                },
            )

            val decline = Commands.literal("decline").then(
                Commands.argument("inviteId", StringArgumentType.string()).executes { ctx ->
                    val id = ctx.getArgument("inviteId", String::class.java)
                    val player = ctx.getSource().sender as Player

                    try {
                        partyService.declineInvite(id)
                    } catch (ex: PartyException) {
                        if (ex.reason == PartyExceptionReason.INVITE_GONE) {
                            messages.send(player, "messages.party.invite.expired-error")
                        }
                        if (ex.reason == PartyExceptionReason.PARTY_GONE) {
                            messages.send(player, "messages.party.invite.party-gone-error")
                        }
                        return@executes Command.SINGLE_SUCCESS
                    }

                    messages.send(player, "messages.party.invite.declined-self")

                    return@executes Command.SINGLE_SUCCESS
                },
            )

            val list = Commands.literal("list").executes { ctx ->
                val player = ctx.getSource().sender as Player
                val party = partyService.getParty(player.uniqueId)

                if (party == null) {
                    messages.send(player, "messages.party.list.not-in-party")
                    return@executes Command.SINGLE_SUCCESS
                }

                messages.send(player, "messages.party.list.header")
                messages.send(
                    player,
                    "messages.party.list.leader",
                    mapOf("member" to messages.player(party.owner.name))
                )
                party.members.forEach { member ->
                    messages.send(
                        player,
                        "messages.party.list.member",
                        mapOf("member" to messages.player(member.name))
                    )
                }

                return@executes Command.SINGLE_SUCCESS
            }

            val disband = Commands.literal("disband").executes { ctx ->
                val player = ctx.getSource().sender as Player
                val party = partyService.getParty(player.uniqueId)

                if (party == null) {
                    messages.send(player, "messages.party.list.not-in-party")
                    return@executes Command.SINGLE_SUCCESS
                }

                try {
                    partyService.disbandParty(party.id, player.uniqueId)
                } catch (ex: PartyException) {
                    if (ex.reason == PartyExceptionReason.PARTY_GONE) {
                        messages.send(player, "messages.party.disband.gone-error")
                    }

                    if (ex.reason == PartyExceptionReason.NOT_OWNER) {
                        messages.send(player, "messages.party.disband.not-owner-error")
                    }

                    return@executes Command.SINGLE_SUCCESS
                }

                messages.send(player, "messages.party.disband.self")
                return@executes Command.SINGLE_SUCCESS
            }

            val leave = Commands.literal("leave").executes { ctx ->
                val player = ctx.getSource().sender as Player
                val party = partyService.getParty(player.uniqueId)

                if (party == null) {
                    messages.send(player, "messages.party.list.not-in-party")
                    return@executes Command.SINGLE_SUCCESS
                }

                try {
                    partyService.leaveParty(party.id, player.uniqueId, player.uniqueId)
                } catch (ex: PartyException) {
                    if (ex.reason == PartyExceptionReason.PARTY_GONE) {
                        messages.send(player, "messages.party.leave.gone-error")
                    }
                    return@executes Command.SINGLE_SUCCESS
                }

                messages.send(player, "messages.party.leave.self")

                return@executes Command.SINGLE_SUCCESS
            }

            val kick = Commands.literal("kick").then(
                Commands.argument("player", StringArgumentType.word())
                    .suggests({ ctx, builder ->
                        val party = partyService.getParty((ctx.getSource().sender as Player).uniqueId)
                            ?: return@suggests builder.buildFuture()

                        party.members.stream()
                            .map(PartyPlayer::name)
                            .filter { name -> name.lowercase().startsWith(builder.remainingLowerCase) }
                            .forEach(builder::suggest)

                        return@suggests builder.buildFuture()
                    })
                    .executes { ctx ->
                        val toKick = ctx.getArgument("player", String::class.java)

                        val player = ctx.getSource().sender as Player
                        val party = partyService.getParty(player.uniqueId)

                        if (party == null) {
                            messages.send(player, "messages.party.list.not-in-party")
                            return@executes Command.SINGLE_SUCCESS
                        }

                        try {
                            val p = party.members.find { it.name.equals(toKick, true) }
                            if (p == null) {
                                mm.deserialize("<#DC2626>$toKick not found.")
                                return@executes Command.SINGLE_SUCCESS
                            }

                            partyService.leaveParty(party.id, player.uniqueId, p.id)
                            messages.send(
                                player,
                                "messages.party.kick.self",
                                mapOf("target" to messages.player(toKick.name))
                            )
                        } catch (_: PartyException) {
                            messages.send(player, "messages.party.kick.not-owner-error")
                        }
                        return@executes Command.SINGLE_SUCCESS
                    }
            )

            val chat = Commands.argument("message", StringArgumentType.greedyString())
                .executes { ctx ->
                    val player = ctx.getSource().sender as Player
                    val party = partyService.getParty(player.uniqueId)
                    val msg = ctx.getArgument("message", String::class.java)

                    if (party == null) {
                        player.sendMessage(
                            PartyCommands.mm.deserialize("<#DC2626>You are not part of a party.")
                        )
                        return@executes Command.SINGLE_SUCCESS
                    }

                    party.sendMessage(Component.text("PARTY > ").append(Component.text(msg)))
                    return@executes Command.SINGLE_SUCCESS
                }


            root.then(invite)
            root.then(accept)
            root.then(decline)
            root.then(list)
            root.then(disband)
            root.then(leave)
            root.then(kick)
            root.then(chat)

            val p = Commands.literal("p")
            p.then(invite)
            p.then(accept)
            p.then(decline)
            p.then(list)
            p.then(disband)
            p.then(leave)
            p.then(kick)
            p.then(chat)

            return listOf(root.build(), p.build())
        }
    }
}

fun playerArg(): RequiredArgumentBuilder<CommandSourceStack, PlayerSelectorArgumentResolver> {
    return Commands.argument("players", ArgumentTypes.players())
        .suggests { ctx, builder ->
            Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter { name -> name.lowercase().startsWith(builder.remainingLowerCase) }
                .forEach(builder::suggest)
            builder.buildFuture()
        }
}
