package com.pokeskies.skiesshop.commands

import com.mojang.brigadier.CommandDispatcher
import com.pokeskies.skiesshop.SkiesShopAPI
import com.pokeskies.skiesshop.config.ConfigManager
import com.pokeskies.skiesshop.config.Lang
import com.pokeskies.skiesshop.utils.asAdventure
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class ShopCommands {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack?>) {
        for ((id, shopConfig) in ConfigManager.SHOPS) {
            for (alias in shopConfig.aliasCommands) {
                dispatcher.register(Commands.literal(alias.command)
                    .requires { obj: CommandSourceStack -> obj.isPlayer }
                    .requires { alias.hasPermission(it) }
                    .executes { ctx ->
                        val player = ctx.source.player
                        if (player == null) {
                            ctx.source.sendMessage(
                                Component.text("Must be a player to run this command!")
                                    .color(NamedTextColor.RED)
                            )
                            return@executes 1
                        }

                        if (!alias.hasPermission(player.createCommandSourceStack())) {
                            ctx.source.sendMessage(
                                Component.text("You don't have permission to run this command!")
                                    .color(NamedTextColor.RED)
                            )
                            return@executes 1
                        }

                        val shop = SkiesShopAPI.getShop(id)
                        if (shop == null) {
                            Lang.ERROR_SHOP_NOT_FOUND.forEach {
                                ctx.source.sendMessage(it.asAdventure(mapOf("%shop_id%" to id)))
                            }
                            return@executes 1
                        }

                        shop.open(player)
                        return@executes 1
                    }
                )
            }
        }
    }
}
