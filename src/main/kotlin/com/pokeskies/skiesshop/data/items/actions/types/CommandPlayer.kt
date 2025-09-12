package com.pokeskies.skiesshop.data.items.actions.types

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.data.items.actions.ActionType
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.ShopGUI
import com.pokeskies.skiesshop.placeholders.PlaceholderManager
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer

class CommandPlayer(
    type: ActionType = ActionType.COMMAND_PLAYER,
    click: List<GenericClickType> = listOf(GenericClickType.ANY),
    @JsonAdapter(FlexibleListAdaptorFactory::class) @SerializedName("commands",  alternate = ["command"])
    private val commands: List<String> = emptyList(),
    @SerializedName("permission_level")
    private val permissionLevel: Int? = null
) : Action(type, click) {
    override fun executeAction(player: ServerPlayer, gui: ShopGUI) {
        val parsedCommands = commands.map { PlaceholderManager.parse(player, it) }

        var source = player.createCommandSourceStack()
        if (permissionLevel != null) {
            source = source.withPermission(permissionLevel)
        }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Parsed Commands($parsedCommands): $this")

        for (command in parsedCommands) {
            SkiesShop.INSTANCE.server.commands.performPrefixedCommand(
                source,
                command
            )
        }
    }

    override fun toString(): String {
        return "CommandPlayer(click=$click, commands=$commands)"
    }
}
