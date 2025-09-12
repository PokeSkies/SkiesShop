package com.pokeskies.skiesshop.gui

import com.pokeskies.skiesshop.config.ConfirmMenuConfig
import com.pokeskies.skiesshop.data.TransactionType
import com.pokeskies.skiesshop.data.confirm.ConfirmAmountItem
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.utils.asNative
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

class ConfirmGUI(
    private val player: ServerPlayer,
    private  val confirmMenu: ConfirmMenuConfig,
    private val shopGUI: ShopGUI,
    private val entry: ShopEntry,
) : SimpleGui(confirmMenu.type.type, player, false) {
    private var stack: ItemStack

    init {
        entry.getGuiItem().getItemStack(player).let {
            stack = it
        }

        renderItems()
        refreshShop()
    }

    private fun refreshShop() {
        setSlot(confirmMenu.entrySlot, GuiElementBuilder(stack).setLore(ShopGUI.getItemLore(entry)))

        for ((id, amountItem) in confirmMenu.amounts) {
            if (amountItem.type == TransactionType.BUY && !entry.isBuyable()) continue
            if (amountItem.type == TransactionType.SELL && !entry.isSellable()) continue

            val button = amountItem.asGuiItem().createButton(player)
                .setCallback { ctx ->
                    processClick(amountItem)
                }

            amountItem.slots.forEach { slot ->
                setSlot(slot, button)
            }
        }
    }

    private fun renderItems() {
        confirmMenu.items.forEach { (id, item) ->
            val button = item.asGuiItem().createButton(player)
                .setCallback { ctx ->
                    item.actions.forEach { (id, action) -> action.executeAction(player, shopGUI) }
                }.build()
            for (slot in item.slots) {
                this.setSlot(slot, button)
            }
        }
    }

    private fun processClick(amountItem: ConfirmAmountItem) {
        when (amountItem.type) {
            TransactionType.BUY -> {
                if (entry.isBuyable() && entry.tryBuy(player, shopGUI.instance, amountItem.amount)) {
                    close()
                }
            }
            TransactionType.SELL -> {
                if (entry.isSellable() && entry.trySell(player, shopGUI.instance, amountItem.amount)) {
                    close()
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