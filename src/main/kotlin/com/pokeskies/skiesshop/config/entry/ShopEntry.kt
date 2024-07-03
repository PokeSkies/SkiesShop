package com.pokeskies.skiesshop.config.entry

import com.pokeskies.skiesshop.config.ItemConfig
import com.pokeskies.skiesshop.config.PriceConfig
import net.minecraft.server.level.ServerPlayer

abstract class ShopEntry(
    val type: ShopEntryType = ShopEntryType.ITEM,
    val display: ItemConfig = ItemConfig(),
    val slot: Int? = null,
    val page: Int = 1,
    val buy: PriceConfig? = null,
    val sell: PriceConfig? = null,
) {
    open fun isValid(): Boolean {
        if (display.getItem() == null) return false
        if (slot == null) return false
        if (page < 0) return false
        if (buy == null && sell == null) return false
        return true
    }

    open fun isBuyable(): Boolean {
        return buy != null
    }

    open fun isSellable(): Boolean {
        return sell != null
    }

    open fun buy(player: ServerPlayer, amount: Int): Boolean {
        return false
    }

    open fun sell(player: ServerPlayer, amount: Int): Boolean {
        return false
    }

    override fun toString(): String {
        return "ShopEntry(type=$type, display=$display, slot=$slot, page=$page, buy=$buy, sell=$sell)"
    }
}
