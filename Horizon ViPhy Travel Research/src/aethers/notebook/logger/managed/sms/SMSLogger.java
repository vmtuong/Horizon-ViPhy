package aethers.notebook.logger.managed.sms;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONStringer;

import aethers.notebook.core.LoggerServiceIdentifier;
import aethers.notebook.logger.managed.PushLogger;
import aethers.notebook.util.Logger;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.PhoneLookup;

/**
 * Logger that using content observer to keep track of ingoing and outgoing sms
 *
 */
public class SMSLogger extends PushLogger<SMSObserver> {
	
	private static final LoggerServiceIdentifier IDENTIFIER = new LoggerServiceIdentifier(
            "aethers.notebook.logger.managed.message.SMSLogger");
    {
        IDENTIFIER.setConfigurable(false);
        IDENTIFIER.setDescription("Logs the SMS ingoing and outgoing");
        IDENTIFIER.setName("Message Logger");
        IDENTIFIER.setServiceClass(SMSLogger.class.getName());
        IDENTIFIER.setVersion(1);
    }

    private static final String ENCODING = "UTF-8";
    
    private static Logger logger = Logger.getLogger(SMSLogger.class);

	@Override
	protected SMSObserver preLogging() {
		//register a content observer
        SMSObserver smsObserver = new SMSObserver(this);
        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsObserver);
        return smsObserver;
	}

	@Override
	protected void postLogging(SMSObserver smsObserver) {
		//unregister content observer
        getContentResolver().unregisterContentObserver(smsObserver);
	}
	
	protected void querySMS() {
        Uri uriSMS = Uri.parse("content://sms/");
        Cursor cur = getContentResolver().query(uriSMS, null, null, null, null);
        cur.moveToNext(); // this will make it point to the first record, which is the last SMS sent
        String body = cur.getString(cur.getColumnIndex("body")); //content of sms
        String address = cur.getString(cur.getColumnIndex("address")); //phone num
        String time = cur.getString(cur.getColumnIndex("date")); //date
        String protocol = cur.getString(cur.getColumnIndex("protocol")); //protocol
        if (protocol == null)
        	protocol = "outbox";
        else protocol = "inbox";
        
        JSONStringer data = new JSONStringer();
        try {
			data.object()
					.key("type").value(protocol)
					.key("address").value(address)
					.key("time").value(time)
					.key("length").value(body.length()); //record the length, not the content
			data.key("contact");
			writeContactToJSON(data, address);
	        data.endObject();
		} catch (JSONException e) {
			logger.error("Unable to log sms.");
		}
		
		if(aethersNotebook != null)
			try {
				aethersNotebook.log(IDENTIFIER, data.toString().getBytes(ENCODING));
			} catch (RemoteException e) {
				logger.error("Unable to log sms: remote exception.");
			} catch (UnsupportedEncodingException e) {
				logger.error("Unable to log sms: unsupport encoding.");
			}
	}
	
	private void writeContactToJSON (JSONStringer data, String number) throws JSONException
    {
    	long id = -1;
    	String displayName = null;
    	String givenName = null;
    	String familyName = null;
    	String phoneNo = null;
    	String phoneType = null;
    	String emailAddress = null;
    	String emailType = null;
    	String street = null;
		String city = null;
		String region = null;
		String postalCode = null;
		String country = null;
		String type = null;
    	
    	int i;
    	//get id and name from number from contact list
    	Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] projection = new String[]{ PhoneLookup._ID, PhoneLookup.DISPLAY_NAME };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        
        if (cursor.getCount() == 0)
        	phoneNo = number;
        else
        {
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
	        if (nameCur.getCount() > 0) 
	        {
		        nameCur.moveToFirst();
		        i = nameCur.getColumnIndex(CommonDataKinds.StructuredName.GIVEN_NAME);
		        givenName = nameCur.getString(i);
		        i = nameCur.getColumnIndex(CommonDataKinds.StructuredName.FAMILY_NAME);
		        familyName = nameCur.getString(i);
		        nameCur.close();
	        }
	        
	        //get phone number
	        Cursor phoneCur = getContentResolver().query(
	        		CommonDataKinds.Phone.CONTENT_URI, 
	        		null, 
	        		CommonDataKinds.Phone.CONTACT_ID +" = ?", 
	        		new String[]{ String.valueOf(id) }, 
	        		null);
	        if (phoneCur.getCount() > 0)
	        {
		        phoneCur.moveToFirst();
		     	i = phoneCur.getColumnIndex(CommonDataKinds.Phone.NUMBER);
		     	phoneNo = phoneCur.getString(i);
		     	i = phoneCur.getColumnIndex(CommonDataKinds.Phone.TYPE);
		     	int phoneTypeInt = phoneCur.getInt(i);
		     	phoneType = (String) CommonDataKinds.Phone.getTypeLabel(this.getResources(), phoneTypeInt, "");
	        }
		    phoneCur.close();
	        
	        //get email
	     	Cursor emailCur = getContentResolver().query(
	        		CommonDataKinds.Email.CONTENT_URI, 
	        		null, 
	        		CommonDataKinds.Email.CONTACT_ID +" = ?", 
	        		new String[]{ String.valueOf(id) }, 
	        		null);
	     	if (emailCur.getCount() > 0)
	     	{
		     	emailCur.moveToFirst();
		     	i = emailCur.getColumnIndex(CommonDataKinds.Email.DATA);
		     	emailAddress = emailCur.getString(i);
		     	i = emailCur.getColumnIndex(CommonDataKinds.Email.TYPE);
		     	int emailTypeInt = emailCur.getInt(i);
		     	emailType = (String) CommonDataKinds.Email.getTypeLabel(this.getResources(), emailTypeInt, "");
	     	}
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
	    	if (addrCur.getCount() > 0)
	    	{
		    	addrCur.moveToFirst();
				street = addrCur.getString(
						addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
				city = addrCur.getString(
						addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
				region = addrCur.getString(
						addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
				postalCode = addrCur.getString(
						addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
				country = addrCur.getString(
						addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
				type = addrCur.getString(
						addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
	    	}
	     	addrCur.close();
        }
        
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
