package aethers.notebook.logger.managed.position;

import org.json.JSONStringer;

import aethers.notebook.core.LoggerServiceIdentifier;
import aethers.notebook.logger.managed.PushLogger;
import aethers.notebook.util.Logger;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

public class PositionLogger
extends PushLogger<OnSharedPreferenceChangeListener>
{
    private static final LoggerServiceIdentifier IDENTIFIER = new LoggerServiceIdentifier(
            "aethers.notebook.logger.managed.position.PositionLogger");
    {
        IDENTIFIER.setConfigurable(true);
        IDENTIFIER.setDescription("Logs position");
        IDENTIFIER.setName("Position Logger");
        IDENTIFIER.setServiceClass(PositionLogger.class.getName());
        IDENTIFIER.setVersion(1);
    }
    
    private static final String ENCODING = "UTF-8";
    
    private static Logger logger = Logger.getLogger(PositionLogger.class);
    
    private Configuration configuration;
    
    private LocationManager locationManager;
    
    private LocationListener locationListener = new LocationListener()
    {   
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) 
        {
            try
            {
                JSONStringer data = new JSONStringer();
                data.object()
                        .key("event").value("statusChanged")
                        .key("provider").value(provider)
                        .key("status");
                switch(status)
                {
                case LocationProvider.AVAILABLE :
                    data.value("AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE :
                    data.value("OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE :
                    data.value("TEMPORARILY_UNAVAILABLE");
                    break;
                default : data.value("UNKNOWN");
                }
                if(extras != null)
                    data.key("extras").value(extras.toString());
                data.endObject();
                aethersNotebook.log(IDENTIFIER, data.toString().getBytes(ENCODING));
            }
            catch(Exception e)
            {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public void onProviderEnabled(String provider) 
        {
            try
            {
                JSONStringer data = new JSONStringer();
                data.object()
                        .key("event").value("enabled")
                        .key("provider").value(provider)
                        .endObject();
                aethersNotebook.log(IDENTIFIER, data.toString().getBytes(ENCODING));
            }
            catch(Exception e)
            {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public void onProviderDisabled(String provider) 
        {
            try
            {
                JSONStringer data = new JSONStringer();
                data.object()
                        .key("event").value("disabled")
                        .key("provider").value(provider)
                        .endObject();
                aethersNotebook.log(IDENTIFIER, data.toString().getBytes(ENCODING));
            }
            catch(Exception e)
            {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public void onLocationChanged(Location location) 
        {
            try
            {
                JSONStringer data = new JSONStringer();
                data.object()
                        .key("event").value("locationChanged")
                        .key("provider").value(location.getProvider());
                data.key("location");
                data.object();
                if(location.hasAccuracy())
                    data.key("accuracy").value(location.getAccuracy());
                if(location.hasAltitude())
                    data.key("altitude").value(location.getAltitude());
                if(location.hasBearing())
                    data.key("bearing").value(location.getBearing());
                data.key("latitude").value(location.getLatitude());
                data.key("longitude").value(location.getLongitude());
                if(location.hasSpeed())
                    data.key("speed").value(location.getSpeed());
                data.key("time").value(location.getTime());
                if(location.getExtras() != null)
                    data.key("extras").value(location.getExtras().toString());
                data.endObject().endObject();
                aethersNotebook.log(IDENTIFIER, data.toString().getBytes(ENCODING));
            }
            catch(Exception e)
            {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    };
    
    @Override
    public void onCreate() 
    {
        super.onCreate();
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        configuration = new Configuration(this);
    }
    
    @Override
    protected OnSharedPreferenceChangeListener preLogging() 
    {
        registerListeners();  
        OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener()
        {
            @Override
            public void onSharedPreferenceChanged(
                    SharedPreferences sharedPreferences,
                    String key)
            {
                unregisterListeners();
                registerListeners();
            }
        };
        return listener;
    }
    
    @Override
    protected void postLogging(OnSharedPreferenceChangeListener listener) 
    {
        configuration.unregisterChangeListener(listener);
        unregisterListeners();
    }
    
    @Override
    protected void configure() 
    {
        Intent i = new Intent(this, ConfigurationActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
    
    private void registerListeners()
    {
        for(String name : locationManager.getAllProviders())
        {
            LocationProvider provider = locationManager.getProvider(name);
            if(!provider.hasMonetaryCost())
                locationManager.requestLocationUpdates(
                        name,
                        configuration.getLocationMinimumTime(),
                        configuration.getLocationMinimumDistance(),
                        locationListener);
        }
    }
    
    private void unregisterListeners()
    {
        locationManager.removeUpdates(locationListener);
    }
}
