package com.pokeskies.skiesshop.logging.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.connection.ClusterSettings
import com.pokeskies.skiesshop.config.LoggingOptions
import com.pokeskies.skiesshop.data.ShopTransaction
import com.pokeskies.skiesshop.logging.ILogger
import com.pokeskies.skiesshop.utils.UUIDCodec
import com.pokeskies.skiesshop.utils.Utils
import org.bson.UuidRepresentation
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import java.io.IOException
import java.util.*

class MongoLogger(config: LoggingOptions): ILogger {
    private var mongoClient: MongoClient? = null
    private var mongoDatabase: MongoDatabase? = null
    private var transactionsCollection: MongoCollection<ShopTransaction>? = null

    init {
        try {
            var settings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)

            settings = if (config.urlOverride.isNotEmpty()) {
                settings.applyConnectionString(ConnectionString(config.urlOverride))
            } else {
                settings.credential(MongoCredential.createCredential(
                    config.username,
                    "admin",
                    config.password.toCharArray()
                )).applyToClusterSettings { builder: ClusterSettings.Builder ->
                    builder.hosts(listOf(ServerAddress(config.host, config.port)))
                }
            }

            this.mongoClient = MongoClients.create(settings.build())

            val codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(UUIDCodec()),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
            )

            this.mongoDatabase = mongoClient!!.getDatabase(config.database)
                .withCodecRegistry(codecRegistry)
            this.transactionsCollection = this.mongoDatabase!!.getCollection("transactions", ShopTransaction::class.java)
        } catch (e: Exception) {
            throw IOException("Error while attempting to setup Mongo Database for Logging: $e")
        }
    }

    override fun logTransaction(transaction: ShopTransaction): Boolean {
        if (mongoDatabase == null) {
            Utils.printError("There was an error while attempting to save transaction log to the Mongo database!")
            return false
        }
        val result = this.transactionsCollection?.insertOne(transaction)

        return result?.wasAcknowledged() ?: false
    }

    override fun getUserTransactions(uuid: UUID): List<ShopTransaction> {
        if (mongoDatabase == null) {
            Utils.printError("There was an error while attempting to fetch transaction log from the Mongo database!")
            return listOf()
        }
        return transactionsCollection?.find(Filters.eq("player", uuid))?.toList() ?: listOf()
    }

    override fun getAllTransactions(): List<ShopTransaction> {
        if (mongoDatabase == null) {
            Utils.printError("There was an error while attempting to fetch transaction logs from the Mongo database!")
            return listOf()
        }
        return transactionsCollection?.find()?.toList() ?: listOf()
    }

    override fun canListLogs(): Boolean {
        return true
    }

    override fun close() {
        mongoClient?.close()
    }
}
