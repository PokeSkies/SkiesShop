package com.pokeskies.skiesshop.logging.database.sql.providers

import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.config.LoggingOptions
import com.zaxxer.hikari.HikariConfig
import java.io.File

class H2Provider(config: LoggingOptions) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:h2:%s;AUTO_SERVER=TRUE",
        File(SkiesShop.INSTANCE.configDir, "logs.db").toPath().toAbsolutePath()
    )

    override fun getDriverClassName(): String = "org.h2.Driver"
    override fun getDriverName(): String = "h2"
    override fun configure(config: HikariConfig) {}
}
