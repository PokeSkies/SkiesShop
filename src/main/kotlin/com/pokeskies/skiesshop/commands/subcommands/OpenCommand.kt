package com.pokeskies.skiesshop.commands.subcommands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.pokeskies.skiesshop.SkiesShopAPI
import com.pokeskies.skiesshop.config.Lang
import com.pokeskies.skiesshop.utils.SubCommand
import com.pokeskies.skiesshop.utils.asAdventure
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class OpenCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("open")
            .requires(Permissions.require("skiesshop.command.open", 2))
            .then(Commands.argument("shop", StringArgumentType.string())
                .suggests { _, builder ->
                    SharedSuggestionProvider.suggest(SkiesShopAPI.getShopIDs().stream(), builder)
                }
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(Permissions.require("skiesshop.command.open.others", 2))
                    .executes { ctx ->
                        openOther(
                            ctx,
                            StringArgumentType.getString(ctx, "shop"),
                            EntityArgument.getPlayer(ctx, "player")
                        )
                    }
                )
                .executes { ctx ->
                    openSelf(
                        ctx,
                        StringArgumentType.getString(ctx, "shop")
                    )
                }
            )
            .build()
    }

    companion object {
        fun openSelf(
            ctx: CommandContext<CommandSourceStack>,
            shop: String,
        ): Int {
            if (!ctx.source.isPlayer) {
                ctx.source.sendSystemMessage(Component.literal("You must be a player to run this command!").withStyle { it.withColor(ChatFormatting.RED) })
                return 1
            }

            return openOther(ctx, shop, ctx.source.playerOrException)
        }

        fun openOther(
            ctx: CommandContext<CommandSourceStack>,
            shopId: String,
            player: ServerPlayer,
        ): Int {
            val shop = SkiesShopAPI.getShop(shopId)
            if (shop == null) {
                Lang.ERROR_SHOP_NOT_FOUND.forEach {
                    ctx.source.sendMessage(it.asAdventure(mapOf("%shop_id%" to shopId)))
                }
                return 1
            }

            shop.open(player)

            ctx.source.sendSystemMessage(Component.literal("Opened shop '$shopId' for ${player.name.string}!").withStyle { it.withColor(ChatFormatting.GREEN) })

            return 1
        }
    }
}
