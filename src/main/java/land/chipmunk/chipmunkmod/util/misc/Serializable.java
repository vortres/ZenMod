package land.chipmunk.chipmunkmod.util.misc;

import net.minecraft.nbt.NbtCompound;

public interface Serializable<T> {
    NbtCompound toTag();

    T fromTag(NbtCompound tag);
}
