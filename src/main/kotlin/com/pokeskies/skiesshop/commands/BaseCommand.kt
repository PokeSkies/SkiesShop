package com.pokeskies.skiesshop.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.SkiesShopAPI
import com.pokeskies.skiesshop.commands.subcommands.DebugCommand
import com.pokeskies.skiesshop.commands.subcommands.OpenCommand
import com.pokeskies.skiesshop.commands.subcommands.ReloadCommand
import com.pokeskies.skiesshop.commands.subcommands.TransactionsCommand
import com.pokeskies.skiesshop.config.ConfigManager
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

class BaseCommand {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val rootCommands: List<LiteralCommandNode<CommandSourceStack>> = ConfigManager.CONFIG.commands.map {
            Commands.literal(it)
                .requires(Permissions.require("${SkiesShop.MOD_ID}.command.base", 2))
                .executes(::execute)
                .build()
        }

        val subCommands: List<LiteralCommandNode<CommandSourceStack>> = listOf(
            ReloadCommand().build(),
            DebugCommand().build(),
            OpenCommand().build(),
            TransactionsCommand().build(),
        )

        rootCommands.forEach { root ->
            subCommands.forEach { sub -> root.addChild(sub) }
            dispatcher.root.addChild(root)
        }
    }

    companion object {
        fun execute(
            ctx: CommandContext<CommandSourceStack>
        ): Int {
            if (!ctx.source.isPlayer) {
                ctx.source.sendSystemMessage(Component.literal("You must be a player to run this command!").withStyle { it.withColor(ChatFormatting.RED) })
                return 0
            }

            if (ConfigManager.CONFIG.baseShop.isEmpty()) {
                ctx.source.sendSystemMessage(Component.literal("No base shop is set in the config!").withStyle { it.withColor(ChatFormatting.RED) })
                return 0
            }

            val shop = SkiesShopAPI.getShop(ConfigManager.CONFIG.baseShop)
            if (shop == null) {
                ctx.source.sendSystemMessage(Component.literal("Could not find the shop '$ConfigManager.CONFIG.baseShop'!").withStyle { it.withColor(ChatFormatting.RED) })
                return 0
            }

            shop.open(ctx.source.playerOrException)

            return 1
        }
    }
}
