package com.pokeskies.skiesshop.data.entry

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.config.GuiItem
import com.pokeskies.skiesshop.config.PriceOption
import com.pokeskies.skiesshop.data.ShopInstance
import com.pokeskies.skiesshop.data.ShopTransaction
import com.pokeskies.skiesshop.data.TransactionResult
import com.pokeskies.skiesshop.data.TransactionType
import com.pokeskies.skiesshop.data.entry.ShopEntryType.Companion.valueOfAnyCase
import com.pokeskies.skiesshop.logging.LoggerManager
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesshop.utils.ShopTransactionEvent
import com.pokeskies.skiesshop.utils.Utils
import com.pokeskies.skiesshop.utils.asNative
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
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
) {
    lateinit var id: String

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

    open fun buy(player: ServerPlayer, amount: Int): TransactionResult {
        return TransactionResult(false, "", 0)
    }

    open fun sell(player: ServerPlayer, amount: Int): TransactionResult {
        return TransactionResult(false, "", 0)
    }

    fun tryBuy(player: ServerPlayer, shop: ShopInstance, amount: Int): Boolean {
        if (buy == null) return false

        val maxAmount = getMaxAmount(TransactionType.BUY)
        if (maxAmount != null && maxAmount < amount) return false

        SkiesShop.INSTANCE.getEconomyService(buy.economy)?.let { economy ->
            if (economy.balance(player, buy.currency) >= (buy.price * amount)) {
                if (economy.withdraw(player, (buy.price * amount), buy.currency)) {
                    val transaction = buy(player, amount)
                    if (!transaction.success) {
                        player.sendSystemMessage(transaction.response.asNative())
                        Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                        return false
                    }

                    player.sendSystemMessage(
                        Component.literal("Purchased ${amount}x ")
                            .append(Component.translatable(display.getItemStack(player).descriptionId))
                            .append(" for ${buy.price * amount}!")
                            .withStyle { it.withColor(ChatFormatting.GREEN) })
                    Utils.sendPlayerSound(player, SoundEvents.ITEM_PICKUP, 0.5f, 1.0f)
                    val shopTransaction = ShopTransaction(
                        player.uuid,
                        System.currentTimeMillis(),
                        shop.id,
                        id,
                        TransactionType.BUY,
                        buy.price * amount,
                        amount,
                        toJson()
                    )
                    ShopTransactionEvent.EVENT.invoker().execute(player, shopTransaction)
                    LoggerManager.logTransaction(shopTransaction)
                    return true
                }
            } else {
                player.sendSystemMessage(
                    Component.literal("You do not have enough to purchase this shop entry!").withStyle { it.withColor(ChatFormatting.RED) })
                Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
            }
        } ?: run {
            Utils.printError("Could not find currency ${buy?.currency}!")
            Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
        }

        return false
    }

    fun trySell(player: ServerPlayer, shop: ShopInstance, amount: Int): Boolean {
        if (sell == null) return false

        val maxAmount = getMaxAmount(TransactionType.SELL)
        if (maxAmount != null && maxAmount < amount) return false

        SkiesShop.INSTANCE.getEconomyService(sell.economy)?.let { economy ->
            val transaction = sell(player, amount)
            if (!transaction.success) {
                player.sendSystemMessage(transaction.response.asNative())
                Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                return false
            }

            val amountSold = transaction.amount
            if (amountSold > 0) {
                if (economy.deposit(player, sell.price * amountSold, sell.currency)) {
                    player.sendSystemMessage(
                        Component.literal("Sold ${amountSold}x ")
                            .append(Component.translatable(display.getItemStack(player).descriptionId))
                            .append(" for ${sell.price * amountSold}!")
                            .withStyle { it.withColor(ChatFormatting.GREEN) })
                    Utils.sendPlayerSound(player, SoundEvents.ITEM_PICKUP, 0.5f, 1.0f)
                    val shopTransaction = ShopTransaction(
                        player.uuid,
                        System.currentTimeMillis(),
                        shop.id,
                        id,
                        TransactionType.SELL,
                        sell.price * amountSold,
                        amountSold,
                        toJson()
                    )
                    ShopTransactionEvent.EVENT.invoker().execute(player, shopTransaction)
                    LoggerManager.logTransaction(shopTransaction)
                    return true
                } else {
                    val timestamp = Utils.getCurrentDateTimeFormatted()
                    Utils.printError("There was an error for player '${player.name.string}' while processing a sell action for '${this}' at $timestamp!")
                    player.sendSystemMessage(
                        Component.literal("There was an error while processing a sell action, please contact staff with the error: $timestamp")
                            .withStyle {
                                it.withColor(
                                    ChatFormatting.RED
                                )
                            })
                    Utils.sendPlayerSound(player, SoundEvents.FIRE_EXTINGUISH, 0.3f, 1.0f)
                }
            } else {
                player.sendSystemMessage(Component.literal("You dont have the required items!").withStyle { it.withColor(ChatFormatting.RED) })
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

    internal class Adapter: JsonSerializer<ShopEntry>, JsonDeserializer<ShopEntry> {
        override fun serialize(src: ShopEntry, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return context.serialize(src, src::class.java)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ShopEntry {
            val jsonObject: JsonObject = json.getAsJsonObject()
            val value = jsonObject.get("type").asString
            val type: ShopEntryType? = valueOfAnyCase(value)
            return try {
                context.deserialize(json, type!!.clazz)
            } catch (e: NullPointerException) {
                throw JsonParseException("Could not deserialize Shop Entry Type: $value", e)
            }
        }
    }

    override fun toString(): String {
        return "ShopEntry(type=$type, display=$display, slot=$slots, page=$pages, buy=$buy, sell=$sell)"
    }
}
