package horizon.viphytravel;

import aethers.notebook.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * The class load ViPhy webpage for user to login/register
 */
public class WebLoader extends Activity {
	
	WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Adds Progrss bar Support
		this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.web);
		
		// Makes Progress bar Visible
		getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		
		//create web view
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new MyWebViewClient());
		mWebView.setWebChromeClient(new MyWebChromeClient());
		mWebView.loadUrl( this.getString(R.string.viphy_page) ); //load home page
	}
	
	@Override
	protected void onDestroy() {
		mWebView.destroy();
		super.onDestroy();
	}

	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        view.loadUrl(url);
	        return true;
	    }
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
	        mWebView.goBack();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	/* this client will save user detail to phone on java script alert */
	private class MyWebChromeClient extends WebChromeClient {

		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				JsResult result) {
			//save user id to shared preferences
			SharedPreferences prefs = getSharedPreferences("Preferences_UserID", MODE_PRIVATE);
			SharedPreferences.Editor prefEditor = prefs.edit();
			prefEditor.putString("UserID", message).commit();
			
			//finish itself
			finish();
			
			return super.onJsAlert(view, url, message, result);
		}

		@Override
		public void onProgressChanged(WebView view, int progress) {
			setTitle("Loading");
			setProgress(progress * 100);
			if (progress == 100)
				setTitle(R.string.application_name);
		}
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
}
