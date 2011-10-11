package aethers.notebook.logger.managed.signalstrength;

import org.json.JSONStringer;

import aethers.notebook.core.LoggerServiceIdentifier;
import aethers.notebook.logger.managed.PushLogger;
import aethers.notebook.util.Logger;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

public class SignalStrengthLogger
extends PushLogger<PhoneStateListener>
{            
    private static final LoggerServiceIdentifier IDENTIFIER = new LoggerServiceIdentifier(
            "aethers.notebook.logger.managed.signalstrength.SignalStrengthLogger");
    {
        IDENTIFIER.setConfigurable(false);
        IDENTIFIER.setDescription("Logs when the signal strength changes.");
        IDENTIFIER.setName("Signal Strength Logger");
        IDENTIFIER.setServiceClass(SignalStrengthLogger.class.getName());
        IDENTIFIER.setVersion(1);
    }

    private static final String ENCODING = "UTF-8";
    
    private static Logger logger = Logger.getLogger(SignalStrengthLogger.class);
    
    @Override
    protected PhoneStateListener preLogging() 
    {
        PhoneStateListener psl = new PhoneStateListener()
        {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength)
            {
                try
                {
                    JSONStringer data = new JSONStringer();
                    data.object()
                            .key("cdmaDbm").value(signalStrength.getCdmaDbm())
                            .key("cdmaEcio").value(signalStrength.getCdmaEcio())
                            .key("evdoDbm").value(signalStrength.getEvdoDbm())
                            .key("evdoEcio").value(signalStrength.getEvdoEcio())
                            .key("evdoSnr").value(signalStrength.getEvdoSnr())
                            .key("gsmBitErrorRate").value(signalStrength.getGsmBitErrorRate())
                            .key("gsmSignalStrength").value(signalStrength.getGsmSignalStrength())
                            .key("isGsm").value(signalStrength.isGsm())
                            .endObject();
                    aethersNotebook.log(IDENTIFIER, data.toString().getBytes(ENCODING));
                }
                catch(Exception e)
                {
                    logger.error("Unable to log signal strength change", e);
                }
            }
        };
        telephonyManager.listen(psl, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        return psl;
    }
    
    @Override
    protected void postLogging(PhoneStateListener psl) 
    {
        telephonyManager.listen(psl, PhoneStateListener.LISTEN_NONE);
    }
}
