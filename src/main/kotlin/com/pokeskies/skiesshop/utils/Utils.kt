package com.pokeskies.skiesshop.utils

import com.google.gson.*
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.config.ConfigManager
import com.pokeskies.skiesshop.placeholders.PlaceholderManager
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Utils {
    val formatter = DateTimeFormatter.ofPattern("yyMMdd-HHmmss")

    // Useful logging functions
    fun printDebug(message: String, bypassCheck: Boolean = false) {
        if (bypassCheck || ConfigManager.CONFIG.debug)
            SkiesShop.LOGGER.info("[${SkiesShop.MOD_NAME}] DEBUG: $message")
    }

    fun printError(message: String) {
        SkiesShop.LOGGER.error("[${SkiesShop.MOD_NAME}] ERROR: $message")
    }

    fun printInfo(message: String) {
        SkiesShop.LOGGER.info("[${SkiesShop.MOD_NAME}] $message")
    }

    fun parseNBT(player: ServerPlayer, tag: CompoundTag): CompoundTag {
        val parsedNBT = tag.copy()
        for (key in parsedNBT.allKeys) {
            var element = parsedNBT.get(key)
            if (element != null) {
                when (element) {
                    is StringTag -> {
                        element = StringTag.valueOf(PlaceholderManager.parse(player, element.asString))
                    }
                    is ListTag -> {
                        val parsed = ListTag()
                        for (entry in element) {
                            if (entry is StringTag) {
                                parsed.add(StringTag.valueOf(PlaceholderManager.parse(player, entry.asString)))
                            } else {
                                parsed.add(entry)
                            }
                        }
                        element = parsed
                    }
                    is CompoundTag -> {
                        element = parseNBT(player, element)
                    }
                }

                if (element != null) {
                    parsedNBT.put(key, element)
                }
            }
        }
        return parsedNBT
    }

    // Sends a player a sound packet
    fun sendPlayerSound(player: ServerPlayer, sound: SoundEvent, volume: Float, pitch: Float) {
        player.connection.send(
            ClientboundSoundPacket(
                Holder.direct(sound),
                SoundSource.MASTER,
                player.x,
                player.y,
                player.z,
                volume,
                pitch,
                player.level().getRandom().nextLong()
            )
        )
    }

    // Formats a time in seconds to the format "xd yh zm zs", but truncates unncessary parts
    fun getFormattedTime(time: Long): String {
        if (time <= 0) return "0s"
        val timeFormatted: MutableList<String> = ArrayList()
        val days = time / 86400
        val hours = time % 86400 / 3600
        val minutes = time % 86400 % 3600 / 60
        val seconds = time % 86400 % 3600 % 60
        if (days > 0) {
            timeFormatted.add(days.toString() + "d")
        }
        if (hours > 0) {
            timeFormatted.add(hours.toString() + "h")
        }
        if (minutes > 0) {
            timeFormatted.add(minutes.toString() + "m")
        }
        if (seconds > 0) {
            timeFormatted.add(seconds.toString() + "s")
        }
        return java.lang.String.join(" ", timeFormatted)
    }

    fun getCurrentDateTimeFormatted(): String {
        val currentDateTime = LocalDateTime.now()
        return currentDateTime.format(formatter)
    }

    // Useful GSON seralizers for Minecraft Codecs. Thank you to Patbox for these
    data class RegistrySerializer<T>(val registry: Registry<T>) : JsonSerializer<T>, JsonDeserializer<T> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): T? {
            var parsed = if (json.isJsonPrimitive) registry.get(ResourceLocation.tryParse(json.asString)) else null
            if (parsed == null)
                printError("There was an error while deserializing a Registry Type: $registry")
            return parsed
        }
        override fun serialize(src: T, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(registry.getId(src).toString())
        }
    }

    data class CodecSerializer<T>(val codec: Codec<T>) : JsonSerializer<T>, JsonDeserializer<T> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): T? {
            return try {
                codec.decode(JsonOps.INSTANCE, json).orThrow.first
            } catch (_: Throwable) {
                printError("There was an error while deserializing a Codec: $codec")
                null
            }
        }

        override fun serialize(src: T?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return try {
                if (src != null)
                    codec.encodeStart(JsonOps.INSTANCE, src).orThrow
                else
                    JsonNull.INSTANCE
            } catch (_: Throwable) {
                printError("There was an error while serializing a Codec: $codec")
                JsonNull.INSTANCE
            }
        }
    }
}

fun Inventory.canFit(stack: ItemStack): Boolean {
    var count = 0
    for (item in this.items) {
        if (item.isEmpty) {
            count += stack.maxStackSize
            if (count >= stack.count) return true
            continue
        }

        if (ItemStack.isSameItemSameComponents(item, stack) && item.count < item.maxStackSize) {
            count += item.maxStackSize - item.count
            if (count >= stack.count) return true
        }
    }

    return false
}