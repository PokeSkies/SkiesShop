package com.pokeskies.skiesshop.utils

import com.bedrockk.molang.runtime.MoLangRuntime
import com.bedrockk.molang.runtime.struct.QueryStruct
import com.bedrockk.molang.runtime.value.StringValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.asMoLangValue
import com.cobblemon.mod.common.api.molang.MoLangFunctions.setup
import com.pokeskies.skiesshop.data.ShopInstance
import net.minecraft.server.level.ServerPlayer

import java.util.function.Function

class MolangManager(shop: ShopInstance, player: ServerPlayer) {
    val runtime: MoLangRuntime = MoLangRuntime().setup().also {
        it.environment.query.addFunction("shop") { shopMolangStruct(shop, player) }
    }

    fun shopMolangStruct(shop: ShopInstance, player: ServerPlayer): QueryStruct = QueryStruct(
        hashMapOf(
            "player" to Function { player.asMoLangValue() },
            "id" to Function { StringValue(shop.id) }
        )
    )
}