package com.pokeskies.skiesshop.data.entry.types

import com.google.gson.annotations.SerializedName
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.config.GuiItem
import com.pokeskies.skiesshop.config.Lang
import com.pokeskies.skiesshop.config.PriceOption
import com.pokeskies.skiesshop.data.TransactionResult
import com.pokeskies.skiesshop.data.click.EntryClickOption
import com.pokeskies.skiesshop.data.entry.ShopEntry
import com.pokeskies.skiesshop.data.entry.ShopEntryType
import com.pokeskies.skiesshop.gui.GenericClickType
import com.pokeskies.skiesshop.placeholders.PlaceholderManager
import com.pokeskies.skiesshop.utils.Utils
import com.pokeskies.skiesshop.utils.asNative
import com.pokeskies.skiesshop.utils.canFit
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.ResolvableProfile
import java.util.*
import kotlin.jvm.optionals.getOrNull

class ItemShopEntry(
    type: ShopEntryType = ShopEntryType.ITEM,
    display: GuiItem = GuiItem(),
    slot: List<Int> = listOf(),
    page: List<Int> = listOf(1),
    buy: PriceOption? = null,
    sell: PriceOption? = null,
    clickOptions: Map<GenericClickType, EntryClickOption>? = null,
    val item: String = "",
    val amount: Int = 1,
    val name: String? = null,
    val lore: List<String> = emptyList(),
    @SerializedName("components", alternate = ["nbt"])
    val components: CompoundTag? = null,
    val customModelData: Int? = null
) : ShopEntry(type, display, slot, page, buy, sell) {
    override fun isValid(): Boolean {
        if (item.isEmpty()) return false
        return super.isValid()
    }

    override fun getGuiItem(): GuiItem {
        return if (display.item.isEmpty()) {
            asGuiItem()
        } else {
            display
        }
    }

    override fun canBuy(player: ServerPlayer, amount: Int): Pair<Boolean, List<String>?> {
        val stack = getItemStack(player, amount)
        if (stack == null) {
            Utils.printError("Error while buying ItemShopEntry, getting ItemStack returned null ($this)")
            return false to Lang.ERROR_TRANSACTION
        }

        if (!player.inventory.canFit(stack)) {
            return false to Lang.TRANSACTION_BUY_INVENTORY
        }

        return true to null
    }

    override fun buy(player: ServerPlayer, amount: Int): TransactionResult {
        val stack = getItemStack(player, amount)
        if (stack == null) {
            Utils.printError("Error while buying ItemShopEntry, getting ItemStack returned null ($this)")
            return TransactionResult(false, Lang.ERROR_TRANSACTION, 0)
        }

        player.inventory.placeItemBackInInventory(stack)

        return TransactionResult(true, amount = amount)
    }

    override fun sell(player: ServerPlayer, amount: Int): TransactionResult {
        var amountFound = 0
        val matchedSlots: MutableList<Int> = mutableListOf()
        for ((i, stack) in player.inventory.items.withIndex()) {
            if (!stack.isEmpty) {
                if (isItem(stack)) {
                    amountFound += stack.count
                    matchedSlots.add(i)
                }
            }
        }

        var removed = 0
        matchedSlots.forEach { i ->
            val stack = player.inventory.items[i]
            val stackSize = stack.count
            if (removed + stackSize >= amount) {
                player.inventory.items[i].shrink(amount - removed)
                removed += amount - removed
                return@forEach
            } else {
                player.inventory.items[i].shrink(stackSize)
                removed += stackSize
            }
        }

        return TransactionResult(true, amount = removed)
    }

    override fun getDisplayName(player: ServerPlayer): String? {
        return if (name != null) PlaceholderManager.parse(player, name) else null
    }

    private fun isItem(checkItem: ItemStack): Boolean {
        val newItem = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(item))
        if (newItem.isEmpty) {
            return false
        }
        if (!checkItem.item.equals(newItem.get())) {
            return false
        }

        var nbtCopy = components?.copy()

        if (customModelData != null) {
            if (nbtCopy != null) {
                nbtCopy.putInt("minecraft:custom_model_data", customModelData)
            } else {
                val newNBT = CompoundTag()
                newNBT.putInt("minecraft:custom_model_data", customModelData)
                nbtCopy = newNBT
            }
        }

        if (nbtCopy != null) {
            val checkNBT = DataComponentPatch.CODEC.encodeStart(SkiesShop.INSTANCE.nbtOpts, checkItem.componentsPatch).result().getOrNull() ?: return false

            if (checkNBT != nbtCopy)
                return false
        }

        return true
    }

    fun asGuiItem(): GuiItem = GuiItem(
        item = item,
        amount = amount,
        name = name,
        lore = lore,
        components = components,
        customModelData = customModelData
    )

    fun getItemStack(player: ServerPlayer, amountOverride: Int = amount): ItemStack? {
        if (item.isEmpty()) {
            Utils.printError("Error while creating ItemShopEntry stack, item ID is empty ($this)")
            return null
        }

        val parsedItem = PlaceholderManager.parse(player, item)

        val optItem = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(parsedItem))
        if (optItem.isEmpty) {
            Utils.printError("Error while creating ItemShopEntry stack, could not parse Item ID to an actual Item: $parsedItem ($this)")
            return null
        }

        var stack = ItemStack(optItem.get(), amountOverride)

        // Handles player head parsing
        if (parsedItem.startsWith("playerhead", true)) {
            stack = ItemStack(Items.PLAYER_HEAD, amount)

            var uuid: UUID? = null
            if (parsedItem.contains("-")) {
                val arg = parsedItem.replace("playerhead-", "")
                if (arg.isNotEmpty()) {
                    if (arg.contains("-")) {
                        // CASE: UUID format
                        try {
                            uuid = UUID.fromString(arg)
                        } catch (_: Exception) {}
                    } else if (arg.length <= 16) {
                        // CASE: Player name format
                        val targetPlayer = SkiesShop.INSTANCE.server.playerList?.getPlayerByName(arg)
                        if (targetPlayer != null) {
                            uuid = targetPlayer.uuid
                        }
                    } else {
                        // CASE: Game Profile format
                        val properties = PropertyMap()
                        properties.put("textures", Property("textures", arg))
                        stack.applyComponents(DataComponentPatch.builder()
                            .set(DataComponents.PROFILE, ResolvableProfile(Optional.empty(), Optional.empty(), properties))
                            .build())
                    }
                }
            } else {
                // CASE: Only "playerhead" is provided, use the viewing player's UUID
                uuid = player.uuid
            }

            if (uuid != null) {
                val gameProfile = SkiesShop.INSTANCE.server.profileCache?.get(uuid)
                if (gameProfile != null && gameProfile.isPresent) {
                    stack.applyComponents(DataComponentPatch.builder()
                        .set(DataComponents.PROFILE, ResolvableProfile(gameProfile.get()))
                        .build())
                }
            }
        }

        if (components != null) {
            DataComponentPatch.CODEC.decode(SkiesShop.INSTANCE.nbtOpts, parseNBT(player, components)).result().ifPresent { result ->
                stack.applyComponents(result.first)
            }
        }

        val dataComponents = DataComponentPatch.builder()

        if (customModelData != null) {
            dataComponents.set(DataComponents.CUSTOM_MODEL_DATA, CustomModelData(customModelData))
        }

        if (name != null)
            dataComponents.set(DataComponents.ITEM_NAME, PlaceholderManager.parse(player, name).asNative())

        if (lore.isNotEmpty()) {
            val parsedLore: MutableList<String> = mutableListOf()
            for (line in lore.stream().map { PlaceholderManager.parse(player, it) }.toList()) {
                if (line.contains("\n")) {
                    line.split("\n").forEach { parsedLore.add(it) }
                } else {
                    parsedLore.add(line)
                }
            }
            dataComponents.set(DataComponents.LORE, ItemLore(
                parsedLore.stream().map { line ->
                    Component.empty().withStyle { it.withItalic(false) }
                        .append(line.asNative())
                }.toList() as List<Component>
            ))
        }

        stack.applyComponents(dataComponents.build())

        return stack
    }

    private fun parseNBT(player: ServerPlayer, tag: CompoundTag): CompoundTag {
        val parsedNBT = tag.copy()
        for (key in parsedNBT.allKeys) {
            var element = parsedNBT.get(key)
            if (element != null) {
                when (element) {
                    is StringTag -> {
                        element = StringTag.valueOf(PlaceholderManager.parse(player, element.asString))
                    }
                    is ListTag -> {
                        val parsed = ListTag()
                        for (entry in element) {
                            if (entry is StringTag) {
                                parsed.add(StringTag.valueOf(PlaceholderManager.parse(player, entry.asString)))
                            } else {
                                parsed.add(entry)
                            }
                        }
                        element = parsed
                    }
                    is CompoundTag -> {
                        element = parseNBT(player, element)
                    }
                }

                if (element != null) {
                    parsedNBT.put(key, element)
                }
            }
        }
        return parsedNBT
    }

    override fun toString(): String {
        return "ItemShopEntry(item=$item) ${super.toString()}"
    }
}
