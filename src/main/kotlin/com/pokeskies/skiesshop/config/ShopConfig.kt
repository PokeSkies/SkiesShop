package com.pokeskies.skiesshop.config

import com.pokeskies.skiesshop.config.entry.ShopEntry

class ShopConfig(
    val title: String,
    val size: Int = 6,
    val entries: MutableMap<String, ShopEntry> = mutableMapOf()
) {
    override fun toString(): String {
        return "ShopConfig(title='$title', size=$size, entries=$entries)"
    }
}
