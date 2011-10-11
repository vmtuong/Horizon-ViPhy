package aethers.notebook.appender.managed.file;

import aethers.notebook.R;
import aethers.notebook.core.ConfigurationTemplate;
import android.content.Context;
import android.os.Environment;

public class Configuration
extends ConfigurationTemplate
{   
    public static final String SHARED_PREFERENCES_NAME 
            = "aethers.notebook.appender.managed.file.Configuration";
    
    private static final String defaultPathPrefix = 
            Environment.getExternalStorageDirectory().getAbsolutePath();
    
    public Configuration(Context context) 
    {
        super(context, SHARED_PREFERENCES_NAME);
    }
    
    public String getLogfilePath()
    {
        String defaultPath = getContext().getString(R.string.FileAppender_Preferences_logfilePath_default);
        String configuredPath = getString(
                R.string.FileAppender_Preferences_logfilePath,
                R.string.FileAppender_Preferences_logfilePath_default);
        if(defaultPath.equals(configuredPath))
            return defaultPathPrefix + defaultPath;
        return configuredPath;
    }
}
