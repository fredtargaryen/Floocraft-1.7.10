package com.fredtargaryen.floocraft.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public class CommandsBase {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        ViewCommand.register(dispatcher);
        RemoveCommand.register(dispatcher);
    }
}
