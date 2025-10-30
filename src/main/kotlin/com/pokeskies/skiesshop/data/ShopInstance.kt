package com.pokeskies.skiesshop.data

import com.pokeskies.skiesshop.config.ShopConfig
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.data.items.GenericItem
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.gui.ShopGUI
import com.pokeskies.skiesshop.utils.asNative
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType

class ShopInstance(
    val id: String,
    val config: ShopConfig,
    val title: Component,
    val type: MenuType<*>,
    val pages: Int,
    // <Page Number -> <Slot Index -> Shop Entry>>
    val entries: Map<Int, Map<Int, ShopEntry>> = mapOf(),
    // <Page Number -> Page Buttons>
    val items: Map<Int, List<GenericItem>> = mapOf(),
) {
    companion object {
        fun create(config: ShopConfig): ShopInstance {
            // Fill with shop entries that have slots assigned
            val entriesMap: MutableMap<Int, MutableMap<Int, ShopEntry>> = mutableMapOf()
            config.entries.filter { it.value.slots.isNotEmpty() }.forEach { (_, entry) ->
                entry.pages.ifEmpty { listOf(1) }.forEach { page ->
                    val pageEntries = entriesMap.getOrPut(page) { mutableMapOf() }
                    entry.slots.forEach { slot ->
                        pageEntries[slot] = entry
                    }
                }
            }

            // Collect the valid slots for any given page based on the background items
            val validSlots = MutableList(config.type.slots) { it }
            config.items.forEach { (_, item) ->
                validSlots.removeAll(item.slots)
            }

            config.entries.filter { it.value.slots.isEmpty() }.forEach { (_, entry) ->
                // For each entry we need to fill, find the first page with an empty slot
                var page = 1
                var placed = false // will flip true when successfully placed
                while (!placed) {
                    val pageEntries = entriesMap.getOrPut(page) { mutableMapOf() }
                    val emptySlotsOnPage = validSlots.filter { it !in pageEntries.keys }
                    val slot = emptySlotsOnPage.firstOrNull()
                    if (slot != null) {
                        pageEntries[slot] = entry
                        placed = true
                    } else {
                        page++
                    }
                }
            }

            val maxPages = if (entriesMap.isEmpty()) 1 else entriesMap.keys.max()

            val itemsMap: MutableMap<Int, MutableList<GenericItem>> = mutableMapOf()
            config.items.forEach { (_, item) ->
                item.pages.ifEmpty { (1..maxPages).toList() }.forEach { page ->
                    val pageEntries = itemsMap.getOrPut(page) { mutableListOf() }
                    pageEntries.add(item)
                }
            }

            return ShopInstance(
                id = config.id,
                config = config,
                title = config.title.asNative(),
                type = config.type.type,
                pages = maxPages,
                entries = entriesMap,
                items = itemsMap
            )
        }
    }

    fun open(player: ServerPlayer, previous: IRefreshableGui? = null) {
        ShopGUI(player, this, previous).open()
    }

    override fun toString(): String {
        return "ShopInstance(id='$id', config=$config, title=$title, type=$type, entries=$entries, items=$items)"
    }
}