package horizon.viphytravel;

import aethers.notebook.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * Activity to show About Us screen
 * Explain data collection for research and contact
 *
 */
public class About extends Activity {
	boolean firstUse = true;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.about);
		
		// Capture Back button from layout
		Button button = (Button) findViewById(R.id.about_back_button);
		// Register the onClick listener with the implementation below
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish(); //end this activity
			}
		});
	}

}
