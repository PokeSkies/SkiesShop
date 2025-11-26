package com.pokeskies.skiesshop.addons.plan

import com.djrapitops.plan.capability.CapabilityService
import com.djrapitops.plan.extension.ExtensionService

object PlanHook {
    fun initialize() {
        if (!areAllCapabilitiesAvailable()) return
        registerDataExtension()
        listenForPlanReloads()
    }

    private fun areAllCapabilitiesAvailable(): Boolean {
        val capabilities = CapabilityService.getInstance()
        return capabilities.hasCapability("DATA_EXTENSION_VALUES")
    }

    private fun registerDataExtension() {
        try {
            ExtensionService.getInstance().register(ShopDataExtension())
        } catch (planIsNotEnabled: IllegalStateException) {
            // Plan is not enabled, handle exception
        } catch (dataExtensionImplementationIsInvalid: IllegalArgumentException) {
            // The DataExtension implementation has an implementation error, handle exception
        }
    }

    private fun listenForPlanReloads() {
        CapabilityService.getInstance().registerEnableListener { isPlanEnabled: Boolean ->
            // Register DataExtension again
            if (isPlanEnabled) registerDataExtension()
        }
    }
}
