package land.chipmunk.chipmunkmod.mixin;

import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.client.MinecraftClient.class)
public interface MinecraftClientAccessor {
  @Accessor("session")
  Session session ();

  @Mutable
  @Accessor("session")
  void session (Session session);
}
