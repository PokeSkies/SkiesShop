package com.pokeskies.skiesshop.data.items.actions.types.internal

import com.pokeskies.skiesshop.SkiesShopAPI
import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.data.items.actions.ActionType
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer

class OpenShop(
    click: List<GenericClickType> = listOf(GenericClickType.ANY),
    private val id: String = ""
) : Action(ActionType.OPEN_SHOP, click) {
    override fun executeAction(player: ServerPlayer, gui: IRefreshableGui) {
        val shop = SkiesShopAPI.getShop(id)
        if (shop == null) {
            Utils.printError("[ACTION - ${type.name}] There was an error while executing for player ${player.name}: Could not find a Shop with the ID $id!")
            return
        }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), Shop($shop): $this")

        gui.close()
        shop.open(player, gui)
    }

    override fun toString(): String {
        return "OpenShop(id='$id')"
    }
}
