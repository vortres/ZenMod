package land.chipmunk.chipmunkmod.util.misc;

public class MathUtils {
    public static double clamp (double value, double min, double max) {
        return Math.max(Math.min(value, max), min);
    }

    public static float clamp (float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }
}
