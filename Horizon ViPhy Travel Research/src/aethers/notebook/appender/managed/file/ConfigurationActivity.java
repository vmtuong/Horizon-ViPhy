package aethers.notebook.appender.managed.file;

import java.io.File;

import aethers.notebook.R;
import aethers.notebook.core.ui.filechooser.FileChooser;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class ConfigurationActivity 
extends PreferenceActivity
{
    private static final int REQUEST_CODE = 111;
    
    private static final String EXTRA_RESULT = "FILENAME";
    
    private Preference filePathPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Configuration.SHARED_PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.fileappender);
        filePathPreference = findPreference(getString(R.string.FileAppender_Preferences_logfilePath));
        filePathPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference) 
            {
                Configuration config = new Configuration(ConfigurationActivity.this);
                File f = new File(config.getLogfilePath());
                Intent i = FileChooser.createStartIntent(
                        ConfigurationActivity.this, f.getParent(),
                        EXTRA_RESULT);
                startActivityForResult(i, REQUEST_CODE);
                return true;
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        if(resultCode == RESULT_CANCELED)
            return;
        if(REQUEST_CODE != requestCode)
            return;
        if(data == null || !data.hasExtra(EXTRA_RESULT))
            return;
        Editor e = getPreferenceManager().getSharedPreferences().edit();
        e.putString(
                filePathPreference.getKey(),
                data.getStringExtra(EXTRA_RESULT));
        e.commit();
    }
}
