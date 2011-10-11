package aethers.notebook.logger.managed.wifi;

import aethers.notebook.R;
import aethers.notebook.core.ui.IntegerPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class ConfigurationActivity 
extends PreferenceActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Configuration.SHARED_PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.wifilogger);
        
        Preference interval = 
                findPreference(getString(R.string.WifiLogger_Preferences_scanInterval));
        interval.setOnPreferenceChangeListener(new IntegerPreferenceChangeListener(
                0, Integer.MAX_VALUE, 
                "Interval must be a number greater than or equal to 0", this));
    }
}
