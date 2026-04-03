package com.pokeskies.skiesshop.config

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import com.pokeskies.skiesshop.SkiesShop
import com.pokeskies.skiesshop.placeholders.PlaceholderManager
import com.pokeskies.skiesshop.utils.FlexibleListAdaptorFactory
import com.pokeskies.skiesshop.utils.Utils
import com.pokeskies.skiesshop.utils.asNative
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomModelData
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.ResolvableProfile
import java.util.*

open class GuiItem(
    val item: String = "",
    @SerializedName("slots", alternate = ["slot"])
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val slots: List<Int> = emptyList(),
    val amount: Int = 1,
    val name: String? = null,
    @JsonAdapter(FlexibleListAdaptorFactory::class)
    val lore: List<String> = emptyList(),
    @SerializedName("components", alternate = ["nbt"])
    val components: CompoundTag? = null,
    @SerializedName("custom_model_data")
    val customModelData: Int? = null
) {
    fun getItemStack(player: ServerPlayer, amountOverride: Int = amount): ItemStack {
        if (item.isEmpty()) return ItemStack(Items.BARRIER, amountOverride)

        val parsedItem = PlaceholderManager.parse(player, item)

        val optItem = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(parsedItem))
        if (optItem.isEmpty) {
            Utils.printError("Error while getting Item, defaulting to Barrier: $parsedItem")
            return ItemStack(Items.BARRIER, amount)
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
            DataComponentPatch.CODEC.decode(SkiesShop.INSTANCE.nbtOpts, Utils.parseNBT(player, components)).result().ifPresent { result ->
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

    fun createButton(player: ServerPlayer): GuiElementBuilder {
        return GuiElementBuilder(getItemStack(player))
    }

    override fun toString(): String {
        return "GuiItem(item='$item', slots=$slots, amount=$amount, name=$name, lore=$lore, components=$components, customModelData=$customModelData)"
    }
}
