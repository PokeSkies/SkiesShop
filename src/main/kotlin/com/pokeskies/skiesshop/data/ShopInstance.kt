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
            val entriesMap: MutableMap<Int, MutableMap<Int, ShopEntry>> = mutableMapOf()
            config.entries.forEach { (_, entry) ->
                entry.pages.ifEmpty { listOf(1) }.forEach { page ->
                    val pageEntries = entriesMap.getOrPut(page) { mutableMapOf() }
                    entry.slots.forEach { slot ->
                        pageEntries[slot] = entry
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