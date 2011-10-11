package horizon.viphytravel;

import aethers.notebook.R;
import aethers.notebook.core.AethersNotebook;
import aethers.notebook.core.ui.ConfigurationActivity;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Activity that display the main menu of application.
 *
 */
public class MainMenu extends Activity {
	
	private final ServiceConnection loggerConnection = new ServiceConnection()
    {
        @Override
        public void onServiceDisconnected(ComponentName name) 
        {
            aethersNotebook = null;
        }
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) 
        {
            aethersNotebook = AethersNotebook.Stub.asInterface(service);
        }
    };
    
    private AethersNotebook aethersNotebook;

	@Override
	protected void onPause() {
		super.onPause();
		unbindService(loggerConnection);
        aethersNotebook = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent i = new Intent("aethers.notebook.action.ACTION_CONNECT");
        bindService(i, loggerConnection, BIND_AUTO_CREATE);
        
        //change button between "start" and "configure" based on running services
        Button button = (Button) findViewById(R.id.menu_control_logging_button);
        if (!isMyServiceRunning())
        	button.setText("Start logging");
        else button.setText("Configure logging");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainmenu);
		//set version text
		TextView versionText = (TextView) findViewById(R.id.menu_version_textView);
		versionText.setText( "Version " + getAppVersion() );
		
		//set up button with listener
		setUpButton();
		
		//load webloader
		Intent intent = new Intent(MainMenu.this, WebLoader.class);
		startActivity(intent);
	}
	
	/*
	 * check if 3 services (call, sms, postion logger) is running
	 */
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    boolean sCall = false;
	    boolean sSMS = false;
	    boolean sPosition = false;
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (service.service.getClassName().equals("aethers.notebook.logger.managed.callhistory.CallHistoryLogger"))
	        	sCall = true;
	        if (service.service.getClassName().equals("aethers.notebook.logger.managed.sms.SMSLogger"))
	        	sSMS = true;
	        if (service.service.getClassName().equals("aethers.notebook.logger.managed.position.PositionLogger"))
	        	sPosition = true;
	    }
	    if (sCall && sSMS && sPosition)
	    	return true;
	    else return false;
	}
	
	private String getAppVersion() {
		Context context = getApplicationContext();
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0 ).versionName;
		} catch (NameNotFoundException e) {
			return "1";
		}
	}
	
	/*
	 * set up the button listener for buttons on menu
	 */
	private void setUpButton() {
		// Capture Configuration button from layout
		Button button = (Button) findViewById(R.id.menu_control_logging_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainMenu.this, ConfigurationActivity.class);
				startActivity(intent);
			}
		});
		
		// Capture User Info button from layout
		button = (Button) findViewById(R.id.menu_user_info_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO: create user page (info + logout)
			}
		});
		
		// Capture Visualization button from layout
		button = (Button) findViewById(R.id.menu_visualization_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO: link to visualization
			}
		});

		// Capture About button from layout
		button = (Button) findViewById(R.id.menu_about_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainMenu.this, About.class);
				startActivity(intent);
			}
		});
	}

}
