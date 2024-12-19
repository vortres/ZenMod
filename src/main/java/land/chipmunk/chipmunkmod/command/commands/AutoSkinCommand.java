package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import land.chipmunk.chipmunkmod.modules.SelfCare;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;
import static land.chipmunk.chipmunkmod.command.CommandManager.literal;

public class AutoSkinCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("autoskin")
                        .then(argument("username", string())
                                .executes(AutoSkinCommand::execute)
                        )
        );
    }

    public static int execute(CommandContext<FabricClientCommandSource> context) {
        String username = getString(context, "username");
        SelfCare selfCare = SelfCare.INSTANCE;

        selfCare.skin = username;

        if ("off".equals(username)) {
            ChatUtils.infoPrefix("Auto Skin", "Auto skin is now [hl]off");
        } else {
            selfCare.hasSkin = false;
            ChatUtils.infoPrefix("Auto Skin", "Set your auto skin username to [hl]%s", username);
        }

        return Command.SINGLE_SUCCESS;
    }
}
