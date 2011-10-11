package aethers.notebook.core;

import aethers.notebook.core.AppenderServiceIdentifier;
import aethers.notebook.core.LoggerServiceIdentifier;
import aethers.notebook.core.UnmanagedAppenderService;

interface AethersNotebook
{
    void log(in LoggerServiceIdentifier identifier, in byte[] data);
    
    void registerUnmanagedAppender(
            in AppenderServiceIdentifier identifier,
            in UnmanagedAppenderService service);
    
    void deregisterUnmanagedAppender(in AppenderServiceIdentifier identifier);
}