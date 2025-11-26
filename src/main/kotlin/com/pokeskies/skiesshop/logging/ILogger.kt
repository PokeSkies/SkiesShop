package com.pokeskies.skiesshop.logging

import com.pokeskies.skiesshop.config.LoggingOptions
import com.pokeskies.skiesshop.data.ShopTransaction
import com.pokeskies.skiesshop.logging.database.MongoLogger
import com.pokeskies.skiesshop.logging.database.sql.SQLLogger
import java.util.*

interface ILogger {
    companion object {
        fun load(config: LoggingOptions): ILogger {
            return when (config.type) {
                LoggerType.CONSOLE -> ConsoleLogger()
                LoggerType.MONGO -> MongoLogger(config)
                LoggerType.MYSQL, LoggerType.SQLITE -> SQLLogger(config)
            }
        }
    }

    fun logTransaction(transaction: ShopTransaction): Boolean
    fun getUserTransactions(uuid: UUID): List<ShopTransaction>
    fun getAllTransactions(): List<ShopTransaction>
    fun canListLogs(): Boolean

    fun close() {}
}
