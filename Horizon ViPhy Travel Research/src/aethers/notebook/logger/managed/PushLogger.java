package aethers.notebook.logger.managed;

import aethers.notebook.core.AethersNotebook;
import aethers.notebook.core.CoreService;
import aethers.notebook.core.LoggerService;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.telephony.TelephonyManager;

public abstract class PushLogger<T>
extends Service
implements Runnable
{    
    private final LoggerService.Stub loggerServiceStub = 
            new LoggerService.Stub()
            {
                @Override
                public void configure()
                throws RemoteException 
                {  
                    PushLogger.this.configure();
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
                            PushLogger.this, 
                            PushLogger.this.getClass()));
                }
        
                @Override
                public void stop() 
                throws RemoteException 
                {
                    PushLogger.this.stopSelf();
                }       
            };
    
    private final ServiceConnection loggerConnection = 
            new ServiceConnection() 
            {   
                @Override
                public void onServiceDisconnected(ComponentName name)
                {
                    PushLogger.this.stopSelf();
                }
                
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) 
                {
                    aethersNotebook = AethersNotebook.Stub.asInterface(service);
                    new Thread(PushLogger.this).start();
                }
            };
            
    private final Object sync = new Object();
    
    protected AethersNotebook aethersNotebook;
    
    protected TelephonyManager telephonyManager;
    
    private volatile boolean running = false;
        
    private Looper looper;
    
    @Override
    public IBinder onBind(Intent intent) 
    {
        return loggerServiceStub;
    }
    
    @Override
    public void onCreate() 
    {
        super.onCreate();
        telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
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
                if(looper != null)
                {
                    looper.quit();
                    looper = null;
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
        Looper.prepare();
        looper = new Handler().getLooper();
        T t = preLogging();
        Looper.loop();
        postLogging(t);
    }
    
    protected T preLogging()
    {
        return null;
    }
    
    protected void postLogging(T t)
    {
        
    }
    
    protected void configure()
    {
        
    }
}
