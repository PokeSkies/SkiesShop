package com.pokeskies.skiesshop

import com.pokeskies.skiesshop.config.ConfigManager
import com.pokeskies.skiesshop.data.ShopInstance

object SkiesShopManager {
    private val instances: MutableMap<String, ShopInstance> = mutableMapOf()

    fun load() {
        instances.clear()

        ConfigManager.SHOPS.forEach { (id, config) ->
            instances[id] = ShopInstance.create(config)
        }
    }

    fun getShop(id: String): ShopInstance? {
        return instances[id]
    }

    fun getAllShops(): Collection<ShopInstance> {
        return instances.values
    }
}