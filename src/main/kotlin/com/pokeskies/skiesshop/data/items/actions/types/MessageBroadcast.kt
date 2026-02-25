package com.pokeskies.skiesshop.data.items.actions.types

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.data.items.actions.ActionType
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.placeholders.PlaceholderManager
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesshop.utils.Utils
import com.pokeskies.skiesshop.utils.asNative
import net.minecraft.server.level.ServerPlayer

class MessageBroadcast(
    click: List<GenericClickType> = listOf(GenericClickType.ANY),
    @JsonAdapter(FlexibleListAdaptorFactory::class) @SerializedName("message",  alternate = ["messages"])
    private val message: List<String> = emptyList()
) : Action(ActionType.BROADCAST, click) {
    override fun executeAction(player: ServerPlayer, gui: IRefreshableGui) {
        val parsedMessages = message.map { PlaceholderManager.parse(player, it) }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Parsed Messages($parsedMessages): $this")

        for (line in parsedMessages) {
            SkiesShop.INSTANCE.adventure.all().sendMessage(line.asNative())
        }
    }

    override fun toString(): String {
        return "MessageBroadcast(click=$click, message=$message)"
    }
}
