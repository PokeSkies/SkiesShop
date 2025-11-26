package com.pokeskies.skiesshop.utils

import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.utils.TextUtils.plainSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.network.chat.Component

object TextUtils {
    val plainSerializer = PlainTextComponentSerializer.plainText()
}

fun String.asNative(): Component {
    return SkiesShop.INSTANCE.adventure.toNative(SkiesShop.MINI_MESSAGE.deserialize(this))
}

fun net.kyori.adventure.text.Component.asNative(): Component {
    return SkiesShop.INSTANCE.adventure.toNative(this)
}

fun String.asAdventure(): net.kyori.adventure.text.Component {
    return SkiesShop.MINI_MESSAGE.deserialize(this)
}

fun net.kyori.adventure.text.Component.asPlain(): String {
    return plainSerializer.serialize(this)
}

fun Component.asPlain(): String {
    return plainSerializer.serialize(this.asComponent())
}
