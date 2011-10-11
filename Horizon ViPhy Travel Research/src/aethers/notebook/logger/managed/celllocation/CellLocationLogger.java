package aethers.notebook.logger.managed.celllocation;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONStringer;

import aethers.notebook.core.LoggerServiceIdentifier;
import aethers.notebook.logger.managed.PushLogger;
import aethers.notebook.util.Logger;
import android.os.RemoteException;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

public class CellLocationLogger
extends PushLogger<PhoneStateListener>
{
    private static final LoggerServiceIdentifier IDENTIFIER = new LoggerServiceIdentifier(
            "aethers.notebook.logger.managed.celllocation.CellLocationLogger");
    {
        IDENTIFIER.setConfigurable(false);
        IDENTIFIER.setDescription("Logs when the cell location changes.");
        IDENTIFIER.setName("Cell Location Logger");
        IDENTIFIER.setServiceClass(CellLocationLogger.class.getName());
        IDENTIFIER.setVersion(1);
    }

    private static final String ENCODING = "UTF-8";
    
    private static Logger logger = Logger.getLogger(CellLocationLogger.class);
    
    @Override
    protected PhoneStateListener preLogging() 
    {
        PhoneStateListener psl = new PhoneStateListener()
        {
            @Override
            public void onCellLocationChanged(CellLocation location) 
            {
                try
                {
                    logLocation(location);
                }
                catch(Exception e)
                {
                    logger.error("Unable to log cell location change", e);
                }
            }
        };
        telephonyManager.listen(psl, PhoneStateListener.LISTEN_CELL_LOCATION);
        return psl;
    }


    @Override
    protected void postLogging(PhoneStateListener psl)
    {
        telephonyManager.listen(psl, PhoneStateListener.LISTEN_NONE);
    }
    
        
    private void logLocation(CellLocation location)
    throws  JSONException,
            RemoteException,
            UnsupportedEncodingException
    {
        JSONStringer data = new JSONStringer();
        data.object();
        data.key("type").value(location.getClass().getName());
        if(location instanceof GsmCellLocation)
        {
            GsmCellLocation loc = (GsmCellLocation)location;
            data.key("cid").value(loc.getCid());
            data.key("lac").value(loc.getLac());
        }
        else if(location instanceof CdmaCellLocation)
        {
            CdmaCellLocation loc = (CdmaCellLocation)location;
            data.key("baseStationId").value(loc.getBaseStationId());
            data.key("baseStationLatitude").value(loc.getBaseStationLatitude());
            data.key("baseStationLongitude").value(loc.getBaseStationLongitude());
            data.key("networkId").value(loc.getNetworkId());
            data.key("systemId").value(loc.getSystemId());
        }
        data.key("neighbouringCells");
        data.array();
        for(NeighboringCellInfo info : telephonyManager.getNeighboringCellInfo())
        {
            data.object()
                    .key("cid").value(info.getCid())
                    .key("lac").value(info.getLac())
                    .key("psc").value(info.getPsc())
                    .key("rssi").value(info.getRssi())
                    .key("networkType");
            switch(info.getNetworkType())
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
        }
        data.endArray();
        data.endObject();
        if(aethersNotebook != null)
            aethersNotebook.log(IDENTIFIER, data.toString().getBytes(ENCODING));
    }
}
