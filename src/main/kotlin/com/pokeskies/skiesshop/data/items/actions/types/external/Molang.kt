package com.pokeskies.skiesshop.data.items.actions.types.external

import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolve
import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.data.items.actions.ActionType
import com.pokeskies.skiesshop.gui.ConfirmGUI
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.gui.ShopGUI
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer

class Molang(
    click: List<GenericClickType> = listOf(GenericClickType.ANY),
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val script: List<String> = listOf()
) : Action(
    ActionType.MOLANG,
    click
) {
    override fun executeAction(player: ServerPlayer, gui: IRefreshableGui) {
        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}): $this")

        val manager = when (gui) {
            is ShopGUI -> gui.molang
            is ConfirmGUI -> gui.shopGUI.molang
            else -> null
        } ?: run {
            Utils.printError("[ACTION - ${type.name}] This action can only be used in a Shop GUI or Confirm GUI, but was used in a ${gui::class.java.simpleName}")
            return
        }

        manager.runtime.resolve(
            script.asExpressionLike(),
            mapOf("player" to player.asMoLangValue())
        )
    }

    override fun toString(): String {
        return "MolangAction(click=$click, script=$script)"
    }
}
