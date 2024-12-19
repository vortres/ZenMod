package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import land.chipmunk.chipmunkmod.modules.Chat;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;
import static land.chipmunk.chipmunkmod.command.CommandManager.literal;

public class SayCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("say")
                        .then(argument("message", greedyString())
                                .executes(SayCommand::say)
                        )
        );
    }

    public static int say(CommandContext<FabricClientCommandSource> context) {
        String message = getString(context, "message");
        Chat.sendChatMessage(message, true);

        return Command.SINGLE_SUCCESS;
    }
}
