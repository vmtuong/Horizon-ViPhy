package horizon.viphytravel;

import aethers.notebook.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Activity to show Introduction Screen.
 * Explain data collection for research and other commitment.
 *
 */
public class Introduction extends Activity {
	boolean termAgree = false;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Using Shared Preferences to check whether the term is agreed	
		final SharedPreferences prefs = getSharedPreferences("Preferences_termAgree", MODE_PRIVATE);
		termAgree = prefs.getBoolean("termAgree", false);
		if (!termAgree) { 
		    setContentView(R.layout.introduction);
			
			// Capture Accept button from layout
			Button button = (Button) findViewById(R.id.introduction_accept_button);
			// Register the onClick listener with the implementation below
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//update shared preferences of terms
					prefs.edit().putBoolean("termAgree", true).commit();
					startMainMenu();
				}
			});
			
			// Capture Exit button from layout
			button = (Button) findViewById(R.id.introduction_exit_button);
			// Register the onClick listener with the implementation below
			button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
			
		} else {
			startMainMenu();
		}
	}
	
	private void startMainMenu() {
		Intent intent = new Intent(Introduction.this, MainMenu.class);
		startActivity(intent);
		finish();
	}

}
