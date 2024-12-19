package land.chipmunk.chipmunkmod.command.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Arrays;

public class TimestampArgumentType implements ArgumentType<Long> {
    private static final Collection<String> EXAMPLES = Arrays.<String>asList("0:01", "1:23", "6:09");

    private TimestampArgumentType () {
    }

    public static TimestampArgumentType timestamp () { return new TimestampArgumentType(); }

    @Override
    public Long parse (StringReader reader) throws CommandSyntaxException {
        long seconds = 0L;
        long minutes = 0L;

        seconds = reader.readLong();
        if (reader.canRead() && reader.peek() == ':') {
            reader.skip();
            minutes = seconds;
            seconds = reader.readLong();
        }

        return (seconds * 1000) + (minutes * 1000 * 60);
    }

    // ? Should I create a getter method? Seems like reinventing the wheel since LongArgumentType#getLong is already a thing.

    @Override
    public Collection<String> getExamples () { return EXAMPLES; }
}
