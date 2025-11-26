package com.pokeskies.skiesshop.placeholders

import net.minecraft.server.level.ServerPlayer

interface IPlaceholderService {
    fun parsePlaceholders(player: ServerPlayer, text: String): String
    fun registerPlayer(placeholder: PlayerPlaceholder)
    fun registerServer(placeholder: ServerPlaceholder)
    fun finalizeRegister()
}
