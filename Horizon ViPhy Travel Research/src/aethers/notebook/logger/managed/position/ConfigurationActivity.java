package aethers.notebook.logger.managed.position;

import aethers.notebook.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ConfigurationActivity 
extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Configuration.SHARED_PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.positionlogger);
    }
}
