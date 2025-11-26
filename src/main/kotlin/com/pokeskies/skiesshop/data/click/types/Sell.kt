package com.pokeskies.skiesshop.data.click.types

import com.pokeskies.skiesshop.data.click.EntryClickOption
import com.pokeskies.skiesshop.data.click.EntryClickOptionType
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.gui.ShopGUI
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents

class Sell(
    type: EntryClickOptionType = EntryClickOptionType.SELL,
    val amount: Int
) : EntryClickOption(type) {
    override fun execute(player: ServerPlayer, gui: ShopGUI, entry: ShopEntry) {
        Utils.printDebug("[Entry Click Option - ${type.name}] Player(${player.gameProfile.name}) Amount($amount): $this")
        if (entry.isSellable()) {
            Utils.sendPlayerSound(player, SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f)
            entry.trySell(player, gui.instance, amount, gui)
        }
    }

    override fun toString(): String {
        return "Sell(amount=$amount)"
    }
}
