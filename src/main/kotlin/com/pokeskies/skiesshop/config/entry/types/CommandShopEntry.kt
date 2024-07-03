package com.pokeskies.skiesshop.config.entry.types

import com.pokeskies.skiesshop.config.ItemConfig
import com.pokeskies.skiesshop.config.PriceConfig
import com.pokeskies.skiesshop.config.entry.ShopEntry
import com.pokeskies.skiesshop.config.entry.ShopEntryType
import net.minecraft.server.level.ServerPlayer

class CommandShopEntry(
    type: ShopEntryType = ShopEntryType.ITEM,
    display: ItemConfig = ItemConfig(),
    slot: Int? = null,
    page: Int = 1,
    buy: PriceConfig? = null,
    sell: PriceConfig? = null,
    private val commands: List<String> = emptyList(),
    private val asPlayer: Boolean = false,
) : ShopEntry() {
    override fun buy(player: ServerPlayer, amount: Int): Boolean {
        for (command in commands) {
            val parsed = command.replace("%player%", player.name.string)
            if (asPlayer) {
                player.server.commands.performPrefixedCommand(player.createCommandSourceStack(), parsed)
            } else {
                player.server.commands.performPrefixedCommand(player.server.createCommandSourceStack(), parsed)
            }
        }

        return true
    }

    override fun isSellable(): Boolean {
        return false
    }

    override fun toString(): String {
        return "CommandShopEntry(commands=$commands, asPlayer=$asPlayer) ${super.toString()}"
    }
}
