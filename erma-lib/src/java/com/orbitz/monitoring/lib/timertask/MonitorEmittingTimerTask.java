package com.orbitz.monitoring.lib.timertask;

import com.orbitz.monitoring.api.Monitor;

import java.util.Collection;
import java.util.TimerTask;

public abstract class MonitorEmittingTimerTask extends TimerTask {

    public abstract Collection<? extends Monitor> emitMonitors();

    @Override
    public void run() {
        emitMonitors();
    }

}
