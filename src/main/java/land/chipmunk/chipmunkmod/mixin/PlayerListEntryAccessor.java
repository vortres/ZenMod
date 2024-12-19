package land.chipmunk.chipmunkmod.mixin;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerListEntry.class)
public interface PlayerListEntryAccessor {
    @Accessor("gameMode")
    void setGameMode (GameMode gameMode);

    @Accessor("latency")
    void setLatency (int latency);
}
