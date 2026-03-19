package com.pokeskies.skiesshop

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pokeskies.skiesshop.commands.BaseCommand
import com.pokeskies.skiesshop.config.ConfigManager
import com.pokeskies.skiesshop.config.Lang
import com.pokeskies.skiesshop.config.ShopConfig
import com.pokeskies.skiesshop.config.ShopEntryMapAdapter
import com.pokeskies.skiesshop.data.TransactionType
import com.pokeskies.skiesshop.data.click.EntryClickOption
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.data.entry.requirements.ComparisonType
import com.pokeskies.skiesshop.data.entry.requirements.Requirement
import com.pokeskies.skiesshop.data.items.actions.Action
import com.pokeskies.skiesshop.economy.EconomyType
import com.pokeskies.skiesshop.economy.IEconomyService
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.InventoryType
import com.pokeskies.skiesshop.logging.LoggerManager
import com.pokeskies.skiesshop.logging.LoggerType
import com.pokeskies.skiesshop.placeholders.PlaceholderManager
import com.pokeskies.skiesshop.storage.StorageType
import com.pokeskies.skiesshop.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SkiesShop : ModInitializer {
    companion object {
        lateinit var INSTANCE: SkiesShop

        var MOD_ID = "skiesshop"
        var MOD_NAME = "SkiesShop"

        val LOGGER: Logger = LogManager.getLogger(MOD_NAME)
        val MINI_MESSAGE: MiniMessage = MiniMessage.miniMessage()

        val asyncScope = CoroutineScope(Dispatchers.IO)

        @JvmStatic
        fun asResource(path: String): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
        }
    }

    lateinit var configDir: File

    lateinit var adventure: FabricServerAudiences
    lateinit var server: MinecraftServer
    lateinit var nbtOpts: RegistryOps<Tag>

    private var economyServices: Map<EconomyType, IEconomyService> = emptyMap()

    val asyncExecutor: ExecutorService = Executors.newFixedThreadPool(8, ThreadFactoryBuilder()
        .setNameFormat("SkiesShops-Async-%d")
        .setDaemon(true)
        .build())

    var gson: Gson = GsonBuilder().disableHtmlEscaping()
        .registerTypeAdapter(ShopEntry::class.java, ShopEntry.Adapter())
        .registerTypeAdapter(ShopConfig::class.java, ShopConfig.Deserializer())
        .registerTypeAdapter(Action::class.java, Action.Adapter())
        .registerTypeAdapter(GenericClickType::class.java, GenericClickType.Adapter())
        .registerTypeAdapter(EntryClickOption::class.java, EntryClickOption.Adapter())
        .registerTypeAdapter(LoggerType::class.java, LoggerType.Adapter())
        .registerTypeAdapter(TransactionType::class.java, TransactionType.Adapter())
        .registerTypeAdapter(StorageType::class.java, StorageType.Adapter())
        .registerTypeAdapter(InventoryType::class.java, InventoryType.Adapter())
        .registerTypeAdapter(EconomyType::class.java, EconomyType.Adapter())
        .registerTypeAdapter(Requirement::class.java, Requirement.Adapter())
        .registerTypeAdapter(ComparisonType::class.java, ComparisonType.Adapter())
        .registerTypeHierarchyAdapter(CompoundTag::class.java, Utils.CodecSerializer(CompoundTag.CODEC))
        .registerTypeAdapter(object : TypeToken<Map<String, ShopEntry>>() {}.type, ShopEntryMapAdapter())
        .create()
    var gsonPretty: Gson = gson.newBuilder().setPrettyPrinting().create()

    override fun onInitialize() {
        INSTANCE = this

        this.configDir = File(FabricLoader.getInstance().configDirectory, MOD_ID)
        ConfigManager.load()
        Lang.init()

        this.economyServices = IEconomyService.getLoadedEconomyServices()

        LoggerManager.load()

        registerEvents()
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server: MinecraftServer ->
            this.adventure = FabricServerAudiences.of(
                server
            )
            this.server = server
            this.nbtOpts = server.registryAccess().createSerializationContext(NbtOps.INSTANCE)

            PlaceholderManager.init()

            if (FabricLoader.getInstance().isModLoaded("impactor")) {
                Utils.printInfo("Impactor Economy Service has been found and loaded for any Currency actions!")
            } else {
                Utils.printError("Impactor was not loaded, things wont work quite right...")
            }

            // Disabled for now
//            if (FabricLoader.getInstance().isModLoaded("plan")) {
//                Utils.printInfo("Plan has been found and loaded for any Economy actions!")
//                PlanHook.initialize()
//            }

            SkiesShopManager.load()
        })
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerLifecycleEvents.ServerStopping {
            try {

            } finally {
                asyncExecutor.shutdownNow()
            }
        })
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            BaseCommand().register(
                dispatcher
            )
        }
    }

    fun reload() {
        ConfigManager.load()

        this.economyServices = IEconomyService.getLoadedEconomyServices()
        Lang.init()

        LoggerManager.load()
        SkiesShopManager.load()
    }

    fun getLoadedEconomyServices(): Map<EconomyType, IEconomyService> {
        return this.economyServices
    }

    fun getEconomyService(economyType: EconomyType?): IEconomyService? {
        return economyType?.let { this.economyServices[it] }
    }

    fun getEconomyServiceOrDefault(economyType: EconomyType?): IEconomyService? {
        return economyType?.let { this.economyServices[it] } ?: this.economyServices.values.firstOrNull()
    }
}
