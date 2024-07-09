package com.pokeskies.skiesshop.data

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.pokeskies.skiesshop.config.ConfigManager
import com.pokeskies.skiesshop.utils.Utils
import org.bson.UuidRepresentation
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import java.io.IOException
import java.util.*

object MongoDBHandler {
    private var mongoClient: MongoClient? = null
    private var mongoDatabase: MongoDatabase? = null

    private var transactionsCollection: MongoCollection<ShopTransaction>? = null

    fun initialize() {
        if (!ConfigManager.CONFIG.storage.enabled) {
            Utils.printInfo("MongoDB is not enabled, skipping setup...")
            return
        }
        try {
            val connectionString = ConnectionString(ConfigManager.CONFIG.storage.mongoURI)

            this.mongoClient = MongoClients.create(MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applyConnectionString(connectionString)
                .build())

            val codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
            )

            this.mongoDatabase = mongoClient!!.getDatabase(ConfigManager.CONFIG.storage.databaseName)
                .withCodecRegistry(codecRegistry)

            this.transactionsCollection = this.mongoDatabase!!.getCollection(ConfigManager.CONFIG.storage.transactionsCollection, ShopTransaction::class.java)

            if (mongoDatabase != null && transactionsCollection != null) {
                Utils.printInfo("MongoDB has been successfully setup!")
            } else {
                Utils.printError("There was an error while attempting to setup Mongo Database!")
            }
        } catch (e: Exception) {
            throw IOException("Error while attempting to setup Mongo Database: $e")
        }
    }

    fun getUserTransactions(uuid: UUID): List<ShopTransaction> {
        if (!ConfigManager.CONFIG.storage.enabled) return listOf()
        if (mongoDatabase == null) {
            Utils.printError("There was an error while attempting to fetch data from the Mongo database!")
            return listOf()
        }
        return transactionsCollection?.find(Filters.eq("player", uuid))?.toList() ?: listOf()
    }

    fun getAllRecords(): List<ShopTransaction> {
        if (!ConfigManager.CONFIG.storage.enabled) return listOf()
        if (mongoDatabase == null) {
            Utils.printError("There was an error while attempting to fetch data from the Mongo database!")
            return listOf()
        }
        return transactionsCollection?.find()?.toList() ?: listOf()
    }

    fun saveUserTransaction(transaction: ShopTransaction): Boolean {
        if (!ConfigManager.CONFIG.storage.enabled) return false
        if (mongoDatabase == null) {
            Utils.printError("There was an error while attempting to save data to the Mongo database, database is null!")
            return false
        }
        try {
            transactionsCollection?.insertOne(transaction)
        } catch (e: Exception) {
            Utils.printError("There was an error while attempting to save data to the Mongo database, insert exception: $e")
            return false
        }

        return true
    }

    fun close() {
        mongoClient?.close()
    }
}
