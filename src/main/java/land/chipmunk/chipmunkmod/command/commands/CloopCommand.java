package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import static land.chipmunk.chipmunkmod.command.CommandManager.literal;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;
import static com.mojang.brigadier.arguments.LongArgumentType.longArg;
import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import land.chipmunk.chipmunkmod.modules.CommandLoopManager;
import java.util.List;

public class CloopCommand {
    public static void register (CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("cloop")
                        .then(
                                literal("add")
                                        .then(
                                                argument("interval", longArg())
                                                        .then(
                                                                argument("command", greedyString())
                                                                        .executes(c -> addCloop(c))
                                                        )
                                        )
                        )
                        .then(
                                literal("remove")
                                        .then(
                                                argument("id", integer())
                                                        .executes(c -> removeCloop(c))
                                        )
                        )
                        .then(
                                literal("clear")
                                        .executes(c -> clearCloops(c))
                        )
                        .then(
                                literal("list")
                                        .executes(c -> listCloops(c))
                        )
        );
    }

    public static int addCloop (CommandContext<FabricClientCommandSource> context) {
        final FabricClientCommandSource source = context.getSource();
        final long interval = getLong(context, "interval");
        final String command = getString(context, "command");

        int id = CommandLoopManager.INSTANCE.loopCommand(command, interval);

        ChatUtils.infoPrefix("CLOOP", "Cloop [hl]\"%s\"[df] created with ID [hl]%s", command, id);
        return Command.SINGLE_SUCCESS;
    }

    public static int removeCloop (CommandContext<FabricClientCommandSource> context) {
        final FabricClientCommandSource source = context.getSource();
        final CommandLoopManager manager = CommandLoopManager.INSTANCE;
        final int id = getInteger(context, "id");

        if (id < 0 || id >= manager.commandLoops.size()) {
            ChatUtils.infoPrefix("CLOOP", "Invalid cloop ID [hl]%s", id);
            return Command.SINGLE_SUCCESS;
        }

        manager.removeAndStop(id);

        ChatUtils.infoPrefix("CLOOP", "Cloop with ID [hl]%s[def] removed", id);
        return Command.SINGLE_SUCCESS;
    }

    public static int clearCloops (CommandContext<FabricClientCommandSource> context) {
        final FabricClientCommandSource source = context.getSource();
        final CommandLoopManager manager = CommandLoopManager.INSTANCE;

        manager.clearLoops();

        ChatUtils.infoPrefix("CLOOP", "Cleared all cloops");
        return Command.SINGLE_SUCCESS;
    }

    public static int listCloops(CommandContext<FabricClientCommandSource> context) {
        final FabricClientCommandSource source = context.getSource();
        final List<CommandLoopManager.CommandLoop> loops = CommandLoopManager.INSTANCE.commandLoops;

        if (loops == null || loops.isEmpty()) {
            ChatUtils.infoPrefix("CLOOP", "No cloops currently running!");
            return Command.SINGLE_SUCCESS;
        }

        ChatUtils.infoPrefix("CLOOP", "Cloops List:");

        int id = 0; // Start indexing at 0
        for (CommandLoopManager.CommandLoop loop : loops) {
            ChatUtils.info("[hl]%d [def]› cmd: [hl]'%s'[def], int: [hl]%d[def]ms", id, loop.command, loop.interval);
            id++;
        }

        return Command.SINGLE_SUCCESS;
    }
}
