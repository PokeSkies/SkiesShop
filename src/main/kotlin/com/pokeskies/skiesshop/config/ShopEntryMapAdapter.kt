package com.pokeskies.skiesshop.config

import com.google.gson.*
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.data.entry.ShopEntryType
import java.lang.reflect.Type

class ShopEntryMapAdapter : JsonSerializer<MutableMap<String, ShopEntry>>, JsonDeserializer<MutableMap<String, ShopEntry>> {
    override fun serialize(src: MutableMap<String, ShopEntry>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        for ((key, entry) in src) {
            val element = if (entry.isPreset) {
                val element = JsonObject()
                element.addProperty("type", "PRESET")
                element
            } else {
                context.serialize(entry, entry::class.java)
            }
            obj.add(key, element)
        }
        return obj
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MutableMap<String, ShopEntry> {
        val result = mutableMapOf<String, ShopEntry>()
        if (!json.isJsonObject) return result
        val obj = json.asJsonObject
        for ((key, value) in obj.entrySet()) {
            try {
                val entryObj = value.asJsonObject
                val typeStr = entryObj.get("type")?.asString
                val isPreset = typeStr?.let { ShopEntryType.valueOfAnyCase(it) } == ShopEntryType.PRESET
                val entry: ShopEntry = if (isPreset) {
                    val preset = ConfigManager.PRESETS[key]
                        ?: throw JsonParseException("Could not find preset with id '$key' for map key '$key'")
                    preset.copy()
                } else {
                    context.deserialize(value, ShopEntry::class.java)
                }
                entry.id = key
                entry.isPreset = isPreset
                result[key] = entry
            } catch (e: Exception) {
                throw JsonParseException("Could not deserialize ShopEntry with key '$key'", e)
            }
        }
        return result
    }
}
