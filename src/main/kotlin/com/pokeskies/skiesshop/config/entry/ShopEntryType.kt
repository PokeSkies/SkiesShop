package com.pokeskies.skiesshop.config.entry

import com.google.gson.*
import com.pokeskies.skiesshop.config.entry.types.ItemShopEntry
import java.lang.reflect.Type

enum class ShopEntryType(val identifier: String, val clazz: Class<*>) {
    ITEM("item", ItemShopEntry::class.java);

    companion object {
        fun valueOfAnyCase(name: String): ShopEntryType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }

    internal class ShopEntryTypeAdaptor : JsonSerializer<ShopEntry>, JsonDeserializer<ShopEntry> {
        override fun serialize(src: ShopEntry, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src, src::class.java)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ShopEntry {
            val jsonObject: JsonObject = json.getAsJsonObject()
            val value = jsonObject.get("type").asString
            val type: ShopEntryType? = ShopEntryType.valueOfAnyCase(value)
            return try {
                context.deserialize(json, type!!.clazz)
            } catch (e: NullPointerException) {
                throw JsonParseException("Could not deserialize Shop Entry Type: $value", e)
            }
        }
    }
}
