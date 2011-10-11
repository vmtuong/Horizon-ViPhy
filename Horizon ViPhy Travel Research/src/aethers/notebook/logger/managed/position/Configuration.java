package aethers.notebook.logger.managed.position;

import aethers.notebook.R;
import aethers.notebook.core.ConfigurationTemplate;
import android.content.Context;

public class Configuration 
extends ConfigurationTemplate
{
    public static final String SHARED_PREFERENCES_NAME 
            = "aethers.notebook.logger.managed.position.Configuration";

    public Configuration(Context context)
    {
        super(context, SHARED_PREFERENCES_NAME);
    }
    
    public int getLocationMinimumTime()
    {
        return Integer.parseInt(getString(
                R.string.PositionLogger_Preferences_locationMinTime,
                R.string.PositionLogger_Preferences_locationMinTime_default));
    }
    
    public int getLocationMinimumDistance()
    {
        return Integer.parseInt(getString(
                R.string.PositionLogger_Preferences_locationMinDistance,
                R.string.PositionLogger_Preferences_locationMinDistance_default));
    }
}
