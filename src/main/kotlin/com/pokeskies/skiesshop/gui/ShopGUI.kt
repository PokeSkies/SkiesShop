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
import net.impactdev.impactor.api.economy.EconomyService
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ShopGUI(
    private val player: ServerPlayer,
    private val shopID: String,
    private val config: ShopConfig
) : UpdateEmitter<Page?>(), Page {
    private val template: ChestTemplate =
        ChestTemplate.Builder(config.size)
            .build()
    private val playerInventory: InventoryTemplate = InventoryTemplate.builder().build()

    // Map of Page Number to list of Shop Entries
    private var items: MutableMap<Int, List<ShopEntry>> = mutableMapOf()

    init {
        refreshInventory()
        refreshShop()
    }

    private fun refreshInventory() {
        for ((i, stack) in player.inventory.items.withIndex()) {
            playerInventory.set(convertIndex(i), GooeyButton.builder().display(stack).build())
        }
    }

    private fun refreshShop() {
        config.entries.forEach { (id, entry) ->
            val stack = entry.display.getItemStack(1)
            if (stack != null && entry.slot != null) {
                template.set(entry.slot, GooeyButton.builder()
                    .display(appendLore(entry, stack))
                    .onClick { ctx ->
                        UIManager.openUIForcefully(player, TransactionGUI(player, entry, this))
                    }
                    .build())
            }
        }

        this.template.rectangle(5, 0, 1, 9, GooeyButton.builder()
            .display(ItemStack(Items.GRAY_STAINED_GLASS_PANE))
            .title(TextUtils.toNative(""))
            .build())

        this.template.set(5, 4, GooeyButton.builder()
            .display(ItemStack(Items.EMERALD))
            .title(TextUtils.toNative("<green>Balance:"))
            .lore(Component::class.java, EconomyService.instance().currencies().registered().map { currency ->
                TextUtils.toNative(" <white>- ${EconomyManager.balance(player, currency)} ${EconomyManager.getCurrencyFormatted(currency, false)}")
            })
            .build())
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

    fun appendLore(entry: ShopEntry, stack: ItemStack): ItemStack {
        val display = stack.getOrCreateTagElement(ItemStack.TAG_DISPLAY)

        val lore: ListTag = if (display.contains(ItemStack.TAG_LORE)) {
            display.getList(ItemStack.TAG_LORE, 8)
        } else {
            ListTag()
        }

        lore.add(StringTag.valueOf(""))

        if (entry.isBuyable()) {
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(TextUtils.toNative("<green>Buy Price: <white>${entry.buy?.price}"))
            )))
        }
        if (entry.isSellable()) {
            lore.add(StringTag.valueOf(Component.Serializer.toJson(
                Component.empty().setStyle(Style.EMPTY.withItalic(false)).append(TextUtils.toNative("<red>Sell Price: <white>${entry.sell?.price}"))
            )))
        }

        display.put(ItemStack.TAG_LORE, lore)
        stack.orCreateTag.put(ItemStack.TAG_DISPLAY, display)
        return stack
    }
}
