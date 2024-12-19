package land.chipmunk.chipmunkmod.song;

public class Instrument {
  public static final Instrument HARP = new Instrument(0, "harp", 54);
  public static final Instrument BASEDRUM = new Instrument(1, "basedrum", 0);
  public static final Instrument SNARE = new Instrument(2, "snare", 0);
  public static final Instrument HAT = new Instrument(3, "hat", 0);
  public static final Instrument BASS = new Instrument(4, "bass", 30);
  public static final Instrument FLUTE = new Instrument(5, "flute", 66);
  public static final Instrument BELL = new Instrument(6, "bell", 78);
  public static final Instrument GUITAR = new Instrument(7, "guitar", 42);
  public static final Instrument CHIME = new Instrument(8, "chime", 78);
  public static final Instrument XYLOPHONE = new Instrument(9, "xylophone", 78);
  public static final Instrument IRON_XYLOPHONE = new Instrument(10, "iron_xylophone", 54);
  public static final Instrument COW_BELL = new Instrument(11, "cow_bell", 66);
  public static final Instrument DIDGERIDOO = new Instrument(12, "didgeridoo", 30);
  public static final Instrument BIT = new Instrument(13, "bit", 54);
  public static final Instrument BANJO = new Instrument(14, "banjo", 54);
  public static final Instrument PLING = new Instrument(15, "pling", 54);

  public final int id;
  public final String name;
  public final int offset;
  public final String sound;

  private Instrument (int id, String name, int offset, String sound) {
    this.id = id;
    this.name = name;
    this.offset = offset;
    this.sound = sound;
  }

  private Instrument (int id, String name, int offset) {
    this.id = id;
    this.name = name;
    this.offset = offset;
    this.sound = "minecraft:block.note_block." + name;
  }

  public static Instrument of (String sound) {
    return new Instrument(-1, null, 0, sound);
  }

  private static final Instrument[] values = {HARP, BASEDRUM, SNARE, HAT, BASS, FLUTE, BELL, GUITAR, CHIME, XYLOPHONE, IRON_XYLOPHONE, COW_BELL, DIDGERIDOO, BIT, BANJO, PLING};
  public static Instrument fromId (int id) {
    return values[id];
  }
}
