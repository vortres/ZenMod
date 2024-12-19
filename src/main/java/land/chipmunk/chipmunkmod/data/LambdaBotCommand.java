package land.chipmunk.chipmunkmod.data;

import java.util.ArrayList;
import java.util.List;

public class LambdaBotCommand {
    public final String name;
    public final AccessLevel accessLevel;
    public final List<String> aliases = new ArrayList<>();

    public LambdaBotCommand (
            String name,
            AccessLevel accessLevel
    ) {
        this.name = name;
        this.accessLevel = accessLevel;
    }

    public enum AccessLevel {
        PUBLIC,
        TRUSTED,
        FULL,
        OWNER
    }
}
