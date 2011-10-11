package aethers.notebook.core;

import android.os.Parcel;
import android.os.Parcelable;

public class TimeStamp
implements Parcelable
{
    public static final Parcelable.Creator<TimeStamp> CREATOR =
        new Parcelable.Creator<TimeStamp>()
        {
            @Override
            public TimeStamp createFromParcel(Parcel source) 
            {
                return new TimeStamp(source);
            }

            @Override
            public TimeStamp[] newArray(int size) 
            {
                return new TimeStamp[size];
            }
        };

    private long systemTime;
    
    private String timezone;
    
    public TimeStamp() { }
    
    public TimeStamp(long systemTime, String timezone)
    {
        this.systemTime = systemTime;
        this.timezone = timezone;
    }
    
    private TimeStamp(Parcel in)
    {
        systemTime = in.readLong();
        timezone = in.readString();
    }
    
    public long getSystemTime()
    {
        return systemTime;
    }
    
    public void setSystemTime(long systemTime)
    {
        this.systemTime = systemTime;
    }
    
    public String getTimezone()
    {
        return timezone;
    }
    
    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }
    
    @Override
    public int describeContents() 
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) 
    {
        dest.writeLong(systemTime);
        dest.writeString(timezone);        
    }
}
