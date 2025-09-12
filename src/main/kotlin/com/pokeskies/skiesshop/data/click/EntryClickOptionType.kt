package com.pokeskies.skiesshop.data.click

import com.pokeskies.skiesshop.data.click.types.Buy
import com.pokeskies.skiesshop.data.click.types.OpenConfirmMenu
import com.pokeskies.skiesshop.data.click.types.Sell

enum class EntryClickOptionType(val identifier: String, val clazz: Class<out EntryClickOption>) {
    OPEN_CONFIRM_MENU("open_confirm_menu", OpenConfirmMenu::class.java),
    BUY("buy", Buy::class.java),
    SELL("sell", Sell::class.java);

    companion object {
        fun valueOfAnyCase(name: String): EntryClickOptionType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }
}
