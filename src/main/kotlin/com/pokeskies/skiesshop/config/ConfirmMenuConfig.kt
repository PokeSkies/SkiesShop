package com.pokeskies.skiesshop.config

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.data.confirm.ConfirmAmountItem
import com.pokeskies.skiesshop.data.items.GenericItem
import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.gui.InventoryType

class ConfirmMenuConfig(
    val title: String,
    val type: InventoryType,
    @SerializedName("entry_slot")
    val entrySlot: Int,
    @SerializedName("back_on_transaction", alternate = ["back_on_purchase"])
    val backOnTransaction: Boolean = true,
    @SerializedName("open_actions")
    val openActions: Map<String, Action> = emptyMap(),
    @SerializedName("close_actions")
    val closeActions: Map<String, Action> = emptyMap(),
    val amounts: Map<String, ConfirmAmountItem> = emptyMap(),
    val items: Map<String, GenericItem> = emptyMap()
) {
    lateinit var id: String
}