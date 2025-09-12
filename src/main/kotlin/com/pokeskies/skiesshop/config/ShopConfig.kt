package com.pokeskies.skiesshop.config

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.data.items.GenericItem
import com.pokeskies.skiesshop.gui.InventoryType
import java.lang.reflect.Type

class ShopConfig(
    val title: String,
    val type: InventoryType = InventoryType.GENERIC_9x5,
    val entries: MutableMap<String, ShopEntry> = mutableMapOf(),
    val items: MutableMap<String, GenericItem> = mutableMapOf(),
) {
    lateinit var id: String

    internal class Deserializer : JsonDeserializer<ShopConfig> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ShopConfig {
            val obj = json.asJsonObject
            val title = obj.get("title").asString
            val type = InventoryType.valueOf(obj.get("type").asString)

            val entries = mutableMapOf<String, ShopEntry>()
            val entriesObj = obj.getAsJsonObject("entries")
            if (entriesObj != null) {
                for ((key, value) in entriesObj.entrySet()) {
                    val entry = context.deserialize<ShopEntry>(value, ShopEntry::class.java)
                    entry.id = key
                    entries[key] = entry
                }
            }

            val items = mutableMapOf<String, GenericItem>()
            val itemsObj = obj.getAsJsonObject("items")
            if (itemsObj != null) {
                for ((key, value) in itemsObj.entrySet()) {
                    val item = context.deserialize<GenericItem>(value, GenericItem::class.java)
                    item.id = key
                    items[key] = item
                }
            }

            return ShopConfig(title, type, entries, items)
        }
    }

    override fun toString(): String {
        return "ShopConfig(title='$title', type=$type, entries=$entries, items=$items, id='$id')"
    }
}
