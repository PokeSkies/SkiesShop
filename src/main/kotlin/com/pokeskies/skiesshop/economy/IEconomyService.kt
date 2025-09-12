package com.pokeskies.skiesshop.economy

import com.pokeskies.skiesshop.utils.Utils
import net.kyori.adventure.text.Component
import net.minecraft.server.level.ServerPlayer
import kotlin.reflect.full.primaryConstructor

interface IEconomyService {
    fun balance(player: ServerPlayer, currency: String = ""): Double
    fun withdraw(player: ServerPlayer, amount: Double, currency: String = ""): Boolean
    fun deposit(player: ServerPlayer, amount: Double, currency: String = ""): Boolean
    fun set(player: ServerPlayer, amount: Double, currency: String = ""): Boolean
    fun name(amount: Double, currency: String = ""): Component?

    companion object {
        private fun getEconomyService(economyType: EconomyType): IEconomyService? {
            if (!economyType.isModPresent()) return null

            return try {
                economyType.clazz.kotlin.primaryConstructor!!.call()
            } catch (ex: Exception) {
                Utils.printError("There was an exception while initializing the Economy Service: ${economyType}. Is it loaded?")
                ex.printStackTrace()
                null
            }
        }

        fun getLoadedEconomyServices(): Map<EconomyType, IEconomyService> {
            return EconomyType.entries.mapNotNull { key ->
                getEconomyService(key)?.let { key to it }
            }.toMap()
        }
    }
}
