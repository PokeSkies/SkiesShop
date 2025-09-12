package com.pokeskies.skiesshop.data.entry

import com.pokeskies.skiesshop.data.entry.types.CommandShopEntry
import com.pokeskies.skiesshop.data.entry.types.ItemShopEntry

enum class ShopEntryType(val identifier: String, val clazz: Class<*>) {
    ITEM("item", ItemShopEntry::class.java),
    COMMAND("command", CommandShopEntry::class.java);

    companion object {
        fun valueOfAnyCase(name: String): ShopEntryType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }
}
