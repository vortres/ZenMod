package land.chipmunk.chipmunkmod.mixin;

import com.google.gson.JsonObject;
import land.chipmunk.chipmunkmod.ChipmunkMod;

import land.chipmunk.chipmunkmod.Configuration;
import land.chipmunk.chipmunkmod.data.LambdaBotCommand;
import land.chipmunk.chipmunkmod.modules.LambdaCommandSuggestions;

import land.chipmunk.chipmunkmod.data.ChomeNSBotCommand;
import land.chipmunk.chipmunkmod.modules.ChomeNSBotCommandSuggestions;

import land.chipmunk.chipmunkmod.data.QBotCommand;
import land.chipmunk.chipmunkmod.modules.QBotCommandSuggestions;

import land.chipmunk.chipmunkmod.util.misc.BotValidationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static land.chipmunk.chipmunkmod.ChipmunkMod.MCInstance;

@Mixin(value = net.minecraft.client.gui.screen.ChatScreen.class)
public class ChatScreenMixin extends Screen {
  @Shadow private String originalChatText;

  public ChatScreenMixin(String originalChatText) {
    super(Text.translatable("chat_screen.title"));
    this.originalChatText = originalChatText;
  }

  @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
  private void sendMessage (String chatText, boolean addToHistory, CallbackInfo cir) {
    final MinecraftClient client = MCInstance;

    if (addToHistory) {
      client.inGameHud.getChatHud().addToMessageHistory(chatText);
    }

    final Configuration.Bots botinfo = ChipmunkMod.CONFIG.bots;
    final String lambdaPrefix = botinfo.lambda.prefix;
    final String chomeNSPrefix = botinfo.chomens.prefix;
    final String qbotPrefix = botinfo.qbot.prefix;
    final String testbotPrefix = botinfo.testbot.prefix;

    if (chatText.startsWith(lambdaPrefix)) {
      final List<LambdaBotCommand> commands = LambdaCommandSuggestions.INSTANCE.commands;

      final List<String> moreOrTrustedCommands = commands.stream()
              .filter((command) -> command.accessLevel != LambdaBotCommand.AccessLevel.PUBLIC)
              .map((command) -> command.name.toLowerCase())
              .toList();

      final List<String> aliases = new ArrayList<>();
      for (LambdaBotCommand command : commands) {
        if (command.accessLevel == LambdaBotCommand.AccessLevel.PUBLIC) continue;

        aliases.addAll(command.aliases);
      }

      final String chatCommand = chatText.toLowerCase().split("\\s")[0];
      final int prefixLength = lambdaPrefix.length();

      if (
              moreOrTrustedCommands.contains(chatCommand) ||
                      aliases.contains(chatCommand.substring(prefixLength))
      ) {
        try {
          BotValidationUtils.lambda(chatText.substring(prefixLength));

          cir.cancel();

          return;
        } catch (Exception ignored) {}
      }
    } else if (chatText.startsWith(chomeNSPrefix)) {
      final List<ChomeNSBotCommand> commands = ChomeNSBotCommandSuggestions.INSTANCE.commands;

      final List<String> moreOrTrustedCommands = commands.stream()
              .filter((command) -> command.trustLevel != ChomeNSBotCommand.TrustLevel.PUBLIC)
              .map((command) -> command.name.toLowerCase())
              .toList();

      final List<String> aliases = new ArrayList<>();
      for (ChomeNSBotCommand command : commands) {
        if (command.trustLevel == ChomeNSBotCommand.TrustLevel.PUBLIC) continue;

        aliases.addAll(command.aliases);
      }

      final String chatCommand = chatText.toLowerCase().split("\\s")[0];
      final int prefixLength = chomeNSPrefix.length();

      if (
              moreOrTrustedCommands.contains(chatCommand) ||
                      aliases.contains(chatCommand.substring(prefixLength))
      ) {
        try {
          BotValidationUtils.chomens(chatText.substring(prefixLength));

          cir.cancel();

          return;
        } catch (Exception ignored) {}
      }
    } else if (chatText.startsWith(qbotPrefix)) {
      final List<QBotCommand> commands = QBotCommandSuggestions.INSTANCE.commands;

      final List<String> moreOrTrustedCommands = commands.stream()
              .filter((command) -> command.trustLevel != QBotCommand.TrustLevel.PUBLIC)
              .map((command) -> command.name.toLowerCase())
              .toList();

      final List<String> aliases = new ArrayList<>();
      for (QBotCommand command : commands) {
        if (command.trustLevel == QBotCommand.TrustLevel.PUBLIC) continue;

        aliases.addAll(command.aliases);
      }

      final String chatCommand = chatText.toLowerCase().split("\\s")[0];
      final int prefixLength = qbotPrefix.length();

      if (
              moreOrTrustedCommands.contains(chatCommand) ||
                      aliases.contains(chatCommand.substring(prefixLength))
      ) {
        try {
          BotValidationUtils.qbot(chatText.substring(prefixLength));

          cir.cancel();

          return;
        } catch (Exception ignored) {}
      }
    } else if (botinfo.testbot.webhookUrl != null && chatText.startsWith(testbotPrefix)) {
      ChipmunkMod.executorService.submit(() -> {
        try {
          final URL url = new URL(botinfo.testbot.webhookUrl);

          final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
          connection.addRequestProperty("Content-Type", "application/json");
          connection.addRequestProperty("User-Agent", "ChipmunkMod");
          connection.setDoOutput(true);
          connection.setRequestMethod("POST");

          final JsonObject jsonObject = new JsonObject();

          jsonObject.addProperty("username", "ChipmunkMod UwU");
          jsonObject.addProperty("content", MCInstance.getSession().getUsername());

          final OutputStream stream = connection.getOutputStream();
          stream.write(jsonObject.toString().getBytes());
          stream.flush();
          stream.close();

          connection.getInputStream().close();
          connection.disconnect();
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }

    if (client == null) return;

    if (chatText.startsWith("/")) {
      client.player.networkHandler.sendChatCommand(chatText.substring(1));
    } else {
      client.player.networkHandler.sendChatMessage(chatText);
    }

    cir.cancel();
  }
}
