package com.pokeskies.skiesshop.gui

import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.SkiesShopManager.getShop
import com.pokeskies.skiesshop.data.ShopTransaction
import com.pokeskies.skiesshop.utils.asNative
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Unit
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.ResolvableProfile
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class TransactionsGUI(viewer: ServerPlayer, target: UUID, val transactions: List<ShopTransaction>): SimpleGui(
    MenuType.GENERIC_9x6, viewer, false
) {
    companion object {
        private val slots = (0..44).toList()
        private val invalidItem = ItemStack(Items.BARRIER)
        private val timestampFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z")
            .withZone(ZoneId.systemDefault())
    }

    private var page = 0
    private var maxPages = (transactions.size + slots.size - 1) / slots.size

    private var playerHead: ItemStack = ItemStack(Items.PLAYER_HEAD)

    init {
        val player = SkiesShop.INSTANCE.server.playerList.getPlayer(target)
        if (player != null) {
            playerHead.set(DataComponents.PROFILE, ResolvableProfile(player.gameProfile))
        }

        val name = player?.name?.string ?: target.toString()
        playerHead.set(DataComponents.ITEM_NAME, Component.literal("${name}'s Shop Transactions").withStyle(ChatFormatting.GREEN))

        this.title = Component.literal("${name}'s Transactions")

        GuiElementBuilder.from(ItemStack(Items.GRAY_STAINED_GLASS_PANE))
            .setComponent(DataComponents.HIDE_TOOLTIP, Unit.INSTANCE)
            .build()
            .let {
                for (i in 45..53) {
                    this.setSlot(i, it)
                }
            }

        this.setSlot(47, GuiElementBuilder.from(ItemStack(Items.ARROW))
            .setName(Component.literal("Previous Page"))
            .setCallback(::previousPage)
            .build())

        this.setSlot(51, GuiElementBuilder.from(ItemStack(Items.ARROW))
            .setName(Component.literal("Next Page"))
            .setCallback(::nextPage)
            .build())

        refresh()
    }

    fun refresh() {
        val head = GuiElementBuilder.from(playerHead.copy())
            .setLore(listOf(
                "<gray>Page <white>${page + 1}</white> of <white>$maxPages",
                " ",
                "<gray>Total Transactions: <white>${transactions.size}"
            ).map { Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(it.asNative()) })

        this.setSlot(49, head.build())

        renderPage()
    }

    private fun renderPage() {
        slots.forEach { slot ->
            this.clearSlot(slot)
        }
        var index = 0
        for (entry in transactions.subList(slots.size * page, minOf(slots.size * (page + 1), transactions.size))) {
            if (index < slots.size) {
                this.setSlot(slots[index++], GuiElementBuilder.from(createDisplay(entry)))
            }
        }
    }

    fun nextPage() {
        page = (page + 1) % maxPages
        refresh()
    }

    fun previousPage() {
        page = if (page - 1 < 0) maxPages - 1 else page - 1
        refresh()
    }

    fun createDisplay(transaction: ShopTransaction): ItemStack {
        val shop = getShop(transaction.shopId)
        val entry = shop?.getEntry(transaction.entryId)

        var stack: ItemStack = invalidItem.copy()

        if (entry != null) {
            stack = entry.getGuiItem().getItemStack(player, transaction.amount)
        }

        stack.set(DataComponents.ITEM_NAME, Component.literal(
            timestampFormat.format(Instant.ofEpochMilli(transaction.timestamp))
        ).withStyle(ChatFormatting.GREEN))

        stack.set(DataComponents.LORE, ItemLore(listOf(
            "<gray>Shop ID <dark_gray>- <white>${transaction.shopId}",
            "<gray>Entry ID <dark_gray>- <white>${transaction.entryId}",
            "<gray>Transaction Type <dark_gray>- <white>${transaction.type.name}",
            "<gray>Total Price <dark_gray>- <white>${transaction.price}",
            "<gray>Amount <dark_gray>- <white>${transaction.amount}",
        ).map { Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(it.asNative()) }))

        return stack
    }
}