package com.pokeskies.skiesshop.data.click.types

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.config.ConfigManager
import com.pokeskies.skiesshop.data.TransactionType
import com.pokeskies.skiesshop.data.click.EntryClickOption
import com.pokeskies.skiesshop.data.click.EntryClickOptionType
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.gui.ConfirmGUI
import com.pokeskies.skiesshop.gui.ShopGUI
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents

class OpenConfirmMenu(
    type: EntryClickOptionType = EntryClickOptionType.OPEN_CONFIRM_MENU,
    @JsonAdapter(FlexibleListAdaptorFactory::class) @SerializedName("transactions", alternate = ["transaction"])
    val transactions: List<TransactionType> = listOf(),
    val menu: String
) : EntryClickOption(type) {
    override fun execute(player: ServerPlayer, gui: ShopGUI, entry: ShopEntry) {
        Utils.printDebug("[Entry Click Option - ${type.name}] Player(${player.gameProfile.name}) Menu($menu): $this")

        if (transactions.isEmpty()) return
        if (!((transactions.contains(TransactionType.BUY) && entry.isBuyable()) || (transactions.contains(TransactionType.SELL) && entry.isSellable()))) {
            return
        }

        val config = ConfigManager.CONFIRM_MENUS[menu]
        if (config == null) {
            Utils.printError("Confirm Menu '$menu' not found! Please check your configuration.")
            return
        }

        Utils.sendPlayerSound(player, SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f)
        ConfirmGUI(player, config, gui, entry).open()
    }

    override fun toString(): String {
        return "OpenConfirmMenu(menu='$menu')"
    }
}
