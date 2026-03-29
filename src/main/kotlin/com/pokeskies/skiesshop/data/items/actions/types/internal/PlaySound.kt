package com.pokeskies.skiesshop.data.items.actions.types.internal

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.data.items.actions.ActionType
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

class PlaySound(
    click: List<GenericClickType> = listOf(GenericClickType.ANY),
    @SerializedName("id", alternate = ["sound"])
    private val id: String = "",
    private val source: String? = null,
    private val volume: Float = 1.0F,
    private val pitch: Float = 1.0F
) : Action(ActionType.PLAY_SOUND, click) {
    override fun executeAction(player: ServerPlayer, gui: IRefreshableGui) {
        if (id.isEmpty()) {
            Utils.printError("[ACTION - ${type.name}] There was an error while executing for player ${player.name}: Sound ID was empty")
            return
        }

        val soundEvent = SoundEvent.createVariableRangeEvent(ResourceLocation.parse(id))

        var category = if (source == null) SoundSource.MASTER else SoundSource.entries.firstOrNull { it.name.equals(source, true) }
        if (category == null) {
            Utils.printError("[ACTION - ${type.name}] There was an error while executing for player ${player.name}: Sound Source '$source' was not found, defaulting to MASTER")
            category = SoundSource.MASTER
        }

        Utils.printDebug("[ACTION - ${type.name}] Player(${player.gameProfile.name}), SoundEvent($soundEvent), Category($category): $this")

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
        return "PlaySound(click=$click, sound='$id', source=$source, volume=$volume, pitch=$pitch)"
    }
}
