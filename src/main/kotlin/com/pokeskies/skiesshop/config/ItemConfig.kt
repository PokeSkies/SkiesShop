package com.pokeskies.skiesshop.config

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesshop.utils.TextUtils
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore
import kotlin.jvm.optionals.getOrNull

class ItemConfig(
    val id: String? = null,
    val name: String? = null,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val lore: List<String> = emptyList(),
    val nbt: CompoundTag? = null
) {
    fun getItem(): Item? {
        if (id.isNullOrEmpty()) return null
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id)).getOrNull()
    }

    fun getItemStack(amount: Int): ItemStack? {
        val item = getItem() ?: return null
        val stack = ItemStack(item, amount)

        if (nbt != null) {
            DataComponentPatch.CODEC.decode(SkiesShop.INSTANCE.nbtOpts, nbt).result().ifPresent { result ->
                stack.applyComponents(result.first)
            }
        }

        val dataComponentPatch = DataComponentPatch.builder()

        if (name != null) {
            dataComponentPatch.set(DataComponents.ITEM_NAME, TextUtils.toNative(name))
        }

        if (lore.isNotEmpty()) {
            dataComponentPatch.set(DataComponents.LORE, ItemLore(lore.map { TextUtils.toNative(it) }))
        }

        return stack.copy()
    }

    override fun toString(): String {
        return "ItemConfig(id=$id, name=$name, lore=$lore, nbt=$nbt)"
    }
}
