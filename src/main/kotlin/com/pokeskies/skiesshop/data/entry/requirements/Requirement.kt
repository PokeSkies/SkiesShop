package com.pokeskies.skiesshop.data.entry.requirements

import com.google.gson.*
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.utils.Utils
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.level.ServerPlayer
import java.lang.reflect.Type

abstract class Requirement(
    val type: RequirementType? = null,
    val comparison: ComparisonType = ComparisonType.EQUALS
) {
    abstract fun checkRequirements(player: ServerPlayer, gui: IRefreshableGui): Boolean

    open fun allowedComparisons(): List<ComparisonType> {
        return emptyList()
    }

    fun checkComparison(): Boolean {
        if (!allowedComparisons().contains(comparison)) {
            Utils.printError("Error while executing a $type Requirement check! Comparison ${comparison.identifier} is not allowed: ${allowedComparisons().map { it.identifier }}")
            return false
        }
        return true
    }

    override fun toString(): String {
        return "Requirement(type=$type, comparison=$comparison)"
    }

    internal class Adapter: JsonSerializer<Requirement>, JsonDeserializer<Requirement> {
        override fun serialize(src: Requirement, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src, src::class.java)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Requirement {
            val jsonObject: JsonObject = json.getAsJsonObject()
            val value = jsonObject.get("type").asString
            if (value == "molang" && !FabricLoader.getInstance().isModLoaded("cobblemon")) {
                throw JsonParseException("Molang action is not supported without the Cobblemon mod")
            }
            val type: RequirementType? = RequirementType.valueOfAnyCase(value)
            return try {
                context.deserialize(json, type!!.clazz)
            } catch (e: NullPointerException) {
                throw JsonParseException("Could not deserialize requirement type: $type", e)
            }
        }
    }
}
