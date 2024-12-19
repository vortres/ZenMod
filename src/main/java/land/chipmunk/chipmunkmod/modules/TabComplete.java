package land.chipmunk.chipmunkmod.modules;

import land.chipmunk.chipmunkmod.listeners.Listener;
import land.chipmunk.chipmunkmod.listeners.ListenerManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TabComplete extends Listener {
    private final MinecraftClient client;

    private final Map<Integer, CompletableFuture<CommandSuggestionsS2CPacket>> transactions = new HashMap<>();

    public static TabComplete INSTANCE = new TabComplete(MinecraftClient.getInstance());

    public TabComplete (MinecraftClient client) {
        this.client = client;
        ListenerManager.addListener(this);
    }

    public void init () {}

    public CompletableFuture<CommandSuggestionsS2CPacket> complete (String command) {
        final ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

        if (networkHandler == null) return null;

        final ClientConnection connection = networkHandler.getConnection();

        if (connection == null) return null;

        final int transactionId = TransactionManager.INSTANCE.nextTransactionId();
        connection.send(new RequestCommandCompletionsC2SPacket(transactionId, command));

        final CompletableFuture<CommandSuggestionsS2CPacket> future = new CompletableFuture<>();
        transactions.put(transactionId, future);
        return future;
    }

    @Override
    public void packetReceived (Packet<?> packet) {
        if (packet instanceof CommandSuggestionsS2CPacket) packetReceived((CommandSuggestionsS2CPacket) packet);
    }

    public void packetReceived (CommandSuggestionsS2CPacket packet) {
        final CompletableFuture<CommandSuggestionsS2CPacket> future = transactions.get(packet.id());

        if (future == null) return;
        future.complete(packet);
        transactions.remove(future);
    }
}
