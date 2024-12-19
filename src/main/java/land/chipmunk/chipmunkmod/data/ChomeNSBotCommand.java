package land.chipmunk.chipmunkmod.data;

import java.util.ArrayList;
import java.util.List;

public class ChomeNSBotCommand {
    public final String name;
    public final TrustLevel trustLevel;
    public final List<String> aliases = new ArrayList<>();

    public ChomeNSBotCommand (
            String name,
            TrustLevel trustLevel
    ) {
        this.name = name;
        this.trustLevel = trustLevel;
    }

    public enum TrustLevel {
        PUBLIC,
        TRUSTED,
        ADMIN,
        OWNER
    }
}
