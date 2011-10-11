package aethers.notebook.core;

import android.os.Parcel;
import android.os.Parcelable;

public class Action
implements Parcelable
{
    public static final Parcelable.Creator<Action> CREATOR =
        new Parcelable.Creator<Action>()
        {
            @Override
            public Action createFromParcel(Parcel source) 
            {
                return new Action(source);
            }

            @Override
            public Action[] newArray(int size) 
            {
                return new Action[size];
            }
        };
        
    private final String ID;
    
    private String name;
    
    private String description;

    public Action(String ID)
    {
        this.ID = ID;
    }
    
    private Action(Parcel in) 
    {
        ID = in.readString();
        name = in.readString();
        description = in.readString();
    }    
    
    @Override
    public int describeContents() 
    {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) 
    {
        dest.writeString(ID);
        dest.writeString(name);
        dest.writeString(description);
    }
    
    public String getID()
    {
        return ID;
    }
    
    public String getName() 
    {
        return name;
    }
    
    public void setName(String name) 
    {
        this.name = name;
    }
    
    public String getDescription() 
    {
        return description;
    }
    
    public void setDescription(String description) 
    {
        this.description = description;
    }
    
    @Override
    public boolean equals(Object o) 
    {
        if(!(o instanceof Action))
            return false;
        Action i = (Action)o;
        if(ID == null)
            return i.getID() == null;
        return ID.equals(i.getID());
}
}
