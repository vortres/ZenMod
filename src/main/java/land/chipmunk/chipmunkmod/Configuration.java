package land.chipmunk.chipmunkmod;

import com.google.gson.JsonObject;
import land.chipmunk.chipmunkmod.data.BlockArea;
import net.minecraft.util.math.BlockPos;

public class Configuration {
  public ClientConfig client = new ClientConfig();
  public Bots bots = new Bots();

  public static class ClientConfig {
    public String prefix = ";";
    public String autoSkinUsername = "off";
    public Colors colors = new Colors();
    public CustomChat customChat = new CustomChat();
    public CommandCore core = new CommandCore();
  }

  public static class Bots {
    public LambdaBotInfo lambda = new LambdaBotInfo("λ", null);
    public ChomeNSBotInfo chomens = new ChomeNSBotInfo("*", null, null, null);
    public BotInfo fnfboyfriend = new BotInfo("~", null);
    public BotInfo nbot = new BotInfo("?", null);
    public BotInfo qbot = new BotInfo("}", null);
    public BotInfo hbot = new BotInfo("#", null);
    public BotInfo sbot = new BotInfo(":", null);
    public TestBotInfo testbot = new TestBotInfo("-", null);
    public BotInfo chipmunk = new BotInfo("'", null);
    public BotInfo kittycorp = new BotInfo("^", null);
  }

  public static class Colors {
    public String PRIMARY = "#3b82f6";
    public String SECONDARY = "#a855f7";
    public String DANGER = "#f43f5e";
    public String SUCCESS = "#10b981";
    public String GRAY = "#353b40";
  }

  public static class CommandCore {
    public BlockArea relativeArea = new BlockArea(new BlockPos(0, 0, 0), new BlockPos(15, 1, 15));
  }

  public static class CustomChat {
    public JsonObject format;
  }

  // Default bot infos
  public static class BotInfo {
    public String prefix;
    public String key;

    public BotInfo (String prefix, String key) {
      this.prefix = prefix;
      this.key = key;
    }
  }

  // Special bot infos
  public static class LambdaBotInfo { // rn its just same as BotInfo class, but I will change it soon fr
    public String prefix;
    public String key;

    public LambdaBotInfo (String prefix, String key) {
      this.prefix = prefix;
      this.key = key;
    }
  }

  public static class ChomeNSBotInfo {
    public String prefix;
    public String key;
    public String authKey;
    public String formatKey;

    public ChomeNSBotInfo(String prefix, String key, String authKey, String formatKey) {
      this.prefix = prefix;
      this.key = key;
      this.authKey = authKey;
      this.formatKey = formatKey;
    }
  }

  public static class TestBotInfo {
    public String prefix;
    public String webhookUrl;

    public TestBotInfo (String prefix, String webhookUrl) {
      this.prefix = prefix;
      this.webhookUrl = webhookUrl;
    }
  }
}
