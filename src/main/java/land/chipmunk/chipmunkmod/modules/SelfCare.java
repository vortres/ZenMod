package land.chipmunk.chipmunkmod.modules;

import land.chipmunk.chipmunkmod.ChipmunkMod;
import land.chipmunk.chipmunkmod.listeners.Listener;
import land.chipmunk.chipmunkmod.listeners.ListenerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;

import java.util.Timer;
import java.util.TimerTask;

import static land.chipmunk.chipmunkmod.util.network.ServerUtil.serverHasCommand;

public class SelfCare extends Listener {
  private final MinecraftClient client;
  public final long interval;
  public final long chatInterval;

  public boolean opEnabled = true;
  public boolean gamemodeEnabled = true;
  public boolean cspyEnabled = true;
  public boolean icuEnabled = false;

  private int gameMode;

  public String skin;

  private Timer timer;
  private Timer chatTimer;

  private boolean cspy = false;
  public boolean hasSkin = false;

  private int positionPacketsPerSecond = 0;

  public static final SelfCare INSTANCE = new SelfCare(MinecraftClient.getInstance(), 70L, 500L); // make the intervals in config?

  public SelfCare (MinecraftClient client, long interval, long chatInterval) {
    this.client = client;
    this.interval = interval;
    this.chatInterval = chatInterval;

    this.skin = ChipmunkMod.CONFIG.client.autoSkinUsername == null ? "off" : ChipmunkMod.CONFIG.client.autoSkinUsername; // can this be null?

    ListenerManager.addListener(this);
  }

  public void init () {}

  public void onJoin () {
    final TimerTask task = new TimerTask() {
      public void run () {
        tick();
      }
    };

    final TimerTask chatTask = new TimerTask() {
      public void run () {
        chatTick();
      }
    };

    timer = new Timer();
    chatTimer = new Timer();

    timer.schedule(task, interval, interval);
    chatTimer.schedule(chatTask, chatInterval, chatInterval);
  }

  public void cleanup () {
    if (timer == null || chatTimer == null) return;

    timer.cancel();
    timer.purge();

    chatTimer.cancel();
    chatTimer.purge();

    gameMode = -1;

    hasSkin = false;
    cspy = false;
  }

  @Override
  public void chatMessageReceived (Text message) {
    final String stringMessage = message.getString();

    if (stringMessage.equals("Successfully enabled CommandSpy")) cspy = true;
    else if (stringMessage.equals("Successfully disabled CommandSpy")) cspy = false;

    else if (stringMessage.equals("Successfully set your skin to " + skin + "'s")) hasSkin = true;
    else if (
            stringMessage.equals("Successfully removed your skin") ||
                    stringMessage.startsWith("Successfully set your skin to ")
    ) hasSkin = false;
  }

  public void tick () {
    final ClientPlayerEntity player = client.player;
    final ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

    if (networkHandler == null) {
      cleanup();
      return;
    }

    if (player != null && !player.hasPermissionLevel(2) && opEnabled) { if (serverHasCommand("op")) networkHandler.sendChatCommand("op @s[type=player]"); }
    else if (gameMode != 1 && gamemodeEnabled) networkHandler.sendChatCommand("gamemode creative");
    else if (positionPacketsPerSecond >= 10 && icuEnabled) CommandCore.INSTANCE.run("sudo * icu stop");
  }

  public void chatTick () {
    final ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

    if (!cspy && cspyEnabled) { if (serverHasCommand("c")) networkHandler.sendChatCommand("c on"); }
    else if (!hasSkin && !skin.equals("off")) { if (serverHasCommand("skin")) networkHandler.sendChatCommand("skin " + skin); }
  }

  @Override
  public void packetReceived(Packet<?> packet) {
    if (packet instanceof GameJoinS2CPacket) packetReceived((GameJoinS2CPacket) packet);
    else if (packet instanceof GameStateChangeS2CPacket) packetReceived((GameStateChangeS2CPacket) packet);
    else if (packet instanceof PlayerPositionLookS2CPacket) packetReceived((PlayerPositionLookS2CPacket) packet);
  }

  public void packetReceived(GameJoinS2CPacket packet) {
    gameMode = packet.commonPlayerSpawnInfo().gameMode().getId();
  }

  public void packetReceived(GameStateChangeS2CPacket packet) {
    if (packet.getReason() != GameStateChangeS2CPacket.GAME_MODE_CHANGED) return;

    gameMode = (int) packet.getValue();
  }

  public void packetReceived(PlayerPositionLookS2CPacket packet) {
    if (timer == null) return;

    try {
      positionPacketsPerSecond++;

      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          positionPacketsPerSecond--;
        }
      }, 1000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
