package com.pokeskies.skiesshop.config

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesshop.utils.TextUtils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
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
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation(id)).getOrNull()
    }

    fun getItemStack(amount: Int): ItemStack? {
        val item = getItem() ?: return null
        val stack = ItemStack(item, amount)

        if (nbt != null) {
            if (stack.tag != null) {
                if (!stack.tag!!.isEmpty) {
                    for (key in nbt.allKeys) {
                        nbt.get(key)?.let { tag ->
                            stack.tag?.put(key, tag)
                        }
                    }
                } else {
                    stack.tag = nbt
                }
            }
        }

        if (name != null) {
            val compoundTag: CompoundTag = stack.orCreateTag.getCompound(ItemStack.TAG_DISPLAY)
            compoundTag.putString(ItemStack.TAG_DISPLAY_NAME, Component.Serializer.toJson(TextUtils.toNative(name)))
        }

        if (lore.isNotEmpty()) {
            val compoundTag: CompoundTag = stack.orCreateTag.getCompound(ItemStack.TAG_DISPLAY)
            val loreTag = ListTag()
            lore.forEach { loreTag.add(StringTag.valueOf(Component.Serializer.toJson(
                Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(TextUtils.toNative(it))
            ))) }
            compoundTag.put(ItemStack.TAG_LORE, loreTag)
            stack.orCreateTag.put(ItemStack.TAG_DISPLAY, compoundTag)
        }

        return stack
    }

    override fun toString(): String {
        return "ItemConfig(id=$id, name=$name, lore=$lore, nbt=$nbt)"
    }
}
