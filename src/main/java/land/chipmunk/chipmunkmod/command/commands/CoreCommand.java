package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static land.chipmunk.chipmunkmod.command.CommandManager.literal;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;

import land.chipmunk.chipmunkmod.util.misc.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.nbt.NbtCompound;
import java.util.concurrent.CompletableFuture;
import land.chipmunk.chipmunkmod.modules.CommandCore;

public class CoreCommand {
  public static void register (CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
      literal("core")
        .then(
          literal("run")
            .then(
              argument("command", greedyString())
                .executes(c -> run(c))
            )
        )

        .then(
          literal("runTracked")
            .then(
              argument("command", greedyString())
                .executes(c -> runTracked(c))
            )
        )

        .then(literal("refill").executes(c -> refill(c)))
        .then(literal("move").executes(c -> move(c)))

        .then(
                literal("runFillCommand")
                        .then(
                                argument("enabled", bool())
                                        .executes(c -> runFillCommand(c))
                        )
        )
    );
  }

  public static int run (CommandContext<FabricClientCommandSource> context) {
    CommandCore.INSTANCE.run(getString(context, "command"));

    return Command.SINGLE_SUCCESS;
  }

  public static int runTracked (CommandContext<FabricClientCommandSource> context) {
    final FabricClientCommandSource source = context.getSource();

    final String command = getString(context, "command");

    final CompletableFuture<NbtCompound> future = CommandCore.INSTANCE.runTracked(command);
    future.thenApply(tag -> {
      try {
        final String output = tag.getString("LastOutput");
        if (output != null) source.sendFeedback(TextUtil.fromJson(output));
      } catch (Exception e) {
        e.printStackTrace();
      }

      return tag;
    });

    return Command.SINGLE_SUCCESS;
  }

  public static int refill (CommandContext<FabricClientCommandSource> context) {
    CommandCore.INSTANCE.refill();

    return Command.SINGLE_SUCCESS;
  }

  public static int move (CommandContext<FabricClientCommandSource> context) {
    final FabricClientCommandSource source = context.getSource();

    CommandCore.INSTANCE.move(source.getClient().player.getPos());

    return Command.SINGLE_SUCCESS;
  }

  public static int runFillCommand(CommandContext<FabricClientCommandSource> context) {
    final FabricClientCommandSource source = context.getSource();

    final boolean bool = getBool(context, "enabled");

    CommandCore.INSTANCE.runFillCommand = bool;

    source.sendFeedback(Text.literal("Running fill commands are now " + (bool ? "on" : "off")));

    return Command.SINGLE_SUCCESS;
  }
}
