package com.pokeskies.skiesshop.economy

import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.utils.Utils
import net.impactdev.impactor.api.economy.EconomyService
import net.impactdev.impactor.api.economy.accounts.Account
import net.impactdev.impactor.api.economy.currency.Currency
import net.kyori.adventure.key.Key
import net.minecraft.server.level.ServerPlayer
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

object EconomyManager {
    fun balance(player: ServerPlayer, currency: Currency) : Double {
        return getAccount(player.uuid, currency)?.thenCompose(Account::balanceAsync)?.join()?.toDouble() ?: 0.0
    }

    fun withdraw(player: ServerPlayer, amount: Double, currency: Currency) : Boolean {
        return getAccount(player.uuid, currency)?.join()?.withdrawAsync(BigDecimal(amount))?.join()?.successful() ?: false
    }

    fun deposit(player: ServerPlayer, amount: Double, currency: Currency) : Boolean {
        return getAccount(player.uuid, currency)?.join()?.depositAsync(BigDecimal(amount))?.join()?.successful() ?: false
    }

    fun set(player: ServerPlayer, amount: Double, currency: Currency) : Boolean {
        return getAccount(player.uuid, currency)?.join()?.setAsync(BigDecimal(amount))?.join()?.successful() ?: false
    }

    fun getCurrencyFormatted(currency: Currency, singular: Boolean): String {
        return SkiesShop.INSTANCE.adventure.toNative(if (singular) currency.singular() else currency.plural()).string
    }

    private fun getAccount(uuid: UUID, currency: Currency): CompletableFuture<Account>? {
        return EconomyService.instance().account(currency, uuid)
    }

    fun getCurrency(id: String) : Currency? {
        if (id.isEmpty()) {
            return EconomyService.instance().currencies().primary()
        }

        val currency: Optional<Currency> = EconomyService.instance().currencies().currency(Key.key(id))
        if (currency.isEmpty) {
            Utils.printError(
                "Could not find a currency by the ID $id! Valid currencies: " +
                        "${EconomyService.instance().currencies().registered().map { it.key() }}"
            )
            return null
        }

        return currency.get()
    }
}
