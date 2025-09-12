package com.pokeskies.skiesshop.utils

import com.pokeskies.skiesshop.data.ShopTransaction
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult

fun interface ShopTransactionEvent {
    companion object {
        @JvmField
        val EVENT: Event<ShopTransactionEvent> =
            EventFactory.createArrayBacked(ShopTransactionEvent::class.java) { listeners ->
                ShopTransactionEvent { player, transaction ->
                    for (listener in listeners) {
                        val result = listener.execute(player, transaction)

                        if (result != InteractionResult.PASS) {
                            return@ShopTransactionEvent result;
                        }
                    }

                    return@ShopTransactionEvent InteractionResult.PASS;
                }
            }
    }

    fun execute(player: ServerPlayer, transaction: ShopTransaction): InteractionResult
}