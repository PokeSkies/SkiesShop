package com.pokeskies.skiesshop.logging.database.sql.providers

import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.config.LoggingOptions
import com.zaxxer.hikari.HikariConfig
import java.io.File

class SQLiteProvider(config: LoggingOptions) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:sqlite:%s",
        File(SkiesShop.INSTANCE.configDir, "logs.db").toPath().toAbsolutePath()
    )

    override fun getDriverClassName(): String = "org.sqlite.JDBC"
    override fun getDriverName(): String = "sqlite"
    override fun configure(config: HikariConfig) {}
}
