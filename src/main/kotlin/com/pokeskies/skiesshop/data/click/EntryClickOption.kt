package com.pokeskies.skiesshop.data.click

import com.google.gson.*
import com.pokeskies.skiesshop.config.SoundOption
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.gui.ShopGUI
import net.minecraft.server.level.ServerPlayer
import java.lang.reflect.Type

abstract class EntryClickOption(
    val type: EntryClickOptionType,
    val sound: SoundOption? = null
) {
    abstract fun execute(player: ServerPlayer, gui: ShopGUI, entry: ShopEntry)

    override fun toString(): String {
        return "EntryClickOption(type=$type, sound=$sound)"
    }

    internal class Adapter: JsonSerializer<EntryClickOption>, JsonDeserializer<EntryClickOption> {
        override fun serialize(src: EntryClickOption, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src, src::class.java)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EntryClickOption {
            val jsonObject: JsonObject = json.getAsJsonObject()
            val value = jsonObject.get("type").asString
            val type: EntryClickOptionType? = EntryClickOptionType.valueOfAnyCase(value)
            return try {
                context.deserialize(json, type!!.clazz)
            } catch (e: NullPointerException) {
                throw JsonParseException("Could not deserialize entry click option type: $value", e)
            }
        }
    }
}
