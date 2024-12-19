package land.chipmunk.chipmunkmod.util.misc;

public interface Copyable<T extends Copyable<T>> {
    T set(T value);

    T copy();
}
