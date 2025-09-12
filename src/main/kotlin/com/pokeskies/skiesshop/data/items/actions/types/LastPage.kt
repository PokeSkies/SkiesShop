package com.pokeskies.skiesshop.data.items.actions.types

import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.data.items.actions.ActionType
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.ShopGUI
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer

class LastPage(
    type: ActionType = ActionType.LAST_PAGE,
    click: List<GenericClickType> = listOf(GenericClickType.ANY)
) : Action(type, click) {
    override fun executeAction(player: ServerPlayer, gui: ShopGUI) {
        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}) $this")

        gui.lastPage()
    }

    override fun toString(): String {
        return "LastPage()"
    }
}
