package com.pokeskies.skiesshop.logging.database.sql.providers

import com.pokeskies.skiesshop.config.LoggingOptions
import com.zaxxer.hikari.HikariConfig

class MySQLProvider(val config: LoggingOptions) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:mysql://%s:%d/%s",
        config.host,
        config.port,
        config.database
    )

    override fun getDriverClassName(): String = "com.mysql.cj.jdbc.Driver"
    override fun getDriverName(): String = "mysql"
    override fun configure(config: HikariConfig) {}
}
