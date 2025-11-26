package com.pokeskies.skiesshop

import com.pokeskies.skiesshop.data.ShopInstance
import net.minecraft.server.level.ServerPlayer

object SkiesShopAPI {
    fun openShop(player: ServerPlayer, shopId: String) {
        SkiesShopManager.getShop(shopId)?.open(player)
    }

    fun getShop(id: String): ShopInstance? {
        return SkiesShopManager.getShop(id)
    }

    fun getShopIDs(): List<String> {
        return SkiesShopManager.getAllShops().map { it.id }
    }

    fun getAllShops(): List<ShopInstance> {
        return SkiesShopManager.getAllShops().toList()
    }
}