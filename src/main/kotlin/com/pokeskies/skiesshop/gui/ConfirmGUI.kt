package com.pokeskies.skiesshop.gui

import com.pokeskies.skiesshop.config.ConfirmMenuConfig
import com.pokeskies.skiesshop.data.TransactionType
import com.pokeskies.skiesshop.data.confirm.ConfirmAmountItem
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.utils.asNative
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

class ConfirmGUI(
    player: ServerPlayer,
    private val confirmMenu: ConfirmMenuConfig,
    private val shopGUI: ShopGUI,
    private val entry: ShopEntry,
) : IRefreshableGui(confirmMenu.type.type, player, false, shopGUI) {
    private var stack: ItemStack

    private var lastClick: Long = 0

    init {
        entry.getGuiItem().getItemStack(player).let {
            stack = it
        }

        refresh()
    }

    override fun refresh() {
        renderItems()
        refreshShop()
    }

    private fun refreshShop() {
        setSlot(confirmMenu.entrySlot, GuiElementBuilder(stack))

        for ((_, amountItem) in confirmMenu.amounts) {
            if (amountItem.type == TransactionType.BUY && !entry.isBuyable()) continue
            if (amountItem.type == TransactionType.SELL && !entry.isSellable()) continue

            val maxAmount = entry.getMaxAmount(amountItem.type)
            if (maxAmount != null && maxAmount < amountItem.amount) continue

            val button = amountItem.asGuiItem().createButton(player)
                .setCallback { _ ->
                    processClick(amountItem)
                }

            amountItem.slots.forEach { slot ->
                setSlot(slot, button)
            }
        }
    }

    private fun renderItems() {
        confirmMenu.items.forEach { (_, item) ->
            val button = item.createButton(player)
                .setCallback { clickType ->
                    item.actions.forEach { (_, action) ->
                        if (action.matchesClick(clickType)) {
                            action.executeAction(player, this)
                        }
                    }
                }.build()
            for (slot in item.slots) {
                this.setSlot(slot, button)
            }
        }
    }

    private fun processClick(amountItem: ConfirmAmountItem) {
        if (System.currentTimeMillis() - lastClick < 200) return
        lastClick = System.currentTimeMillis()

        val maxAmount = entry.getMaxAmount(amountItem.type)
        if (maxAmount != null && maxAmount < amountItem.amount) return

        when (amountItem.type) {
            TransactionType.BUY -> {
                if (entry.isBuyable() && entry.tryBuy(player, shopGUI.instance, amountItem.amount, this)) {
                    if (confirmMenu.backOnTransaction) close()
                }
            }
            TransactionType.SELL -> {
                if (entry.isSellable() && entry.trySell(player, shopGUI.instance, amountItem.amount, this)) {
                    if (confirmMenu.backOnTransaction) close()
                }
            }
        }
    }

    override fun getTitle(): Component {
        return confirmMenu.title.asNative()
    }

    override fun onClose() {
        shopGUI.open()
    }
}