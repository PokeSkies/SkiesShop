package com.pokeskies.skiesshop.config

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.data.confirm.ConfirmAmountItem
import com.pokeskies.skiesshop.data.items.GenericItem
import com.pokeskies.skiesshop.gui.InventoryType

class ConfirmMenuConfig(
    val title: String,
    val type: InventoryType,
    @SerializedName("entry_slot")
    val entrySlot: Int,
    @SerializedName("back_on_purchase")
    val backOnPurchase: Boolean = true,
    val amounts: Map<String, ConfirmAmountItem> = emptyMap(),
    val items: Map<String, GenericItem> = emptyMap()
) {
    lateinit var id: String
}