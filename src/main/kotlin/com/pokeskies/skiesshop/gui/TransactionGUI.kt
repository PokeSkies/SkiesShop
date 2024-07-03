package com.pokeskies.skiesshop.gui

import ca.landonjw.gooeylibs2.api.UIManager
import ca.landonjw.gooeylibs2.api.button.ButtonClick
import ca.landonjw.gooeylibs2.api.button.GooeyButton
import ca.landonjw.gooeylibs2.api.data.UpdateEmitter
import ca.landonjw.gooeylibs2.api.page.Page
import ca.landonjw.gooeylibs2.api.template.Template
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate
import ca.landonjw.gooeylibs2.api.template.types.InventoryTemplate
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.config.entry.ShopEntry
import com.pokeskies.skiesshop.economy.EconomyManager
import com.pokeskies.skiesshop.utils.TextUtils
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TransactionGUI(
    private val player: ServerPlayer,
    private val entry: ShopEntry,
    private val shopGUI: ShopGUI
) : UpdateEmitter<Page>(), Page {
    private val template: ChestTemplate =
        ChestTemplate.Builder(5)
            .build()
    private val playerInventory: InventoryTemplate = InventoryTemplate.builder().build()

    private lateinit var stack: ItemStack

    init {
        entry.display.getItemStack(1)?.let {
            stack = it
            refreshInventory()
            refreshShop()
        } ?: run {
            Utils.printError("Could not find item for shop entry!")
            returnToShop()
        }
    }

    private fun refreshInventory() {
        for ((i, stack) in player.inventory.items.withIndex()) {
            playerInventory.set(convertIndex(i), GooeyButton.builder().display(stack).build())
        }
    }

    private fun refreshShop() {
        this.template.set(2, 4, GooeyButton.builder()
            .display(stack)
            .build())

        this.template.set(4, 4, GooeyButton.builder()
            .display(ItemStack(Items.BARRIER))
            .title(TextUtils.toNative("<red><b>Return to Shop"))
            .onClick { ctx ->
                returnToShop()
            }
            .build())

        val amountSlots: Map<Int, Int> = mapOf(Pair(1, 10), Pair(4, 11), Pair(8, 19), Pair(16, 20), Pair(32, 28), Pair(64, 29))

        // Buy Buttons
        if (entry.isBuyable()) {
            amountSlots.forEach() { amount, slot ->
                this.template.set(slot, GooeyButton.builder()
                    .display(ItemStack(Items.GREEN_STAINED_GLASS, amount))
                    .title(TextUtils.toNative("<green><b>Buy $amount"))
                    .onClick { ctx ->
                        EconomyManager.getCurrency(entry.buy!!.currency)?.let { currency ->
                            if (EconomyManager.balance(player, currency) >= (entry.buy.price * amount)) {
                                if (EconomyManager.withdraw(player, (entry.buy.price * amount), currency)) {
                                    if (entry.buy(player, amount)) {
                                        player.sendSystemMessage(Component.literal("Yipee").withStyle { it.withColor(
                                            ChatFormatting.GREEN) })
                                    } else {
                                        val timestamp = getCurrentDateTimeFormatted()
                                        Utils.printError("There was an error for player '${player.name.string}' while purchasing '${entry}' at $timestamp!")
                                        player.sendSystemMessage(Component.literal("There was an error while purchasing this item, please contact staff with the error: $timestamp").withStyle { it.withColor(
                                            ChatFormatting.RED) })
                                    }
                                }
                            } else {
                                player.sendSystemMessage(
                                    Component.literal("You do not have enough ")
                                        .append(SkiesShop.INSTANCE.adventure.toNative(currency.plural()))
                                        .append("!").withStyle { it.withColor(ChatFormatting.RED) })
                            }
                        } ?: run {
                            Utils.printError("Could not find currency ${entry.buy?.currency}!")
                            return@onClick
                        }
                        refreshInventory()
                    }
                    .build()
                )
            }
        } else {
            amountSlots.forEach { (amount, slot) ->
                this.template.set(slot, GooeyButton.builder()
                    .display(ItemStack(Items.BARRIER, 1))
                    .title(TextUtils.toNative("<red><b>Not Purchasable"))
                    .build()
                )
            }
        }

        // Sell Buttons
        if (entry.isSellable()) {
            amountSlots.forEach() { amount, slot ->
                this.template.set(slot + 5, GooeyButton.builder()
                    .display(ItemStack(Items.RED_STAINED_GLASS, amount))
                    .title(TextUtils.toNative("<red><b>Sell $amount"))
                    .onClick { ctx ->
                        EconomyManager.getCurrency(entry.buy!!.currency)?.let { currency ->
                            if (entry.sell(player, amount)) {
                                if (EconomyManager.deposit(player, entry.sell!!.price * amount, currency)) {
                                    player.sendSystemMessage(Component.literal("Yipee").withStyle { it.withColor(
                                        ChatFormatting.GREEN) })
                                } else {
                                    val timestamp = getCurrentDateTimeFormatted()
                                    Utils.printError("There was an error for player '${player.name.string}' while selling '${entry}' at $timestamp!")
                                    player.sendSystemMessage(Component.literal("There was an error while selling this item, please contact staff with the error: $timestamp").withStyle { it.withColor(
                                        ChatFormatting.RED) })
                                }
                            } else {
                                player.sendSystemMessage(Component.literal("You dont have the required items!").withStyle { it.withColor(
                                    ChatFormatting.RED) })
                            }
                        } ?: run {
                            Utils.printError("Could not find currency ${entry.sell?.currency}!")
                            return@onClick
                        }
                        refreshInventory()
                    }
                    .build()
                )
            }
        } else {
            amountSlots.forEach { (amount, slot) ->
                this.template.set(slot + 5, GooeyButton.builder()
                    .display(ItemStack(Items.BARRIER, 1))
                    .title(TextUtils.toNative("<red><b>Not Sellable"))
                    .build()
                )
            }
        }
    }

    private fun returnToShop() {
        UIManager.openUIForcefully(player, shopGUI)
    }

    private fun convertIndex(index: Int): Int {
        return if (index < 9) 27 + index else index - 9
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getInventoryTemplate(): Optional<InventoryTemplate> {
        return Optional.of(playerInventory)
    }

    override fun getTitle(): Component {
        return TextUtils.toNative("Buy & Sell")
    }

    fun getCurrentDateTimeFormatted(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyMMdd-HHmmss")
        return currentDateTime.format(formatter)
    }
}

fun ButtonClick.isLeftClick(): Boolean {
    return this == ButtonClick.LEFT_CLICK || this == ButtonClick.SHIFT_LEFT_CLICK
}

fun ButtonClick.isRightClick(): Boolean {
    return this == ButtonClick.RIGHT_CLICK || this == ButtonClick.SHIFT_RIGHT_CLICK
}
