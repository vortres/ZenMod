package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import land.chipmunk.chipmunkmod.ChipmunkMod;
import land.chipmunk.chipmunkmod.modules.CommandCore;
import land.chipmunk.chipmunkmod.modules.CustomChat;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;

import static land.chipmunk.chipmunkmod.command.CommandManager.literal;

public class ReloadConfigCommand {
    public static void register (CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("reloadconfig")
                        .executes(ReloadConfigCommand::reload)
        );
    }

    public static int reload(CommandContext<FabricClientCommandSource> context) {
        final FabricClientCommandSource source = context.getSource();

        try {
            ChipmunkMod.CONFIG = ChipmunkMod.loadConfig();

            CustomChat.INSTANCE.reloadFormat();
            CommandCore.INSTANCE.reloadRelativeArea();

            ChatUtils.info("Successfully reloaded the config");
        } catch (IOException e) {
            ChatUtils.error("Could not load the config, see logs for stacktrace");
            ChatUtils.warning("Error: [hl]%s", e.getMessage());
            e.printStackTrace();
        }

        return Command.SINGLE_SUCCESS;
    }
}
