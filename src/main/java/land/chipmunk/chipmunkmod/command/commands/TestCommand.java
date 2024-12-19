package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import static land.chipmunk.chipmunkmod.command.CommandManager.literal;

import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class TestCommand {
  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
            literal("test")
                    .executes(TestCommand::helloWorld)
    );
  }

  public static int helloWorld(CommandContext<FabricClientCommandSource> context) {
    displayMessages();

    return Command.SINGLE_SUCCESS;
  }

  private static void displayMessages() {
    ChatUtils.infoPrefix("TEST", "Hello world!");
    ChatUtils.info("Hello world!");
    ChatUtils.warningPrefix("TEST", "Hello world!");
    ChatUtils.warning("Hello world!");
    ChatUtils.errorPrefix("TEST", "Hello world!");
    ChatUtils.error("Hello world!");
  }
}
