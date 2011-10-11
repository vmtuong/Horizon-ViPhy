package aethers.notebook.util;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class Logger
{
    private static final Map<Package, Logger> loggers = new HashMap<Package, Logger>();
    
    public synchronized static Logger getLogger(Class<?> clazz)
    {
        if(loggers.containsKey(clazz.getPackage()))
            return loggers.get(clazz.getPackage());
        
        Logger logger = new Logger(clazz.getPackage().getName());
        loggers.put(clazz.getPackage(), logger);
        return logger;
    }
    
    private final String name;
    
    public Logger(String name)
    {
        this.name = name;
    }
    
    public void debug(String message)
    {
        Log.d(name, message);
    }
    
    public void debug(String message, Throwable cause)
    {
        Log.d(name, message, cause);
    }
    
    public void error(String message)
    {
        Log.e(name, message);
    }
    
    public void error(String message, Throwable cause)
    {
        Log.e(name, message, cause);
    }
    
    public void info(String message)
    {
        Log.i(name, message);
    }
    
    public void info(String message, Throwable cause)
    {
        Log.i(name, message, cause);
    }
    
    public void verbose(String message)
    {
        Log.v(name, message);
    }
    
    public void verbose(String message, Throwable cause)
    {
        Log.v(name, message, cause);
    }
    
    public void warn(String message)
    {
        Log.w(name, message);
    }
    
    public void warn(String message, Throwable cause)
    {
        Log.w(name, message, cause);
    }
}
