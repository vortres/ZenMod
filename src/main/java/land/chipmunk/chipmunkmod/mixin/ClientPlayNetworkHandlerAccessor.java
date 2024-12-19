package land.chipmunk.chipmunkmod.mixin;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(net.minecraft.client.network.ClientPlayNetworkHandler.class)
public interface ClientPlayNetworkHandlerAccessor {
  @Accessor("CHAT_VALIDATION_FAILED_TEXT")
  static Text chatValidationFailedText () { throw new AssertionError(); }

  @Accessor("playerListEntries")
  Map<UUID, PlayerListEntry> playerListEntries();

  @Accessor("listedPlayerListEntries")
  Set<PlayerListEntry> listedPlayerListEntries();
}
