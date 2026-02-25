package com.pokeskies.skiesshop.data.entry.types

import com.pokeskies.skiesshop.config.GuiItem
import com.pokeskies.skiesshop.config.PriceOption
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.data.entry.ShopEntryType

class PresetShopEntry(
    type: ShopEntryType = ShopEntryType.PRESET,
    display: GuiItem = GuiItem(),
    slot: List<Int> = listOf(),
    page: List<Int> = listOf(1),
    buy: PriceOption? = null,
    sell: PriceOption? = null,
) : ShopEntry(type, display, slot, page, buy, sell)
