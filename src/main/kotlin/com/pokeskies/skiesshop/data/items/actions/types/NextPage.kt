package com.pokeskies.skiesshop.data.items.actions.types

import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.data.items.actions.ActionType
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.gui.ShopGUI
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer

class NextPage(
    click: List<GenericClickType> = listOf(GenericClickType.ANY)
) : Action(ActionType.NEXT_PAGE, click) {
    override fun executeAction(player: ServerPlayer, gui: IRefreshableGui) {
        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}) $this")

        if (gui !is ShopGUI) {
            Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}) tried to execute a NextPage action not in a ShopGUI.")
            return
        }

        gui.nextPage()
    }

    override fun toString(): String {
        return "NextPage()"
    }
}
