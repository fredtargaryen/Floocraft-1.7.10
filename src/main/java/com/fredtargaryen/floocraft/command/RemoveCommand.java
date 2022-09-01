package com.fredtargaryen.floocraft.command;

import com.fredtargaryen.floocraft.network.FloocraftWorldData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class RemoveCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("removefireplace")
                .requires(stack -> stack.hasPermission(2))
                .then(Commands.argument("Dimension", DimensionArgument.dimension())
                .then(Commands.argument("Fireplace search query (\"\" for all fireplaces)", StringArgumentType.string())
                .executes(e -> execute(
                        e.getSource(),
                        DimensionArgument.getDimension(e, "Dimension"),
                        StringArgumentType.getString(e, "Fireplace search query (\"\" for all fireplaces)"))))));
    }

    private static int execute(CommandSourceStack stack, ServerLevel level, String query) {
        FloocraftWorldData fwd = FloocraftWorldData.forLevel(level);
        ConcurrentHashMap<String, int[]> placeList = fwd.placeList;
        Iterator<String> keyIterator = placeList.keySet().iterator();
        boolean placesFound = false;
        while(keyIterator.hasNext())
        {
            String s = keyIterator.next();
            if(s.startsWith(query)) {
                placesFound = true;
                int[] coords = placeList.get(s);
                stack.sendSuccess(new TextComponent(String.format("DELETED \"%s\", which was at (%d, %d, %d)", s, coords[0], coords[1], coords[2])), true);
                placeList.remove(s);
            }
        }
        if(placesFound)
        {
            fwd.setDirty();
        }
        else
        {
            stack.sendFailure(new TextComponent(String.format("No places deleted - no places were found with names beginning with \"%s\".", query)));
        }
        return 0;
    }
}
