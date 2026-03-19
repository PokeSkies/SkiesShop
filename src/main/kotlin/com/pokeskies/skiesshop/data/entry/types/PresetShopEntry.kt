package com.pokeskies.skiesshop.data.entry.types

import com.pokeskies.skiesshop.config.GuiItem
import com.pokeskies.skiesshop.config.MainConfig.EntryLore
import com.pokeskies.skiesshop.config.PriceOption
import com.pokeskies.skiesshop.data.click.EntryClickOption
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.data.entry.ShopEntryType
import com.pokeskies.skiesshop.gui.GenericClickType

class PresetShopEntry(
    type: ShopEntryType = ShopEntryType.PRESET,
    display: GuiItem = GuiItem(),
    slot: List<Int> = listOf(),
    page: List<Int> = listOf(1),
    buy: PriceOption? = null,
    sell: PriceOption? = null,
    entryLore: EntryLore? = null,
    clickOptions: Map<GenericClickType, EntryClickOption>? = null,
) : ShopEntry(type, display, slot, page, buy, sell, entryLore, clickOptions)
