package aethers.notebook.logger.managed.servicestate;

import org.json.JSONStringer;

import aethers.notebook.core.LoggerServiceIdentifier;
import aethers.notebook.logger.managed.PushLogger;
import aethers.notebook.util.Logger;

import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;

public class ServiceStateLogger
extends PushLogger<PhoneStateListener>
{            
    private static final LoggerServiceIdentifier IDENTIFIER = new LoggerServiceIdentifier(
            "aethers.notebook.logger.managed.servicestate.ServiceStateLogger");
    {
        IDENTIFIER.setConfigurable(false);
        IDENTIFIER.setDescription("Logs when the service state changes.");
        IDENTIFIER.setName("Service State Logger");
        IDENTIFIER.setServiceClass(ServiceStateLogger.class.getName());
        IDENTIFIER.setVersion(1);
    }

    private static final String ENCODING = "UTF-8";
    
    private static Logger logger = Logger.getLogger(ServiceStateLogger.class);
    
    @Override
    protected PhoneStateListener preLogging() 
    {
        PhoneStateListener psl = new PhoneStateListener()
        {
            @Override
            public void onServiceStateChanged(ServiceState serviceState)
            {
                try
                {
                    JSONStringer data = new JSONStringer();
                    data.object()
                            .key("isManualSelection").value(serviceState.getIsManualSelection())
                            .key("operatorAlphaLong").value(serviceState.getOperatorAlphaLong())
                            .key("operatorAlphaShort").value(serviceState.getOperatorAlphaShort())
                            .key("operatorNumeric").value(serviceState.getOperatorNumeric())
                            .key("roaming").value(serviceState.getRoaming())
                            .key("state");
                    switch(serviceState.getState())
                    {
                    case ServiceState.STATE_EMERGENCY_ONLY :
                        data.value("STATE_EMERGENCY_ONLY");
                        break;
                    case ServiceState.STATE_IN_SERVICE :
                        data.value("STATE_IN_SERVICE");
                        break;
                    case ServiceState.STATE_OUT_OF_SERVICE :
                        data.value("STATE_OUT_OF_SERVICE");
                        break;
                    case ServiceState.STATE_POWER_OFF :
                        data.value("STATE_POWER_OFF");
                        break;
                    default :
                        data.value("STATE_UNKNOWN");
                    }        
                    data.endObject();
                    aethersNotebook.log(IDENTIFIER, data.toString().getBytes(ENCODING));
                }
                catch(Exception e)
                {
                    logger.error("Unable to log service state change", e);
                }
            }
        };
        telephonyManager.listen(psl, PhoneStateListener.LISTEN_SERVICE_STATE);
        return psl;
    }
    
    @Override
    protected void postLogging(PhoneStateListener psl) 
    {
        telephonyManager.listen(psl, PhoneStateListener.LISTEN_NONE);
    }
}
