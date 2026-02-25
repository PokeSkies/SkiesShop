package com.pokeskies.skiesshop.data.entry

import com.pokeskies.skiesshop.data.entry.types.CommandShopEntry
import com.pokeskies.skiesshop.data.entry.types.ItemShopEntry
import com.pokeskies.skiesshop.data.entry.types.PresetShopEntry

enum class ShopEntryType(val identifier: String, val clazz: Class<*>) {
    ITEM("item", ItemShopEntry::class.java),
    COMMAND("command", CommandShopEntry::class.java),
    PRESET("preset", PresetShopEntry::class.java),;

    companion object {
        fun valueOfAnyCase(name: String): ShopEntryType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }
}
