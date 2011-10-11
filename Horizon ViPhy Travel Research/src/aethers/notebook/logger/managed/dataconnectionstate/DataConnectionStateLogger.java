package aethers.notebook.logger.managed.dataconnectionstate;

import org.json.JSONStringer;

import aethers.notebook.core.LoggerServiceIdentifier;
import aethers.notebook.logger.managed.PushLogger;
import aethers.notebook.util.Logger;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class DataConnectionStateLogger
extends PushLogger<PhoneStateListener>
{            
    private static final LoggerServiceIdentifier IDENTIFIER = new LoggerServiceIdentifier(
            "aethers.notebook.logger.managed.dataconnectionstate.DataConnectionStateLogger");
    {
        IDENTIFIER.setConfigurable(false);
        IDENTIFIER.setDescription("Logs when the data connection state changes.");
        IDENTIFIER.setName("Data Connection State Logger");
        IDENTIFIER.setServiceClass(DataConnectionStateLogger.class.getName());
        IDENTIFIER.setVersion(1);
    }

    private static final String ENCODING = "UTF-8";
    
    private static Logger logger = Logger.getLogger(DataConnectionStateLogger.class);
    
    @Override
    protected PhoneStateListener preLogging() 
    {
        PhoneStateListener psl = new PhoneStateListener()
        {
            @Override
            public void onDataConnectionStateChanged(int state, int networkType)
            {
                try
                {
                    JSONStringer data = new JSONStringer();
                    data.object();
                    data.key("state");
                    switch(state)
                    {
                    case TelephonyManager.DATA_CONNECTED :
                        data.value("DATA_CONNECTED");
                        break;
                    case TelephonyManager.DATA_CONNECTING :
                        data.value("DATA_CONNECTING");
                        break;
                    case TelephonyManager.DATA_DISCONNECTED :
                        data.value("DATA_DISCONNECTED");
                        break;
                    case TelephonyManager.DATA_SUSPENDED :
                        data.value("DATA_SUSPENDED");
                        break;
                    default:
                        data.value("DATA_UNKNOWN");
                    }
                    data.key("networkType");
                    switch(networkType)
                    {
                    case TelephonyManager.NETWORK_TYPE_1xRTT :
                        data.value("NETWORK_TYPE_1xRTT");
                        break;
                    case TelephonyManager.NETWORK_TYPE_CDMA :
                        data.value("NETWORK_TYPE_CDMA");
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE :
                        data.value("NETWORK_TYPE_EDGE");
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_0 :
                        data.value("NETWORK_TYPE_EVDO_0");
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_A :
                        data.value("NETWORK_TYPE_EVDO_A");
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS :
                        data.value("NETWORK_TYPE_GPRS");
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSDPA :
                        data.value("NETWORK_TYPE_HSDPA");
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPA :
                        data.value("NETWORK_TYPE_HSPA");
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSUPA :
                        data.value("NETWORK_TYPE_HSUPA");
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS :
                        data.value("NETWORK_TYPE_UMTS");
                        break;
                    default: 
                        data.value("NETWORK_TYPE_UNKNOWN");
                    }
                    data.endObject();
                    aethersNotebook.log(IDENTIFIER, data.toString().getBytes(ENCODING));
                }
                catch(Exception e)
                {
                    logger.error("Unable to log data connection state change", e);
                }
            }
        };
        telephonyManager.listen(psl, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        return psl;
    }
    
    @Override
    protected void postLogging(PhoneStateListener psl) 
    {
        telephonyManager.listen(psl, PhoneStateListener.LISTEN_NONE);
    }
}
