package land.chipmunk.chipmunkmod.modules;

import land.chipmunk.chipmunkmod.ChipmunkMod;
import land.chipmunk.chipmunkmod.data.BlockArea;
import land.chipmunk.chipmunkmod.listeners.Listener;
import land.chipmunk.chipmunkmod.listeners.ListenerManager;
import land.chipmunk.chipmunkmod.util.misc.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class CommandCore {
  private final MinecraftClient client;
  public boolean ready = false;
  public BlockPos origin;
  public BlockArea noPos;
  public BlockPos block;
  public BlockArea withPos;

  private Timer timer;

  private boolean shouldRefill = false;

  public boolean runFillCommand = true;

  public boolean clientPlayerEntityFilled = false;

  public static CommandCore INSTANCE = new CommandCore(MinecraftClient.getInstance());

  public CommandCore (MinecraftClient client) {
    this.client = client;
    reloadRelativeArea();
  }

  public void init () {
    if (timer != null) cleanup();

    final TimerTask task = new TimerTask() {
      public void run () {
        tick();
      }
    };

    final TimerTask refillTask = new TimerTask() {
      @Override
      public void run() {
        if (clientPlayerEntityFilled) {
          clientPlayerEntityFilled = false;
          return;
        }

        check();

        if (!shouldRefill) return;

        refill();

        shouldRefill = false;
      }
    };

    timer = new Timer();

    timer.schedule(task, 50, 50);

    timer.schedule(refillTask, 50, 1000);

    move(client.player.getPos());
  }

  private void tick () {
    final ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

    if (networkHandler == null) {
      cleanup();

      return;
    }

    reloadRelativeArea();
  }

  public void reloadRelativeArea () {
    noPos = ChipmunkMod.CONFIG.client.core.relativeArea;
  }

  public void check () {
    final ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

    if (networkHandler == null || withPos == null || !ready) return;

    try {
      for (int x = withPos.start.getX(); x <= withPos.end.getX(); x++) {
        for (int y = withPos.start.getY(); y <= withPos.end.getY(); y++) {
          for (int z = withPos.start.getZ(); z <= withPos.end.getZ(); z++) {
            final BlockPos pos = new BlockPos(x, y, z);

            final ClientWorld world = client.world;

            if (world == null) return;

            final Block block = world.getBlockState(pos).getBlock();

            if (block instanceof CommandBlock) continue;

            shouldRefill = true;

            return;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void move (Vec3d position) {
    final ClientWorld world = client.world;

    if (world == null || noPos == null) return;

    final DimensionType dimension = world.getDimension();

    origin = new BlockPos(
            ((int) position.getX() / 16) * 16,
            (int) MathUtils.clamp(noPos.start.getY(), dimension.minY(), dimension.height()),
            ((int) position.getZ() / 16) * 16
    );

    withPos = new BlockArea(
            new BlockPos(
                    noPos.start.getX() + origin.getX(),
                    (int) MathUtils.clamp(noPos.start.getY(), dimension.minY(), dimension.height()),
                    noPos.start.getZ() + origin.getZ()
            ),
            new BlockPos(
                    noPos.end.getX() + origin.getX(),
                    (int) MathUtils.clamp(noPos.end.getY(), dimension.minY(), dimension.height()),
                    noPos.end.getZ() + origin.getZ()
            )
    );

    block = new BlockPos(withPos.start);
    refill();

    for (Listener listener : ListenerManager.listeners) listener.coreMoved();
    if (!ready) {
      ready = true;

      for (Listener listener : ListenerManager.listeners) listener.coreReady();
    }
  }

  public void refill () {
    if (!runFillCommand || withPos == null) return;

    final String command = String.format(
            KaboomCheck.INSTANCE.isKaboom ?
                    "fill %s %s %s %s %s %s repeating_command_block replace" :
                    "fill %s %s %s %s %s %s command_block",
            withPos.start.getX(),
            withPos.start.getY(),
            withPos.start.getZ(),

            withPos.end.getX(),
            withPos.end.getY(),
            withPos.end.getZ()
    );

    client.getNetworkHandler().sendChatCommand(command);
  }

  public void incrementCurrentBlock () {
    if (withPos == null) return;

    final BlockPos start = withPos.start;
    final BlockPos end = withPos.end;

    if (start == null || end == null) return;

    int x = block.getX();
    int y = block.getY();
    int z = block.getZ();

    x++;

    if (x > end.getX()) {
      x = start.getX();
      z++;
    }

    if (z > end.getZ()) {
      z = start.getZ();
      y++;
    }

    if (y > end.getY()) {
      x = start.getX();
      y = start.getY();
      z = start.getZ();
    }

    block = new BlockPos(x, y, z);
  }

  public void run (String command) {
    final ClientConnection connection = client.getNetworkHandler().getConnection();

    if (block == null) return;

    System.out.println(command);

    if (KaboomCheck.INSTANCE.isKaboom) {
      connection.send(
              new UpdateCommandBlockC2SPacket(
                      block,
                      command,
                      CommandBlockBlockEntity.Type.AUTO,
                      false,
                      false,
                      true
              )
      );
    } else {
      connection.send(
              new UpdateCommandBlockC2SPacket(
                      block,
                      "",
                      CommandBlockBlockEntity.Type.REDSTONE,
                      false,
                      false,
                      false
              )
      );

      connection.send(
              new UpdateCommandBlockC2SPacket(
                      block,
                      command,
                      CommandBlockBlockEntity.Type.REDSTONE,
                      false,
                      false,
                      true
              )
      );
    }

    incrementCurrentBlock();
  }

  public CompletableFuture<NbtCompound> runTracked (String command) {
    final ClientConnection connection = client.getNetworkHandler().getConnection();

    if (block == null) return new CompletableFuture<>();

    if (KaboomCheck.INSTANCE.isKaboom) {
      connection.send(
              new UpdateCommandBlockC2SPacket(
                      block,
                      command,
                      CommandBlockBlockEntity.Type.AUTO,
                      true,
                      false,
                      true
              )
      );
    } else {
      connection.send(
              new UpdateCommandBlockC2SPacket(
                      block,
                      "",
                      CommandBlockBlockEntity.Type.REDSTONE,
                      true,
                      false,
                      false
              )
      );

      connection.send(
              new UpdateCommandBlockC2SPacket(
                      block,
                      command,
                      CommandBlockBlockEntity.Type.REDSTONE,
                      true,
                      false,
                      true
              )
      );
    }

    incrementCurrentBlock();

    CompletableFuture<NbtCompound> future = new CompletableFuture<>();

    final Timer timer = new Timer();

    final TimerTask queryTask = new TimerTask() {
      public void run () {
        client.getNetworkHandler().getDataQueryHandler().queryBlockNbt(block, future::complete);

        timer.cancel(); // ? Is this necesary?
        timer.purge();
      }
    };

    timer.schedule(queryTask, 50);

    return future;
  }

  public void cleanup () {
    if (timer == null) return;

    timer.cancel();
    timer.purge();

    withPos = null;
    block = null;
    ready = false;
  }
}
