package aethers.notebook.logger.managed.callhistory;

import android.database.ContentObserver;
import android.os.Handler;

public class CallLogObserver extends ContentObserver
{
	private CallHistoryLogger callHistoryLogger;
	
	public CallLogObserver(CallHistoryLogger callHistoryLogger) {
		super( new Handler() );
		this.callHistoryLogger = callHistoryLogger;
	}
	
	@Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        callHistoryLogger.queryCallHistory();
    }
}