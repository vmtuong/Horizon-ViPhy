package aethers.notebook.logger.managed.wifi;

import aethers.notebook.R;
import aethers.notebook.core.ConfigurationTemplate;
import android.content.Context;

public class Configuration
extends ConfigurationTemplate
{   
    public static final String SHARED_PREFERENCES_NAME 
            = "aethers.notebook.logger.managed.wifi.Configuration";
    
    public Configuration(Context context) 
    {
        super(context, SHARED_PREFERENCES_NAME);
    }
    
    public int getScanInterval()
    {
        return Integer.parseInt(getString(
                R.string.WifiLogger_Preferences_scanInterval,
                R.string.WifiLogger_Preferences_scanInterval_default));
    }
}
