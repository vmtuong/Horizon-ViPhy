package aethers.notebook.appender.managed.uploader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import aethers.notebook.R;
import aethers.notebook.core.ConfigurationTemplate;
import android.content.Context;
import android.os.Environment;

public class Configuration
extends ConfigurationTemplate
{   
    public static final String SHARED_PREFERENCES_NAME 
            = "aethers.notebook.appender.managed.uploader.Configuration";
    
    public static enum ConnectionType
    {
        WifiAnd3G("Wifi and 3G"),
        Wifi("Wifi Only"),
        Manual("Manual");
        
        public final String friendlyName;
        
        private ConnectionType(String friendlyName)
        {
            this.friendlyName = friendlyName;
        }
    }
    
    private static final String defaultPathPrefix = 
            Environment.getExternalStorageDirectory().getAbsolutePath();
    
    public Configuration(Context context) 
    {
        super(context, SHARED_PREFERENCES_NAME);
    }
    
    public ConnectionType getConnectionType()
    {
        return ConnectionType.valueOf(getString(
                R.string.UploaderAppender_Preferences_connectionType,
                R.string.UploaderAppender_Preferences_connectionType_default));
    }
    
    public void setConnectionType(ConnectionType connectionType)
    {
        setString(
                R.string.UploaderAppender_Preferences_connectionType,
                connectionType.toString());
    }
    
    public URL getUrl()
    {
        try
        {
            return new URL(getString(
                    R.string.UploaderAppender_Preferences_url,
                    R.string.UploaderAppender_Preferences_url_default));
        }
        catch(MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void setUrl(URL url)
    {
        setString(R.string.UploaderAppender_Preferences_url, url.toExternalForm());
    }
    
    public long getMaxFileSize()
    {
        return Long.parseLong(getString(
                R.string.UploaderAppender_Preferences_maxFileSize,
                R.string.UploaderAppender_Preferences_maxFileSize_default));
    }
    
    public void setMaxFileSize(long maxFileSize)
    {
        setString(R.string.UploaderAppender_Preferences_maxFileSize, String.valueOf(maxFileSize));
    }
    
    public File getLogDirectory()
    {
        String defaultDir = getContext().getString(
                R.string.UploaderAppender_Preferences_logDirectory_default);
        String configuredDir = getString(
                R.string.UploaderAppender_Preferences_logDirectory,
                R.string.UploaderAppender_Preferences_logDirectory_default);
        if(defaultDir.equals(configuredDir))
            return new File(defaultPathPrefix + configuredDir);
        return new File(configuredDir);
    }
    
    public void setLogDirectory(File logDirectory)
    {
        setString(
                R.string.UploaderAppender_Preferences_logDirectory,
                logDirectory.getAbsolutePath());
    }
    
    public boolean isDeleteUploadedFiles()
    {
        return getBoolean(
                R.string.UploaderAppender_Preferences_deleteUploadedFiles,
                R.string.UploaderAppender_Preferences_logDirectory_default);
    }
    
    public void setDeleteUploadedFiles(boolean deleteUploadedFiles)
    {
        setBoolean(
                R.string.UploaderAppender_Preferences_deleteUploadedFiles, 
                deleteUploadedFiles);
    }
    
    public int getMaxFiles()
    {
        
        String s = getString(
                R.string.UploaderAppender_Preferences_maxFiles,
                R.string.UploaderAppender_Preferences_maxFiles_default);
        return s == null || s.equals("")
                ? -1
                : Integer.parseInt(s);
    }
    
    public void setMaxFiles(int maxFiles)
    {
        setString(R.string.UploaderAppender_Preferences_maxFiles,
                maxFiles == -1
                        ? ""
                        : String.valueOf(maxFiles));
    }
    
    public String getCustomHeader()
    {
        return getString(
                R.string.UploaderAppender_Preferences_customHeader,
                R.string.UploaderAppender_Preferences_customHeader_default);
    }
    
    public void setCustomHeader(String customHeader)
    {
        setString(R.string.UploaderAppender_Preferences_customHeader, customHeader);
    }
}
