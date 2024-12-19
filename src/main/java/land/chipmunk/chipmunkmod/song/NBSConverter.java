package land.chipmunk.chipmunkmod.song;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class NBSConverter {
  public static Instrument[] instrumentIndex = new Instrument[] {
    Instrument.HARP,
    Instrument.BASS,
    Instrument.BASEDRUM,
    Instrument.SNARE,
    Instrument.HAT,
    Instrument.GUITAR,
    Instrument.FLUTE,
    Instrument.BELL,
    Instrument.CHIME,
    Instrument.XYLOPHONE,
    Instrument.IRON_XYLOPHONE,
    Instrument.COW_BELL,
    Instrument.DIDGERIDOO,
    Instrument.BIT,
    Instrument.BANJO,
    Instrument.PLING,
  };

  private static class NBSNote {
    public int tick;
    public short layer;
    public byte instrument;
    public byte key;
    public byte velocity = 100;
    public byte panning = 100;
    public short pitch = 0;
  }

  private static class NBSLayer {
    public String name;
    public byte lock = 0;
    public byte volume;
    public byte stereo = 100;
  }

  private static class NBSCustomInstrument {
    public String name;
    public String file;
    public byte pitch = 0;
    public boolean key = false;
  }

  public static Song getSongFromBytes(byte[] bytes, String fileName) throws IOException {
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    short songLength = 0;
    byte format = 0;
    byte vanillaInstrumentCount = 0;
    songLength = buffer.getShort(); // If it's not 0, then it uses the old format
    if (songLength == 0) {
      format = buffer.get();
    }

    if (format >= 1) {
      vanillaInstrumentCount = buffer.get();
    }
    if (format >= 3) {
      songLength = buffer.getShort();
    }

    short layerCount = buffer.getShort();
    String songName = getString(buffer, bytes.length);
    String songAuthor = getString(buffer, bytes.length);
    String songOriginalAuthor = getString(buffer, bytes.length);
    String songDescription = getString(buffer, bytes.length);
    short tempo = buffer.getShort();
    byte autoSaving = buffer.get();
    byte autoSavingDuration = buffer.get();
    byte timeSignature = buffer.get();
    int minutesSpent = buffer.getInt();
    int leftClicks = buffer.getInt();
    int rightClicks = buffer.getInt();
    int blocksAdded = buffer.getInt();
    int blocksRemoved = buffer.getInt();
    String origFileName = getString(buffer, bytes.length);

    byte loop = 0;
    byte maxLoopCount = 0;
    short loopStartTick = 0;
    if (format >= 4) {
      loop = buffer.get();
      maxLoopCount = buffer.get();
      loopStartTick = buffer.getShort();
    }

    ArrayList<NBSNote> nbsNotes = new ArrayList<>();
    short tick = -1;
    while (true) {
      int tickJumps = buffer.getShort();
      if (tickJumps == 0) break;
      tick += (short) tickJumps;

      short layer = -1;
      while (true) {
        int layerJumps = buffer.getShort();
        if (layerJumps == 0) break;
        layer += (short) layerJumps;
        NBSNote note = new NBSNote();
        note.tick = tick;
        note.layer = layer;
        note.instrument = buffer.get();
        note.key = buffer.get();
        if (format >= 4) {
          note.velocity = buffer.get();
          note.panning = buffer.get();
          note.pitch = buffer.getShort();
        }
        nbsNotes.add(note);
      }
    }

    ArrayList<NBSLayer> nbsLayers = new ArrayList<>();
    if (buffer.hasRemaining()) {
      for (int i=0; i<layerCount; i++) {
        NBSLayer layer = new NBSLayer();
        layer.name = getString(buffer, bytes.length);
        if (format >= 4) {
          layer.lock = buffer.get();
        }
        layer.volume = buffer.get();
        if (format >= 2) {
          layer.stereo = buffer.get();
        }
        nbsLayers.add(layer);
      }
    }

    ArrayList<NBSCustomInstrument> customInstruments = new ArrayList<>();
    if (buffer.hasRemaining()) {
      byte customInstrumentCount = buffer.get();
      for (int i = 0; i < customInstrumentCount; i++) {
        NBSCustomInstrument customInstrument = new NBSCustomInstrument();
        customInstrument.name = getString(buffer, bytes.length);
        customInstrument.file = getString(buffer, bytes.length);
        customInstrument.pitch = buffer.get();
        customInstrument.key = buffer.get() != 0;
        customInstruments.add(customInstrument);
      }
    }

    Song song = new Song(!songName.trim().isEmpty() ? songName : fileName);
    if (loop > 0) {
      song.looping = true;
      song.loopPosition = getMilliTime(loopStartTick, tempo);
      song.loopCount = maxLoopCount;
    }
    for (NBSNote note : nbsNotes) {
      Instrument instrument;
      int key = note.key;
      if (note.instrument < instrumentIndex.length) {
        instrument = instrumentIndex[note.instrument];
      } else {
        int index = note.instrument - instrumentIndex.length;
        if (index >= customInstruments.size()) continue;
        NBSCustomInstrument customInstrument = customInstruments.get(index);
        instrument = Instrument.of(customInstrument.name);
        key += customInstrument.pitch;
      }

      byte layerVolume = 100;
      if (nbsLayers.size() > note.layer) {
        layerVolume = nbsLayers.get(note.layer).volume;
      }

      int pitch = key-33;
      song.add(new Note(instrument, pitch, (float) note.velocity * (float) layerVolume / 10000f, getMilliTime(note.tick, tempo)));
    }

    song.length = song.get(song.size()-1).time + 50;

    return song;
  }

  private static String getString (ByteBuffer buffer, int maxSize) throws IOException {
    int length = buffer.getInt();
    if (length > maxSize) {
      throw new IOException("String is too large");
    }
    byte[] arr = new byte[length];
    buffer.get(arr, 0, length);
    return new String(arr);
  }

  private static int getMilliTime(int tick, int tempo) {
    return 1000 * tick * 100 / tempo;
  }
}
