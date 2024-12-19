package land.chipmunk.chipmunkmod.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientConnection.class)
public interface ClientConnectionInvoker {
    @Invoker("handlePacket")
    static <T extends PacketListener> void handlePacket (Packet<T> packet, PacketListener listener) {
        throw new AssertionError();
    }
}
