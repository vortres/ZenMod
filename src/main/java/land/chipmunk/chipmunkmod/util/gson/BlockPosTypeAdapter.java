package land.chipmunk.chipmunkmod.util.gson;

import net.minecraft.util.math.BlockPos;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public class BlockPosTypeAdapter extends TypeAdapter<BlockPos> {
    @Override
    public BlockPos read (JsonReader reader) throws IOException {
        int x = 0;
        int y = 0;
        int z = 0;

        reader.beginObject();

        while (!reader.peek().equals(JsonToken.END_OBJECT)) {
            if (reader.peek().equals(JsonToken.NAME)) {
                String name = reader.nextName();

                // ? Is there a better way to do this?
                if (name.equals("x")) x = reader.nextInt();
                else if (name.equals("y")) y = reader.nextInt();
                else if (name.equals("z")) z = reader.nextInt();
                else reader.skipValue();
            }
        }

        reader.endObject();

        return new BlockPos(x, y, z);
    }

    @Override
    public void write (JsonWriter out, BlockPos vec) throws IOException {
        // TODO: make this do something lmfaooo
    }
}
