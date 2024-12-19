package land.chipmunk.chipmunkmod.song;

import net.minecraft.text.Text;

public class SongLoaderException extends Exception {
  public final Text message;

  public SongLoaderException (Text message) {
    super();
    this.message = message;
  }

  public SongLoaderException (Text message, Throwable cause) {
    super(null, cause);
    this.message = message;
  }

  @Override
  public String getMessage () {
    return message.getString();
  }
}
