package com.pokeskies.skiesshop.storage.database.sql

import com.google.gson.reflect.TypeToken
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.config.StorageOptions
import com.pokeskies.skiesshop.data.UserData
import com.pokeskies.skiesshop.storage.IStorage
import com.pokeskies.skiesshop.storage.StorageType
import com.pokeskies.skiesshop.storage.database.sql.providers.MySQLProvider
import com.pokeskies.skiesshop.storage.database.sql.providers.SQLiteProvider
import com.pokeskies.skiesshop.utils.ConnectionProvider
import java.lang.reflect.Type
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture

class SQLStorage(private val config: StorageOptions) : IStorage {
    private val connectionProvider: ConnectionProvider = when (config.type) {
        StorageType.MYSQL -> MySQLProvider(config)
        StorageType.SQLITE -> SQLiteProvider(config)
        else -> throw IllegalStateException("Invalid storage type!")
    }
    private val keysType: Type = object : TypeToken<HashMap<String, Int>>() {}.type

    init {
        connectionProvider.init()
    }

    override fun getUser(uuid: UUID): UserData {
        val userData = UserData(uuid)
        try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                val result = statement.executeQuery(String.format("SELECT * FROM ${config.tablePrefix}userdata WHERE uuid='%s'", uuid.toString()))
                if (result != null && result.next()) {

                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return userData
    }

    override fun saveUser(uuid: UUID, userData: UserData): Boolean {
        return try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                statement.execute(String.format("REPLACE INTO ${config.tablePrefix}userdata (uuid) VALUES ('%s')",
                    uuid.toString()
                ))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getUserAsync(uuid: UUID): CompletableFuture<UserData> {
        return CompletableFuture.supplyAsync({
            try {
                val result = getUser(uuid)
                result
            } catch (e: Exception) {
                UserData(uuid)  // Return default data rather than throwing
            }
        }, SkiesShop.INSTANCE.asyncExecutor)
    }

    override fun saveUserAsync(uuid: UUID, userData: UserData): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync({
            saveUser(uuid, userData)
        }, SkiesShop.INSTANCE.asyncExecutor)
    }

    override fun close() {
        connectionProvider.shutdown()
    }
}
