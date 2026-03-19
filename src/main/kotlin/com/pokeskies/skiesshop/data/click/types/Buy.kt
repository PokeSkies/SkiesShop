package com.pokeskies.skiesshop.data.click.types

import com.pokeskies.skiesshop.config.SoundOption
import com.pokeskies.skiesshop.data.click.EntryClickOption
import com.pokeskies.skiesshop.data.click.EntryClickOptionType
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.gui.ShopGUI
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer

class Buy(
    sound: SoundOption? = null,
    val amount: Int
) : EntryClickOption(EntryClickOptionType.BUY, sound) {
    override fun execute(player: ServerPlayer, gui: ShopGUI, entry: ShopEntry) {
        Utils.printDebug("[Entry Click Option - ${type.name}] Player(${player.gameProfile.name}) Amount($amount): $this")
        if (entry.isBuyable()) {
            entry.tryBuy(player, gui.instance, amount, gui, sound)
        }
    }

    override fun toString(): String {
        return "Buy(amount=$amount)"
    }
}
