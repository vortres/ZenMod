package land.chipmunk.chipmunkmod.song;

import land.chipmunk.chipmunkmod.modules.SongPlayer;
import land.chipmunk.chipmunkmod.util.network.DownloadUtils;
import net.minecraft.text.Text;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SongLoaderThread extends Thread {
  private String location;
  private File songPath;
  private URL songUrl;
  public SongLoaderException exception;
  public Song song;

  private boolean isUrl;

  public SongLoaderThread (URL location) throws SongLoaderException {
    isUrl = true;
    songUrl = location;
  }

  public SongLoaderThread (Path location) throws SongLoaderException {
    isUrl = false;
    songPath = location.toFile();
  }

  public void run () {
    byte[] bytes;
    String name;
    try {
      if (isUrl) {
        bytes = DownloadUtils.DownloadToByteArray(songUrl);
        name = Paths.get(songUrl.toURI().getPath()).getFileName().toString();
      } else {
        bytes = Files.readAllBytes(songPath.toPath());
        name = songPath.getName();
      }
    } catch (Exception e) {
      exception = new SongLoaderException(Text.literal(e.getMessage()), e);
      return;
    }

    try {
      song = MidiConverter.getSongFromBytes(bytes, name);
    } catch (Exception ignored) {
    }

    if (song == null) {
      try {
        song = NBSConverter.getSongFromBytes(bytes, name);
      } catch (Exception ignored) {
      }
    }

    if (song == null) {
      exception = new SongLoaderException(Text.translatable("Invalid song format"));
    }
  }

  private File getSongFile (String name) {
    return new File(SongPlayer.SONG_DIR, name);
  }
}
