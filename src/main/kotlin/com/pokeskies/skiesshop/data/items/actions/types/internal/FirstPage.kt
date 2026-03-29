package com.pokeskies.skiesshop.data.items.actions.types.internal

import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.data.items.actions.ActionType
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.gui.ShopGUI
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer

class FirstPage(
    click: List<GenericClickType> = listOf(GenericClickType.ANY)
) : Action(ActionType.FIRST_PAGE, click) {
    override fun executeAction(player: ServerPlayer, gui: IRefreshableGui) {
        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}) $this")

        if (gui !is ShopGUI) {
            Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}) tried to execute a FirstPage action not in a ShopGUI.")
            return
        }

        gui.firstPage()
    }

    override fun toString(): String {
        return "FirstPage()"
    }
}
