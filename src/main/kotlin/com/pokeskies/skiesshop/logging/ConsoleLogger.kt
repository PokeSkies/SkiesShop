package com.pokeskies.skiesshop.logging

import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.data.ShopTransaction
import java.util.*

class ConsoleLogger: ILogger {
    override fun logTransaction(transaction: ShopTransaction): Boolean {
        SkiesShop.LOGGER.info(transaction)
        return true
    }

    override fun getUserTransactions(uuid: UUID): List<ShopTransaction> {
        return emptyList()
    }

    override fun getAllTransactions(): List<ShopTransaction> {
        return emptyList()
    }

    override fun canListLogs(): Boolean {
        return false
    }
}