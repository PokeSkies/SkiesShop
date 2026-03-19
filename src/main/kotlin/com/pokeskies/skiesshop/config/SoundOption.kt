package com.pokeskies.skiesshop.config

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

class SoundOption(
    @SerializedName("id", alternate = ["sound"])
    private val id: String = "",
    private val source: String? = null,
    private val volume: Float = 1.0F,
    private val pitch: Float = 1.0F
) {
    fun playSound(player: ServerPlayer) {
        if (id.isEmpty()) {
            Utils.printError("There was an error while executing a Sound on click for player ${player.name}: Sound ID was empty")
            return
        }

        val soundEvent = SoundEvent.createVariableRangeEvent(ResourceLocation.parse(id))

        var category = if (source == null) SoundSource.MASTER else SoundSource.entries.firstOrNull { it.name.equals(source, true) }
        if (category == null) {
            Utils.printError("There was an error while executing a Sound on click for player ${player.name}: Sound Source '$source' was not found, defaulting to MASTER")
            category = SoundSource.MASTER
        }

        if (!player.server.isStopped) {
            player.server.executeIfPossible {
                player.playNotifySound(
                    soundEvent,
                    category,
                    volume,
                    pitch,
                )
            }
        }
    }

    override fun toString(): String {
        return "SoundOption(sound='$id', source=$source, volume=$volume, pitch=$pitch)"
    }
}
