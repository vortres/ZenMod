package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import land.chipmunk.chipmunkmod.command.CommandManager;
import land.chipmunk.chipmunkmod.modules.SongPlayer;
import land.chipmunk.chipmunkmod.song.Song;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.FloatArgumentType.getFloat;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.LongArgumentType.getLong;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;
import static land.chipmunk.chipmunkmod.command.CommandManager.literal;
import static land.chipmunk.chipmunkmod.command.arguments.LocationArgumentType.*;
import static land.chipmunk.chipmunkmod.command.arguments.TimestampArgumentType.timestamp;

public class MusicCommand {
    private static final SimpleCommandExceptionType NO_SONG_IS_CURRENTLY_PLAYING = new SimpleCommandExceptionType(Text.translatable("No song is currently playing"));
    private static final SimpleCommandExceptionType OOB_TIMESTAMP = new SimpleCommandExceptionType(Text.translatable("Invalid timestamp for the current song"));
    private static final SimpleCommandExceptionType DIRECTORY_DOES_NOT_EXIST = new SimpleCommandExceptionType(Text.translatable("The specified directory does not exist"));

    public static void register (CommandDispatcher<FabricClientCommandSource> dispatcher) {
        final MusicCommand instance = new MusicCommand();

        Path root = Path.of(SongPlayer.SONG_DIR.getPath());

        dispatcher.register(
                literal("music")
                        .then(
                                literal("play")
                                        .then(
                                                argument("location", location(root))
                                                        .executes(instance::play)
                                        )
                        )

                        .then(literal("stop").executes(instance::stop))
                        .then(literal("skip").executes(instance::skip))
                        .then(literal("pause").executes(instance::pause))

                        .then(
                                literal("list")
                                        .executes(c -> instance.list(root))
                                        .then(
                                                argument("location", filepath(root))
                                                        .executes(c -> instance.list(getPath(c, "location")))
                                        )
                        )

                        .then(
                                literal("loop")
                                        .executes(instance::toggleLoop)
                                        .then(
                                                argument("count", integer())
                                                        .executes(instance::loop)
                                        )
                        )

                        .then(
                                literal("goto")
                                        .then(
                                                argument("timestamp", timestamp())
                                                        .executes(instance::gotoCommand)
                                        )
                        )

                        .then(
                                literal("useCore")
                                        .then(
                                                argument("boolean", bool())
                                                        .executes(instance::useCore)
                                        )
                        )

                        .then(
                                literal("actionbar")
                                        .then(
                                                argument("boolean", bool())
                                                        .executes(instance::actionbar)
                                        )
                        )

                        .then(
                                literal("pitch")
                                        .then(
                                                argument("pitch", floatArg())
                                                        .executes(instance::pitch)
                                        )
                        )
        );
    }

    public int play (CommandContext<FabricClientCommandSource> context) {
        final SongPlayer songPlayer = SongPlayer.INSTANCE;

        final Path path = getPath(context, "location");

        if (path != null) songPlayer.loadSong(path);
        else songPlayer.loadSong(getUrl(context, "location"));

        return 1;
    }

    public int stop (CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        final SongPlayer songPlayer = SongPlayer.INSTANCE;

        if (songPlayer.currentSong == null) throw NO_SONG_IS_CURRENTLY_PLAYING.create();

        songPlayer.stopPlaying();
        songPlayer.songQueue.clear();

        ChatUtils.infoPrefix("Music", "[hl]Stopped[def] music playback");
        return 1;
    }

    public int skip (CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        final FabricClientCommandSource source = context.getSource();
        final SongPlayer songPlayer = SongPlayer.INSTANCE;

        if (songPlayer.currentSong == null) throw NO_SONG_IS_CURRENTLY_PLAYING.create();

        songPlayer.stopPlaying();

        ChatUtils.infoPrefix("Music", "Skipped current song");
        return 1;
    }

    public int pause (CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        final SongPlayer songPlayer = SongPlayer.INSTANCE;
        final Song currentSong = songPlayer.currentSong;

        if (currentSong == null) throw NO_SONG_IS_CURRENTLY_PLAYING.create();

        if (!currentSong.paused) {
            currentSong.pause();
            ChatUtils.infoPrefix("Music", "[hl]Paused[def] current song");
        } else {
            currentSong.play();
            ChatUtils.infoPrefix("Music", "[hl]Unpaused[def] current song");
        }

        return 1;
    }

    public int list (Path path) throws CommandSyntaxException {
        final CommandManager commandManager = CommandManager.INSTANCE;

        final String prefix = commandManager.prefix;

        final File directory = path.toFile();
        final String[] filenames = directory.list();
        if (filenames == null) throw DIRECTORY_DOES_NOT_EXIST.create();

        final Path root = Path.of(SongPlayer.SONG_DIR.getAbsoluteFile().getPath()).toAbsolutePath();
        String relativePath;
        if (path.getNameCount() - root.getNameCount() > 0) relativePath = path.subpath(root.getNameCount(), path.getNameCount()).toString();
        else relativePath = "";

        final List<Component> directories = new ArrayList<>();
        final List<Component> files = new ArrayList<>();
        int i = 0;

        for (String filename : filenames) {
            final File file = new File(directory, filename);
            if (!file.isDirectory()) continue;

            final NamedTextColor color = (i++ & 1) == 0 ? NamedTextColor.DARK_GREEN : NamedTextColor.GREEN;

            final Path relativeFilepath = Path.of(relativePath, filename);
            final String escapedPath = escapePath(relativeFilepath.toString());

            directories.add(
                    Component.text(filename + "/", color)
                            .clickEvent(ClickEvent.suggestCommand(prefix + "music list " + escapedPath))
                            .hoverEvent(HoverEvent.showText(Component.translatable("Click to list %s", Component.text(filename))))
            );
        }

        for (String filename : filenames) {
            final File file = new File(directory, filename);
            if (file.isDirectory()) continue;

            final NamedTextColor color = (i++ & 1) == 0 ? NamedTextColor.DARK_GREEN : NamedTextColor.GREEN;

            final Path relativeFilepath = Path.of(relativePath, filename);
            final String escapedPath = escapePath(relativeFilepath.toString());

            files.add(
                    Component.text(filename, color)
                            .clickEvent(ClickEvent.suggestCommand(prefix + "music play " + escapedPath))
                            .hoverEvent(HoverEvent.showText(Component.translatable("Click to play %s", Component.text(filename))))
            );
        }

        final ArrayList<Component> mergedList = new ArrayList<>();
        mergedList.addAll(directories);
        mergedList.addAll(files);
        final Component component = Component.translatable("Songs - %s", Component.join(JoinConfiguration.separator(Component.space()), mergedList)).color(NamedTextColor.GREEN);

        ((Audience) MinecraftClient.getInstance().player).sendMessage(component);
        return 1;
    }

    // TODO: Move this into some utility class, as it is more related to brigadier strings in general than to the list command in specific
    private String escapePath (String path) {
        final StringBuilder sb = new StringBuilder("'");

        for (char character : path.toCharArray()) {
            if (character == '\'' || character == '\\') sb.append('\\');
            sb.append(character);
        }

        sb.append("'");
        return sb.toString();
    }

    public int toggleLoop (CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        final SongPlayer songPlayer = SongPlayer.INSTANCE;
        final Song currentSong = songPlayer.currentSong;

        if (currentSong == null) throw NO_SONG_IS_CURRENTLY_PLAYING.create();

        currentSong.looping = !currentSong.looping;

        ChatUtils.infoPrefix("Music", "Looping is now [hl]" + (currentSong.looping ? "on" : "off"));
        return 1;
    }


    public int loop (CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        final SongPlayer songPlayer = SongPlayer.INSTANCE;
        final Song currentSong = songPlayer.currentSong;
        final int count = getInteger(context, "count");

        if (currentSong == null) throw NO_SONG_IS_CURRENTLY_PLAYING.create();

        currentSong.looping = true;
        currentSong.loopCount = count;

        ChatUtils.infoPrefix("Music", "Enabled loop for [hl]%s[def] times", String.valueOf(count));
        return 1;
    }

    public int gotoCommand (CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        final SongPlayer songPlayer = SongPlayer.INSTANCE;
        final Song currentSong = songPlayer.currentSong;
        final long millis = getLong(context, "timestamp");

        if (currentSong == null) throw NO_SONG_IS_CURRENTLY_PLAYING.create();
        if (millis < 0 || millis > currentSong.length) throw OOB_TIMESTAMP.create();

        currentSong.setTime(millis);

        ChatUtils.infoPrefix("Music", "Song time stamp now set to [hl]%s[def].", songPlayer.formatTime(millis));
        return 1;
    }

    public int useCore (CommandContext<FabricClientCommandSource> context) {
        final boolean enabled = getBool(context, "boolean");

        SongPlayer.INSTANCE.useCore = enabled;

        ChatUtils.infoPrefix("Music", "Playing music with [hl]core[def] is now " + (enabled ? "on" : "off"));
        return 1;
    }

    public int actionbar (CommandContext<FabricClientCommandSource> context) {
        final boolean enabled = getBool(context, "boolean");

        SongPlayer.INSTANCE.actionbar = enabled;

        ChatUtils.infoPrefix("Music", "Actionbar is now [hl]" + (enabled ? "on" : "off"));
        return 1;
    }

    public int pitch (CommandContext<FabricClientCommandSource> context) {
        final float pitch = getFloat(context, "pitch");

        SongPlayer.INSTANCE.pitch = pitch;

        ChatUtils.infoPrefix("Music", "Set the pitch to [hl]%s[def].", pitch);
        return 1;
    }
}
