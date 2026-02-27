package com.pokeskies.skiesshop.config

import com.google.gson.JsonElement
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.utils.Utils
import java.io.File
import java.io.FileReader
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.javaType

object Lang {
    var TRANSACTION_BUY_REQUIREMENTS = listOf("<red>You do not meet the requirements to purchase this item!")
    var TRANSACTION_BUY_BALANCE = listOf("<red>You do not have enough to purchase this item!")
    var TRANSACTION_BUY_INVENTORY = listOf("<red>You do not have enough inventory space to purchase this item!")
    var TRANSACTION_BUY = listOf("<green>Purchased %transaction_amount%x %transaction_entry_name% for %transaction_total%!")

    var TRANSACTION_SELL_REQUIREMENTS = listOf("<red>You do not meet the requirements to sell this item!")
    var TRANSACTION_SELL = listOf("<green>Sold %transaction_amount%x %transaction_entry_name% for %transaction_total%!")
    var TRANSACTION_SELL_ERROR = listOf("<red>There was an error processing your sale! Please contact an admin with the error: %transaction_timestamp%")
    var TRANSACTION_SELL_ITEMS = listOf("<red>You do not have the required items to sell!")

    // Errors resulting from misconfiguration
    var ERROR_STORAGE = listOf("<red>There was an error with the storage system! Please contact an admin.")
    var ERROR_NO_BASE_SHOP = listOf("<red>No base shop is configured! Please contact an admin.")
    var ERROR_SHOP_NOT_FOUND = listOf("<red>Shop %shop_id% not found! Please contact an admin.")
    var ERROR_TRANSACTION = listOf("<red>There was an error while getting your purchased items! Please contact an admin.")

    @OptIn(ExperimentalStdlibApi::class)
    fun init() {
        // Create default lang file if it doesn't exist
        val defaultMessages = mutableMapOf<String, JsonElement>()
        this::class.memberProperties.forEach { prop ->
            val value = prop.getter.call(this)
            defaultMessages[prop.name] = SkiesShop.INSTANCE.gsonPretty.toJsonTree(value)
        }

        val langFile = File(SkiesShop.INSTANCE.configDir, "lang.json")
        if (!langFile.exists()) {
            langFile.parentFile.mkdirs()
            langFile.writeText(SkiesShop.INSTANCE.gsonPretty.toJson(defaultMessages))
        } else {
            // Ensure all default messages are present in the file and write missing ones back
            val json = SkiesShop.INSTANCE.gsonPretty.fromJson(FileReader(langFile), JsonElement::class.java).asJsonObject
            defaultMessages.forEach { (key, value) ->
                if (!json.has(key)) {
                    json.add(key, value)
                }
            }
            langFile.writeText(SkiesShop.INSTANCE.gsonPretty.toJson(json))
        }

        try {
            val json = SkiesShop.INSTANCE.gsonPretty.fromJson(FileReader(langFile), JsonElement::class.java).asJsonObject

            // Iterate the variables in the Lang class and set their values
            this::class.memberProperties.forEach { prop ->
                // ANY because the variables in Lang can be of various types
                @Suppress("UNCHECKED_CAST")
                val property = prop as KMutableProperty1<Lang, Any>
                try {
                    // Get the property from the JsonObject and set it in the Lang class
                    json.get(prop.name)?.let {
                        property.set(this, SkiesShop.INSTANCE.gsonPretty.fromJson(it, prop.returnType.javaType))
                    }
                } catch (e: Exception) {
                    Utils.printError("Failed to load Language setting for ${prop.name}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Utils.printError("Failed to load language file 'lang.json': ${e.message}")
        }
    }
}