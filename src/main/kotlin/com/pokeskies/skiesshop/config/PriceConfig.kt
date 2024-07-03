package com.pokeskies.skiesshop.config

class PriceConfig(
    val price: Double,
    val currency: String
) {
    override fun toString(): String {
        return "PriceConfig(price=$price, currency='$currency')"
    }
}
