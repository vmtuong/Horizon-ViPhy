package aethers.notebook.core;

import aethers.notebook.core.Action;
import aethers.notebook.core.LoggerServiceIdentifier;
import aethers.notebook.core.TimeStamp;

import android.location.Location;

interface ManagedAppenderService
{
    void configure();
    
    boolean isRunning();
    
    void start();
    
    void stop();
    
    void log(in LoggerServiceIdentifier identifier, in TimeStamp timestamp, in Location location, in byte[] data);
    
    List<Action> listActions();
    
    void doAction(in Action action);
}