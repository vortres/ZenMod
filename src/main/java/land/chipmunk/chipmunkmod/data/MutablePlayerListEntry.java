package land.chipmunk.chipmunkmod.data;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class MutablePlayerListEntry {
    public GameProfile profile;
    public GameMode gamemode;
    public int latency;
    public Text displayName;

    public MutablePlayerListEntry(GameProfile profile, GameMode gamemode, int latency, Text displayName) {
        this.profile = profile;
        this.gamemode = gamemode;
        this.latency = latency;
        this.displayName = displayName;
    }

    public MutablePlayerListEntry (PlayerListS2CPacket.Entry entry) {
        this(entry.profile(), entry.gameMode(), entry.latency(), entry.displayName());
    }
}
