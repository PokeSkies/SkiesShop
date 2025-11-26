package com.pokeskies.skiesshop.data.items.actions

import com.pokeskies.skiesshop.data.items.actions.types.*

enum class ActionType(val identifier: String, val clazz: Class<out Action>) {
    COMMAND_CONSOLE("command_console", CommandConsole::class.java),
    COMMAND_PLAYER("command_player", CommandPlayer::class.java),
    MESSAGE("message", MessagePlayer::class.java),
    BROADCAST("broadcast", MessageBroadcast::class.java),
    OPEN_SHOP("open_shop", OpenShop::class.java),
    CLOSE_SHOP("close_shop", CloseShop::class.java),
    NEXT_PAGE("next_page", NextPage::class.java),
    PREVIOUS_PAGE("previous_page", PreviousPage::class.java),
    LAST_PAGE("last_page", LastPage::class.java),
    FIRST_PAGE("first_page", FirstPage::class.java),
    BACK("back", Back::class.java);

    companion object {
        fun valueOfAnyCase(name: String): ActionType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }
}
