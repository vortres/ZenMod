package land.chipmunk.chipmunkmod.mixin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import land.chipmunk.chipmunkmod.listeners.Listener;
import land.chipmunk.chipmunkmod.listeners.ListenerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(net.minecraft.network.ClientConnection.class)
public class ClientConnectionMixin {
  @Unique
  private static final Pattern CUSTOM_PITCH_PATTERN = Pattern.compile(".*\\.pitch\\.(.*)");

  @Inject(at = @At("HEAD"), method = "disconnect", cancellable = true)
  public void disconnect (Text disconnectReason, CallbackInfo ci) {
    if (disconnectReason == ClientPlayNetworkHandlerAccessor.chatValidationFailedText()) {
      ci.cancel();
    }
  }

  @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
  private void exceptionCaught (ChannelHandlerContext context, Throwable ex, CallbackInfo ci) {
    ci.cancel();
    ex.printStackTrace();
  }

  @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
  private static void handlePacket (Packet<?> packet, PacketListener _listener, CallbackInfo ci) {
    for (Listener listener : ListenerManager.listeners) {
      listener.packetReceived(packet);
    }

    final MinecraftClient client = MinecraftClient.getInstance();

    // please don't skid this.,.
    // mabe mabe mabe
    if (packet instanceof ParticleS2CPacket t_packet) {
      final double max = 1000;

      if (t_packet.getCount() > max) {
        ci.cancel();
      }
    } else if (packet instanceof PlaySoundS2CPacket t_packet) {
      final SoundEvent soundEvent = t_packet.getSound().value();

      final Identifier sound = soundEvent.getId();

      final Matcher matcher = CUSTOM_PITCH_PATTERN.matcher(sound.getPath());

      if (!matcher.find()) return;

      try {
        final String stringPitch = matcher.group(1);

        final float pitch = Float.parseFloat(stringPitch);

        final ClientWorld world = client.world;

        if (world == null) return;

        // huge mess
        final SoundEvent newSound = SoundEvent.of(Identifier.of(sound.getNamespace(), sound.getPath().substring(0, sound.getPath().length() - (".pitch." + stringPitch).length())));

        client.executeSync(() -> world.playSound(client.player, t_packet.getX(), t_packet.getY(), t_packet.getZ(), newSound, t_packet.getCategory(), t_packet.getVolume(), pitch, t_packet.getSeed()));

        ci.cancel();
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }

      if (t_packet.getVolume() == 1 && sound.getPath().equals("entity.enderman.scream")) ci.cancel();
    }
  }

  @Inject(at = @At("HEAD"), method = "send(Lnet/minecraft/network/packet/Packet;)V", cancellable = true)
  private void sendPacket (Packet<?> packet, CallbackInfo ci) {
    if (packet instanceof RequestCommandCompletionsC2SPacket t_packet) {
      if (t_packet.getPartialCommand().length() > 2048) {
        ci.cancel();
        return;
      }
    }

    for (Listener listener : ListenerManager.listeners) {
      listener.packetSent(packet);
    }
  }
}
