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
import net.minecraft.server.level.ServerPlayer

class CommandConsole(
    click: List<GenericClickType> = listOf(GenericClickType.ANY),
    @JsonAdapter(FlexibleListAdaptorFactory::class) @SerializedName("commands",  alternate = ["command"])
    private val commands: List<String> = emptyList()
) : Action(ActionType.COMMAND_CONSOLE, click) {
    override fun executeAction(player: ServerPlayer, gui: IRefreshableGui) {
        val parsedCommands = commands.map { PlaceholderManager.parse(player, it) }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Parsed Commands($parsedCommands): $this")

        for (command in parsedCommands) {
            SkiesShop.INSTANCE.server.commands.performPrefixedCommand(
                SkiesShop.INSTANCE.server.createCommandSourceStack(),
                command
            )
        }
    }

    override fun toString(): String {
        return "CommandConsole(click=$click, commands=$commands)"
    }
}
