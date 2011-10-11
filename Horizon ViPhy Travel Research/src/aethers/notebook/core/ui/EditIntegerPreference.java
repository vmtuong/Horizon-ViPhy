package aethers.notebook.core.ui;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

public class EditIntegerPreference 
extends EditTextPreference
{
    public EditIntegerPreference(Context context)
    {
        super(context);
    }
    
    public EditIntegerPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    
    public EditIntegerPreference(
            Context context,
            AttributeSet attrs,
            int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public EditText getEditText() 
    {
        EditText e = super.getEditText();
        e.setInputType(InputType.TYPE_CLASS_NUMBER);
        
        return e;
    }

    @Override
    public void setText(String text) 
    {
        
        super.setText(text);
    }
}
