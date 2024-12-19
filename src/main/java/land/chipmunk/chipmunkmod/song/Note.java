package land.chipmunk.chipmunkmod.song;


public class Note implements Comparable<Note> {
  public Instrument instrument;
  public int pitch;
  public float volume;
  public long time;

  public Note (Instrument instrument, int pitch, float volume, long time) {
    this.instrument = instrument;
    this.pitch = pitch;
    this.volume = volume;
    this.time = time;
  }

  @Override
  public int compareTo(Note other) {
      return Long.compare(time, other.time);
  }

  public int noteId () {
    return pitch + instrument.id * 25;
  }
}
