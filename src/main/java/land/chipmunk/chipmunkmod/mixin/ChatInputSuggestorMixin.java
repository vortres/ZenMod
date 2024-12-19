package land.chipmunk.chipmunkmod.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import land.chipmunk.chipmunkmod.ChipmunkMod;
import land.chipmunk.chipmunkmod.Configuration;
import land.chipmunk.chipmunkmod.command.CommandManager;
import land.chipmunk.chipmunkmod.modules.ChomeNSBotCommandSuggestions;
import land.chipmunk.chipmunkmod.modules.LambdaCommandSuggestions;
import land.chipmunk.chipmunkmod.modules.QBotCommandSuggestions;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static land.chipmunk.chipmunkmod.ChipmunkMod.MCInstance;

@Mixin(net.minecraft.client.gui.screen.ChatInputSuggestor.class)
public class ChatInputSuggestorMixin {
  @Shadow
  private CompletableFuture<Suggestions> pendingSuggestions;

  @Shadow
  public void show(boolean narrateFirstSuggestion) {
  }

  @Shadow
  private static int getStartOfCurrentWord(String input) {
    return 0;
  }

  @Mutable
  @Final
  @Shadow
  final TextFieldWidget textField;

  public ChatInputSuggestorMixin() {
    textField = null;
  }

  @Inject(at = @At("TAIL"), method = "refresh()V")
  public void refresh(CallbackInfo ci) {
    final CommandManager commandManager = CommandManager.INSTANCE;

    final String text = this.textField.getText();
    final int cursor = this.textField.getCursor();

    final ClientPlayerEntity player = MCInstance.player;

    final Configuration.Bots botinfo = ChipmunkMod.CONFIG.bots;
    final String lambdaPrefix = botinfo.lambda.prefix;
    final String chomeNSPrefix = botinfo.chomens.prefix;
    final String qbotPrefix = botinfo.qbot.prefix;

    if (!text.contains(" ") && text.startsWith(lambdaPrefix) && player != null) {
      final String textUpToCursor = text.substring(0, cursor);

      final List<String> commands = LambdaCommandSuggestions.INSTANCE.commands
              .stream()
              .map((command) -> command.name).toList();

      pendingSuggestions = CommandSource.suggestMatching(
              commands,
              new SuggestionsBuilder(
                      textUpToCursor,
                      getStartOfCurrentWord(textUpToCursor)
              )
      );

      pendingSuggestions.thenRun(() -> {
        if (!pendingSuggestions.isDone()) return;

        show(true);
      });
    } else if (!text.contains(" ") && text.startsWith(chomeNSPrefix) && player != null) {
      final String textUpToCursor = text.substring(0, cursor);

      final List<String> commands = ChomeNSBotCommandSuggestions.INSTANCE.commands
              .stream()
              .map((command) -> command.name)
              .toList();

      pendingSuggestions = CommandSource.suggestMatching(
              commands,
              new SuggestionsBuilder(
                      textUpToCursor,
                      getStartOfCurrentWord(textUpToCursor)
              )
      );

      pendingSuggestions.thenRun(() -> {
        if (!pendingSuggestions.isDone()) return;

        show(true);
      });
    } else if (!text.contains(" ") && text.startsWith(qbotPrefix) && player != null) {
      final String textUpToCursor = text.substring(0, cursor);

      final List<String> commands = QBotCommandSuggestions.INSTANCE.commands
              .stream()
              .map((command) -> command.name).toList();

      pendingSuggestions = CommandSource.suggestMatching(
              commands,
              new SuggestionsBuilder(
                      textUpToCursor,
                      getStartOfCurrentWord(textUpToCursor)
              )
      );

      pendingSuggestions.thenRun(() -> {
        if (!pendingSuggestions.isDone()) return;

        show(true);
      });
    } else if (cursor > commandManager.prefix.length() && text.startsWith(commandManager.prefix)) {
      final StringReader reader = new StringReader(text);
      reader.setCursor(commandManager.prefix.length()); // Skip the prefix

      final MinecraftClient client = MCInstance;
      final ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

      if (networkHandler == null) return;

      final CommandDispatcher<FabricClientCommandSource> dispatcher = commandManager.dispatcher;
      final FabricClientCommandSource commandSource = (FabricClientCommandSource) networkHandler.getCommandSource();

      pendingSuggestions = dispatcher.getCompletionSuggestions(dispatcher.parse(reader, commandSource), cursor);
      show(true);
    }
  }
}
