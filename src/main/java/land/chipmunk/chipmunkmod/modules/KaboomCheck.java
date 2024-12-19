package land.chipmunk.chipmunkmod.modules;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import land.chipmunk.chipmunkmod.listeners.Listener;
import land.chipmunk.chipmunkmod.listeners.ListenerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class KaboomCheck extends Listener {
    public boolean isKaboom = false;

    private Timer timer = null;

    private final MinecraftClient client;

    public static final KaboomCheck INSTANCE = new KaboomCheck(MinecraftClient.getInstance());

    public KaboomCheck (MinecraftClient client) {
        this.client = client;

        ListenerManager.addListener(this);
    }

    public void init () {}

    public void onJoin () {
        final TimerTask task = new TimerTask() {
            public void run () {
                tick();
            }
        };

        if (timer != null) cleanup();

        timer = new Timer();

        timer.schedule(task, 50, 50);

        check();
    }

    private void tick () {
        final ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

        if (networkHandler == null) cleanup();
    }

    private void check () {
        final CompletableFuture<CommandSuggestionsS2CPacket> future = TabComplete.INSTANCE.complete("/ver ");

        future.thenApply((packet) -> {
            final Suggestions suggestions = packet.getSuggestions();

            for (int i = 0; i < suggestions.getList().size(); i++) {
                final Suggestion suggestion = suggestions.getList().get(i);

                if (suggestion.getText().equals("Extras")) {
                    isKaboom = true;
                    break;
                }
            }

            return true;
        });
    }

    private void cleanup () {
        if (timer == null) return;

        isKaboom = false;

        timer.purge();
        timer.cancel();
    }


}
