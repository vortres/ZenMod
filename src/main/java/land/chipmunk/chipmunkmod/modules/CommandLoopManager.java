package land.chipmunk.chipmunkmod.modules;

import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class CommandLoopManager {
    private final CommandCore core;
    public List<CommandLoop> commandLoops = new ArrayList<>();

    public CommandLoopManager (CommandCore core) {
        this.core = core;
    }

    public static final CommandLoopManager INSTANCE = new CommandLoopManager(CommandCore.INSTANCE);

    public int loopCommand (String command, long interval) {
        final CommandLoop loop = new CommandLoop(this.core, command, interval);
        if (!commandLoops.add(loop)) return -1;
        return commandLoops.size() - 1;
    }

    public boolean removeAndStop (CommandLoop loop) {
        loop.stop();
        return commandLoops.remove(loop);
    }

    public boolean removeAndStop (int id) {
        return removeAndStop(commandLoops.get(id));
    }

    public void clearLoops () {
        for (CommandLoop loop : this.commandLoops) loop.stop();
        commandLoops.clear();
    }

    public void cleanup () { this.clearLoops(); }

    public static class CommandLoop {
        public CommandCore core;
        public String command;
        public long interval;
        private Timer timer;

        public CommandLoop (CommandCore core, String command, long interval) {
            this.core = core;
            this.command = command;
            this.interval = interval;
            this.timer = new Timer();
            timer.schedule(this.createTimerTask(), interval, interval);
        }

        private long interval (long interval) {
            if (timer == null) throw new IllegalStateException("Attempted to set the interval of a stopped command loop");

            timer.cancel();
            timer.purge();

            this.interval = interval;
            this.timer = new Timer();
            timer.schedule(this.createTimerTask(), interval, interval);

            return interval;
        }

        private void stop () {
            timer.cancel();
            timer.purge();
        }

        public TimerTask createTimerTask () {
            return new TimerTask() {
                @Override
                public void run () {
                    core.run(command);
                }
            };
        }
    }
}
