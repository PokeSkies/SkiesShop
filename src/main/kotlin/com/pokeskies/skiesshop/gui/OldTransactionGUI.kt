package com.pokeskies.skiesshop.gui

import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.data.ShopTransaction
import com.pokeskies.skiesshop.data.TransactionType
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.logging.LoggerManager
import com.pokeskies.skiesshop.utils.ShopTransactionEvent
import com.pokeskies.skiesshop.utils.Utils
import com.pokeskies.skiesshop.utils.asNative
import com.pokeskies.skiesshop.utils.setSlot
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OldTransactionGUI(
    private val player: ServerPlayer,
    private val entryId: String,
    private val entry: ShopEntry,
    private val shopGUI: ShopGUI
) : SimpleGui(MenuType.GENERIC_9x5, player, false) {

    private var stack: ItemStack

    init {
        entry.getGuiItem().getItemStack(player).let {
            stack = it
            refreshShop()
        }
    }

    private fun refreshShop() {
        setSlot(2, 4, GuiElementBuilder(stack).setLore(ShopGUI.getItemLore(entry)))

        setSlot(4, 4, GuiElementBuilder(ItemStack(Items.BARRIER))
            .setName("<red><b>Return to Shop".asNative())
            .setCallback { ctx ->
                returnToShop()
            })

        val amountSlots: Map<Int, Int> = mapOf(Pair(1, 10), Pair(4, 11), Pair(8, 19), Pair(16, 20), Pair(32, 28), Pair(64, 29))

        // Buy Buttons
        if (entry.isBuyable()) {
            amountSlots.forEach { (amount, slot) ->
                setSlot(slot, GuiElementBuilder(ItemStack(Items.GREEN_STAINED_GLASS, amount))
                    .setName("<green><b>Buy $amount</b></green>".asNative())
                    .setLore(listOf(getBuyLine(entry, amount)))
                    .setCallback { ctx ->
                        SkiesShop.INSTANCE.getEconomyService(entry.buy!!.economy)?.let { economy ->
                                if (economy.balance(player, entry.buy.currency) >= (entry.buy.price * amount)) {
                                    if (economy.withdraw(player, (entry.buy.price * amount), entry.buy.currency)) {
                                        if (entry.buy(player, amount)) {
                                            player.sendSystemMessage(
                                                Component.literal("Purchased ${amount}x ")
                                                    .append(Component.translatable(stack.descriptionId))
                                                    .append(" for ${entry.buy.price * amount}!")
                                                    .withStyle { it.withColor(ChatFormatting.GREEN) })
                                            Utils.sendPlayerSound(player, SoundEvents.ITEM_PICKUP, 0.5f, 1.0f)
                                            val transaction = ShopTransaction(
                                                player.uuid,
                                                System.currentTimeMillis(),
                                                shopGUI.instance.id,
                                                entryId,
                                                TransactionType.BUY,
                                                entry.buy.price,
                                                amount,
                                                entry.toJson()
                                            )
                                            ShopTransactionEvent.EVENT.invoker().execute(player, transaction)
                                            LoggerManager.logTransaction(transaction)
                                            returnToShop()
                                        } else {
                                            val timestamp = getCurrentDateTimeFormatted()
                                            Utils.printError("There was an error for player '${player.name.string}' while purchasing '${entry}' at $timestamp!")
                                            player.sendSystemMessage(
                                                Component.literal("There was an error while purchasing this item, please contact staff with the error: $timestamp")
                                                    .withStyle {
                                                        it.withColor(
                                                            ChatFormatting.RED
                                                        )
                                                    })
                                            Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                                        }
                                    }
                                } else {
                                    player.sendSystemMessage(
                                        Component.literal("You do not have enough to purchase this shop entry!").withStyle { it.withColor(ChatFormatting.RED) })
                                    Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                                }
                        } ?: run {
                            Utils.printError("Could not find currency ${entry.buy?.currency}!")
                            Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                            return@setCallback
                        }
                    }
                    .build()
                )
            }
        } else {
            amountSlots.forEach { (amount, slot) ->
                setSlot(slot, GuiElementBuilder(ItemStack(Items.BARRIER, 1))
                    .setName("<red><b>Not Purchasable".asNative())
                    .build()
                )
            }
        }

        // Sell Buttons
        if (entry.isSellable()) {
            amountSlots.forEach() { amount, slot ->
                setSlot(slot + 5, GuiElementBuilder(ItemStack(Items.RED_STAINED_GLASS, amount))
                    .setName("<red><b>Sell $amount".asNative())
                    .setLore(listOf(getSellLine(entry, amount)))
                    .setCallback { ctx ->
                        SkiesShop.INSTANCE.getEconomyService(entry.sell!!.economy)?.let { economy ->
                            val amountSold = entry.sell(player, amount)
                            if (amountSold > 0) {
                                if (economy.deposit(player, entry.sell.price * amountSold, entry.sell.currency)) {
                                    player.sendSystemMessage(Component.literal("Sold ${amountSold}x ")
                                        .append(Component.translatable(stack.descriptionId))
                                        .append(" for ${entry.sell.price * amountSold}!")
                                        .withStyle { it.withColor(ChatFormatting.GREEN) })
                                    Utils.sendPlayerSound(player, SoundEvents.ITEM_PICKUP, 0.5f, 1.0f)
                                    val transaction = ShopTransaction(
                                        player.uuid,
                                        System.currentTimeMillis(),
                                        shopGUI.instance.id,
                                        entryId,
                                        TransactionType.SELL,
                                        entry.sell.price,
                                        amountSold,
                                        entry.toJson()
                                    )
                                    ShopTransactionEvent.EVENT.invoker().execute(player, transaction)
                                    LoggerManager.logTransaction(transaction)
                                    returnToShop()
                                } else {
                                    val timestamp = getCurrentDateTimeFormatted()
                                    Utils.printError("There was an error for player '${player.name.string}' while selling '${entry}' at $timestamp!")
                                    player.sendSystemMessage(Component.literal("There was an error while selling this item, please contact staff with the error: $timestamp").withStyle { it.withColor(
                                        ChatFormatting.RED) })
                                    Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                                }
                            } else {
                                player.sendSystemMessage(Component.literal("You dont have the required items!").withStyle { it.withColor(
                                    ChatFormatting.RED) })
                                Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                            }
                        } ?: run {
                            Utils.printError("Could not find currency ${entry.sell?.currency}!")
                            Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                            return@setCallback
                        }
                    }
                    .build()
                )
            }
        } else {
            amountSlots.forEach { (amount, slot) ->
                setSlot(slot + 5, GuiElementBuilder(ItemStack(Items.BARRIER, 1))
                    .setName("<red><b>Not Sellable".asNative())
                    .build()
                )
            }
        }
    }

    private fun returnToShop() {
        shopGUI.open()
    }

    private fun convertIndex(index: Int): Int {
        return if (index < 9) 27 + index else index - 9
    }

    override fun getTitle(): Component {
        return "Buy & Sell".asNative()
    }

    fun getCurrentDateTimeFormatted(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyMMdd-HHmmss")
        return currentDateTime.format(formatter)
    }

    fun getBuyLine(entry: ShopEntry, multiplier: Int = 1): Component {
        return Component.empty().setStyle(Style.EMPTY.withItalic(false)).append("<green>Price: <white>${(entry.buy?.price ?: 0.0) * multiplier}".asNative())
    }

    fun getSellLine(entry: ShopEntry, multiplier: Int = 1): Component {
        return Component.empty().setStyle(Style.EMPTY.withItalic(false)).append("<red>Price: <white>${(entry.sell?.price ?: 0.0) * multiplier}".asNative())
    }
}