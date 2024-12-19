package land.chipmunk.chipmunkmod;

import com.google.gson.GsonBuilder;
import land.chipmunk.chipmunkmod.util.misc.Version;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import land.chipmunk.chipmunkmod.modules.KaboomCheck;
import land.chipmunk.chipmunkmod.modules.Players;
import land.chipmunk.chipmunkmod.modules.SelfCare;
import land.chipmunk.chipmunkmod.util.gson.BlockPosTypeAdapter;
import net.fabricmc.api.ModInitializer;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

public class ChipmunkMod implements ModInitializer {
  public static final Logger LOGGER;
  public static final File MOD_DIR = new File("zenmod");
  private static final File CONFIG_FILE = new File(MOD_DIR, "config.json");

  public static final String MOD_ID = "zenmod";
  public static final ModMetadata MOD_META;
  public static final String NAME;
  public static final Version VERSION;

  public static ChipmunkMod INSTANCE;
  public static Configuration CONFIG;
  public static MinecraftClient MCInstance;

  public static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  static {
    MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();

    NAME = MOD_META.getName();
    LOGGER = LoggerFactory.getLogger(NAME);

    String versionString = MOD_META.getVersion().getFriendlyString();
    if (versionString.contains("-")) versionString = versionString.split("-")[0];
    // When building and running through IntelliJ and not Gradle it doesn't replace the version so just default to v0.0.0 (IDK why)
    if (versionString.equals("${version}")) versionString = "0.0.0";

    VERSION = new Version(versionString);
  }

  @Override
  public void onInitialize() {
    INSTANCE = this;

    try {
      CONFIG = loadConfig();
    } catch (IOException exception) {
      LOGGER.error("Could not load the config", exception);
      CONFIG = new Configuration(); // Create default config if loading fails
    }

    MCInstance = MinecraftClient.getInstance();

    if (CONFIG != null) {
      Players.INSTANCE.init();
      KaboomCheck.INSTANCE.init();
      SelfCare.INSTANCE.init();
      LOGGER.info("Loaded ZenMod (chipmunk mod fork)");
    } else {
      LOGGER.error("Failed to initialize ZenMod - CONFIG is null");
    }
  }

  public static Configuration loadConfig() throws IOException {
    MOD_DIR.mkdirs();

    final Gson gson = new GsonBuilder()
            .registerTypeAdapter(BlockPos.class, new BlockPosTypeAdapter())
            .create();
    final File file = CONFIG_FILE;

    if (!file.exists()) {
      InputStream is = ChipmunkMod.class.getClassLoader().getResourceAsStream("default_config.json");
      if (is == null) {
        LOGGER.warn("Default config not found, creating new configuration");
        return new Configuration();
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      final StringBuilder sb = new StringBuilder();
      while (reader.ready()) sb.append((char) reader.read());
      final String defaultConfig = sb.toString();

      // Write the default config
      BufferedWriter configWriter = new BufferedWriter(new FileWriter(file));
      configWriter.write(defaultConfig);
      configWriter.close();

      return gson.fromJson(defaultConfig, Configuration.class);
    }

    try (InputStream is = new FileInputStream(file);
         BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      return gson.fromJson(reader, Configuration.class);
    }
  }
}
