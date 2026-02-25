package com.pokeskies.skiesshop.commands.subcommands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.gui.TransactionsGUI
import com.pokeskies.skiesshop.logging.LoggerManager
import com.pokeskies.skiesshop.utils.SubCommand
import com.pokeskies.skiesshop.utils.Utils
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.network.chat.Component
import java.util.*

class TransactionsCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("transactions")
            .requires(Permissions.require("skiesshop.command.transactions", 2))
            .then(Commands.argument("player", StringArgumentType.string())
                .suggests { _, builder ->
                    SharedSuggestionProvider.suggest(SkiesShop.INSTANCE.server.playerList.players.map { it.name.string }, builder)
                }
                .executes(::execute)
            )
            .build()
    }

    companion object {
        fun execute(
            ctx: CommandContext<CommandSourceStack>
        ): Int {
            val viewer = ctx.source.player ?: run {
                ctx.source.sendSystemMessage(Component.literal("You must be a player to run this command!").withStyle { it.withColor(ChatFormatting.RED) })
                return 0
            }

            val target = StringArgumentType.getString(ctx, "player")

            var uuid: UUID? = try {
                UUID.fromString(target)
            } catch (_: IllegalArgumentException) {
                null
            }

            if (uuid == null) {
                val player = SkiesShop.INSTANCE.server.playerList.players.find { it.name.string.equals(target, ignoreCase = true) }
                if (player == null) {
                    ctx.source.sendSystemMessage(Component.literal("Player '$target' not found online! Offline lookups require using a UUID").withStyle { it.withColor(ChatFormatting.RED) })
                    return 0
                }
                uuid = player.uuid
            }

            if (!LoggerManager.canListLogs()) {
                ctx.source.sendSystemMessage(Component.literal("The current logger does not support listing transactions!").withStyle { it.withColor(ChatFormatting.RED) })
                return 0
            }

            ctx.source.sendSystemMessage(Component.literal("Fetching transactions for ${target}...").withStyle(ChatFormatting.GREEN))

            LoggerManager.getUserTransactions(uuid).thenAccept { transactions ->
                if (transactions.isEmpty()) {
                    ctx.source.sendSystemMessage(Component.literal("No transactions found for player ${target}!").withStyle(ChatFormatting.YELLOW))
                    return@thenAccept
                }

                TransactionsGUI(viewer, uuid, transactions.reversed()).open()
            }.exceptionally {
                Utils.printError("An error occurred while fetching transactions for player ${target}!")
                ctx.source.sendSystemMessage(Component.literal("An error occurred while fetching transactions for player ${target}!").withStyle(ChatFormatting.RED))
                return@exceptionally null
            }

            return 1
        }
    }
}
