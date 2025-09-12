package com.pokeskies.skiesshop.addons.plan

import com.djrapitops.plan.extension.CallEvents
import com.djrapitops.plan.extension.DataExtension
import com.djrapitops.plan.extension.annotation.PluginInfo
import com.djrapitops.plan.extension.annotation.Tab
import com.djrapitops.plan.extension.annotation.TabInfo
import com.djrapitops.plan.extension.annotation.TableProvider
import com.djrapitops.plan.extension.icon.Color
import com.djrapitops.plan.extension.icon.Family
import com.djrapitops.plan.extension.icon.Icon
import com.djrapitops.plan.extension.table.Table
import java.util.*
import java.util.concurrent.ExecutionException

@PluginInfo(name = "SkiesShop", iconName = "vial", iconFamily = Family.SOLID, color = Color.LIGHT_BLUE)
@TabInfo(tab = "Transactions", iconName = "inbox", elementOrder = [])
class ShopDataExtension : DataExtension {
    override fun callExtensionMethodsOn(): Array<CallEvents> {
        return arrayOf(
            CallEvents.SERVER_PERIODICAL,
            CallEvents.PLAYER_LEAVE
        )
    }

    // Global
    @TableProvider(tableColor = Color.BLUE)
    @Tab("Transactions")
    fun transactionsTab(): Table {
        val tableBuilder: Table.Factory = Table.builder()
            .columnOne("Player", Icon.called("user").build())
            .columnTwo("Action", Icon.called("code-branch").build())
            .columnThree("Shop", Icon.called("shopping-cart").build())
            .columnFour("Item(amount)", Icon.called("box").build())
            .columnFive("Price", Icon.called("money-bill-wave").build())
        try {
//            LoggerManager.getAllLogs().forEach {
//                tableBuilder.addRow(
//                    it.player,
//                    it.action.name,
//                    it.shopId,
//                    it.itemId + " (" + it.amount + ")",
//                    it.price
//                )
//            }
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
        return tableBuilder.build()
    }

    // Player
    @TableProvider(tableColor = Color.BLUE)
    @Tab("Transactions")
    fun transactionsTabPlayer(player: UUID): Table {
        val tableBuilder: Table.Factory = Table.builder()
            .columnOne("Player", Icon.called("user").build())
            .columnTwo("Action", Icon.called("code-branch").build())
            .columnThree("Shop", Icon.called("shopping-cart").build())
            .columnFour("Item(amount)", Icon.called("box").build())
            .columnFive("Price", Icon.called("money-bill-wave").build())
        try {
//            LoggerManager.getUserLogs(player).forEach {
//                tableBuilder.addRow(
//                    it.player,
//                    it.action.name,
//                    it.shopId,
//                    it.itemId + " (" + it.amount + ")",
//                    it.price
//                )
//            }
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
        return tableBuilder.build()
    }
}
