package com.pokeskies.skiesshop.data.entry.requirements

import com.pokeskies.skiesshop.data.entry.requirements.types.extensions.plan.PlanPlaytimeRequirement
import com.pokeskies.skiesshop.data.entry.requirements.types.internal.*

enum class RequirementType(val identifier: String, val clazz: Class<*>) {
    // Internal
    PERMISSION("permission", PermissionRequirement::class.java),
    ITEM("item", ItemRequirement::class.java),
    CURRENCY("currency", CurrencyRequirement::class.java),
    DIMENSION("dimension", DimensionRequirement::class.java),
    PLACEHOLDER("placeholder", PlaceholderRequirement::class.java),

    // Extensions
    PLAN_PLAYTIME("plan_playtime", PlanPlaytimeRequirement::class.java);

    companion object {
        fun valueOfAnyCase(name: String): RequirementType? {
            for (type in RequirementType.entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }
}