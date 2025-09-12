package com.pokeskies.skiesshop.logging.database.sql.providers

import com.pokeskies.skiesshop.config.LoggingOptions
import com.pokeskies.skiesshop.utils.ConnectionProvider
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.SQLException

abstract class HikariCPProvider(private val loggingOptions: LoggingOptions): ConnectionProvider {
    private lateinit var dataSource: HikariDataSource

    @Throws(SQLException::class)
    override fun init() {
        val config = HikariConfig()
        configure(config)

        config.username = loggingOptions.username
        config.password = loggingOptions.password
        config.jdbcUrl = getConnectionURL()
        config.driverClassName = getDriverClassName()
        config.poolName = "skiesshop-${getDriverName()}"
        loggingOptions.properties.forEach { (propertyName, value) -> config.addDataSourceProperty(propertyName, value) }
        config.maximumPoolSize = loggingOptions.poolSettings.maximumPoolSize
        config.minimumIdle = loggingOptions.poolSettings.minimumIdle
        config.keepaliveTime = loggingOptions.poolSettings.keepaliveTime
        config.connectionTimeout = loggingOptions.poolSettings.connectionTimeout
        config.idleTimeout = loggingOptions.poolSettings.idleTimeout
        config.maxLifetime = loggingOptions.poolSettings.maxLifetime

        dataSource = HikariDataSource(config)

        val idColumn = when (getDriverName().lowercase()) {
            "mysql", "mariadb" -> "`id` INT PRIMARY KEY AUTO_INCREMENT"
            "sqlite" -> "`id` INTEGER PRIMARY KEY AUTOINCREMENT"
            "h2" -> "`id` INT AUTO_INCREMENT PRIMARY KEY"
            else -> throw IllegalArgumentException("Unsupported database")
        }

        try {
            createConnection().use {
                val statement = it.createStatement()
                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS ${loggingOptions.tablePrefix}transactions (" +
                            "${idColumn}, " +
                            "`player` VARCHAR(36) NOT NULL, " +
                            "`timestamp` BIGINT, " +
                            "`shopId` VARCHAR(255), " +
                            "`entryId` VARCHAR(255), " +
                            "`type` VARCHAR(16), " +
                            "`price` DOUBLE, " +
                            "`amount` INT, " +
                            "`entry` TEXT" +
                            ")"
                )
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    @Throws(SQLException::class)
    override fun shutdown() {
        if (this::dataSource.isInitialized)
            dataSource.close()
    }

    @Throws(SQLException::class)
    override fun createConnection(): Connection {
        if (!this::dataSource.isInitialized)
            throw SQLException("The data source is not initialized!")
        return dataSource.connection
    }

    abstract fun configure(config: HikariConfig)
    abstract fun getConnectionURL(): String
    abstract fun getDriverClassName(): String
    abstract fun getDriverName(): String

    override fun getName(): String {
        return "hikaricp - ${getDriverName()}"
    }

    override fun isInitialized(): Boolean {
        if (!this::dataSource.isInitialized)
            return false
        return !dataSource.isClosed
    }
}