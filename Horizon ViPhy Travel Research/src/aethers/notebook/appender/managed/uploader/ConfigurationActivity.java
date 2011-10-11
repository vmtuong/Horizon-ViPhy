package aethers.notebook.appender.managed.uploader;

import java.io.File;
import java.util.ArrayList;

import aethers.notebook.R;
import aethers.notebook.appender.managed.uploader.Configuration.ConnectionType;
import aethers.notebook.core.ui.IntegerPreferenceChangeListener;
import aethers.notebook.core.ui.filechooser.DirectoryChooser;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class ConfigurationActivity 
extends PreferenceActivity
{
    private static final int REQUEST_CODE = 111;
    
    private static final String EXTRA_RESULT = "DIRNAME";
    
    private Configuration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        configuration = new Configuration(this);
        getPreferenceManager().setSharedPreferencesName(Configuration.SHARED_PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.uploaderappender);
        
        ListPreference connTypes = (ListPreference)findPreference(
                getString(R.string.UploaderAppender_Preferences_connectionType));
        ArrayList<String> entries = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();
        for(ConnectionType ct : ConnectionType.values())
        {
            entries.add(ct.friendlyName);
            values.add(ct.toString());
        }
        connTypes.setEntries(entries.toArray(new String[0]));
        connTypes.setEntryValues(values.toArray(new String[0]));
        
        Preference maxSize = 
                findPreference(getString(R.string.UploaderAppender_Preferences_maxFileSize));
        maxSize.setOnPreferenceChangeListener(new IntegerPreferenceChangeListener(
                1, Integer.MAX_VALUE, 
                "Maximum must be a number greater than or equal to 1", this));
        
        Preference maxFiles = 
            findPreference(getString(R.string.UploaderAppender_Preferences_maxFiles));
        maxFiles.setOnPreferenceChangeListener(
                new IntegerPreferenceChangeListener(
                        1, Integer.MAX_VALUE,
                        "Leave blank for unlimited, otherwise must be a number greater than 1",
                        true,
                        this));
        
        Preference dirPreference = findPreference(getString(R.string.UploaderAppender_Preferences_logDirectory));
        dirPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference) 
            {
                Intent i = DirectoryChooser.createStartIntent(
                        ConfigurationActivity.this,
                        configuration.getLogDirectory().getAbsolutePath(),
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
        String dir = data.getStringExtra(EXTRA_RESULT);
        configuration.setLogDirectory(new File(dir));
    }
}
