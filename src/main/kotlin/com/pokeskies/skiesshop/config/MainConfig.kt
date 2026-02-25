package com.pokeskies.skiesshop.config

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.data.click.EntryClickOption
import com.pokeskies.skiesshop.gui.GenericClickType

class MainConfig(
    var debug: Boolean = false,
    val commands: List<String> = listOf("skiesshop", "shops", "shop"),
    @SerializedName("base_shop")
    val baseShop: String = "example",
    @SerializedName("entry_lore")
    var entryLore: EntryLore = EntryLore(),
    @SerializedName("click_options")
    var clickOptions: Map<GenericClickType, EntryClickOption> = mapOf(),
    var storage: StorageOptions = StorageOptions(),
    var logging: LoggingOptions = LoggingOptions()
) {
    class EntryLore(
        val buy: List<String> = emptyList(),
        val sell: List<String> = emptyList(),
        @SerializedName("buy_sell")
        val buySell: List<String> = emptyList(),
    ) {
        override fun toString(): String {
            return "EntryLore(buy=$buy, sell=$sell, buySell=$buySell)"
        }
    }

    override fun toString(): String {
        return "MainConfig(debug=$debug, entryLore=$entryLore, clickOptions=$clickOptions, storage=$storage, logging=$logging)"
    }
}
