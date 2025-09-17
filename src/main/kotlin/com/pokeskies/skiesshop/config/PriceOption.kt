package com.pokeskies.skiesshop.config

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.economy.EconomyType
import com.pokeskies.skiesshop.utils.asPlain

class PriceOption(
    val price: Double,
    val economy: EconomyType,
    val currency: String,
    val name: String?,
    @SerializedName("max_amount")
    val maxAmount: Int? = null,
) {
    fun getCurrencyName(): String {
        return name ?: SkiesShop.INSTANCE.getEconomyService(economy)?.name(price, currency)?.asPlain() ?: ""
    }

    override fun toString(): String {
        return "PriceOption(price=$price, economy=$economy, currency='$currency', name='$name')"
    }
}
