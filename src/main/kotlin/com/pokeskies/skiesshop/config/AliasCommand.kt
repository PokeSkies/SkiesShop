package com.pokeskies.skiesshop.config

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.commands.CommandSourceStack
import java.lang.reflect.Type

class AliasCommand(
    val command: String,
    val permission: String? = null
) {
    fun hasPermission(ctx: CommandSourceStack): Boolean {
        return if (permission != null) Permissions.check(ctx, permission, 1) else true
    }

    override fun toString(): String {
        return "AliasCommand(command='$command', permission=$permission)"
    }

    class Adapter: JsonDeserializer<AliasCommand>, JsonSerializer<AliasCommand> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): AliasCommand {
            if (json.isJsonNull) throw JsonParseException("Somehow the JSON was null when it can't be!")

            return when {
                json.isJsonPrimitive -> {
                    val prim = json.asJsonPrimitive
                    AliasCommand(if (prim.isString) prim.asString else prim.toString())
                }
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    AliasCommand(if (obj.has("command") && !obj.get("command").isJsonNull)
                        obj.get("command").asString else throw JsonParseException("Missing the command field for AliasCommand, it is required: $obj"),
                        if (obj.has("permission") && !obj.get("permission").isJsonNull)
                            obj.get("permission").asString else null
                    )
                }
                else -> throw JsonParseException("Unsupported JSON for AliasCommand: $json")
            }
        }

        override fun serialize(
            src: AliasCommand,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            if (src.permission == null) return JsonPrimitive(src.command)

            val obj = JsonObject()
            obj.addProperty("command", src.command)
            obj.addProperty("permission", src.permission)
            return obj
        }
    }
}