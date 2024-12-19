package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;
import static land.chipmunk.chipmunkmod.command.CommandManager.literal;
import static land.chipmunk.chipmunkmod.util.misc.BotValidationUtils.*;

public class ValidateCommand {
  public static void register (CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
      literal("validate")
        .then(literal("lambda").then(argument("command", greedyString())
                .executes(c -> lambda(getString(c, "command")))))
        .then(literal("chomens").then(argument("command", greedyString())
                .executes(c -> {
                  ChatUtils.warningPrefix("Validation", "Manual ChomeNS Bot validation is deprecated. Please use the completions from typing the bot's prefix.");

                  return chomens(getString(c, "command"));
                })))
        .then(literal("fnfboyfriend").then(argument("command", greedyString())
                .executes(c -> fnfboyfriend(getString(c, "command")))))
        .then(literal("nbot").then(argument("command", greedyString())
                .executes(c -> nbot(getString(c, "command")))))
        .then(literal("qbot").then(argument("command", greedyString())
                .executes(c -> qbot(getString(c, "command")))))
        .then(literal("hbot").then(argument("command", greedyString())
                .executes(c -> hbot(getString(c, "command")))))
        .then(literal("sbot").then(argument("command", greedyString())
                .executes(c -> sbot(getString(c, "command")))))
        .then(literal("kittycorp").then(argument("command", greedyString())
                .executes(c -> kittycorp(getString(c, "command")))))
    );
  }
}
