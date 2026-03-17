package com.pokeskies.skiesshop.data.entry.requirements.types.internal

import com.pokeskies.skiesshop.data.entry.requirements.ComparisonType
import com.pokeskies.skiesshop.data.entry.requirements.Requirement
import com.pokeskies.skiesshop.data.entry.requirements.RequirementType
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.utils.Utils
import net.minecraft.server.level.ServerPlayer

class XPRequirement(
    type: RequirementType = RequirementType.XP,
    comparison: ComparisonType = ComparisonType.GREATER_THAN_OR_EQUALS,
    private val level: Boolean = true,
    private val amount: Int = 0,
) : Requirement(type, comparison) {
    override fun checkRequirements(player: ServerPlayer, gui: IRefreshableGui): Boolean {
        if (!checkComparison()) return false

        val experience = if (level) player.experienceLevel else player.totalExperience

        Utils.printDebug("[REQUIREMENT - ${type?.name}] Player(${player.gameProfile.name}), Player Experience($experience): $this")

        return when (comparison) {
            ComparisonType.EQUALS -> experience == amount
            ComparisonType.NOT_EQUALS -> experience != amount
            ComparisonType.GREATER_THAN -> experience > amount
            ComparisonType.LESS_THAN -> experience < amount
            ComparisonType.GREATER_THAN_OR_EQUALS -> experience >= amount
            ComparisonType.LESS_THAN_OR_EQUALS -> experience <= amount
        }
    }

    override fun allowedComparisons(): List<ComparisonType> {
        return ComparisonType.entries
    }

    override fun toString(): String {
        return "XPRequirement(level=$level, amount=$amount)"
    }
}
