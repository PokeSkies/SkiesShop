package com.pokeskies.skiesshop.data

import com.google.gson.*
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.util.StringRepresentable
import java.lang.reflect.Type

enum class TransactionType(val identifier: String): StringRepresentable {
    BUY("buy"),
    SELL("sell"),;

    override fun getSerializedName(): String {
        return this.identifier
    }

    companion object {
        fun valueOfAnyCase(name: String): TransactionType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }

    internal class Adaptor(): JsonSerializer<TransactionType>, JsonDeserializer<TransactionType> {
        override fun serialize(src: TransactionType, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.identifier)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TransactionType {
            val click = valueOfAnyCase(json.asString)

            if (click == null) {
                Utils.printError("Could not deserialize Transaction Type '${json.asString}'! Defaulting to BUY.")
                return BUY
            }

            return click
        }
    }
}