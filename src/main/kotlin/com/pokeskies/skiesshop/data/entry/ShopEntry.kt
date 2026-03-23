package com.pokeskies.skiesshop.data.entry

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.config.GuiItem
import com.pokeskies.skiesshop.config.Lang
import com.pokeskies.skiesshop.config.PriceOption
import com.pokeskies.skiesshop.config.SoundOption
import com.pokeskies.skiesshop.data.ShopInstance
import com.pokeskies.skiesshop.data.ShopTransaction
import com.pokeskies.skiesshop.data.TransactionResult
import com.pokeskies.skiesshop.data.TransactionType
import com.pokeskies.skiesshop.data.click.EntryClickOption
import com.pokeskies.skiesshop.data.entry.ShopEntryType.Companion.valueOfAnyCase
import com.pokeskies.skiesshop.data.entry.types.ItemShopEntry
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.gui.IRefreshableGui
import com.pokeskies.skiesshop.logging.LoggerManager
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesshop.utils.ShopTransactionEvent
import com.pokeskies.skiesshop.utils.Utils
import com.pokeskies.skiesshop.utils.asAdventure
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import java.lang.reflect.Type

abstract class ShopEntry(
    val type: ShopEntryType = ShopEntryType.ITEM,
    val display: GuiItem = GuiItem(),
    @SerializedName("slots", alternate = ["slot"]) @JsonAdapter(FlexibleListAdaptorFactory::class)
    val slots: List<Int> = listOf(),
    @SerializedName("pages", alternate = ["page"]) @JsonAdapter(FlexibleListAdaptorFactory::class)
    val pages: List<Int> = listOf(1),
    val buy: PriceOption? = null,
    val sell: PriceOption? = null,
    @SerializedName("entry_lore")
    var entryLore: List<String>? = null,
    @SerializedName("click_options")
    var clickOptions: Map<GenericClickType, EntryClickOption>? = null,
) {
    lateinit var id: String
    var isPreset: Boolean = false

    open fun isValid(): Boolean {
        if (slots.isEmpty()) return false
        if (pages.isEmpty()) return false
        if (buy == null && sell == null) return false
        return true
    }

    open fun getGuiItem(): GuiItem {
        return display
    }

    open fun isBuyable(): Boolean {
        return buy != null
    }

    open fun isSellable(): Boolean {
        return sell != null
    }

    open fun canBuy(player: ServerPlayer, amount: Int): Pair<Boolean, List<String>?> {
        return true to null
    }

    open fun canSell(player: ServerPlayer, amount: Int): Pair<Boolean, List<String>?> {
        return true to null
    }

    open fun buy(player: ServerPlayer, amount: Int): TransactionResult {
        return TransactionResult(false, listOf(), 0)
    }

    open fun sell(player: ServerPlayer, amount: Int): TransactionResult {
        return TransactionResult(false, listOf(), 0)
    }

    fun tryBuy(player: ServerPlayer, shop: ShopInstance, amount: Int, gui: IRefreshableGui, successSound: SoundOption?): Boolean {
        if (buy == null) return false

        // Amount Validity Check
        val maxAmount = getMaxAmount(TransactionType.BUY)
        if (maxAmount != null && maxAmount < amount) return false

        // Requirements Check
        buy.requirements?.let { requirements ->
            if (requirements.checkRequirements(player, gui)) {
                requirements.executeSuccessActions(player, gui)
            } else {
                if (requirements.sendDenialMessage) {
                    Lang.TRANSACTION_BUY_REQUIREMENTS.forEach {
                        player.sendMessage(it.asAdventure())
                    }
                    Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                }
                requirements.executeDenyActions(player, gui)
                return false
            }
        }

        // General checks
        val check = canBuy(player, amount)
        if (!check.first) {
            check.second?.let { message ->
                message.forEach {
                    player.sendMessage(it.asAdventure())
                }
            }
            Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
            return false
        }

        // Economy Check and Transaction
        SkiesShop.INSTANCE.getEconomyService(buy.economy)?.let { economy ->
            if (economy.balance(player, buy.currency) >= (buy.price * amount)) {
                if (economy.withdraw(player, (buy.price * amount), buy.currency)) {
                    val transaction = buy(player, amount)
                    if (!transaction.success) {
                        transaction.response.forEach {
                            player.sendMessage(it.asAdventure())
                        }
                        Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                        economy.deposit(player, (buy.price * amount), buy.currency)
                        return false
                    }

                    var transactionAmount = amount
                    // If the entry is a ItemShopEntry, there is a possibility that the total items bought is different
                    // from the buy amount as the base item may have the `amount` field set, requiring a multiplication
                    if (this is ItemShopEntry) {
                        transactionAmount *= this.amount
                    }

                    Lang.TRANSACTION_BUY.forEach {
                        player.sendMessage(it.asAdventure(mapOf(
                            "%transaction_amount%" to transactionAmount.toString(),
                            "%transaction_entry_name%" to getSafeDisplayName(player),
                            "%transaction_total%" to (buy.price * amount).toString(),
                        )))
                    }
                    successSound?.playSound(player)
                    val shopTransaction = ShopTransaction(
                        player.uuid,
                        System.currentTimeMillis(),
                        shop.id,
                        id,
                        TransactionType.BUY,
                        buy.price * amount,
                        amount
                    )
                    ShopTransactionEvent.EVENT.invoker().execute(player, shopTransaction)
                    LoggerManager.logTransaction(shopTransaction)
                    return true
                }
            } else {
                Lang.TRANSACTION_BUY_BALANCE.forEach {
                    player.sendMessage(it.asAdventure(mapOf(
                        "%transaction_total%" to (buy.price * amount).toString(),
                        "%transaction_currency%" to buy.currency
                    )))
                }
                Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
            }
        } ?: run {
            Utils.printError("Could not find currency ${buy?.currency}!")
            Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
        }

        return false
    }

    fun trySell(player: ServerPlayer, shop: ShopInstance, amount: Int, gui: IRefreshableGui, successSound: SoundOption?): Boolean {
        if (sell == null) return false

        // Amount Validity Check
        val maxAmount = getMaxAmount(TransactionType.SELL)
        if (maxAmount != null && maxAmount < amount) return false

        // Requirements Check
        sell.requirements?.let { requirements ->
            if (requirements.checkRequirements(player, gui)) {
                requirements.executeSuccessActions(player, gui)
            } else {
                if (requirements.sendDenialMessage) {
                    Lang.TRANSACTION_SELL_REQUIREMENTS.forEach {
                        player.sendMessage(it.asAdventure())
                    }
                    Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                }
                requirements.executeDenyActions(player, gui)
                return false
            }
        }

        // General checks
        val check = canSell(player, amount)
        if (!check.first) {
            check.second?.let { messages ->
                messages.forEach {
                    player.sendMessage(it.asAdventure())
                }
            }
            Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
            return false
        }

        // Economy Transaction
        SkiesShop.INSTANCE.getEconomyService(sell.economy)?.let { economy ->
            val transaction = sell(player, amount)
            if (!transaction.success) {
                transaction.response.forEach {
                    player.sendMessage(it.asAdventure())
                }
                Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                return false
            }

            val amountSold = transaction.amount
            if (amountSold > 0) {
                if (economy.deposit(player, sell.price * amountSold, sell.currency)) {
                    Lang.TRANSACTION_SELL.forEach {
                        player.sendMessage(it.asAdventure(mapOf(
                            "%transaction_amount%" to amountSold.toString(),
                            "%transaction_entry_name%" to getSafeDisplayName(player),
                            "%transaction_total%" to (sell.price * amountSold).toString(),
                        )))
                    }
                    successSound?.playSound(player)
                    val shopTransaction = ShopTransaction(
                        player.uuid,
                        System.currentTimeMillis(),
                        shop.id,
                        id,
                        TransactionType.SELL,
                        sell.price * amountSold,
                        amountSold
                    )
                    ShopTransactionEvent.EVENT.invoker().execute(player, shopTransaction)
                    LoggerManager.logTransaction(shopTransaction)
                    return true
                } else {
                    val timestamp = Utils.getCurrentDateTimeFormatted()
                    Utils.printError("There was an error for player '${player.name.string}' while processing a sell action for '${this}' at $timestamp!")
                    Lang.TRANSACTION_SELL_ERROR.forEach {
                        player.sendMessage(it.asAdventure(mapOf(
                            "%transaction_timestamp%" to timestamp
                        )))
                    }
                    Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                }
            } else {
                Lang.TRANSACTION_SELL_ITEMS.forEach {
                    player.sendMessage(it.asAdventure())
                }
                Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
            }
        } ?: run {
            Utils.printError("Could not find currency ${buy?.currency}!")
            Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
        }

        return false
    }

    open fun getMaxAmount(transactionType: TransactionType): Int? {
        return when (transactionType) {
            TransactionType.BUY -> buy?.maxAmount
            TransactionType.SELL -> sell?.maxAmount
        }
    }

    fun toJson(): String {
        return SkiesShop.INSTANCE.gson.toJson(this)
    }

    open fun getDisplayName(player: ServerPlayer): String? {
        return null
    }

    private fun getSafeDisplayName(player: ServerPlayer): String {
        if (display.name != null) return display.name
        return getDisplayName(player) ?: "<lang:${getGuiItem().getItemStack(player).descriptionId}>"
    }

    internal class Adapter: JsonSerializer<ShopEntry>, JsonDeserializer<ShopEntry> {
        override fun serialize(src: ShopEntry, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src, src::class.java)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ShopEntry {
            val jsonObject: JsonObject = json.getAsJsonObject()
            val value = jsonObject.get("type").asString
            val type: ShopEntryType = valueOfAnyCase(value) ?: throw JsonParseException("Invalid Shop Entry type entered!")
            return try {
                context.deserialize(json, type.clazz)
            } catch (e: NullPointerException) {
                throw JsonParseException("Could not deserialize Shop Entry Type: $value", e)
            }
        }
    }

    // Possible do a less intensive GSON copy method in the future
    fun copy(): ShopEntry {
        val json = toJson()
        return SkiesShop.INSTANCE.gson.fromJson(json, this::class.java)
    }

    override fun toString(): String {
        return "ShopEntry(type=$type, display=$display, slot=$slots, page=$pages, buy=$buy, sell=$sell)"
    }
}
