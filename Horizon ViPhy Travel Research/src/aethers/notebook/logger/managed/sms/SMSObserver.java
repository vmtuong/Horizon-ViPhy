package aethers.notebook.logger.managed.sms;

import android.database.ContentObserver;
import android.os.Handler;

public class SMSObserver extends ContentObserver
{
	SMSLogger smsLogger;
	
	public SMSObserver(SMSLogger smsLogger) {
		super(new Handler());
		this.smsLogger = smsLogger;
	}
	
	@Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        smsLogger.querySMS();
    }
}
