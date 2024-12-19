package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import land.chipmunk.chipmunkmod.modules.CustomChat;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;
import static land.chipmunk.chipmunkmod.command.CommandManager.literal;

public class CustomChatCommand {
    public static void register (CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("customchat")
                        .then(
                                literal("enabled")
                                        .then(
                                                argument("boolean", bool())
                                                        .executes(CustomChatCommand::enabled)
                                        )
                        )
                        .then(
                                literal("format")
                                        .then(
                                                argument("format", greedyString())
                                                        .executes(CustomChatCommand::setFormat)
                                        )
                        )
        );
    }

    public static int enabled (CommandContext<FabricClientCommandSource> context) {
        final FabricClientCommandSource source = context.getSource();
        final boolean bool = getBool(context, "boolean");
        CustomChat.INSTANCE.enabled = bool;

        ChatUtils.infoPrefix("CHAT", "Custom chat is now [hl]%s", bool ? "on" : "off");

        return Command.SINGLE_SUCCESS;
    }

    public static int setFormat (CommandContext<FabricClientCommandSource> context) {
        final FabricClientCommandSource source = context.getSource();
        final String format = getString(context, "format");
        CustomChat.INSTANCE.format = format;

        ChatUtils.infoPrefix("CHAT", "Format for custom chat set to [hl]%s", format);

        return Command.SINGLE_SUCCESS;
    }
}
