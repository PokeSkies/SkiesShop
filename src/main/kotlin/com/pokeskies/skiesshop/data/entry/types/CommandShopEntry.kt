package com.pokeskies.skiesshop.data.entry.types

import com.pokeskies.skiesshop.config.GuiItem
import com.pokeskies.skiesshop.config.PriceOption
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.data.entry.ShopEntryType
import net.minecraft.server.level.ServerPlayer

class CommandShopEntry(
    type: ShopEntryType = ShopEntryType.COMMAND,
    display: GuiItem = GuiItem(),
    slot: List<Int> = listOf(),
    page: List<Int> = listOf(1),
    buy: PriceOption? = null,
    sell: PriceOption? = null,
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
