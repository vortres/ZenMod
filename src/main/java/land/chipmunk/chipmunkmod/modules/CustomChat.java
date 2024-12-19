package land.chipmunk.chipmunkmod.modules;

import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import land.chipmunk.chipmunkmod.ChipmunkMod;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

import static land.chipmunk.chipmunkmod.ChipmunkMod.MCInstance;

public class CustomChat {
    public static final CustomChat INSTANCE = new CustomChat(MCInstance);
    private final MinecraftClient client;

    public boolean enabled = true;
    public String format;
    private Timer timer;
    private int total = 0;

    public CustomChat(MinecraftClient client) {
        this.client = client;
        reloadFormat();
    }

    public void init() {
        final TimerTask task = new TimerTask() {
            public void run () {
                tick();
            }
        };

        resetTotal();

        timer = new Timer();
        timer.schedule(task, 0, 50);
    }

    private void tick() {
        final ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

        if (networkHandler != null) return;

        resetTotal();
        cleanup();
    }

    public void resetTotal() {
        total = 0;
    }

    private void cleanup() {
        if (timer == null) return;

        timer.cancel();
        timer.purge();
    }

    public void reloadFormat() {
        final JsonElement formatElement = ChipmunkMod.CONFIG.client.customChat.format;

        if (formatElement == null) {
            format = "{\"translate\":\"chat.type.text\",\"with\":[\"USERNAME\",\"MESSAGE\"]}";
        } else {
            // First deserialize the JsonElement to a Component
            Component formatComponent = GsonComponentSerializer.gson().deserializeFromTree(formatElement);
            // Then serialize it back to a string format
            format = GsonComponentSerializer.gson().serialize(formatComponent);
        }
    }

    public void chat(String message) {
        final ClientPlayerEntity player = client.player;

        if (!enabled || !player.hasPermissionLevel(2) || !player.isCreative()) {
            Chat.sendChatMessage(message, true);
            return;
        }

        final String username = MCInstance.getSession().getUsername();
        final String sanitizedMessage = message
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
        final String randomized = String.valueOf(Math.random());

        final Component deserialized = serializer.deserialize(message);
        final String messageWithColor = GsonComponentSerializer.gson().serialize(deserialized).replace("MESSAGE", randomized);

        final String key = ChipmunkMod.CONFIG.bots.chomens.formatKey;
        final String chomeNSKey = key != null ?
                Hashing.sha256()
                        .hashString(key + total, StandardCharsets.UTF_8)
                        .toString()
                        .substring(0, 8) :
                "";
        total++;

        try {
            // Process the format string
            String sanitizedFormat = format
                    .replace("USERNAME", username)
                    .replace("HASH", chomeNSKey)
                    .replace("{\"text\":\"MESSAGE\"}", messageWithColor)
                    .replace("\"extra\":[\"MESSAGE\"]", "\"extra\":[" + messageWithColor + "]")
                    .replace("MESSAGE", sanitizedMessage.replaceAll("&.", ""));

            // Remove any remaining randomized placeholders
            sanitizedFormat = sanitizedFormat.replace(randomized, "");

            // Send the formatted message
            String command = (KaboomCheck.INSTANCE.isKaboom ? "minecraft:tellraw @a " : "tellraw @a ") + sanitizedFormat;
            CommandCore.INSTANCE.run(command);
        } catch (Exception err) {
            ChatUtils.errorPrefix("CustomChat", "Something went wrong: [hl]%s", err.toString() );
        }
    }
}