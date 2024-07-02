package com.pokeskies.skiesshop.utils

import com.pokeskies.skiesshop.SkiesShop
import net.minecraft.network.chat.Component

object TextUtils {
    fun toNative(text: String): Component {
        return SkiesShop.INSTANCE.adventure.toNative(SkiesShop.MINI_MESSAGE.deserialize(text))
    }

    fun toComponent(text: String): net.kyori.adventure.text.Component {
        return SkiesShop.MINI_MESSAGE.deserialize(text)
    }
}
