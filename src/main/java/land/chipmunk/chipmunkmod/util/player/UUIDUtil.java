package land.chipmunk.chipmunkmod.util.player;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDUtil {
    public static int[] intArray (UUID uuid) {
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(0, uuid.getMostSignificantBits());
        buffer.putLong(8, uuid.getLeastSignificantBits());

        final int[] intArray = new int[4];
        for (int i = 0; i < intArray.length; i++) intArray[i] = buffer.getInt();

        return intArray;
    }

    public static String snbt (UUID uuid) {
        int[] array = intArray(uuid);
        return "[I;" + array[0] + "," + array[1] + "," + array[2] + "," + array[3] + "]"; // TODO: improve lol
    }

    public static String selector (UUID uuid) { return "@a[limit=1,nbt={UUID:" + snbt(uuid) + "}]"; }
}
