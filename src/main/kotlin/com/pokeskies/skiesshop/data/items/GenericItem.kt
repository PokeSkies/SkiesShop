package com.pokeskies.skiesshop.data.items

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.config.GuiItem
import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import net.minecraft.nbt.CompoundTag

class GenericItem(
    val item: String = "",
    @SerializedName("slots", alternate = ["slot"])
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val slots: List<Int> = emptyList(),
    @SerializedName("pages", alternate = ["page"]) @JsonAdapter(FlexibleListAdaptorFactory::class)
    val pages: List<Int> = emptyList(),
    val amount: Int = 1,
    val name: String? = null,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val lore: List<String> = emptyList(),
    @SerializedName("components", alternate = ["nbt"])
    val components: CompoundTag? = null,
    @SerializedName("custom_model_data")
    val customModelData: Int? = null,
    val actions: Map<String, Action> = emptyMap(),
) {
    lateinit var id: String

    fun asGuiItem(): GuiItem = GuiItem(
        item = item,
        amount = amount,
        name = name,
        lore = lore,
        components = components,
        customModelData = customModelData
    )

    override fun toString(): String {
        return "GenericItem(item=$item, actions=$actions, pages=$pages, slots=$slots, id='$id')"
    }
}