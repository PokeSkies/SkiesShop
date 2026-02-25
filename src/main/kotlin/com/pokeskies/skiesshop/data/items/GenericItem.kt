package com.pokeskies.skiesshop.data.items

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.config.GuiItem
import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import net.minecraft.nbt.CompoundTag

class GenericItem(
    item: String = "",
    slots: List<Int> = emptyList(),
    amount: Int = 1,
    name: String? = null,
    lore: List<String> = emptyList(),
    components: CompoundTag? = null,
    customModelData: Int? = null,
    @SerializedName("pages", alternate = ["page"]) @JsonAdapter(FlexibleListAdaptorFactory::class)
    val pages: List<Int> = emptyList(),
    val actions: Map<String, Action> = emptyMap(),
): GuiItem(item, slots, amount, name, lore, components, customModelData) {
    lateinit var id: String

    override fun toString(): String {
        return "GenericItem(item=$item, actions=$actions, pages=$pages, slots=$slots, id='$id')"
    }
}