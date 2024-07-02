package com.pokeskies.$mod_id$.utils

import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.commands.CommandSourceStack

interface SubCommand {
    fun build(): LiteralCommandNode<CommandSourceStack>
}