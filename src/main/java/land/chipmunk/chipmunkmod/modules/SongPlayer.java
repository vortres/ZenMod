package land.chipmunk.chipmunkmod.modules;

import land.chipmunk.chipmunkmod.song.Note;
import land.chipmunk.chipmunkmod.song.Song;
import land.chipmunk.chipmunkmod.song.SongLoaderException;
import land.chipmunk.chipmunkmod.song.SongLoaderThread;
import land.chipmunk.chipmunkmod.util.misc.ColorUtils;
import land.chipmunk.chipmunkmod.util.misc.MathUtils;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import static land.chipmunk.chipmunkmod.ChipmunkMod.*;

public class SongPlayer {
    public static File SONG_DIR = new File(MOD_DIR, "songs");
    static {
        if (!SONG_DIR.exists()) {
            SONG_DIR.mkdir();
        }
    }

    public static final SongPlayer INSTANCE = new SongPlayer();
    public static final String SELECTOR  = "@a[tag=!nomusic,tag=!zenmod_nomusic,tag=!chimunkmod_nomusic]";

    public SongLoaderThread loaderThread;
    private int ticksUntilPausedActionbar = 40;

    public LinkedList<Song> songQueue = new LinkedList<>();
    public Song currentSong;
    public Timer playTimer;

    public boolean useCore = true;
    public boolean actionbar = true;
    public float pitch = 0;

    public void loadSong (Path location) {
        if (loaderThread != null) {
            ChatUtils.infoPrefix("Music", "Already loading a song, [hl]cannot load another[def].");
            return;
        }

        try {
            final SongLoaderThread _loaderThread = new SongLoaderThread(location);
            ChatUtils.infoPrefix("Music", "Loading the song...");

            _loaderThread.start();
            loaderThread = _loaderThread;
        } catch (SongLoaderException e) {
            ChatUtils.warningPrefix("Music", "Failed to load song.");
            ChatUtils.warning("Error: [hl]%s", e.message.getString());
            LOGGER.warn("Song player Error (Loading Song File): {}", e.message.getString());

            loaderThread = null;
        }
    }

    public void loadSong (URL location) {
        if (loaderThread != null) {
            ChatUtils.infoPrefix("Music", "Already loading a song, [hl]cannot load another[def].");
            return;
        }

        try {
            final SongLoaderThread _loaderThread = new SongLoaderThread(location);
            ChatUtils.infoPrefix("Music", "Loading the song...");

            _loaderThread.start();
            loaderThread = _loaderThread;
        } catch (SongLoaderException e) {
            ChatUtils.warningPrefix("Music", "Failed to load song.");
            ChatUtils.warning("Error: [hl]%s", e.message.getString());
            LOGGER.warn("Song player Error (Loading Song URL): {}", e.message.getString());

            loaderThread = null;
        }
    }

    public void coreReady () {
        playTimer = new Timer();

        final TimerTask playTask = new TimerTask() {
            @Override
            public void run () {
                final ClientPlayNetworkHandler networkHandler = MCInstance.getNetworkHandler();
                if (networkHandler == null) {
                    disconnected();
                    return;
                }

                if (loaderThread != null && !loaderThread.isAlive()) {
                    if (loaderThread.exception != null) {
                        ChatUtils.warningPrefix("Music", "Failed to load song.");
                        ChatUtils.warning("Error: [hl]%s", loaderThread.exception.message.getString());
                        LOGGER.warn("Song player Error (Loading Exception): {}", loaderThread.exception.message.getString());
                    } else {
                        songQueue.add(loaderThread.song);
                        ChatUtils.infoPrefix("Music", "Added the song to the [hl]Queue[def].");
                    }
                    loaderThread = null;
                }

                if (currentSong == null) {
                    if (songQueue.isEmpty()) return;

                    currentSong = songQueue.poll();
                    ChatUtils.infoPrefix("Music", "Now playing [hl]%s", currentSong.name);

                    currentSong.play();
                }

                if (currentSong.paused && ticksUntilPausedActionbar-- < 0) return;
                else ticksUntilPausedActionbar = 20;
                try {
                    if (!useCore && actionbar && MCInstance.player != null) {
                        ((Audience) MCInstance.player).sendActionBar(generateActionbar());
                    }
                    else if (actionbar) CommandCore.INSTANCE.run("title " + SELECTOR + " actionbar " + GsonComponentSerializer.gson().serialize(generateActionbar()));
                } catch (Exception e) {
                    ChatUtils.warningPrefix("Music", "Something went wrong.");
                    ChatUtils.warning("Error: [hl]%s", e.getMessage());
                    LOGGER.warn("Song player Error (Actionbar): {}", e.getMessage());
                }

                if (currentSong.paused) return;
                handlePlaying();

                if (currentSong.finished()) {
                    ChatUtils.infoPrefix("Music", "Finished playing [hl]%s", currentSong.name);
                    currentSong = null;
                }
            }
        };

        playTimer.schedule(playTask, 60, 50);
        if (currentSong != null) currentSong.play();
    }

    public Component generateActionbar() {
        final ClientPlayerEntity player = MCInstance.player;
        assert player != null;

        TextColor primaryColor = currentSong.paused ? NamedTextColor.DARK_GRAY : TextColor.color(ColorUtils.PRIMARY);
        TextColor secondaryColor = currentSong.paused ? NamedTextColor.GRAY : TextColor.color(ColorUtils.SECONDARY);
        TextColor dividerColor = TextColor.color(ColorUtils.GRAY);

        // Build the action bar component
        Component component = Component.empty()
                .append(Component.translatable("Now playing %s", Component.empty()
                        .append(Component.text(currentSong.name, secondaryColor))))
                .append(Component.translatable(" | ", dividerColor))
                .append(Component.translatable("%s/%s",
                        formatTime(currentSong.time).color(primaryColor),
                        formatTime(currentSong.length).color(secondaryColor)))
                .append(Component.translatable(" | ", dividerColor))
                .append(Component.translatable("%s/%s",
                        Component.text(currentSong.position, primaryColor),
                        Component.text(currentSong.size(), secondaryColor)));

        if (currentSong.looping) {
            if (currentSong.loopCount > 0) {
                return component
                        .append(Component.text(" \uD83D\uDD01", TextColor.color(ColorUtils.PRIMARY)).decorate(TextDecoration.BOLD))
                        .append(Component.translatable(" (%s/%s)",
                                        Component.text(currentSong.currentLoop),
                                        Component.text(currentSong.loopCount))
                                .color(TextColor.color(ColorUtils.WARNING)));
            }
            return component.append(Component.translatable(" \uD83D\uDD01", TextColor.color(ColorUtils.PRIMARY)).decorate(TextDecoration.BOLD));
        }

        if (currentSong.paused) {
            return component.append(Component.translatable(" ||", TextColor.color(ColorUtils.PRIMARY)).decorate(TextDecoration.BOLD));
        }

        return component;
    }

    public Component formatTime (long millis) {
        final int seconds = (int) millis / 1000;

        final String minutePart = String.valueOf(seconds / 60);
        final String unpaddedSecondPart = String.valueOf(seconds % 60);
        
        return Component.translatable(
                "%s:%s",
                Component.text(minutePart),
                Component.text(unpaddedSecondPart.length() < 2 ? "0" + unpaddedSecondPart : unpaddedSecondPart)
        );
    }

    public void stopPlaying () {
        currentSong = null;
    }

    public void disconnected () {
        playTimer.cancel();
        playTimer.purge();

        if (currentSong != null) currentSong.pause();
    }

    public void handlePlaying () {
        currentSong.advanceTime();
        while (currentSong.reachedNextNote()) {
            final Note note = currentSong.getNextNote();

            try {
                if (!useCore && MCInstance.player != null) {
                    final float floatingPitch = (float) (0.5 * (Math.pow(2, ((note.pitch + (pitch / 10)) / 12))));
                    final String[] thing = note.instrument.sound.split(":");

                    if (thing[1] == null) return;
                    MCInstance.submit(() -> {
                        assert MCInstance.world != null;
                        assert MCInstance.player != null;
                        MCInstance.world.playSound(
                                MCInstance.player.getX(),
                                MCInstance.player.getY(),
                                MCInstance.player.getZ(),
                                SoundEvent.of(Identifier.of(thing[0], thing[1])),
                                SoundCategory.RECORDS,
                                note.volume,
                                floatingPitch,
                                true
                        );
                    });
                } else {
                    final float floatingPitch = MathUtils.clamp((float) (0.5 * (Math.pow(2, ((note.pitch + (pitch / 10)) / 12)))), 0F, 2F);
                    CommandCore.INSTANCE.run("execute as " + SELECTOR + " at @s run playsound " + note.instrument.sound + " record @s ~ ~ ~ " + note.volume + " " + floatingPitch);
                }
            } catch (Exception e) {
                ChatUtils.warningPrefix("Music", "Something went wrong.");
                ChatUtils.warning("Error: [hl]%s", e.getMessage());
                LOGGER.warn("Song player Error (Handle Playing): {}", e.getMessage());
            }
        }
    }
}
