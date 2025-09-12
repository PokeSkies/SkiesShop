package com.pokeskies.skiesshop.logging

import com.pokeskies.skiesshop.config.ConfigManager
import com.pokeskies.skiesshop.data.ShopTransaction
import java.util.*

object LoggerManager {
    private lateinit var logger: ILogger
    private var enabled: Boolean = false

    fun load() {
        try {
            logger = ILogger.load(ConfigManager.CONFIG.logging)
            enabled = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logTransaction(transaction: ShopTransaction) {
        if (enabled) {
            logger.logTransaction(transaction)
        }
    }

    fun getUserLogs(uuid: UUID): List<ShopTransaction> {
        return if (canListLogs()) {
            logger.getUserTransactions(uuid)
        } else {
            emptyList()
        }
    }

    fun getAllLogs(): List<ShopTransaction> {
        return if (canListLogs()) {
            logger.getAllTransactions()
        } else {
            emptyList()
        }
    }

    fun canListLogs(): Boolean {
        return enabled && logger.canListLogs()
    }
}