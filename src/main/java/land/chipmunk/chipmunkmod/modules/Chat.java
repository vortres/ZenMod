package land.chipmunk.chipmunkmod.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public class Chat {
    public static double secret = Math.random();

    public static void sendChatMessage (String message) { sendChatMessage(message, false); }
    public static void sendChatMessage (String message, boolean usePlayerChat) {
        if (message == null) return;

        final ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();

        if (usePlayerChat) networkHandler.sendChatMessage(secret + message);
        else networkHandler.sendChatMessage(message);
    }
}
