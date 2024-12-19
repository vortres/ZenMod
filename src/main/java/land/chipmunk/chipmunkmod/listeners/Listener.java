package land.chipmunk.chipmunkmod.listeners;

import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

public class Listener {
    public void chatMessageReceived (Text message) {}

    public void packetReceived (Packet<?> packet) {}

    public void packetSent (Packet<?> packet) {}

    public void coreReady () {}

    public void coreMoved () {}
}
