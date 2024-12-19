package land.chipmunk.chipmunkmod.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

public class LocationArgumentType implements ArgumentType<Object> {
    private static final Collection<String> EXAMPLES = Arrays.<String>asList("songs/amogus.mid", "images/cat.jpg", "videos/badapple.mp4");
    private static final SimpleCommandExceptionType OOB_FILEPATH = new SimpleCommandExceptionType(Text.literal("The specified file path is outside of the allowed directory"));

    private boolean allowsUrls = false;
    private boolean allowsPaths = false;
    private final Path root;

    private LocationArgumentType (boolean allowsUrls, boolean allowsPaths, Path root) {
        this.allowsUrls = allowsUrls;
        this.allowsPaths = allowsPaths;
        this.root = root.toAbsolutePath().normalize();
    }

    public static LocationArgumentType location (Path rootPath) { return new LocationArgumentType(true, true, rootPath); }
    public static LocationArgumentType url () { return new LocationArgumentType(true, false, null); }
    public static LocationArgumentType filepath (Path rootPath) { return new LocationArgumentType(false, true, rootPath); }

    @Override
    public Object parse (StringReader reader) throws CommandSyntaxException {
        final String remaining = reader.getString().substring(reader.getCursor());
        if (allowsUrls && isUrlStart(remaining)) return parseUrl(reader);
        if (allowsPaths) return parsePath(reader);
        return null;
    }

    public boolean isUrlStart (String string) { return string.startsWith("http://") || string.startsWith("https://") || string.startsWith("ftp://"); }

    public URL parseUrl (StringReader reader) throws CommandSyntaxException {
        final StringBuilder sb = new StringBuilder();
        while (reader.canRead() && reader.peek() != ' ') {
            sb.append(reader.read());
        }

        try {
            return new URL(sb.toString());
        } catch (MalformedURLException exception) {
            throw new SimpleCommandExceptionType(Text.literal(exception.getMessage())).create();
        }
    }

    public Path parsePath (StringReader reader) throws CommandSyntaxException {
        final String pathString = reader.readString();
        final Path path = Path.of(root.toString(), pathString).toAbsolutePath().normalize();
        if (!path.startsWith(root)) throw OOB_FILEPATH.create();
        return path;
    }

    private static Object getLocation (CommandContext<?> context, String name) {
        return context.getArgument(name, Object.class);
    }

    public static URL getUrl (CommandContext<?> context, String name) {
        final Object location = getLocation(context, name);
        if (location instanceof URL) return (URL) location;
        try {
            if (location instanceof Path) return new URL("file", "", -1, location.toString());
        } catch (MalformedURLException ignored) {
            return null; // The real question is whether this will actually ever get called
        }
        return null;
    }

    public static Path getPath (CommandContext<?> context, String name) {
        final Object location = getLocation(context, name);
        if (location instanceof Path) return (Path) location;
        return null;
    }

    @Override
    public Collection<String> getExamples () { return EXAMPLES; }
}
