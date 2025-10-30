package com.pokeskies.skiesshop.data.click.types

import com.pokeskies.skiesshop.data.click.EntryClickOption
import com.pokeskies.skiesshop.data.click.EntryClickOptionType
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.gui.ShopGUI
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents

class Buy(
    type: EntryClickOptionType = EntryClickOptionType.BUY,
    val amount: Int
) : EntryClickOption(type) {
    override fun execute(player: ServerPlayer, gui: ShopGUI, entry: ShopEntry) {
        Utils.printDebug("[Entry Click Option - ${type.name}] Player(${player.gameProfile.name}) Amount($amount): $this")
        if (entry.isBuyable()) {
            Utils.sendPlayerSound(player, SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f)
            entry.tryBuy(player, gui.instance, amount, gui)
        }
    }

    override fun toString(): String {
        return "Buy(amount=$amount)"
    }
}
