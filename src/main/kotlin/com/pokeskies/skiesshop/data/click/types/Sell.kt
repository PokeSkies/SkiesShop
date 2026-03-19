package com.pokeskies.skiesshop.data.click.types

import com.pokeskies.skiesshop.config.SoundOption
import com.pokeskies.skiesshop.data.click.EntryClickOption
import com.pokeskies.skiesshop.data.click.EntryClickOptionType
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.gui.ShopGUI
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer

class Sell(
    sound: SoundOption? = null,
    val amount: Int
) : EntryClickOption(EntryClickOptionType.SELL, sound) {
    override fun execute(player: ServerPlayer, gui: ShopGUI, entry: ShopEntry) {
        Utils.printDebug("[Entry Click Option - ${type.name}] Player(${player.gameProfile.name}) Amount($amount): $this")
        if (entry.isSellable()) {
            sound?.playSound(player)
            entry.trySell(player, gui.instance, amount, gui)
        }
    }

    override fun toString(): String {
        return "Sell(amount=$amount)"
    }
}
