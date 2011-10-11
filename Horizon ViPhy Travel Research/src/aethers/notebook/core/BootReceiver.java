package aethers.notebook.core;

import aethers.notebook.util.Logger;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver
extends BroadcastReceiver 
{
    private static final Logger logger = Logger.getLogger(BootReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) 
    {
        if(!intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
            return;
        
        Configuration config = new Configuration(context);
        if(!config.isEnabled() || !config.isStartOnBoot())
            return;
        
        logger.verbose("BootReceiver.onReceive(): Starting core service");
        context.startService(new Intent(context, CoreService.class));         
    }
}
