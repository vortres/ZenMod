package land.chipmunk.chipmunkmod.listeners;

import java.util.ArrayList;
import java.util.List;

public class ListenerManager {
    public static List<Listener> listeners = new ArrayList<>();

    public static void addListener (Listener listener) {
        listeners.add(listener);
    }
}
