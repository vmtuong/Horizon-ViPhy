package aethers.notebook.logger.managed.wifi;

import java.util.List;

import org.json.JSONStringer;

import aethers.notebook.R;
import aethers.notebook.core.AethersNotebook;
import aethers.notebook.core.CoreService;
import aethers.notebook.core.LoggerServiceIdentifier;
import aethers.notebook.core.LoggerService;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteException;

public class WifiLogger
extends Service
implements Runnable
{
    private static final LoggerServiceIdentifier IDENTIFIER = new LoggerServiceIdentifier(
            "aethers.notebook.logger.managed.wifi.WifiLogger");
    {
        IDENTIFIER.setConfigurable(false);
        IDENTIFIER.setDescription("Logs the results of scanning Wifi access points");
        IDENTIFIER.setName("Wifi Logger");
        IDENTIFIER.setServiceClass(WifiLogger.class.getName());
        IDENTIFIER.setVersion(1);
    }

    private static final String ENCODING = "UTF-8";
    
    private final LoggerService.Stub loggerServiceStub = 
            new LoggerService.Stub()
            {
                @Override
                public void configure() 
                throws RemoteException 
                {
                    Intent i = new Intent(WifiLogger.this, ConfigurationActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
                
                @Override
                public boolean isRunning() 
                throws RemoteException 
                {
                    synchronized(sync)
                    {
                        return running;
                    }
                }
        
                @Override
                public void start() 
                throws RemoteException 
                {
                    startService(new Intent(
                            WifiLogger.this, 
                            WifiLogger.this.getClass()));
                }
                
                @Override
                public void stop() 
                throws RemoteException 
                {
                    WifiLogger.this.stopSelf();
                }
            };
    
    private final ServiceConnection loggerConnection = 
            new ServiceConnection() 
            {   
                @Override
                public void onServiceDisconnected(ComponentName name)
                {
                    WifiLogger.this.stopSelf();
                }
                
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) 
                {
                    aethersNotebook = AethersNotebook.Stub.asInterface(service);
                    new Thread(WifiLogger.this).start();
                }
            };
            
    private final OnSharedPreferenceChangeListener listener = 
            new OnSharedPreferenceChangeListener()
            {
                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences,
                        String key) 
                {
                    if(key.equals(WifiLogger.this.getString(
                            R.string.WifiLogger_Preferences_scanInterval)))
                        interval = Long.parseLong(sharedPreferences.getString(key, "0")) * 1000;
                }
            };
    
    private final Object sync = new Object();
    
    protected AethersNotebook aethersNotebook;
    
    private WifiManager wifiManager;
    
    private volatile boolean running = false;
    
    private volatile long interval;
    
    private Thread thread;
    
    @Override
    public IBinder onBind(Intent intent) 
    {
        return loggerServiceStub;
    }

    @Override
    public void onCreate() 
    {
        super.onCreate();
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void onDestroy() 
    {
        super.onDestroy();
        synchronized(sync)
        {
            if(running)
            {
                running = false;
                unbindService(loggerConnection);
                if(thread != null)
                {
                    thread.interrupt();
                    thread = null;
                }
            }
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) 
    {
        synchronized(sync)
        {
            if(running)
                return START_STICKY;
            running = bindService(
                    new Intent(this, CoreService.class),
                    loggerConnection, 
                    BIND_AUTO_CREATE);
        }
        
        return START_STICKY;
    }

    @Override
    public void run() 
    {
        synchronized(sync)
        {
            thread = Thread.currentThread();

            Configuration config = new Configuration(WifiLogger.this);
            config.registerChangeListener(listener);
            interval = config.getScanInterval() * 1000;            
            
            while(running)
            {
                try
                {
                    doScan();
                    sync.wait(interval);
                }
                catch(InterruptedException e) { }
            }   
            config.unregisterChangeListener(listener);
        }        
    }
    
    private void doScan()
    {
        if(!wifiManager.isWifiEnabled())
            return;
        wifiManager.startScan();
        JSONStringer data = new JSONStringer();
        try
        {
            data.array();
            List<ScanResult> scanResults = wifiManager.getScanResults();
            if(scanResults != null)
                for(ScanResult result : scanResults)
                {
                    data.object()
                            .key("bssid").value(result.BSSID)
                            .key("ssid").value(result.SSID)
                            .key("capabilities").value(result.capabilities)
                            .key("frequency").value(result.frequency)
                            .key("level").value(result.level)
                            .endObject();
                }
            data.endArray();
            aethersNotebook.log(IDENTIFIER, data.toString().getBytes(ENCODING));
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
