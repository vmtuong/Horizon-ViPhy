package aethers.notebook.logger.managed.callhistory;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONStringer;

import aethers.notebook.core.LoggerServiceIdentifier;
import aethers.notebook.logger.managed.PushLogger;
import aethers.notebook.util.Logger;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.PhoneLookup;

/**
 * Logger that using content observer to keep track of ingoing and outgoing call
 * 
 */
public class CallHistoryLogger extends PushLogger<CallLogObserver> {
	
	private static final LoggerServiceIdentifier IDENTIFIER = new LoggerServiceIdentifier(
            "aethers.notebook.logger.managed.callhistory.CallHistoryLogger");
    {
        IDENTIFIER.setConfigurable(false);
        IDENTIFIER.setDescription("Logs the call history");
        IDENTIFIER.setName("Calls Logger");
        IDENTIFIER.setServiceClass(CallHistoryLogger.class.getName());
        IDENTIFIER.setVersion(1);
    }

    private static final String ENCODING = "UTF-8";
    
    private static Logger logger = Logger.getLogger(CallHistoryLogger.class);
    
	@Override
	protected CallLogObserver preLogging() {
		//register a content observer
        CallLogObserver callLogObserver = new CallLogObserver(this);
        getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogObserver);
		return callLogObserver;
	}

	@Override
	protected void postLogging(CallLogObserver callLogObserver) {
		//unregister content observer
		getContentResolver().unregisterContentObserver(callLogObserver);
	}
	
	protected void queryCallHistory() {
		//query the call history
		Cursor mCallCursor = getContentResolver().query(
				android.provider.CallLog.Calls.CONTENT_URI, 
				null, null,	null, 
				android.provider.CallLog.Calls.DATE + " DESC");

		if (mCallCursor.getCount() > 0) //if there is more than 1 call
		{
			mCallCursor.moveToFirst();
			try {
				logCall(mCallCursor);
			} catch (Exception e) {
				logger.error("Unable to log call history.", e);
			}
		}
	}
	
	//logging a call
    private void logCall(Cursor mCallCursor) throws JSONException, RemoteException, UnsupportedEncodingException 
    {
    	int i;
		JSONStringer data = new JSONStringer();
        data.object(); //json object to store the call
        
        data.key("number");
        i = mCallCursor.getColumnIndex(Calls.NUMBER);
        String number = mCallCursor.getString(i);
        data.value(number);
        
        data.key("name");
        i = mCallCursor.getColumnIndex(Calls.CACHED_NAME);
        data.value( mCallCursor.getString(i) );
        
        data.key("numberType");
        i = mCallCursor.getColumnIndex(Calls.CACHED_NUMBER_TYPE);
        int numberType = mCallCursor.getInt(i);
        //use getTypeLabel to have the String of numberType
        String str = (String) CommonDataKinds.Phone.getTypeLabel(this.getResources(), numberType, "");
        if (mCallCursor.getString(i) != null)
        	data.value( str );
        else data.value("");
        
        data.key("callType");
        i = mCallCursor.getColumnIndex(Calls.TYPE);
        switch ( mCallCursor.getInt(i) )
        {
        case android.provider.CallLog.Calls.INCOMING_TYPE:
        	data.value("INCOMING");
        	break;
        case android.provider.CallLog.Calls.MISSED_TYPE:
        	data.value("MISSED");
        	break;
        case android.provider.CallLog.Calls.OUTGOING_TYPE:
        	data.value("OUTGOING");
        	break;
        }
        
        data.key("date");
        i = mCallCursor.getColumnIndex(Calls.DATE);
        data.value( mCallCursor.getLong(i) );
        
        data.key("duration");
        i = mCallCursor.getColumnIndex(Calls.DURATION);
        data.value( mCallCursor.getLong(i) );
        
        data.key("contact");
        writeContactToJSON(data, number);
        data.endObject();
        if (aethersNotebook != null)
        	aethersNotebook.log(IDENTIFIER, data.toString().getBytes(ENCODING));
        	
    }
	
    private void writeContactToJSON (JSONStringer data, String number) throws JSONException
    {
    	long id;
    	String displayName;
    	String givenName;
    	String familyName;
    	String phoneNo;
    	String phoneType;
    	String emailAddress;
    	String emailType;
    	
    	int i;
    	//get id and name from number from contact list
    	Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] projection = new String[]{ PhoneLookup._ID, PhoneLookup.DISPLAY_NAME };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        cursor.moveToFirst();
        i = cursor.getColumnIndex(PhoneLookup._ID);
        id = cursor.getLong(i);
        i = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
        displayName = cursor.getString(i);
        cursor.close();
        
        //get first name and last name
        String nameWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"; 
        String[] nameWhereParameters = new String[]{ String.valueOf(id), CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        Cursor nameCur = getContentResolver().query(
        		ContactsContract.Data.CONTENT_URI,
        		null,
        		nameWhere,
        		nameWhereParameters,
        		null);
        nameCur.moveToFirst();
        i = nameCur.getColumnIndex(CommonDataKinds.StructuredName.GIVEN_NAME);
        givenName = nameCur.getString(i);
        i = nameCur.getColumnIndex(CommonDataKinds.StructuredName.FAMILY_NAME);
        familyName = nameCur.getString(i);
        nameCur.close();
        
        //get phone number
        Cursor phoneCur = getContentResolver().query(
        		CommonDataKinds.Phone.CONTENT_URI, 
        		null, 
        		CommonDataKinds.Phone.CONTACT_ID +" = ?", 
        		new String[]{ String.valueOf(id) }, 
        		null);
        phoneCur.moveToFirst();
     	i = phoneCur.getColumnIndex(CommonDataKinds.Phone.NUMBER);
     	phoneNo = phoneCur.getString(i);
     	i = phoneCur.getColumnIndex(CommonDataKinds.Phone.TYPE);
     	int phoneTypeInt = phoneCur.getInt(i);
     	phoneType = (String) CommonDataKinds.Phone.getTypeLabel(this.getResources(), phoneTypeInt, "");
     	phoneCur.close();
        
        //get email
     	Cursor emailCur = getContentResolver().query(
        		CommonDataKinds.Email.CONTENT_URI, 
        		null, 
        		CommonDataKinds.Email.CONTACT_ID +" = ?", 
        		new String[]{ String.valueOf(id) }, 
        		null);
     	emailCur.moveToFirst();
     	i = emailCur.getColumnIndex(CommonDataKinds.Email.DATA);
     	emailAddress = emailCur.getString(i);
     	i = emailCur.getColumnIndex(CommonDataKinds.Email.TYPE);
     	int emailTypeInt = emailCur.getInt(i);
     	emailType = (String) CommonDataKinds.Email.getTypeLabel(this.getResources(), emailTypeInt, "");
     	emailCur.close();
     	
     	//get postal address
     	String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"; 
    	String[] addrWhereParams = new String[]{ 
    			String.valueOf(id), 
    			ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE}; 
    	Cursor addrCur = getContentResolver().query(
    			ContactsContract.Data.CONTENT_URI, 
    			null,
    			addrWhere,
    			addrWhereParams,
    			null);
    	addrCur.moveToFirst();
		String street = addrCur.getString(
				addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
		String city = addrCur.getString(
				addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
		String region = addrCur.getString(
				addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
		String postalCode = addrCur.getString(
				addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
		String country = addrCur.getString(
				addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
		String type = addrCur.getString(
				addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
     	addrCur.close();
        
        data.array();
    	data.object()
    			.key("displayName").value(displayName)
    			.key("givenName").value(givenName)
    			.key("familyName").value(familyName)
    			.key("type").value(type)
    			.key("phoneNo").value(phoneNo)
    			.key("phoneType").value(phoneType)
    			.key("emailAddress").value(emailAddress)
    			.key("emailType").value(emailType)
    			.key("street").value(street)
    			.key("city").value(city)
    			.key("region").value(region)
    			.key("postalCode").value(postalCode)
    			.key("country").value(country)
    			.endObject();
    	data.endArray();
    }

}
