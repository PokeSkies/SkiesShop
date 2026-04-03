package com.pokeskies.skiesshop.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.SkiesShop
import net.minecraft.core.component.DataComponentMap
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import java.lang.reflect.Type
import kotlin.jvm.optionals.getOrNull

object ItemCompareUtils {
    // Check if the stacks are equal based on the strictness level provided (ignores quantity)
    fun matches(player: ServerPlayer, a: ItemStack, b: ItemStack, comparison: ComparisonOption): Boolean {
        if (a.isEmpty && b.isEmpty) return true
        if (a.item != b.item) return false

        comparison.antiComponents?.let { tag ->
            try {
                val mapResult = DataComponentMap.CODEC.decode(SkiesShop.INSTANCE.nbtOpts, Utils.parseNBT(player, tag)).result().getOrNull()
                if (mapResult != null) {
                    val componentMap = mapResult.first
                    val bComponents = b.components

                    for (component in componentMap) {
                        if (bComponents.contains(component)) {
                            Utils.printDebug("Component $component from ANTI_COMPONENTS found in B, item is not a match!")
                            return false
                        }
                    }
                    return@let
                }
            } catch (e: Exception) {
                Utils.printError("There was an error while parsing the anti_components for an item comparison: ${e.message}")
            }
        }

        when (comparison.mode) {
            StrictnessLevel.NONE -> return true
            StrictnessLevel.CONTAINS -> {
                val aComponents = a.components
                val bComponents = b.components

                for (component in aComponents) {
                    if (!bComponents.contains(component)) {
                        return false
                    }
                }
            }
            StrictnessLevel.EXACT -> {
                val aComponents = a.components
                val bComponents = b.components

                // if they aint the same size, they aint a match!
                if (aComponents.size() != bComponents.size()) return false

                return aComponents == bComponents
            }
        }


        return true
    }

    class ComparisonOption(
        val mode: StrictnessLevel = StrictnessLevel.NONE,
        @SerializedName("anti_components")
        val antiComponents: CompoundTag? = null,
    ) {
        override fun toString(): String {
            return "ComparisonOption(mode=$mode, anti_components=$antiComponents)"
        }
    }

    enum class StrictnessLevel {
        NONE, // Item Types just need to be equal
        CONTAINS, // Components on A must be on B, but B may have extras
        EXACT; // A must match B exactly (all components the same)

        companion object {
            fun valueOfAnyCase(name: String): StrictnessLevel? {
                for (type in StrictnessLevel.entries) {
                    if (name.equals(type.name, true)) return type
                }
                return null
            }
        }

        internal class Adapter: JsonSerializer<StrictnessLevel>, JsonDeserializer<StrictnessLevel> {
            override fun serialize(src: StrictnessLevel, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                return JsonPrimitive(src.name)
            }

            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): StrictnessLevel {
                val strictnessLevel = StrictnessLevel.valueOfAnyCase(json.asString)

                if (strictnessLevel == null) {
                    Utils.printError("Could not deserialize Strictness Level '${json.asString}', using NONE as a default!")
                    return NONE
                }

                return strictnessLevel
            }
        }
    }
}