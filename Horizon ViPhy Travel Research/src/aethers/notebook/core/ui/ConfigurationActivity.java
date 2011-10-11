package aethers.notebook.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import aethers.notebook.R;
import aethers.notebook.core.Action;
import aethers.notebook.core.ManagedAppenderService;
import aethers.notebook.core.Configuration;
import aethers.notebook.core.CoreService;
import aethers.notebook.core.LoggerService;
import aethers.notebook.core.Configuration.AppenderConfigurationHolder;
import aethers.notebook.core.Configuration.LoggerConfigurationHolder;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

public class ConfigurationActivity
extends PreferenceActivity
{    
    private class LoggerConfigureOnClickListener
    implements Preference.OnPreferenceClickListener
    {
        private final Class<?> serviceClass;
        
        public LoggerConfigureOnClickListener(Class<?> serviceClass)
        {
            this.serviceClass = serviceClass;
        }
        
        @Override
        public boolean onPreferenceClick(Preference p) 
        {
            if(loggerConnectionPool.containsKey(serviceClass))
            {
                try
                {
                    loggerConnectionPool.get(serviceClass).configure();
                }
                catch(RemoteException e)
                {
                    throw new RuntimeException(e);
                }
                return false;
            }
            
            ServiceConnection conn;
            ConfigurationActivity.this.bindService(
                    new Intent(ConfigurationActivity.this, serviceClass),
                    conn = new ServiceConnection()
                    {
                        @Override
                        public void onServiceDisconnected(ComponentName name)
                        { 
                            loggerConnectionPool.remove(serviceClass);
                        }
                        
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) 
                        { 
                            try
                            {
                                LoggerService s = (LoggerService)service;
                                loggerConnectionPool.put(serviceClass, s);
                                s.configure();
                            }
                            catch(RemoteException e)
                            {
                                throw new RuntimeException(e);
                            }                            
                        }
                    }, 
                    BIND_AUTO_CREATE);
            serviceConnections.add(conn);
            return true;
        }   
    }
    
    private class AppenderConfigureOnClickListener
    implements Preference.OnPreferenceClickListener
    {
        private final Class<?> serviceClass;
        
        public AppenderConfigureOnClickListener(Class<?> serviceClass)
        {
            this.serviceClass = serviceClass;
        }
        
        @Override
        public boolean onPreferenceClick(Preference p) 
        {
            if(loggerConnectionPool.containsKey(serviceClass))
            {
                try
                {
                    loggerConnectionPool.get(serviceClass).configure();
                }
                catch(RemoteException e)
                {
                    throw new RuntimeException(e);
                }
                return false;
            }
            
            ServiceConnection conn;
            ConfigurationActivity.this.bindService(
                    new Intent(ConfigurationActivity.this, serviceClass),
                    conn = new ServiceConnection()
                    {
                        @Override
                        public void onServiceDisconnected(ComponentName name)
                        { 
                            appenderConnectionPool.remove(serviceClass);
                        }
                        
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) 
                        { 
                            try
                            {
                                ManagedAppenderService s = ManagedAppenderService.Stub.asInterface(service);
                                appenderConnectionPool.put(serviceClass, s);
                                s.configure();
                            }
                            catch(RemoteException e)
                            {
                                throw new RuntimeException(e);
                            }                            
                        }
                    }, 
                    BIND_AUTO_CREATE);
            serviceConnections.add(conn);
            return true;
        }   
    }
    
    private Map<Class<?>, LoggerService> loggerConnectionPool = 
            Collections.synchronizedMap(new HashMap<Class<?>, LoggerService>());
    
    private Map<Class<?>, ManagedAppenderService> appenderConnectionPool = 
            Collections.synchronizedMap(new HashMap<Class<?>, ManagedAppenderService>());
    
    private List<ServiceConnection> serviceConnections = 
            Collections.synchronizedList(new ArrayList<ServiceConnection>());
    
    private Configuration configuration;
    
    private OnSharedPreferenceChangeListener enabledListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(
                Configuration.SHARED_PREFERENCES_NAME);
        configuration = new Configuration(this);
        addPreferencesFromResource(R.xml.coreservice);
        Preference minDistance = findPreference(getString(R.string.Preferences_locationMinDistance));
        Preference minTime = findPreference(getString(R.string.Preferences_locationMinTime));
        IntegerPreferenceChangeListener listener = new IntegerPreferenceChangeListener(
                0, Integer.MAX_VALUE, 
                "You must input a whole number greater than or equal to 0", this);
        minDistance.setOnPreferenceChangeListener(listener);
        minTime.setOnPreferenceChangeListener(listener);
        setupCorePreferences();
        setupLoggerPreferences();
        setupAppenderPreferences();
    }
    
    @Override
    protected void onDestroy() 
    {
        super.onDestroy();
        if(enabledListener != null)
            configuration.unregisterChangeListener(enabledListener);
        for(ServiceConnection sc : serviceConnections)
            unbindService(sc);
    }
    
    @Override
    protected void onStart() 
    {
        super.onStart();
        if(configuration.isEnabled())
            startService(new Intent(this, CoreService.class));
    }
    
    private void setupCorePreferences()
    {
        configuration.registerChangeListener(
                enabledListener = new SharedPreferences.OnSharedPreferenceChangeListener()
                {
                    @Override
                    public void onSharedPreferenceChanged(
                            SharedPreferences sharedPreferences,
                            String key) 
                    {
                        if(!key.equals(getString(R.string.Preferences_enabled)))
                            return;
                        Intent intent = new Intent(ConfigurationActivity.this, CoreService.class);
                        if(configuration.isEnabled())
                            startService(intent);
                    }
                });
    }
    
    private void setupLoggerPreferences()
    {
        final Configuration conf = new Configuration(this);
        final List<LoggerConfigurationHolder> loggers = conf.getLoggerConfigurationHolders();
        PreferenceCategory cat = (PreferenceCategory)findPreference(
                getString(R.string.Preferences_category_loggers));
        
        Collections.sort(loggers, new Comparator<LoggerConfigurationHolder>()
        {
            @Override
            public int compare(
                    LoggerConfigurationHolder object1,
                    LoggerConfigurationHolder object2) 
            {
                return object1.getName().compareTo(object2.getName());
            }
        });
        
        for(final LoggerConfigurationHolder holder : loggers)
        {
            if(holder.isDeleted())
                continue;
            
            PreferenceScreen ps = getPreferenceManager().createPreferenceScreen(this);
            ps.setTitle(holder.getName());
            ps.setSummary(holder.getDescription());
                        
            final CheckBoxPreference activate = new CheckBoxPreference(this)
            {
                @Override
                protected boolean shouldPersist() { return false; }
            };
            activate.setKey(holder.getUniqueID());
            activate.setTitle("Enabled");
            activate.setSummary("Enable/Disable this logger");
            activate.setChecked(holder.isEnabled());
            activate.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener()
                    { 
                        @Override
                        public boolean onPreferenceClick(Preference preference) 
                        {
                            SharedPreferences.Editor editor = 
                                getPreferenceManager().getSharedPreferences().edit();
                            
                            holder.setEnabled(activate.isChecked());                   
                            ObjectMapper mapper = new ObjectMapper();
                            try
                            {
                                editor.putString(getString(R.string.Preferences_loggers),
                                        mapper.writeValueAsString(loggers));
                            }
                            catch(Exception e)
                            {
                                return false;
                            }
                            
                            return editor.commit();
                        }
                    });
            ps.addItemFromInflater(activate);
            
            if(holder.isConfigurable())
            {
                NonPersistingButtonPreference configure = new NonPersistingButtonPreference(this);
                configure.setTitle("Configure");
                try
                {
                    configure.setOnPreferenceClickListener(
                            new LoggerConfigureOnClickListener(
                                    Class.forName(holder.getServiceClass())));
                }
                catch(ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                }         
                ps.addItemFromInflater(configure);
            }
            
            if(!holder.isBuiltin())
            {
                NonPersistingButtonPreference remove = new NonPersistingButtonPreference(this);
                remove.setTitle("Remove");
                ps.addItemFromInflater(remove);
            }
            
            cat.addItemFromInflater(ps); 
        }
    }
    
    private void setupAppenderPreferences()
    {
        final Configuration conf = new Configuration(this);
        final List<AppenderConfigurationHolder> appenders = conf.getAppenderConfigurationHolders();
        PreferenceCategory cat = (PreferenceCategory)findPreference(
                getString(R.string.Preferences_category_appenders));
        
        Collections.sort(appenders, new Comparator<AppenderConfigurationHolder>()
        {
            @Override
            public int compare(
                    AppenderConfigurationHolder object1,
                    AppenderConfigurationHolder object2) 
            {
                return object1.getName().compareTo(object2.getName());
            }
        });
        
        for(final AppenderConfigurationHolder holder : appenders)
        {
            if(holder.isDeleted())
                continue;
            
            final PreferenceScreen ps = getPreferenceManager().createPreferenceScreen(this);
            ps.setTitle(holder.getName());
            ps.setSummary(holder.getDescription());
                        
            final CheckBoxPreference activate = new CheckBoxPreference(this)
            {
                @Override
                protected boolean shouldPersist() { return false; }
            };
            activate.setKey(holder.getUniqueID());
            activate.setTitle("Enabled");
            activate.setSummary("Enable/Disable this appender");
            activate.setChecked(holder.isEnabled());
            activate.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener()
                    { 
                        @Override
                        public boolean onPreferenceClick(Preference preference) 
                        {
                            SharedPreferences.Editor editor = 
                                getPreferenceManager().getSharedPreferences().edit();
                            
                            holder.setEnabled(activate.isChecked());                   
                            ObjectMapper mapper = new ObjectMapper();
                            try
                            {
                                editor.putString(getString(R.string.Preferences_appenders),
                                        mapper.writeValueAsString(appenders));
                            }
                            catch(Exception e)
                            {
                                return false;
                            }
                            
                            return editor.commit();
                        }
                    });
            ps.addItemFromInflater(activate);
            
            if(holder.isConfigurable())
            {
                NonPersistingButtonPreference configure = new NonPersistingButtonPreference(this);
                configure.setTitle("Configure");
                try
                {
                    configure.setOnPreferenceClickListener(
                            new AppenderConfigureOnClickListener(
                                    Class.forName(holder.getServiceClass())));
                }
                catch(ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                }         
                ps.addItemFromInflater(configure);
            }
            
            if(!holder.isBuiltin())
            {
                NonPersistingButtonPreference remove = new NonPersistingButtonPreference(this);
                remove.setTitle("Remove");
                ps.addItemFromInflater(remove);
            }
            
            try
            {
                bindService(
                        new Intent(ConfigurationActivity.this, Class.forName(holder.getServiceClass())),
                        new ServiceConnection()
                        {
                            @Override
                            public void onServiceDisconnected(ComponentName name) { }
                            
                            @Override
                            public void onServiceConnected(ComponentName name, IBinder service)
                            {
                                ManagedAppenderService as = ManagedAppenderService.Stub.asInterface(service);
                                try
                                {
                                    List<Action> actions = as.listActions();
                                    if(actions == null || actions.size() == 0)
                                        return;
                                    for(final Action action : actions)
                                    {
                                        NonPersistingButtonPreference p = new NonPersistingButtonPreference(ConfigurationActivity.this);
                                        p.setTitle(action.getName());
                                        p.setSummary(action.getDescription());
                                        p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                                        {
                                            @Override
                                            public boolean onPreferenceClick(Preference preference) 
                                            {
                                                try
                                                {
                                                    bindService(new Intent(ConfigurationActivity.this, Class.forName(holder.getServiceClass())),
                                                            new ServiceConnection()
                                                            {
                                                                @Override
                                                                public void onServiceDisconnected(ComponentName name) { }
                                                                
                                                                @Override
                                                                public void onServiceConnected(ComponentName name, IBinder service) 
                                                                {
                                                                    ManagedAppenderService as = ManagedAppenderService.Stub.asInterface(service);
                                                                    try
                                                                    {
                                                                        as.doAction(action);
                                                                    }
                                                                    catch(RemoteException e)
                                                                    {
                                                                        throw new RuntimeException(e);
                                                                    }
                                                                    finally
                                                                    {
                                                                        unbindService(this);
                                                                    }
                                                                }
                                                            }, BIND_AUTO_CREATE);
                                                }
                                                catch(ClassNotFoundException e)
                                                {
                                                    throw new RuntimeException(e);
                                                  
                                                }
                                                return true;
                                            }
                                        });
                                        ps.addItemFromInflater(p);
                                    }
                                }
                                catch(RemoteException e)
                                {
                                    throw new RuntimeException(e);
                                }
                                finally
                                {
                                    unbindService(this);
                                }
                            }
                        }, BIND_AUTO_CREATE);
            }
            catch(ClassNotFoundException e)
            {
               throw new RuntimeException(e);
            }
            
            cat.addItemFromInflater(ps); 
        }
    }
}
