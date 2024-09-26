package com.pokeskies.skiesshop.gui

import ca.landonjw.gooeylibs2.api.UIManager
import ca.landonjw.gooeylibs2.api.button.GooeyButton
import ca.landonjw.gooeylibs2.api.data.UpdateEmitter
import ca.landonjw.gooeylibs2.api.page.Page
import ca.landonjw.gooeylibs2.api.template.Template
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate
import ca.landonjw.gooeylibs2.api.template.types.InventoryTemplate
import com.pokeskies.skiesshop.config.ShopConfig
import com.pokeskies.skiesshop.config.entry.ShopEntry
import com.pokeskies.skiesshop.economy.EconomyManager
import com.pokeskies.skiesshop.utils.TextUtils
import com.pokeskies.skiesshop.utils.Utils
import net.impactdev.impactor.api.economy.EconomyService
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ShopGUI(
    private val player: ServerPlayer,
    val shopID: String,
    private val config: ShopConfig
) : UpdateEmitter<Page?>(), Page {
    private val template: ChestTemplate =
        ChestTemplate.Builder(config.size)
            .build()
    private val playerInventory: InventoryTemplate = InventoryTemplate.builder().build()

    // Map of Page Number to list of Pair <ID, Shop Entries>
    private var items: MutableMap<Int, List<Pair<String, ShopEntry>>> = mutableMapOf()
    private var page = 1
    private var maxPage = 1

    init {
        config.entries.forEach { id, entry ->
            val pageItems = items[entry.page]?.toMutableList() ?: mutableListOf()
            pageItems.add(Pair(id, entry))
            items[entry.page] = pageItems
        }

        // Ensure all pages are present
        maxPage = items.keys.maxOrNull() ?: 1
        for (i in 0 until maxPage) {
            if (!items.containsKey(i)) {
                items[i] = listOf()
            }
        }

        refresh()
    }

    private fun refresh() {
        this.template.clear()
        refreshInventory()
        refreshShop()
    }

    private fun refreshInventory() {
        for ((i, stack) in player.inventory.items.withIndex()) {
            playerInventory.set(convertIndex(i), GooeyButton.builder().display(stack).build())
        }
    }

    private fun refreshShop() {
        this.template.border(0, 0, 6, 9, GooeyButton.builder()
            .display(ItemStack(Items.BLACK_STAINED_GLASS_PANE))
            .title(TextUtils.toNative(""))
            .build())

        (items[page] ?: listOf()).forEach { (id, entry) ->
            val stack = entry.display.getItemStack(1)
            if (stack != null && entry.slot != null) {
                template.set(entry.slot, GooeyButton.builder()
                    .display(appendPrice(entry, stack))
                    .onClick { ctx ->
                        Utils.sendPlayerSound(player, SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f)
                        UIManager.openUIForcefully(player, TransactionGUI(player, id, entry, this))
                    }
                    .build())
            }
        }

        this.template.set(5, 4, GooeyButton.builder()
            .display(ItemStack(Items.EMERALD))
            .title(TextUtils.toNative("<green>Balance:"))
            .lore(Component::class.java, EconomyService.instance().currencies().registered().map { currency ->
                TextUtils.toNative(" <white>• ${EconomyManager.balance(player, currency)} ${EconomyManager.getCurrencyFormatted(currency, false)}")
            })
            .build())

        if (page > 1) {
            this.template.set(5, 3, GooeyButton.builder()
                .display(ItemStack(Items.ARROW))
                .title(TextUtils.toNative("<red>Previous Page"))
                .onClick { ctx ->
                    if (page > 0) {
                        page--
                        refresh()
                    }
                }
                .build())
        }

        if (page < maxPage) {
            this.template.set(5, 5, GooeyButton.builder()
                .display(ItemStack(Items.ARROW))
                .title(TextUtils.toNative("<red>Next Page"))
                .onClick { ctx ->
                    if (page > 0) {
                        page++
                        refresh()
                    }
                }
                .build())
        }
    }

    private fun convertIndex(index: Int): Int {
        return if (index < 9) 27 + index else index - 9
    }

    override fun getTemplate(): Template {
        return template
    }

    override fun getInventoryTemplate(): Optional<InventoryTemplate> {
        return Optional.of(playerInventory)
    }

    override fun getTitle(): Component {
        return TextUtils.toNative(config.title)
    }

    fun getCurrentDateTimeFormatted(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyMMdd-HHmmss")
        return currentDateTime.format(formatter)
    }

    companion object {
        fun appendPrice(entry: ShopEntry, stack: ItemStack): ItemStack {
            val display = stack.getOrCreateTagElement(ItemStack.TAG_DISPLAY)

            val lore: ListTag = if (display.contains(ItemStack.TAG_LORE)) {
                display.getList(ItemStack.TAG_LORE, 8)
            } else {
                ListTag()
            }

            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                Component.literal(" ")
            )))

            if (entry.isBuyable()) {
                lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(TextUtils.toNative("<green>Buy Price: <white>${entry.buy?.price ?: 0.0}"))
                )))
            }
            if (entry.isSellable()) {
                lore.add(StringTag.valueOf(Component.Serializer.toJson(
                    Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(TextUtils.toNative("<red>Sell Price: <white>${entry.sell?.price ?: 0.0}"))
                )))
            }

            display.put(ItemStack.TAG_LORE, lore)
            stack.orCreateTag.put(ItemStack.TAG_DISPLAY, display)
            return stack
        }
    }
}
