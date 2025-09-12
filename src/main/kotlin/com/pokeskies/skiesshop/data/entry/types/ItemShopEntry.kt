package com.pokeskies.skiesshop.data.entry.types

import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.config.GuiItem
import com.pokeskies.skiesshop.config.PriceOption
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.data.entry.ShopEntryType
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

class ItemShopEntry(
    type: ShopEntryType = ShopEntryType.ITEM,
    display: GuiItem = GuiItem(),
    slot: List<Int> = listOf(),
    page: List<Int> = listOf(1),
    buy: PriceOption? = null,
    sell: PriceOption? = null,
    val item: String = "",
    val amount: Int = 1,
    val name: String? = null,
    val lore: List<String> = emptyList(),
    val components: CompoundTag? = null,
    val customModelData: Int? = null
) : ShopEntry(type, display, slot, page, buy, sell) {
    override fun isValid(): Boolean {
        if (item.isEmpty()) return false
        return super.isValid()
    }

    override fun getGuiItem(): GuiItem {
        return if (display.item.isEmpty()) {
            asGuiItem()
        } else {
            display
        }
    }

    override fun buy(player: ServerPlayer, amount: Int): Boolean {
        return asGuiItem().getItemStack(player, amount).let { player.inventory.add(it) }
    }

    override fun sell(player: ServerPlayer, amount: Int): Int {
        var amountFound = 0
        val matchedSlots: MutableList<Int> = mutableListOf()
        for ((i, stack) in player.inventory.items.withIndex()) {
            if (!stack.isEmpty) {
                if (isItem(stack)) {
                    amountFound += stack.count
                    matchedSlots.add(i)
                }
            }
        }

        var removed = 0
        matchedSlots.forEach { i ->
            val stack = player.inventory.items[i]
            val stackSize = stack.count
            if (removed + stackSize >= amount) {
                player.inventory.items[i].shrink(amount - removed)
                removed += amount - removed
                return@forEach
            } else {
                player.inventory.items[i].shrink(stackSize)
                removed += stackSize
            }
        }

        return removed
    }

    private fun isItem(checkItem: ItemStack): Boolean {
        val newItem = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(item))
        if (newItem.isEmpty) {
            return false
        }
        if (!checkItem.item.equals(newItem.get())) {
            return false
        }

        var nbtCopy = components?.copy()

        if (customModelData != null) {
            if (nbtCopy != null) {
                nbtCopy.putInt("minecraft:custom_model_data", customModelData)
            } else {
                val newNBT = CompoundTag()
                newNBT.putInt("minecraft:custom_model_data", customModelData)
                nbtCopy = newNBT
            }
        }

        if (nbtCopy != null) {
            val checkNBT = DataComponentPatch.CODEC.encodeStart(SkiesShop.INSTANCE.nbtOpts, checkItem.componentsPatch).result().getOrNull() ?: return false

            if (checkNBT != nbtCopy)
                return false
        }

        return true
    }

    fun asGuiItem(): GuiItem = GuiItem(
        item = item,
        amount = amount,
        name = name,
        lore = lore,
        components = components,
        customModelData = customModelData
    )

    override fun toString(): String {
        return "ItemShopEntry(item=$item) ${super.toString()}"
    }
}
