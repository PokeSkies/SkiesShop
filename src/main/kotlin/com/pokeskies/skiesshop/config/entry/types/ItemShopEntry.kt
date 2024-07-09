package com.pokeskies.skiesshop.config.entry.types

import com.pokeskies.skiesshop.config.ItemConfig
import com.pokeskies.skiesshop.config.PriceConfig
import com.pokeskies.skiesshop.config.entry.ShopEntry
import com.pokeskies.skiesshop.config.entry.ShopEntryType
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

class ItemShopEntry(
    type: ShopEntryType = ShopEntryType.ITEM,
    display: ItemConfig = ItemConfig(),
    slot: Int? = null,
    page: Int = 1,
    buy: PriceConfig? = null,
    sell: PriceConfig? = null,
    private val item: ItemConfig? = null,
) : ShopEntry(type, display, slot, page, buy, sell) {
    override fun isValid(): Boolean {
        if (!super.isValid()) return false
        if (item != null && item.getItem() == null) return false
        return true
    }

    override fun buy(player: ServerPlayer, amount: Int): Boolean {
        return (item ?: display).getItemStack(amount)?.let { player.inventory.add(it) } ?: false
    }

    override fun sell(player: ServerPlayer, amount: Int): Int {
        val itemConfig = item ?: display

        var amountFound = 0
        val matchedSlots: MutableList<Int> = mutableListOf()
        for ((i, stack) in player.inventory.items.withIndex()) {
            if (!stack.isEmpty) {
                if (checkItem(itemConfig, stack)) {
                    amountFound += stack.count
                    matchedSlots.add(i)
                }
            }
        }

        var removed = 0
        matchedSlots.forEach { i ->
            val stack = player.inventory.items[i]
            val stackSize = stack.count
            if (removed + stackSize >= amount) {
                player.inventory.items[i].shrink(amount - removed)
                removed += amount - removed
                return@forEach
            } else {
                player.inventory.items[i].shrink(stackSize)
                removed += stackSize
            }
        }

        return removed
    }

    private fun checkItem(item: ItemConfig, checkItem: ItemStack): Boolean {
        if (!checkItem.item.equals(item.getItem())) {
            return false
        }

        val nbtCopy = item.nbt?.copy()

        if (nbtCopy != null) {
            val checkNBT = checkItem.tag ?: return false

            if (checkNBT != nbtCopy)
                return false
        }

        return true
    }

    override fun toString(): String {
        return "ItemShopEntry(item=$item) ${super.toString()}"
    }
}
