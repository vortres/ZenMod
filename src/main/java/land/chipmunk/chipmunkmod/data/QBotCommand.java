package land.chipmunk.chipmunkmod.data;

import java.util.ArrayList;
import java.util.List;

public class QBotCommand {
    public final String name;
    public final TrustLevel trustLevel;
    public final List<String> aliases = new ArrayList<>();

    public QBotCommand (
            String name,
            TrustLevel trustLevel
    ) {
        this.name = name;
        this.trustLevel = trustLevel;
    }

    public enum TrustLevel {
        PUBLIC,
        TRUSTED,
        OWNER
    }
}
