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
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PartyCommands {
    companion object {
        fun root(partyService: PartyService): LiteralCommandNode<CommandSourceStack> {
            val root = Commands.literal("party")

            val invite = Commands.literal("invite").then(
                playerArg().executes { ctx ->
                    val invitee = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                        .resolve(ctx.getSource()).first()
                    val inviter = ctx.getSource().sender as Player

                    try {
                        partyService.invitePlayer(inviter, invitee)
                    } catch (ex: PartyException) {
                        if (ex.reason == PartyExceptionReason.INVITER_IS_INVITEE) {
                            inviter.sendMessage("You cannot invite yourself to your own party.")
                        }
                        if (ex.reason == PartyExceptionReason.NOT_OWNER) {
                            inviter.sendMessage("You have to be owner of the party to invite someone.")
                        }
                    }

                    inviter.sendMessage("You have invited ${invitee.name} to your party.")
                    return@executes Command.SINGLE_SUCCESS
                }
            )

            val accept = Commands.literal("accept").then(
                Commands.argument("inviteId", StringArgumentType.string()).executes { ctx ->
                    val id = ctx.getArgument("inviteId", String::class.java)
                    val player = ctx.getSource().sender as Player

                    try {
                        partyService.acceptInvite(id)
                    } catch (ex: PartyException) {
                        if (ex.reason == PartyExceptionReason.INVITE_GONE) {
                            player.sendMessage("Invite has already expired")
                        }
                        if (ex.reason == PartyExceptionReason.PARTY_GONE) {
                            player.sendMessage("The party does not longer exist :/")
                        }
                    }

                    return@executes Command.SINGLE_SUCCESS
                },
            )

            val decline = Commands.literal("decline").then(
                Commands.argument("inviteId", StringArgumentType.string()).executes { ctx ->
                    val id = ctx.getArgument("inviteId", String::class.java)
                    val player = ctx.getSource().sender as Player

                    partyService.declineInvite(id)

                    player.sendMessage("Party invite declined.")

                    return@executes Command.SINGLE_SUCCESS
                },
            )

            val list = Commands.literal("list").executes { ctx ->
                val player = ctx.getSource().sender as Player
                val party = partyService.getParty(player)

                if (party == null) {
                    player.sendMessage("Your are not part of a party.")
                    return@executes Command.SINGLE_SUCCESS
                }

                player.sendMessage("Party members:")
                player.sendMessage(party.owner.name)
                party.members.forEach { member ->
                    player.sendMessage(member.name)
                }

                return@executes Command.SINGLE_SUCCESS
            }

            val disband = Commands.literal("disband").executes { ctx ->
                val player = ctx.getSource().sender as Player
                val party = partyService.getParty(player)

                if (party == null) {
                    player.sendMessage("You are not part of a party.")
                    return@executes Command.SINGLE_SUCCESS
                }

                try {
                    partyService.disbandParty(party.id, player)
                } catch (ex: PartyException) {

                    if (ex.reason == PartyExceptionReason.PARTY_GONE) {
                        player.sendMessage(
                            Component
                                .text("Party is already gone")
                                .color(NamedTextColor.RED)
                        )
                    }

                    if (ex.reason == PartyExceptionReason.NOT_OWNER) {
                        player.sendMessage(
                            Component
                                .text("You must be owner of the party to disband it.")
                                .color(NamedTextColor.RED)
                        )
                    }

                    return@executes Command.SINGLE_SUCCESS
                }

                player.sendMessage("Party disbanded.")
                return@executes Command.SINGLE_SUCCESS
            }

            val leave = Commands.literal("leave").executes { ctx ->
                val player = ctx.getSource().sender as Player
                val party = partyService.getParty(player)

                if (party == null) {
                    player.sendMessage("You are not part of a party.")
                    return@executes Command.SINGLE_SUCCESS
                }

                try {
                    partyService.leaveParty(party.id, player, player)
                } catch (ex: PartyException) {
                    if (ex.reason == PartyExceptionReason.PARTY_GONE) {
                        player.sendMessage(
                            Component
                                .text("Party is already gone")
                                .color(NamedTextColor.RED)
                        )
                    }

                    if (ex.reason == PartyExceptionReason.NOT_OWNER) {
                        player.sendMessage(
                            Component
                                .text("ADAWDAWDAWDAW")
                                .color(NamedTextColor.RED)
                        )
                    }
                    return@executes Command.SINGLE_SUCCESS
                }

                player.sendMessage(
                    Component
                        .text("You have left the party")
                        .color(NamedTextColor.RED)
                )

                return@executes Command.SINGLE_SUCCESS
            }

            val kick = Commands.literal("kick").then(
            Commands.argument("player", ArgumentTypes.player())
                .suggests({ ctx, builder ->
                    val party = partyService.getParty(ctx.getSource().sender as Player)
                        ?: return@suggests builder.buildFuture()

                    party.members.stream()
                        .map(Player::getName)
                        .filter({ name -> name.lowercase().startsWith(builder.remainingLowerCase) })
                        .forEach(builder::suggest);

                    return@suggests builder.buildFuture();
                })
                .executes { ctx ->
                    val toKick = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                        .resolve(ctx.getSource()).first()

                val player = ctx.getSource().sender as Player
                val party = partyService.getParty(player)

                if (party == null) {
                    player.sendMessage("You are not part of a party.")
                    return@executes Command.SINGLE_SUCCESS
                }

                    try {
                        partyService.leaveParty(party.id, player, toKick)
                    } catch (_: PartyException) {
                        player.sendMessage("You must be the owner of the party to kick someone.")
                    }
                    return@executes Command.SINGLE_SUCCESS
            }
            )

            root.then(invite)
            root.then(accept)
            root.then(decline)
            root.then(list)
            root.then(disband)
            root.then(leave)
            root.then(kick)
            return root.build()
        }
    }
}

fun playerArg(): RequiredArgumentBuilder<CommandSourceStack, PlayerSelectorArgumentResolver> {
    return Commands.argument("player", ArgumentTypes.player())
        .suggests({ ctx, builder ->
            Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter({ name -> name.lowercase().startsWith(builder.remainingLowerCase) })
                .forEach(builder::suggest);
            return@suggests builder.buildFuture();
        })
}