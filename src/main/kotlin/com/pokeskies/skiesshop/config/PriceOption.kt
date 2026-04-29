package com.pokeskies.skiesshop.config

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.data.entry.requirements.RequirementOptions
import com.pokeskies.skiesshop.economy.EconomyType
import com.pokeskies.skiesshop.utils.asPlain
import com.pokeskies.skiesshop.utils.trimTrailingZeros

class PriceOption(
    val price: Double,
    val economy: EconomyType,
    val currency: String,
    val name: String?,
    @SerializedName("max_amount")
    val maxAmount: Int? = null,
    var requirements: RequirementOptions? = null
) {
    companion object {
        fun formatPricing(price: Double): String {
            return if (ConfigManager.CONFIG.truncatePrices)
                price.trimTrailingZeros() else price.toString()
        }
    }

    fun getCurrencyName(): String {
        return name ?: SkiesShop.INSTANCE.getEconomyService(economy)?.name(price, currency)?.asPlain() ?: ""
    }

    override fun toString(): String {
        return "PriceOption(price=$price, economy=$economy, currency='$currency', name='$name')"
    }
}
