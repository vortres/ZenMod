package land.chipmunk.chipmunkmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import land.chipmunk.chipmunkmod.util.misc.ColorUtils;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import land.chipmunk.chipmunkmod.command.commands.*;

import java.util.Objects;

public class CommandManager {
  public CommandDispatcher<FabricClientCommandSource> dispatcher = new CommandDispatcher<>();
  public String prefix;

  public static CommandManager INSTANCE;

  public CommandManager (String prefix, CommandRegistryAccess commandRegistryAccess) {
    this.prefix = prefix;

    TestCommand.register(this.dispatcher);
    CoreCommand.register(this.dispatcher);
    UsernameCommand.register(this.dispatcher);
    ItemCommand.register(this.dispatcher, commandRegistryAccess);
    EvalCommand.register(this.dispatcher);
    CustomChatCommand.register(this.dispatcher);
    MusicCommand.register(this.dispatcher);
    RainbowNameCommand.register(this.dispatcher);
    SayCommand.register(this.dispatcher);
    AutoSkinCommand.register(this.dispatcher);
    ReloadConfigCommand.register(this.dispatcher);
    SelfCareCommand.register(this.dispatcher);
    ValidateCommand.register(this.dispatcher);

    // CloopCommand.register(this.dispatcher);
    // KickCommand.register(this.dispatcher);
  }

  public void executeCommand (String command) {
    final MinecraftClient client = MinecraftClient.getInstance();

    final FabricClientCommandSource commandSource = (FabricClientCommandSource) Objects.requireNonNull(client.getNetworkHandler()).getCommandSource();

    try {
      dispatcher.execute(command, commandSource);
    } catch (CommandSyntaxException e) {
      ChatUtils.warningPrefix("CMD", String.valueOf(e.getRawMessage()));
      final Text context = getContext(e);
      if (context != null) commandSource.sendError(context);
    } catch (Exception e) {
      ChatUtils.warning(e.getMessage());
    }
  }

  public Text getContext (CommandSyntaxException exception) {
    final int _cursor = exception.getCursor();
    final String input = exception.getInput();

    if (input == null || _cursor < 0) {
      return null;
    }
    final MutableText text = Text.literal("» ")
            .formatted(Formatting.GRAY);
    text.setStyle(text.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, prefix + input)));

    final int cursor = Math.min(input.length(), _cursor);

    if (cursor > CommandSyntaxException.CONTEXT_AMOUNT) {
      text.append(Text.literal("..."));
    }

    text
            .append(Text.literal(input.substring(Math.max(0, cursor - CommandSyntaxException.CONTEXT_AMOUNT), cursor)).setStyle(Style.EMPTY.withColor(ColorUtils.PRIMARY)))
            .append(Text.literal(input.substring(cursor)).setStyle(Style.EMPTY.withColor(ColorUtils.DANGER).withFormatting(Formatting.UNDERLINE)))
            .append(Text.translatable("command.context.here").setStyle(Style.EMPTY.withColor(ColorUtils.WARNING).withFormatting(Formatting.ITALIC)));

    return text;
  }

  public static LiteralArgumentBuilder<FabricClientCommandSource> literal (String name) { return LiteralArgumentBuilder.literal(name); }
  public static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> argument (String name, ArgumentType<T> type) { return RequiredArgumentBuilder.argument(name, type); }
}
