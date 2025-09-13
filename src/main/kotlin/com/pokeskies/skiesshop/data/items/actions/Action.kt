package com.pokeskies.skiesshop.data.items.actions

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import eu.pb4.sgui.api.ClickType
import net.minecraft.server.level.ServerPlayer
import java.lang.reflect.Type

abstract class Action(
    val type: ActionType,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val click: List<GenericClickType> = listOf(GenericClickType.ANY)
) {
    abstract fun executeAction(player: ServerPlayer, gui: IRefreshableGui)

    fun matchesClick(buttonClick: ClickType): Boolean {
        return click.any { it.buttonClicks.contains(buttonClick) }
    }

    override fun toString(): String {
        return "Action(type=$type, click=$click)"
    }

    internal class Adaptor : JsonSerializer<Action>, JsonDeserializer<Action> {
        override fun serialize(src: Action, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src, src::class.java)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Action {
            val jsonObject: JsonObject = json.getAsJsonObject()
            val value = jsonObject.get("type").asString
            val type: ActionType? = ActionType.Companion.valueOfAnyCase(value)
            return try {
                context.deserialize(json, type!!.clazz)
            } catch (e: NullPointerException) {
                throw JsonParseException("Could not deserialize action type: $value", e)
            }
        }
    }
}
