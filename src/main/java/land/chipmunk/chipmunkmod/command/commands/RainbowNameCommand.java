package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import land.chipmunk.chipmunkmod.modules.RainbowName;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;
import static land.chipmunk.chipmunkmod.command.CommandManager.literal;

public class RainbowNameCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("rainbowname")
                        .then(literal("enabled")
                                .then(argument("boolean", bool())
                                        .executes(RainbowNameCommand::toggleRainbowName)
                                )
                        )
                        .then(literal("setName")
                                .then(argument("name", greedyString())
                                        .executes(RainbowNameCommand::setDisplayName)
                                )
                        )
        );
    }

    public static int toggleRainbowName(CommandContext<FabricClientCommandSource> context) {
        boolean isEnabled = getBool(context, "boolean");

        if (isEnabled) {
            RainbowName.INSTANCE.enable();
            ChatUtils.infoPrefix("Rainbow Name", "Rainbow name is now [hl]on");
        } else {
            RainbowName.INSTANCE.disable();
            ChatUtils.infoPrefix("Rainbow Name", "Rainbow name is now [hl]off");
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int setDisplayName(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        String name = getString(context, "name");

        RainbowName.INSTANCE.displayName = name;
        ChatUtils.infoPrefix("Rainbow Name", "Name set to [hl]%s", name);

        return Command.SINGLE_SUCCESS;
    }
}
