package aethers.notebook.core;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import aethers.notebook.R;
import android.content.Context;
import android.content.SharedPreferences;

public class Configuration
extends ConfigurationTemplate
{  
    public static class LoggerConfigurationHolder
    extends LoggerServiceIdentifier
    {
        private boolean enabled;
        
        private boolean builtin;
        
        private boolean deleted;
        
        @JsonCreator
        public LoggerConfigurationHolder(@JsonProperty("uniqueID") String uniqueID)
        {
            super(uniqueID);
        }

        public boolean isEnabled() { return enabled; }

        public void setEnabled(boolean enabled) { this.enabled = enabled; }     
        
        public boolean isBuiltin() { return builtin; }
        
        public void setBuiltin(boolean builtin) { this.builtin = builtin; }
        
        public boolean isDeleted() { return deleted; }
        
        public void setDeleted(boolean deleted) { this.deleted = deleted; }
    }
    
    public static class AppenderConfigurationHolder
    extends AppenderServiceIdentifier
    {
        private boolean enabled;
        
        private boolean builtin;
        
        private boolean deleted;
        
        @JsonCreator
        public AppenderConfigurationHolder(@JsonProperty("uniqueID") String uniqueID)
        {
            super(uniqueID);
        }

        public boolean isEnabled() { return enabled; }

        public void setEnabled(boolean enabled) { this.enabled = enabled; }     
        
        public boolean isBuiltin() { return builtin; }
        
        public void setBuiltin(boolean builtin) { this.builtin = builtin; }
        
        public boolean isDeleted() { return deleted; }
        
        public void setDeleted(boolean deleted) { this.deleted = deleted; }
    }
    
    public static final String SHARED_PREFERENCES_NAME
            = "aethers.notebook.core.Configuration";
    
    public Configuration(Context context) 
    {
        super(context, SHARED_PREFERENCES_NAME);
    }    
    
    public boolean isEnabled()
    {
        return getBoolean(R.string.Preferences_enabled,
                R.string.Preferences_enabled_default);
    }
    
    public boolean isStartOnBoot()
    {
        return getBoolean(R.string.Preferences_startOnBoot,
                R.string.Preferences_startOnBoot_default);
    }
    
    public boolean isLocationLoggingEnabled()
    {
        return getBoolean(R.string.Preferences_logLocation,
                R.string.Preferences_logLocation_default);
    }
    
    public int getLocationMinimumDistance()
    {
        return Integer.parseInt(getString(
                R.string.Preferences_locationMinDistance,
                R.string.Preferences_locationMinDistance_default));
    }
    
    public int getLocationMinimumTime()
    {
        return Integer.parseInt(getString(
                R.string.Preferences_locationMinTime,
                R.string.Preferences_locationMinTime_default));
    }
    
    public List<LoggerConfigurationHolder> getLoggerConfigurationHolders()
    {
        if(shouldUpdateLoggers())
            updateLoggers();
        ObjectMapper mapper = new ObjectMapper();
        try
        {            
            return mapper.readValue(
                    getString(
                            R.string.Preferences_loggers,
                            R.string.Preferences_loggers_default), 
                    new TypeReference<List<LoggerConfigurationHolder>>() { });
        }
        catch(Exception e)
        {
        	System.err.println(e);
            throw new RuntimeException(e);
        }
    }
    
    public List<AppenderConfigurationHolder> getAppenderConfigurationHolders()
    {
        if(shouldUpdateAppenders())
            updateAppenders();
        ObjectMapper mapper = new ObjectMapper();
        try
        {            
            return mapper.readValue(
                    getString(
                            R.string.Preferences_appenders,
                            R.string.Preferences_appenders_default), 
                    new TypeReference<List<AppenderConfigurationHolder>>() { });
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private boolean shouldUpdateLoggers()
    {
        int installedVersion = getInt(
                R.string.Preferences_loggers_version,
                R.string.Preferences_loggers_version_default);
        int currentVersion = Integer.parseInt(
                getContext().getString(R.string.Preferences_loggers_version_default));
        return installedVersion < currentVersion;
    }
    
    private synchronized void updateLoggers()
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            List<LoggerConfigurationHolder> old = mapper.readValue(
                    getString(
                            R.string.Preferences_loggers,
                            R.string.Preferences_loggers_default), 
                    new TypeReference<List<LoggerConfigurationHolder>>() { });
            List<LoggerConfigurationHolder> current = mapper.readValue(
                    getContext().getString(R.string.Preferences_loggers_default), 
                    new TypeReference<List<LoggerConfigurationHolder>>() { });
            
            ArrayList<LoggerConfigurationHolder> future = 
                    new ArrayList<LoggerConfigurationHolder>();
            
            for(LoggerConfigurationHolder c : current)
            {
                if(old.contains(c))
                {
                    LoggerConfigurationHolder o = old.get(old.indexOf(c));
                    o.setBuiltin(c.isBuiltin());
                    o.setConfigurable(c.isConfigurable());
                    o.setDeleted(c.isDeleted());
                    o.setDescription(c.getDescription());
                    o.setName(c.getName());
                    o.setServiceClass(c.getServiceClass());
                }
                else
                    future.add(c);               
            }
            for(LoggerConfigurationHolder o : old)
                if(!future.contains(o))
                    future.add(o);
            
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putString(
                    getContext().getString(R.string.Preferences_loggers),
                    mapper.writeValueAsString(future));
            editor.putInt(
                    getContext().getString(R.string.Preferences_loggers_version),
                    Integer.parseInt(
                            getContext().getString(R.string.Preferences_loggers_version_default)));
            editor.commit();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private boolean shouldUpdateAppenders()
    {
        int installedVersion = getInt(
                R.string.Preferences_appenders_version,
                R.string.Preferences_appenders_version_default);
        int currentVersion = Integer.parseInt(
                getContext().getString(R.string.Preferences_appenders_version_default));
        return installedVersion < currentVersion;
    }
    
    private synchronized void updateAppenders()
    {
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            List<AppenderConfigurationHolder> old = mapper.readValue(
                    getString(
                            R.string.Preferences_appenders,
                            R.string.Preferences_appenders_default), 
                    new TypeReference<List<AppenderConfigurationHolder>>() { });
            List<AppenderConfigurationHolder> current = mapper.readValue(
                    getContext().getString(R.string.Preferences_appenders_default), 
                    new TypeReference<List<AppenderConfigurationHolder>>() { });
            
            ArrayList<AppenderConfigurationHolder> future = 
                    new ArrayList<AppenderConfigurationHolder>();
            
            for(AppenderConfigurationHolder c : current)
            {
                if(old.contains(c))
                {
                    AppenderConfigurationHolder o = old.get(old.indexOf(c));
                    o.setBuiltin(c.isBuiltin());
                    o.setConfigurable(c.isConfigurable());
                    o.setDeleted(c.isDeleted());
                    o.setDescription(c.getDescription());
                    o.setName(c.getName());
                    o.setServiceClass(c.getServiceClass());
                }
                else
                    future.add(c);               
            }
            for(AppenderConfigurationHolder o : old)
                if(!future.contains(o))
                    future.add(o);
            
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putString(
                    getContext().getString(R.string.Preferences_appenders),
                    mapper.writeValueAsString(future));
            editor.putInt(
                    getContext().getString(R.string.Preferences_appenders_version),
                    Integer.parseInt(
                            getContext().getString(R.string.Preferences_appenders_version_default)));
            editor.commit();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
