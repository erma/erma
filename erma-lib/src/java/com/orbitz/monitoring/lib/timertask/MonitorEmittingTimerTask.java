package com.orbitz.monitoring.lib.timertask;

import java.util.Collection;
import java.util.TimerTask;

import com.orbitz.monitoring.api.Monitor;

public abstract class MonitorEmittingTimerTask extends TimerTask {

    public abstract Collection<? extends Monitor> emitMonitors();

    @Override
    public void run() {
        emitMonitors();
    }

}
