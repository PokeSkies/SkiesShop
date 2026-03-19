package com.pokeskies.skiesshop.config

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.pokeskies.skiesshop.config.MainConfig.EntryLore
import com.pokeskies.skiesshop.data.click.EntryClickOption
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.data.items.GenericItem
import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.InventoryType
import java.lang.reflect.Type

class ShopConfig(
    val title: String,
    val type: InventoryType = InventoryType.GENERIC_9x5,
    @SerializedName("entry_lore")
    var entryLore: EntryLore? = null,
    @SerializedName("click_options")
    var clickOptions: Map<GenericClickType, EntryClickOption>? = null,
    @SerializedName("open_actions")
    val openActions: Map<String, Action> = emptyMap(),
    @SerializedName("close_actions")
    val closeActions: Map<String, Action> = emptyMap(),
    val entries: MutableMap<String, ShopEntry> = mutableMapOf(),
    val items: MutableMap<String, GenericItem> = mutableMapOf(),
) {
    lateinit var id: String

    companion object {
        private val CLICK_OPTIONS_MAP = object : TypeToken<Map<GenericClickType, EntryClickOption>>() {}.type
        private val ACTIONS_MAP = object : TypeToken<Map<String, Action>>() {}.type
    }

    internal class Deserializer : JsonDeserializer<ShopConfig> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ShopConfig {
            val obj = json.asJsonObject
            val title = obj.get("title").asString
            val type = InventoryType.valueOf(obj.get("type").asString)
            var clickOptions: MutableMap<GenericClickType, EntryClickOption>? = null
            obj.getAsJsonObject("click_options")?.let {
                clickOptions = mutableMapOf()
                clickOptions.putAll(context.deserialize<Map<GenericClickType, EntryClickOption>>(it, CLICK_OPTIONS_MAP))
            }
            var entryLore: EntryLore? = null
            obj.getAsJsonObject("entry_lore")?.let {
                entryLore = context.deserialize<EntryLore>(it, EntryLore::class.java)
            }
            val openActions: MutableMap<String, Action> = mutableMapOf()
            obj.getAsJsonObject("open_actions")?.let {
                openActions.putAll(context.deserialize<Map<String, Action>>(it, ACTIONS_MAP))
            }
            val closeActions: MutableMap<String, Action> = mutableMapOf()
            obj.getAsJsonObject("close_actions")?.let {
                closeActions.putAll(context.deserialize<Map<String, Action>>(it, ACTIONS_MAP))
            }

            val entries = mutableMapOf<String, ShopEntry>()
            val entriesObj = obj.getAsJsonObject("entries")
            if (entriesObj != null) {
                entries.putAll(ShopEntryMapAdapter().deserialize(entriesObj, Map::class.java, context))
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

            return ShopConfig(
                title,
                type,
                entryLore ?: ConfigManager.CONFIG.entryLore,
                clickOptions ?: ConfigManager.CONFIG.clickOptions,
                openActions,
                closeActions,
                entries,
                items
            )
        }
    }

    override fun toString(): String {
        return "ShopConfig(title='$title', type=$type, entryLore=$entryLore, clickOptions=$clickOptions, " +
                "openActions=$openActions, closeActions=$closeActions, entries=$entries, items=$items, id='$id')"
    }
}
