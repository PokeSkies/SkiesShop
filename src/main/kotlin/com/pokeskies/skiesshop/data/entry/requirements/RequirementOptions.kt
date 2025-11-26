package com.pokeskies.skiesshop.data.entry.requirements

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.gui.IRefreshableGui
import net.minecraft.server.level.ServerPlayer

class RequirementOptions(
    val requirements: Map<String, Requirement> = emptyMap(),
    @SerializedName("deny_actions")
    val denyActions: Map<String, Action> = emptyMap(),
    @SerializedName("success_actions")
    val successActions: Map<String, Action> = emptyMap(),
    @SerializedName("minimum_requirements")
    val minimumRequirements: Int? = null,
    @SerializedName("stop_at_success")
    val stopAtSuccess: Boolean = false,
    @SerializedName("send_denial_message")
    var sendDenialMessage: Boolean = true,
) {
    fun checkRequirements(player: ServerPlayer, gui: IRefreshableGui): Boolean {
        var successes = 0
        for (requirement in requirements) {
            if (requirement.value.checkRequirements(player, gui)) {
                successes++
                if (minimumRequirements != null && stopAtSuccess && successes >= minimumRequirements) {
                    return true
                }
            }
        }
        return if (minimumRequirements == null) successes == requirements.size else successes >= minimumRequirements
    }

    fun executeDenyActions(player: ServerPlayer, gui: IRefreshableGui) {
        for ((_, action) in denyActions) {
            action.executeAction(player, gui)
        }
    }

    fun executeSuccessActions(player: ServerPlayer, gui: IRefreshableGui) {
        for ((_, action) in successActions) {
            action.executeAction(player, gui)
        }
    }
    override fun toString(): String {
        return "RequirementOptions(requirements=$requirements, denyActions=$denyActions, successActions=$successActions)"
    }
}
