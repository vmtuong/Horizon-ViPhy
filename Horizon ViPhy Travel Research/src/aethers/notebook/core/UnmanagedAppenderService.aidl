package aethers.notebook.core;

import aethers.notebook.core.LoggerServiceIdentifier;
import aethers.notebook.core.TimeStamp;

import android.location.Location;

interface UnmanagedAppenderService
{    
    void log(in LoggerServiceIdentifier identifier, in TimeStamp timestamp, in Location location, in byte[] data);
}