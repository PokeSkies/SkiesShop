package com.pokeskies.skiesshop.data.entry.requirements.types.extensions

import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.util.asExpressionLike
import com.cobblemon.mod.common.util.resolve
import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesshop.data.entry.requirements.ComparisonType
import com.pokeskies.skiesshop.data.entry.requirements.Requirement
import com.pokeskies.skiesshop.data.entry.requirements.RequirementType
import com.pokeskies.skiesshop.gui.ConfirmGUI
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.gui.ShopGUI
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer

class MolangRequirement(
    comparison: ComparisonType = ComparisonType.EQUALS,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val script: List<String> = listOf(),
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val output: List<String> = emptyList(),
    private val strict: Boolean = false,
) : Requirement(RequirementType.MOLANG, comparison) {
    override fun checkRequirements(player: ServerPlayer, gui: IRefreshableGui): Boolean {
        if (!checkComparison()) return false

        val manager = when (gui) {
            is ShopGUI -> gui.molang
            is ConfirmGUI -> gui.shopGUI.molang
            else -> null
        } ?: run {
            Utils.printError("[REQUIREMENT - ${type?.name}] This requirement can only be used in a Shop GUI or Confirm GUI, but was used in a ${gui::class.java.simpleName}")
            return false
        }

        val value = manager.runtime.resolve(
            script.asExpressionLike(),
            mapOf("player" to player.asMoLangValue())
        ).asString()

        if (value == null) {
            Utils.printDebug("[REQUIREMENT - ${type?.name}] Player(${player.gameProfile.name}), Parsed Script(null), Output Check(false): $this")
            return false
        }

        val result = output.any { it.equals(value, strict) }

        Utils.printDebug("[REQUIREMENT - ${type?.name}] Player(${player.gameProfile.name}), Parsed Script($value), Output Check($result): $this")

        return if (comparison == ComparisonType.EQUALS) result else !result
    }

    override fun allowedComparisons(): List<ComparisonType> {
        return listOf(ComparisonType.EQUALS, ComparisonType.NOT_EQUALS)
    }

    override fun toString(): String {
        return "MolangRequirement(comparison=$comparison, script='$script', output='$output', strict=$strict)"
    }

}