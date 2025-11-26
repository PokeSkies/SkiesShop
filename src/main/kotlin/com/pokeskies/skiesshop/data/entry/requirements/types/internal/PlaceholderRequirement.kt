package com.pokeskies.skiesshop.data.entry.requirements.types.internal

import com.google.gson.annotations.JsonAdapter
import com.pokeskies.skiesshop.data.entry.requirements.ComparisonType
import com.pokeskies.skiesshop.data.entry.requirements.Requirement
import com.pokeskies.skiesshop.data.entry.requirements.RequirementType
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.placeholders.PlaceholderManager
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer

class PlaceholderRequirement(
    type: RequirementType = RequirementType.PLACEHOLDER,
    comparison: ComparisonType = ComparisonType.EQUALS,
    private val input: String = "",
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    private val output: List<String> = emptyList(),
    private val strict: Boolean = false
) : Requirement(type, comparison) {
    override fun checkRequirements(player: ServerPlayer, gui: IRefreshableGui): Boolean {
        if (!checkComparison()) return false

        val parsed = PlaceholderManager.parse(player, input)

        val result = output.any { it.equals(parsed, strict) }

        Utils.printDebug("[REQUIREMENT - ${type?.name}] Player(${player.gameProfile.name}), Parsed Input($parsed), Output Check($result): $this")

        return if (comparison == ComparisonType.EQUALS) result else !result
    }

    override fun allowedComparisons(): List<ComparisonType> {
        return listOf(ComparisonType.EQUALS, ComparisonType.NOT_EQUALS)
    }

    override fun toString(): String {
        return "PlaceholderRequirement(comparison=$comparison, input='$input', output='$output', strict=$strict)"
    }

}
