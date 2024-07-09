package com.pokeskies.skiesshop.config

import com.google.gson.annotations.SerializedName

class MainConfig(
    var debug: Boolean = false,
    var storage: StorageConfig = StorageConfig(),
) {

    open class StorageConfig(
        var enabled: Boolean = false,
        @SerializedName("mongo_uri")
        var mongoURI: String= "mongodb://localhost:27017",
        @SerializedName("database_name")
        var databaseName: String = "SkiesShop",
        @SerializedName("transactions_collection")
        var transactionsCollection: String = "transactions",
    ) {
        override fun toString(): String {
            return "StorageConfig(enabled=$enabled, mongoURI='$mongoURI', databaseName='$databaseName', transactionsCollection='$transactionsCollection')"
        }
    }

    override fun toString(): String {
        return "MainConfig(debug=$debug, storage=$storage)"
    }
}
