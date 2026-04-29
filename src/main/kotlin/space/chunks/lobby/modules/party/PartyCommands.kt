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

class PartyCommands {
    companion object {
        val mm = MiniMessage.miniMessage()

        fun root(partyService: PartyService): List<LiteralCommandNode<CommandSourceStack>> {
            val invite = Commands.literal("invite").then(
                playerArg().executes { ctx ->
                    val invitees = ctx.getArgument("players", PlayerSelectorArgumentResolver::class.java)
                        .resolve(ctx.getSource())
                    val inviter = ctx.getSource().sender as Player

                    try {
                        invitees.forEach {
                            partyService.invitePlayer(inviter, it)
                            inviter.sendMessage(
                                mm.deserialize(
                                    "<#E2E8F0>You invited <white><head:${it.name}:true> <#7c3aed>${it.name} <#E2E8F0>to your party."
                                )
                            )
                        }
                    } catch (ex: PartyException) {
                        if (ex.reason == PartyExceptionReason.INVITER_IS_INVITEE) {
                            inviter.sendMessage(
                                mm.deserialize("<#DC2626>You cannot invite yourself to your own party.")
                            )
                        }
                        if (ex.reason == PartyExceptionReason.NOT_OWNER) {
                            inviter.sendMessage(
                                mm.deserialize("<#DC2626>You must be the party owner to invite players.")
                            )
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
                        player.sendMessage(
                            mm.deserialize("<#A3E635>You joined the party.")
                        )
                    } catch (ex: PartyException) {
                        if (ex.reason == PartyExceptionReason.INVITE_GONE) {
                            player.sendMessage(
                                mm.deserialize("<#DC2626>This party invite has already expired.")
                            )
                        }
                        if (ex.reason == PartyExceptionReason.PARTY_GONE) {
                            player.sendMessage(
                                mm.deserialize("<#DC2626>The party no longer exists.")
                            )
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
                            player.sendMessage(
                                mm.deserialize("<#DC2626>This party invite has already expired.")
                            )
                        }
                        if (ex.reason == PartyExceptionReason.PARTY_GONE) {
                            player.sendMessage(
                                mm.deserialize("<#DC2626>The party no longer exists.")
                            )
                        }
                        return@executes Command.SINGLE_SUCCESS
                    }

                    player.sendMessage(
                        mm.deserialize("<#F59E0B>You declined the party invite.")
                    )

                    return@executes Command.SINGLE_SUCCESS
                },
            )

            val list = Commands.literal("list").executes { ctx ->
                val player = ctx.getSource().sender as Player
                val party = partyService.getParty(player)

                if (party == null) {
                    player.sendMessage(
                        mm.deserialize("<#DC2626>You are not part of a party.")
                    )
                    return@executes Command.SINGLE_SUCCESS
                }

                player.sendMessage(
                    mm.deserialize("<#94A3B8>Party Members")
                )

                player.sendMessage(
                    mm.deserialize("   <#94A3B8>Leader<#475569>: <white><head:${party.owner.name}:true> <#7c3aed>${party.owner.name}")
                )

                player.sendMessage(
                    mm.deserialize("   <#94A3B8>Members")
                )

                party.members.forEach { member ->
                    player.sendMessage(
                        mm.deserialize("      <#475569>- <white><head:${member.name}:true> <#7c3aed>${member.name}")
                    )
                }

                return@executes Command.SINGLE_SUCCESS
            }

            val disband = Commands.literal("disband").executes { ctx ->
                val player = ctx.getSource().sender as Player
                val party = partyService.getParty(player)

                if (party == null) {
                    player.sendMessage(
                        mm.deserialize("<#DC2626>You are not part of a party.")
                    )
                    return@executes Command.SINGLE_SUCCESS
                }

                try {
                    partyService.disbandParty(party.id, player)
                } catch (ex: PartyException) {
                    if (ex.reason == PartyExceptionReason.PARTY_GONE) {
                        player.sendMessage(
                            mm.deserialize("<#DC2626>The party is already gone.")
                        )
                    }

                    if (ex.reason == PartyExceptionReason.NOT_OWNER) {
                        player.sendMessage(
                            mm.deserialize("<#DC2626>You must be the party owner to disband the party.")
                        )
                    }

                    return@executes Command.SINGLE_SUCCESS
                }

                player.sendMessage(
                    mm.deserialize("<#F59E0B>The party has been disbanded.")
                )
                return@executes Command.SINGLE_SUCCESS
            }

            val leave = Commands.literal("leave").executes { ctx ->
                val player = ctx.getSource().sender as Player
                val party = partyService.getParty(player)

                if (party == null) {
                    player.sendMessage(
                        mm.deserialize("<#DC2626>You are not part of a party.")
                    )
                    return@executes Command.SINGLE_SUCCESS
                }

                try {
                    partyService.leaveParty(party.id, player, player)
                } catch (ex: PartyException) {
                    if (ex.reason == PartyExceptionReason.PARTY_GONE) {
                        player.sendMessage(
                            mm.deserialize("<#DC2626>The party is already gone.")
                        )
                    }
                    return@executes Command.SINGLE_SUCCESS
                }

                player.sendMessage(
                    mm.deserialize("<#F59E0B>You left the party.")
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
                            .filter { name -> name.lowercase().startsWith(builder.remainingLowerCase) }
                            .forEach(builder::suggest)

                        return@suggests builder.buildFuture()
                    })
                    .executes { ctx ->
                        val toKick = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
                            .resolve(ctx.getSource()).first()

                        val player = ctx.getSource().sender as Player
                        val party = partyService.getParty(player)

                        if (party == null) {
                            player.sendMessage(
                                mm.deserialize("<#DC2626>You are not part of a party.")
                            )
                            return@executes Command.SINGLE_SUCCESS
                        }

                        try {
                            partyService.leaveParty(party.id, player, toKick)
                            player.sendMessage(
                                mm.deserialize("<#E2E8F0>You removed <white><head:${toKick.name}:true> <#7c3aed>${toKick.name} <#E2E8F0>from the party.")
                            )
                        } catch (_: PartyException) {
                            player.sendMessage(
                                mm.deserialize("<#DC2626>You must be the party owner to kick a player.")
                            )
                        }
                        return@executes Command.SINGLE_SUCCESS
                    }
            )

            val chat = Commands.argument("message", StringArgumentType.greedyString())
                .executes { ctx ->
                    val player = ctx.getSource().sender as Player
                    val party = partyService.getParty(player)
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


            val root = Commands.literal("party")
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