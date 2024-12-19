package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static land.chipmunk.chipmunkmod.command.CommandManager.literal;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;

import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import java.util.Optional;
import java.util.UUID;

import land.chipmunk.chipmunkmod.mixin.MinecraftClientAccessor;

public class UsernameCommand {
  private static final Session ORIGINAL_SESSION = ((MinecraftClientAccessor) MinecraftClient.getInstance()).session();

  public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
            literal("username")
                    .then(literal("set")
                            .then(argument("username", greedyString())
                                    .executes(UsernameCommand::updateUsername)))
                    .then(literal("revert")
                            .executes(context -> updateSession(context, ORIGINAL_SESSION)))
    );
  }

  public static int updateUsername(CommandContext<FabricClientCommandSource> context) {
    String username = getString(context, "username");

    if (username.length() > 16) {
      ChatUtils.infoPrefix("Username", "Invalid username length!");

      return Command.SINGLE_SUCCESS;
    }

    Session session = new Session(username, new UUID(0L, 0L), "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
    return updateSession(context, session);
  }

  public static int updateSession(CommandContext<FabricClientCommandSource> context, Session session) {
    FabricClientCommandSource source = context.getSource();
    MinecraftClient client = source.getClient();

    ((MinecraftClientAccessor) client).session(session);
    reconnectToCurrentServer(client);

    return Command.SINGLE_SUCCESS;
  }

  private static void reconnectToCurrentServer(MinecraftClient client) {
    ServerInfo serverInfo = client.getCurrentServerEntry();

    if (client.world != null) {
      client.world.disconnect();
    }
    client.disconnect();

    ConnectScreen.connect(new TitleScreen(), client, ServerAddress.parse(serverInfo.address), serverInfo, false, null);
  }
}
