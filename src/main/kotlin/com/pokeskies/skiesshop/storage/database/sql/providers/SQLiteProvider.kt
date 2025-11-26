package com.pokeskies.skiesshop.storage.database.sql.providers

import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.config.StorageOptions
import com.zaxxer.hikari.HikariConfig
import java.io.File

class SQLiteProvider(config: StorageOptions) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:sqlite:%s",
        File(SkiesShop.INSTANCE.configDir, "storage.db").toPath().toAbsolutePath()
    )

    override fun getDriverClassName(): String = "org.sqlite.JDBC"
    override fun getDriverName(): String = "sqlite"
    override fun configure(config: HikariConfig) {}
}
