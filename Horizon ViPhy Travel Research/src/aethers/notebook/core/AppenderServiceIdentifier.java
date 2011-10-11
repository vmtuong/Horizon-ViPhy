package aethers.notebook.core;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import android.os.Parcel;
import android.os.Parcelable;

public class AppenderServiceIdentifier
implements Parcelable
{
    public static final Parcelable.Creator<AppenderServiceIdentifier> CREATOR =
            new Parcelable.Creator<AppenderServiceIdentifier>()
            {
                @Override
                public AppenderServiceIdentifier createFromParcel(Parcel source) 
                {
                    return new AppenderServiceIdentifier(source);
                }

                @Override
                public AppenderServiceIdentifier[] newArray(int size) 
                {
                    return new AppenderServiceIdentifier[size];
                }
            };

    private final String uniqueID;
    
    private String name;
    
    private String description;
    
    private String serviceClass;
    
    private boolean configurable;
    
    private int version;
    
    @JsonCreator
    public AppenderServiceIdentifier(@JsonProperty("uniqueID") String uniqueID)
    {
        this.uniqueID = uniqueID;
    }
    
    private AppenderServiceIdentifier(Parcel in) 
    {
        this.uniqueID = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.serviceClass = in.readString();
        this.version = in.readInt();
    }    
    
    @Override
    public int describeContents() 
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) 
    {
        dest.writeString(uniqueID);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(serviceClass);
        dest.writeInt(version);
    }
    
    public String getUniqueID()
    {
        return uniqueID;
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

    public String getServiceClass() 
    {
        return serviceClass;
    }

    public void setServiceClass(String serviceClass) 
    {
        this.serviceClass = serviceClass;
    }
    
    public boolean isConfigurable()
    {
        return configurable;
    }
    
    public void setConfigurable(boolean configurable)
    {
        this.configurable = configurable;
    }
    
    public int getVersion()
    {
        return version;
    }
    
    public void setVersion(int version)
    {
        this.version = version;
    }
    
    @Override
    public boolean equals(Object o) 
    {
        if(!(o instanceof AppenderServiceIdentifier))
            return false;
        AppenderServiceIdentifier i = (AppenderServiceIdentifier)o;
        if(uniqueID == null)
            return i.getUniqueID() == null;
        return uniqueID.equals(i.getUniqueID())
                 && version == i.getVersion();
    }
}
