package land.chipmunk.chipmunkmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import land.chipmunk.chipmunkmod.modules.CommandCore;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
  @Unique private static MinecraftClient CLIENT = MinecraftClient.getInstance();

  @Inject(at = @At("HEAD"), method = "move")
  public void move (MovementType type, Vec3d relPos, CallbackInfo ci) {
    if ((ClientPlayerEntity) (Object) this != CLIENT.player) return;

    final Vec3d position = ((ClientPlayerEntity) (Object) this).getPos().add(relPos);

    final ClientWorld world = CLIENT.getNetworkHandler().getWorld();

    final BlockPos origin = CommandCore.INSTANCE.origin;
    if (origin == null) { CommandCore.INSTANCE.move(position); return; }
    final int distance = (int) Math.sqrt(new Vec2f(origin.getX() / 16, origin.getZ() / 16).distanceSquared(new Vec2f((int) position.getX() / 16, (int) position.getZ() / 16)));
    if (distance > world.getSimulationDistance()) {
      CommandCore.INSTANCE.clientPlayerEntityFilled = true;
      CommandCore.INSTANCE.move(position);
    }
  }
}
