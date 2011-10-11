package aethers.notebook.core.ui;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class PersistingButtonPreference
extends DialogPreference
{    
    public PersistingButtonPreference(Context context)
    {
        super(context, null);
    }
    
    public PersistingButtonPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    public PersistingButtonPreference(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected final void onClick() { }
    
    @Override
    public boolean isPersistent() 
    {
        return true;
    }
}
