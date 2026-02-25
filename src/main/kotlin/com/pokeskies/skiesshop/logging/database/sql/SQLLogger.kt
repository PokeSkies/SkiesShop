package com.pokeskies.skiesshop.logging.database.sql

import com.pokeskies.skiesshop.config.LoggingOptions
import com.pokeskies.skiesshop.data.ShopTransaction
import com.pokeskies.skiesshop.data.TransactionType
import com.pokeskies.skiesshop.logging.ILogger
import com.pokeskies.skiesshop.logging.LoggerType
import com.pokeskies.skiesshop.logging.database.sql.providers.MySQLProvider
import com.pokeskies.skiesshop.logging.database.sql.providers.SQLiteProvider
import com.pokeskies.skiesshop.utils.ConnectionProvider
import java.sql.SQLException
import java.util.*

class SQLLogger(private val config: LoggingOptions): ILogger {
    private val connectionProvider: ConnectionProvider = when (config.type) {
        LoggerType.MYSQL -> MySQLProvider(config)
        LoggerType.SQLITE -> SQLiteProvider(config)
        else -> throw IllegalStateException("Invalid logging type!")
    }

    init {
        connectionProvider.init()
    }

    override fun logTransaction(transaction: ShopTransaction): Boolean {
        return try {
            connectionProvider.createConnection().use { conn ->
                val sql = """
                INSERT INTO ${config.tablePrefix}transactions 
                (`player`, `timestamp`, `shopId`, `entryId`, `type`, `price`, `amount`)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
                val statement = conn.prepareStatement(sql)
                statement.setString(1, transaction.player.toString())
                statement.setLong(2, transaction.timestamp)
                statement.setString(3, transaction.shopId)
                statement.setString(4, transaction.entryId)
                statement.setString(5, transaction.type.name)
                statement.setDouble(6, transaction.price)
                statement.setInt(7, transaction.amount)
                statement.executeUpdate()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getUserTransactions(uuid: UUID): List<ShopTransaction> {
        val transactions = mutableListOf<ShopTransaction>()
        try {
            connectionProvider.createConnection().use { conn ->
                val statement = conn.prepareStatement(
                    "SELECT * FROM ${config.tablePrefix}transactions WHERE `player` = ?"
                )
                statement.setString(1, uuid.toString())
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    transactions.add(ShopTransaction(
                        UUID.fromString(resultSet.getString("player")),
                        resultSet.getLong("timestamp"),
                        resultSet.getString("shopId"),
                        resultSet.getString("entryId"),
                        TransactionType.valueOf(resultSet.getString("type")),
                        resultSet.getDouble("price"),
                        resultSet.getInt("amount")
                    ))
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return transactions
    }

    override fun getAllTransactions(): List<ShopTransaction> {
        val transactions = mutableListOf<ShopTransaction>()
        try {
            connectionProvider.createConnection().use { conn ->
                val statement = conn.prepareStatement(
                    "SELECT * FROM ${config.tablePrefix}transactions"
                )
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    transactions.add(ShopTransaction(
                        UUID.fromString(resultSet.getString("player")),
                        resultSet.getLong("timestamp"),
                        resultSet.getString("shopId"),
                        resultSet.getString("entryId"),
                        TransactionType.valueOf(resultSet.getString("type")),
                        resultSet.getDouble("price"),
                        resultSet.getInt("amount")
                    ))
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return transactions
    }

    override fun canListLogs(): Boolean {
        return true
    }

    override fun close() {
        connectionProvider.shutdown()
    }
}
