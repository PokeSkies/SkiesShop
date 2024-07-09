package com.pokeskies.skiesshop.data

import java.util.UUID

class ShopTransaction(
    val player: UUID,
    val timestamp: Long,
    val shopId: String,
    val entryId: String,
    val entryPrice: Double,
    val currency: String,
    val amount: Int,
    val isPurchase: Boolean,
    val itemId: String
)
