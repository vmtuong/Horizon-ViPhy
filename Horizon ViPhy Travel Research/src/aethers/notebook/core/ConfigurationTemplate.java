package aethers.notebook.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class ConfigurationTemplate
{
    private final Context context;
    
    private final SharedPreferences prefs;
    
    public ConfigurationTemplate(Context context, String sharedPreferencesName) 
    {
        this.context = context;
        prefs = context.getSharedPreferences(
                sharedPreferencesName, 
                Context.MODE_PRIVATE);
        //prefs.edit().clear().commit();
    }
    
    public void registerChangeListener(OnSharedPreferenceChangeListener listener)
    {
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }
    
    public void unregisterChangeListener(OnSharedPreferenceChangeListener listener)
    {
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }
    
    protected boolean getBoolean(int prefName, int prefDefault)
    {
        return prefs.getBoolean(context.getString(prefName), 
                Boolean.parseBoolean(context.getString(prefDefault)));
    }
    
    protected void setBoolean(int prefName, boolean value)
    {
        Editor e = prefs.edit();
        e.putBoolean(context.getString(prefName), value);
        e.commit();
    }
    
    protected int getInt(int prefName, int prefDefault)
    {
        return prefs.getInt(context.getString(prefName),
                Integer.parseInt(context.getString(prefDefault)));
    }
    
    protected void setInt(int prefName, int value)
    {
        Editor e = prefs.edit();
        e.putInt(context.getString(prefName), value);
        e.commit();
    }
    
    protected long getLong(int prefName, int prefDefault)
    {
        return prefs.getLong(context.getString(prefName),
                Integer.parseInt(context.getString(prefDefault)));
    }
    
    protected void setLong(int prefName, long value)
    {
        Editor e = prefs.edit();
        e.putLong(context.getString(prefName), value);
        e.commit();
    }
    
    protected float getFloat(int prefName, int prefDefault)
    {
        return prefs.getFloat(context.getString(prefName),
                Integer.parseInt(context.getString(prefDefault)));
    }
    
    protected void setFloat(int prefName, float value)
    {
        Editor e = prefs.edit();
        e.putFloat(context.getString(prefName), value);
        e.commit();
    }
    
    protected String getString(int prefName, int prefDefault)
    {
        return prefs.getString(context.getString(prefName),
                context.getString(prefDefault));
    }
    
    protected void setString(int prefName, String value)
    {
        Editor e = prefs.edit();
        e.putString(context.getString(prefName), value);
        e.commit();
    }
    
    protected Context getContext()
    {
        return context;
    }
    
    protected SharedPreferences getSharedPreferences()
    {
        return prefs;
    }
}
