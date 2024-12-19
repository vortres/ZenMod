package land.chipmunk.chipmunkmod.util.network;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public class ServerUtil {
    public static boolean serverHasCommand(String name) {
        final MinecraftClient client = MinecraftClient.getInstance();
        final ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

        if (networkHandler == null) return false;

        for (CommandNode node : networkHandler.getCommandDispatcher().getRoot().getChildren()) {
            if (!(node instanceof LiteralCommandNode literal)) continue;

            if (literal.getLiteral().equals(name)) return true;
        }

        return false;
    }
}
