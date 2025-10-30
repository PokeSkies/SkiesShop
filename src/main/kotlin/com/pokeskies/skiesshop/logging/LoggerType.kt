package com.pokeskies.skiesshop.logging

import com.google.gson.*
import com.pokeskies.skiesshop.utils.Utils
import java.lang.reflect.Type

enum class LoggerType(val identifier: String) {
    CONSOLE("console"),
    MONGO("mongo"),
    MYSQL("mysql"),
    SQLITE("sqlite");

    companion object {
        fun valueOfAnyCase(identifier: String): LoggerType? {
            for (type in entries) {
                if (identifier.equals(type.identifier, true)) return type
            }
            return null
        }
    }

    internal class Adapter: JsonSerializer<LoggerType>, JsonDeserializer<LoggerType> {
        override fun serialize(src: LoggerType, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.identifier)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LoggerType {
            val storageType = valueOfAnyCase(json.asString)

            if (storageType == null) {
                Utils.printError("Could not deserialize Logging Type '${json.asString}' using SQLite as backup!")
                return SQLITE
            }

            return storageType
        }
    }
}
