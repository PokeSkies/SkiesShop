package com.pokeskies.skiesshop.data.items.actions.types

import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.data.items.actions.ActionType
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer

class Back(
    click: List<GenericClickType> = listOf(GenericClickType.ANY),
) : Action(ActionType.BACK, click) {
    override fun executeAction(player: ServerPlayer, gui: IRefreshableGui) {
        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}): $this")

        gui.close()
        gui.previous?.open()
    }

    override fun toString(): String {
        return "Back()"
    }
}
