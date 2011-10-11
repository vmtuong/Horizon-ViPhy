package aethers.notebook.core.ui;

import android.content.Context;
import android.util.AttributeSet;

public class NonPersistingButtonPreference 
extends PersistingButtonPreference 
{ 
    public NonPersistingButtonPreference(Context context) 
    {
        super(context);
    }

    public NonPersistingButtonPreference(Context context, AttributeSet attrs) 
    {  
        super(context, attrs);
    }
    
    public NonPersistingButtonPreference(Context context, AttributeSet attrs, int defStyle) 
    {  
        super(context, attrs, defStyle); 
    }
    
    @Override
    protected final boolean shouldPersist()
    {
        return false;
    }
}
