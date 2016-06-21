package com.habitrpg.android.habitica.ui.helpers;

import java.util.Timer;
import java.util.TimerTask;

// from https://code.google.com/p/tarsius/source/browse/branches/tarsius_0.00_dev2/src/org/tarsius/util/Debounce.java

public abstract class Debounce {
    private Timer timer = null;
    private long lastHit = 0;
    private long debounceDelay = 0;
    private long checkDelay = 0;

    public Debounce(long debounceDelay, long checkDelay) {
        this.debounceDelay = debounceDelay;
        this.checkDelay = checkDelay;
    }

    public abstract void execute();

    public void hit() {
        lastHit = System.currentTimeMillis();
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        this.timer = new Timer("Debounce", true);
        this.timer.schedule(new DebounceTask(this), 0, checkDelay);
    }

    private void checkExecute() {
        if ((System.currentTimeMillis() - lastHit) > debounceDelay) {
            this.timer.cancel();
            this.timer = null;
            execute();
        }
    }

    private class DebounceTask extends TimerTask {

        private Debounce debounceInstance = null;

        public DebounceTask(Debounce debounceInstance) {
            this.debounceInstance = debounceInstance;
        }

        @Override
        public void run() {
            debounceInstance.checkExecute();
        }
    }

}
