package aethers.notebook.appender.managed.uploader;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPOutputStream;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import aethers.notebook.R;
import aethers.notebook.core.Action;
import aethers.notebook.core.LoggerServiceIdentifier;
import aethers.notebook.core.ManagedAppenderService;
import aethers.notebook.core.TimeStamp;
import aethers.notebook.util.Logger;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.telephony.TelephonyManager;

/**
 * This method has been modified for ViPhy server.
 * Method run() of class Upload originally send file using chunked encoding transfer,
 * which is not support by server. So it'd been rewrote to send file as body of post request
 */
public class UploaderAppender
extends Service
implements Runnable
{
    private static final String ENCODING = "UTF-8";
    
    private static final ArrayList<Action> actions = new ArrayList<Action>();
    private static final Action ACTION_UPLOAD = new Action(
            "aethers.notebook.appender.managed.uploader.UploaderAppender.upload"); 
    static 
    { 
        ACTION_UPLOAD.setName("Upload");
        ACTION_UPLOAD.setDescription("Upload all complete log files now");
        actions.add(ACTION_UPLOAD);
    }
    
    private static Logger logger = Logger.getLogger(UploaderAppender.class);
    
    private class OnPostFileComplete
    implements Runnable
    {
        public void run()
        {
            int maxFiles = configuration.getMaxFiles();
            if(maxFiles < 1)
            {
                checkUploadConditions();
                return;
            }
            FileFilter filter = new FileFilter()
            {
                @Override
                public boolean accept(File pathname) 
                {
                    if(!pathname.isFile())
                        return false;
                    return pathname.getName().startsWith("aether")
                        && pathname.getName().endsWith(".gz");
                }
            };
            List<File> files = Arrays.asList(currentDirectory.listFiles(filter));
            while(files.size() > maxFiles)
            {
                Collections.sort(files, new Comparator<File>()
                {
                    @Override
                    public int compare(File object1, File object2) 
                    {
                        return new Long(object2.lastModified()).
                                compareTo(new Long(object1.lastModified()));
                    }
                });
                files.get(0).delete();
                files = Arrays.asList(currentDirectory.listFiles(filter));
            }
            checkUploadConditions();
        }
    }
    
    private class Upload
    implements Runnable
    {
        @Override
        public void run() 
        {
            boolean delete = configuration.isDeleteUploadedFiles();
            File uploaddir = new File(currentDirectory, "uploaded");
            uploaddir.mkdir();
            HttpClient client = new DefaultHttpClient();
            File[] files = currentDirectory.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File pathname) 
                {
                    if(!pathname.isFile())
                        return false;
                    return pathname.getName().startsWith("aether")
                        && pathname.getName().endsWith(".gz");
                }
            });
            try
            {
                //URI uri = configuration.getUrl().toURI();
            	String targetURL = configuration.getUrl().toString();
                for(File f : files)
                {
                	/* rewrite 'send file using chunked encoding by dominic' for my server
                	HttpPost post = new HttpPost(uri);
                    FileEntity reqEntity = new FileEntity(f, "application/x-gzip");
                    reqEntity.setContentType("binary/octet-stream");
                    reqEntity.setChunked(true);
                    post.addHeader("X-AethersNotebook-Custom", 
                            configuration.getCustomHeader());
                    post.setEntity(reqEntity);
                    HttpResponse response = client.execute(post);
                    HttpEntity resEntity = response.getEntity();
                    EntityUtils.toString(resEntity);
                    resEntity.consumeContent();
                    */
                	postFile(targetURL, f); //post file as request body
                    if(delete)
                        f.delete();
                    else
                        f.renameTo(new File(uploaddir, f.getName()));
                }
            }
            catch(Exception e)
            {
                logger.error(e.getMessage(), e);
            }
            finally
            {
                client.getConnectionManager().shutdown();
            }
        }
        
        //post file as request body
        public void postFile(String targetURL, File file) throws Exception {

            HttpURLConnection conn = null;

            try {

                // Make a connect to the server
                URL url = new URL(targetURL);
                conn = (HttpURLConnection) url.openConnection();

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-gzip");

                // Send the body
                DataOutputStream dataOS = new DataOutputStream(conn.getOutputStream());
                dataOS.write( getBytesFromFile(file) );
                dataOS.flush();
                dataOS.close();

                // Ensure we got the HTTP 200 response code
                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    throw new Exception(String.format("Received the response code %d from the URL %s", responseCode, url));
                }

                // Read the response
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                int bytesRead;
                while((bytesRead = is.read(bytes)) != -1) {
                    baos.write(bytes, 0, bytesRead);
                }
                byte[] bytesReceived = baos.toByteArray();
                baos.close();

                is.close();
                String response = new String(bytesReceived);

                // TODO: Do something here to handle the 'response' string

            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

        }
        
        //get bytes from file
        public byte[] getBytesFromFile (File file) throws IOException {
        	byte[] b = new byte[(int) file.length()];
        	FileInputStream fileInputStream = new FileInputStream(file);
        	fileInputStream.read(b);
        	return b;
        }
    }
    
    private final ManagedAppenderService.Stub appenderServiceStub = 
        new ManagedAppenderService.Stub()
        {
            @Override
            public void stop()
            throws RemoteException 
            {
                UploaderAppender.this.stopSelf();
            }
            
            @Override
            public void start()
            throws RemoteException 
            {
                startService(new Intent(
                        UploaderAppender.this, 
                        UploaderAppender.this.getClass()));
            }
            
            @Override
            public void log(
                    final LoggerServiceIdentifier identifier,
                    final TimeStamp timestamp,
                    final Location location,
                    final byte[] data)
            throws RemoteException 
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run() 
                    {
                        synchronized(fileLockSync)
                        {
                            try
                            {
                                ObjectMapper m = new ObjectMapper();
                                JsonFactory fac = new JsonFactory();
                                JsonGenerator gen = fac.createJsonGenerator(fileOut);
                                gen.setCodec(m);
                                ObjectNode o = m.createObjectNode();
                                ObjectNode o2 = o.objectNode();
                                o2.put("uniqueID", identifier.getUniqueID());
                                o2.put("version", identifier.getVersion());
                                o.put("identifier", o2);
                                
                                /*get user id from prefs*/
                                SharedPreferences prefs = getSharedPreferences("Preferences_UserID", MODE_PRIVATE);
                                String userID = prefs.getString("UserID", "");
                                o.put("userId", userID);
                                
                                o.putPOJO("timestamp", timestamp);
                                o.putPOJO("location", location);
                                o.put("data", data);
                                m.writeTree(gen, o);
                                fileOut.write("\n");
                                fileOut.flush();
                                checkFileSize();
                            }
                            catch(Exception e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
            }
            
            @Override
            public boolean isRunning() 
            throws RemoteException 
            {
                return running;
            }
            
            @Override
            public void configure() 
            throws RemoteException 
            {
                Intent i = new Intent(UploaderAppender.this, ConfigurationActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }

            @Override
            public List<Action> listActions() 
            throws RemoteException 
            {
                return actions;
            }

            @Override
            public void doAction(Action action) 
            throws RemoteException 
            {
                if(action.getID().equals(ACTION_UPLOAD.getID()))
                    handler.post(new Upload());
            }
        };
        
    private final Object sync = new Object();
    
    private final Object fileLockSync = new Object();
    
    private volatile Writer fileOut;
    
    private volatile File currentDirectory;
    
    private volatile File currentFile;
    
    private volatile boolean running = false;
    
    private Handler handler;
    
    private Configuration configuration;
    
    private WifiManager wifiManager;
    
    private TelephonyManager telephonyManager;

    @Override
    public IBinder onBind(Intent intent) 
    {
        return appenderServiceStub;
    }
    
    @Override
    public void onDestroy() 
    {
        super.onDestroy();
        synchronized(sync)
        {
            if(running)
            {
                running = false;
                if(handler != null)
                {
                    handler.getLooper().quit();
                    handler = null;
                }
            }
        }
    }
    
    @Override
    public void onCreate() 
    {
        super.onCreate();
        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
        telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) 
    {      
        synchronized(sync)
        {
            if(running)
                return START_STICKY;
            running = true;
            new Thread(this).start();
            return START_STICKY;
        }        
    }

    @Override
    public void run() 
    {
        configuration = new Configuration(this);
        prepareOutput();
        Looper.prepare();
        handler = new Handler();
        final OnSharedPreferenceChangeListener listener =
                new OnSharedPreferenceChangeListener()
                {
                    @Override
                    public void onSharedPreferenceChanged(
                            SharedPreferences sharedPreferences,
                            String key) 
                    {
                        if(!key.equals(getString(
                                R.string.UploaderAppender_Preferences_logDirectory)))
                            return;
                        handler.post(new Runnable()
                        {                            
                            @Override
                            public void run() 
                            {
                                switchOutput();                                
                            }
                        });
                    }
                };
        configuration.registerChangeListener(listener);
        Timer t = new Timer(true);
        t.scheduleAtFixedRate(new TimerTask()
        {
            private boolean wifiEnabled = wifiManager.isWifiEnabled();
            @Override
            public void run() 
            {
                boolean b = wifiEnabled;
                wifiEnabled = wifiManager.isWifiEnabled();
                if(wifiEnabled == b)
                    checkUploadConditions();
            }
        }, 0, 1000);
        Looper.loop();
        t.cancel();
        configuration.unregisterChangeListener(listener);
        closeOutput();
    }
    
    private void prepareOutput()
    {
        synchronized(fileLockSync)
        {
            try
            {
                currentDirectory = configuration.getLogDirectory();
                if(!currentDirectory.exists())
                    currentDirectory.mkdirs();
                currentFile = File.createTempFile("aether", "", currentDirectory);
                fileOut = new OutputStreamWriter(
                                new BufferedOutputStream(
                                        new GZIPOutputStream(
                                                new FileOutputStream(currentFile))), ENCODING);
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void closeOutput()
    {
        synchronized(fileLockSync)
        {
            try
            {
                fileOut.flush();
                fileOut.close();
                currentFile.renameTo(new File(currentFile.getParentFile(), currentFile.getName() + ".gz"));
                fileOut = null;
                currentFile = null;
                currentDirectory = null;
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void switchOutput()
    {
        synchronized(fileLockSync)
        {
            try
            {
                fileOut.flush();
                fileOut.close();
                File oldDir = currentDirectory;
                currentDirectory = configuration.getLogDirectory();
                if(!currentDirectory.exists())
                    currentDirectory.mkdirs();
                currentFile.renameTo(new File(currentDirectory, currentFile.getName() + ".gz"));
                for(File f : oldDir.listFiles())
                    f.renameTo(new File(currentDirectory, f.getName()));
                currentFile = File.createTempFile("aether", "", currentDirectory);
                fileOut = new OutputStreamWriter(new GZIPOutputStream(
                        new BufferedOutputStream(new FileOutputStream(currentFile))), ENCODING);
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void checkFileSize()
    throws IOException
    {
        synchronized(fileLockSync)
        {
            if(currentFile.length() < configuration.getMaxFileSize())
                return;
            fileOut.flush();
            fileOut.close();
            currentFile.renameTo(new File(currentDirectory, currentFile.getName() + ".gz"));
            currentFile = File.createTempFile("aether", "", currentDirectory);
            fileOut = new OutputStreamWriter(new GZIPOutputStream(
                    new BufferedOutputStream(new FileOutputStream(currentFile))), ENCODING);
        }
        handler.post(new OnPostFileComplete());
    }
    
    private void checkUploadConditions()
    {
        switch(configuration.getConnectionType())
        {
            case Manual : return;
            case Wifi :
                if(wifiManager.isWifiEnabled() && wifiManager.pingSupplicant())
                    handler.post(new Upload());
                return;
            case WifiAnd3G :
                if(wifiManager.isWifiEnabled() 
                        || telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED)
                    handler.post(new Upload());
                return;
        }
    }
}
