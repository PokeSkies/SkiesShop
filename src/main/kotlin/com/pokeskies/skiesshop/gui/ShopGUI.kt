package com.pokeskies.skiesshop.gui

import com.pokeskies.skiesshop.config.ConfigManager
import com.pokeskies.skiesshop.data.ShopInstance
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.placeholders.PlaceholderManager
import com.pokeskies.skiesshop.utils.asNative
import com.pokeskies.skiesshop.utils.clear
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class ShopGUI(
    player: ServerPlayer,
    val instance: ShopInstance,
    previous: IRefreshableGui? = null
) : IRefreshableGui(instance.type, player, false, previous) {
    private var page = 0

    init {
        refresh()
    }

    override fun refresh() {
        this.clear()
        renderItems()
        renderPage()
    }

    private fun renderPage() {
        (instance.entries[page + 1] ?: mapOf()).forEach { (slot, entry) ->
            val builder = entry.getGuiItem().createButton(player)
            this.setSlot(slot, builder
                .setLore(getItemLore(entry))
                .setCallback { ctx ->
                    val genericClicks = GenericClickType.fromClickType(ctx)

                    for (click in genericClicks) {
                        ConfigManager.CONFIG.clickOptions[click]?.execute(player, this, entry)
                    }
                }
                .build())
        }
    }

    private fun renderItems() {
        (instance.items[page + 1] ?: listOf()).forEach { item ->
            val button = item.asGuiItem().createButton(player)
                .setCallback { ctx ->
                    item.actions.forEach { (id, action) -> action.executeAction(player, this) }
                }.build()
            for (slot in item.slots) {
                this.setSlot(slot, button)
            }
        }
    }

    fun nextPage() {
        if (page < instance.pages - 1) {
            page++
            refresh()
        }
    }

    fun previousPage() {
        if (page > 0) {
            page--
            refresh()
        }
    }

    fun lastPage() {
        page = instance.pages - 1
        refresh()
    }

    fun firstPage() {
        page = 0
        refresh()
    }

    override fun getTitle(): Component {
        return PlaceholderManager.parse(player, instance.config.title).asNative()
    }

    companion object {
        fun getItemLore(entry: ShopEntry): List<Component> {
            val description = entry.display.lore.map { line ->
                Component.empty().withStyle { it.withItalic(false) }.append(line.asNative())
            }

            val lines = if (entry.isBuyable() && entry.isSellable()) {
                ConfigManager.CONFIG.entryLore.buySell
            } else if (entry.isBuyable()) {
                ConfigManager.CONFIG.entryLore.buy
            } else if (entry.isSellable()) {
                ConfigManager.CONFIG.entryLore.sell
            } else {
                listOf()
            }

            val lore = mutableListOf<Component>()
            lines.forEach { line ->
                if (line.contains("%display_item_lore%")) {
                    lore.addAll(description)
                } else {
                    lore.add(
                        Component.empty().withStyle { it.withItalic(false) }.append(
                            line.replace("%buy_price%", (entry.buy?.price ?: 0.0).toString())
                                .replace("%sell_price%", (entry.sell?.price ?: 0.0).toString())
                                .replace("%buy_price_currency%", entry.buy?.getCurrencyName() ?: "")
                                .replace("%sell_price_currency%", entry.sell?.getCurrencyName() ?: "")
                                .asNative()
                        )
                    )
                }
            }

            return lore
        }
    }
}
