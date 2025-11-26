package com.pokeskies.skiesshop.economy.services

import com.pokeskies.skiesshop.economy.IEconomyService
import com.pokeskies.skiesshop.utils.Utils
import net.kyori.adventure.text.Component
import net.minecraft.server.level.ServerPlayer
import org.beconomy.api.BEconomy
import java.math.BigDecimal

class BEconomyService : IEconomyService {
    init {
        Utils.printInfo("BlanketEconomy has been found and loaded for any Currency actions/requirements!")
    }

    override fun balance(player: ServerPlayer, currency: String) : Double {
        return BEconomy.getAPI().getBalance(player.uuid, currency).toDouble()
    }

    override fun withdraw(player: ServerPlayer, amount: Double, currency: String) : Boolean {
        return BEconomy.getAPI().subtractBalance(player.uuid, BigDecimal.valueOf(amount), currency)
    }

    override fun deposit(player: ServerPlayer, amount: Double, currency: String) : Boolean {
        BEconomy.getAPI().addBalance(player.uuid, BigDecimal.valueOf(amount), currency)
        return true
    }

    override fun set(player: ServerPlayer, amount: Double, currency: String) : Boolean {
        BEconomy.getAPI().setBalance(player.uuid, BigDecimal.valueOf(amount), currency)
        return true
    }

    override fun name(amount: Double, currency: String): Component? {
        return if (currency.isEmpty()) null else Component.text(BEconomy.getAPI().getCurrencySymbol(currency))
    }
}
