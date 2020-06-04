/*
 * Copyright (c) 2020. Nils Witt
 */

package de.nils_witt.splan;

import java.util.TimerTask;

public class TimerProccess extends TimerTask {
    private final String changed;
    private final CustomWatcher customWatcher;

    public TimerProccess(String changed, CustomWatcher customWatcher) {
        this.changed = changed;
        this.customWatcher = customWatcher;

    }

    @Override
    public void run() {
        customWatcher.fileProccessor(changed);
    }

}
