package aethers.notebook.core.ui;

import android.content.Context;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

public class IntegerPreferenceChangeListener
implements OnPreferenceChangeListener
{
    private final int minimum;
    
    private final int maximum;
    
    private final String toastMessage;
    
    private final Context context;
    
    private final boolean allowBlank;
    
    public IntegerPreferenceChangeListener(
            int minimum,
            int maximum,
            String toastMessage,
            Context context)
    {
        this(minimum, maximum, toastMessage, false, context);
    }
    
    public IntegerPreferenceChangeListener(
            int minimum,
            int maximum,
            String toastMessage,
            boolean allowBlank,
            Context context)
    {
        this.minimum = minimum;
        this.maximum = maximum;
        this.toastMessage = toastMessage;
        this.allowBlank = allowBlank;
        this.context = context;
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) 
    {
        try
        {
            String s = (String)newValue;
            if(allowBlank && s.equals(""))
                return true;
            int val = Integer.parseInt(s);
            if(val < minimum || val > maximum)
                throw new RuntimeException();
            return true;
        }
        catch(Exception e)
        {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
            return false;   
        }
    }
}
