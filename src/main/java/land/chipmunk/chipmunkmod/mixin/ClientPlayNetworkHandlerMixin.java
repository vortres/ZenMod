package land.chipmunk.chipmunkmod.mixin;

import land.chipmunk.chipmunkmod.ChipmunkMod;
import land.chipmunk.chipmunkmod.command.CommandManager;
import land.chipmunk.chipmunkmod.listeners.Listener;
import land.chipmunk.chipmunkmod.listeners.ListenerManager;
import land.chipmunk.chipmunkmod.modules.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;

import static land.chipmunk.chipmunkmod.ChipmunkMod.MCInstance;

@Mixin(value = net.minecraft.client.network.ClientPlayNetworkHandler.class, priority = 1002)
public class ClientPlayNetworkHandlerMixin {
  @Final
  @Shadow private FeatureSet enabledFeatures;
  @Final
  @Shadow private DynamicRegistryManager.Immutable combinedDynamicRegistries;
  @Shadow private LastSeenMessagesCollector lastSeenMessagesCollector;
  @Shadow private MessageChain.Packer messagePacker;

  @Inject(method = "onGameJoin", at = @At("TAIL"))
  private void onGameJoin (GameJoinS2CPacket packet, CallbackInfo ci) {
    final CommandRegistryAccess commandRegistryAccess = CommandRegistryAccess.of(this.combinedDynamicRegistries, this.enabledFeatures);

    KaboomCheck.INSTANCE.onJoin();
    CommandManager.INSTANCE = new CommandManager(ChipmunkMod.CONFIG.client.prefix, commandRegistryAccess);
    SelfCare.INSTANCE.onJoin();
    CommandCore.INSTANCE.init();
    CustomChat.INSTANCE.init();
    SongPlayer.INSTANCE.coreReady();
    RainbowName.INSTANCE.init();

    // Bot command suggestions stuff
    LambdaCommandSuggestions.INSTANCE.init();
    LambdaAuth.INSTANCE.init();

    ChomeNSBotCommandSuggestions.INSTANCE.init();
    ChomeNSAuth.INSTANCE.init();

    QBotCommandSuggestions.INSTANCE.init();
    QBotAuth.INSTANCE.init(); // Fix incorrect uppercase on QBotAuth
  }

  @Inject(method = "onPlayerRemove", at = @At("HEAD"), cancellable = true)
  private void onPlayerRemove (PlayerRemoveS2CPacket packet, CallbackInfo ci) { ci.cancel(); }

  @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
  private void onGameMessage (GameMessageS2CPacket packet, CallbackInfo ci) {
    final Text message = packet.content();

    // Ignore annoying messages
    try {
      if (RainbowName.INSTANCE.enabled) {
        if (message.getString().contains("Your nickname is now ") || message.getString().contains("Nickname changed.")) {
          ci.cancel();
          return;
        }
      }

      try {
        if (((TranslatableTextContent) message.getContent()).getKey().equals("advMode.setCommand.success")) {
          ci.cancel();
          return;
        }
      } catch (ClassCastException ignored) {}

      for (Listener listener : ListenerManager.listeners) {
        listener.chatMessageReceived(message);
      }

      // Bot command suggestion requests messages
      final String suggestionId = message.getSiblings().getFirst().getString();
      final String authId = ((PlainTextContent) message.getContent()).string();

      try {
        if (suggestionId.equals(LambdaCommandSuggestions.ID) || authId.equals(LambdaAuth.INSTANCE.ID)) {
          ci.cancel();
        }
      } catch (Exception ignored) {}

      try {
        if (suggestionId.equals(ChomeNSBotCommandSuggestions.ID) || authId.equals(ChomeNSAuth.INSTANCE.ID)) {
          ci.cancel();
        }
      } catch (Exception ignored) {}

      try {
        if (suggestionId.equals(QBotCommandSuggestions.ID) || authId.equals(QBotAuth.INSTANCE.ID)) { // Fix incorrect uppercase on QBotAuth
          ci.cancel();
        }
      } catch (Exception ignored) {}
    } catch (Exception ignored) {}
  }

  @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
  private void sendChatMessage (String chatText, CallbackInfo ci) {
    final CommandManager commandManager = CommandManager.INSTANCE;

    final String secret = String.valueOf(Chat.secret);

    if (chatText.startsWith(commandManager.prefix)) {
      commandManager.executeCommand(chatText.substring(commandManager.prefix.length()));

      ci.cancel();
    } else if (!chatText.startsWith("/") && !chatText.startsWith(secret)) {
      CustomChat.INSTANCE.chat(chatText);

      ci.cancel();
    }

    if (chatText.startsWith(secret)) {
      final String content = chatText.substring(secret.length());

      Instant instant = Instant.now();
      long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();

      LastSeenMessagesCollector.LastSeenMessages lastSeenMessages = this.lastSeenMessagesCollector.collect();
      MessageSignatureData messageSignatureData = this.messagePacker.pack(new MessageBody(content, instant, l, lastSeenMessages.lastSeen()));
      MCInstance.getNetworkHandler().sendPacket(new ChatMessageC2SPacket(content, instant, l, messageSignatureData, lastSeenMessages.update()));

      ci.cancel();
    }
  }
}