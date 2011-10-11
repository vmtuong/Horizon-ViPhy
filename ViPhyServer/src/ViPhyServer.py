'''
Created on Sep 8, 2011
Based on Prefstore Server of James
'''

import logging
import json
import OpenIDManager
import ProcessingModule
import ViPhyDB
from bottle import * #@UnusedWildImport
import MySQLdb
import gzip
import StringIO

#//////////////////////////////////////////////////////////
# DATAWARE WEB-API CALLS
#//////////////////////////////////////////////////////////
 
@route( '/invoke_request', method = "POST")
def invoke_request():
    
    try:
        access_token = request.forms.get( 'access_token' )
        jsonParams = request.forms.get( 'parameters' )
        result = pm.invoke_request( 
            access_token, 
            jsonParams 
        )

        return result
    
    except Exception, e:
        raise e
     

#///////////////////////////////////////////////
 
 
@route( '/permit_request', method = "POST" )
def permit_request():

    #TODO: Worth checking why this takes so long to parse
    #TODO: the input parameters into the request.forms object.
    #TODO: Long post parameters (queries) are taking ages.   
    try:
        user_id = request.forms.get( 'user_id' )
        catalog_secret = request.forms.get( 'catalog_secret' )
        client_id = request.forms.get( 'client_id' )
        jsonScope = request.forms.get( 'scope' )
        result = pm.permit_request( 
            catalog_secret, 
            client_id,  
            user_id, 
            jsonScope 
        )
        
        #the result, if successful, will include an access_code
        return result
    
    except Exception, e:
        raise e
          

#///////////////////////////////////////////////
 
 
@route( '/revoke_request', method = "POST")
def revoke_request():
    
    try:
        access_token = request.forms.get( 'access_token' )
        catalog_secret = request.forms.get( 'catalog_secret' )
        user_id = request.forms.get( 'user_id' )

        result = pm.revoke_request( 
            user_id,
            catalog_secret,
            access_token, 
        )
        
        return result
    
    except Exception, e:
        raise e


    
#//////////////////////////////////////////////////////////
# OPENID SPECIFIC WEB-API CALLS
#//////////////////////////////////////////////////////////


@route( '/login', method = "GET" )
def openIDlogin():

    try: 
        username = request.GET[ 'username' ]
    except: 
        username = None
    
    try: 
        provider = request.GET[ 'provider' ]
    except: 
        return error( "provider must be supplied" )
    
    url = OpenIDManager.process(
        realm=REALM,
        return_to=REALM + "/checkauth",
        provider=provider,
        username=username
    )
    
    redirect( url )


#///////////////////////////////////////////////

 
@route( "/checkauth", method = "GET" )
def authenticate():
    
    o = OpenIDManager.Response( request.GET )
    
    #check to see if the user logged in succesfully
    if ( o.is_success() ):
        
        user_id = o.get_user_id()
         
        #if so check we received a viable claimed_id
        if user_id:
            
            try:
                user = safetyCall( lambda: viphydb.fetch_user_by_id( user_id ) )
                
                #if this is a new user add them
                if ( not user ):
                    viphydb.insert_user( o.get_user_id() )
                    viphydb.commit()
                    screen_name = None
                else :
                    screen_name = user[ "screen_name" ]
                
                #if they have no "screen_name" it means that they
                #haven't registered an account yet    
                if ( not screen_name ):
                    json = '{"user_id":"%s","screen_name":null}' \
                        % ( user_id, )
                    
                else:
                    json = '{"user_id":"%s","screen_name":"%s"}' \
                        % ( user_id, user[ "screen_name" ] )
                     
                response.set_cookie( EXTENSION_COOKIE, json )
            
            except Exception, e:
                return error( e )
            
            
        #if they don't something has gone horribly wrong, so mop up
        else:
            delete_authentication_cookie()

    #else make sure the user is still logged out
    else:
        delete_authentication_cookie()
        
    redirect( ROOT_PAGE )

    
#///////////////////////////////////////////////


@route('/logout')
def logout():
    delete_authentication_cookie()
    redirect( ROOT_PAGE )
    
        
#///////////////////////////////////////////////
 
         
def delete_authentication_cookie():
    response.set_cookie( 
        key=EXTENSION_COOKIE,
        value='',
        max_age=-1,
        expires=0
    )
            

#//////////////////////////////////////////////////////////
# VIPHY SPECIFIC WEB-API CALLS
#//////////////////////////////////////////////////////////


class LoginException ( Exception ):
    def __init__(self, msg):
        self.msg = msg


#///////////////////////////////////////////////  


class RegisterException ( Exception ):
    """Base class for RegisterException in this module."""
    pass

    
#///////////////////////////////////////////////   
    

@route( '/', method = "GET" )
def web_main( ):
    
    try:
        user_details = check_login()
        if ( user_details ):
            return "<script type=\"text/javascript\">alert(\"%s\");</script>\"Welcome to the viphy, user: %s\"" % ( user_details, user_details, )
        else:
            return '''<html><body><p style="text-align:center;">
                <a href="/login?provider=google">Login via google account</a>
                </p></body></html>'''
    
    except RegisterException, e:
        redirect( "/register" )
        
    except LoginException, e:
        return error( e.msg )
    
    except Exception, e:
        return error( e )
        

#///////////////////////////////////////////////


def valid_email( str ):
    return re.search( "^[A-Za-z0-9%._+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,4}$", str )


#///////////////////////////////////////////////


def valid_name( str ):
    return  re.search( "^[A-Za-z0-9 ']{3,64}$", str )


#///////////////////////////////////////////////


def valid_phone_number( str ):
    return  re.search( "^[0-9+\- ]{3,20}$", str )


#///////////////////////////////////////////////


@route( '/register', method = "GET" )
def register():
    
    try:
        user_id = extract_user_id()
    except LoginException, e:
        return error( e.msg )
    except Exception, e:
        return error( e )
    
    errors = {}
    
    #if the user has submitted registration info, parse it
    try: 
        request.GET[ "submit" ]
        submission = True;
    except:
        submission = False
        
    if ( submission ): 
        #validate the screen name supplied by the user
        try:
            screen_name = request.GET[ "screen_name" ]
            if ( not valid_name( screen_name ) ):
                errors[ 'screen_name' ] = "Must be 3-64 legal characters"    
        except:
            errors[ 'screen_name' ] = "You must supply a valid screen name"
    
        #validate the email address supplied by the user
        try:
            email = request.GET[ "email" ]
            if ( not valid_email( email ) ):
                errors[ 'email' ] = "The supplied email address is invalid"
            else: 
                match = viphydb.fetch_user_by_email( email )
                if ( not match is None ):
                    errors[ 'email' ] = "That email has already been taken"
        except:
            errors[ 'email' ] = "You must supply a valid email"
            
        try:
            phone_number = request.GET[ "phone_number" ]
            if ( not valid_phone_number( phone_number ) ):
                errors[ 'phone_number' ] = "Must be 3-20 legal characters"    
        except:
            errors[ 'phone_number' ] = "You must supply a valid phone_number"

        #if everything is okay so far, add the data to the database    
        if ( len( errors ) == 0 ):
            try:
                viphydb.insert_registration( user_id, screen_name, email, phone_number)
                viphydb.commit()
            except Exception, e:
                return error( e )

            redirect( ROOT_PAGE )

    #if this is the first visit to the page, or there are errors
    if ( errors ):
        return  "registration: %s " % ( errors, )
    else:
        return '''<h1>Register information</h1> <br />
            <form method="get">
            <input type="hidden" name="submit" /> <br />
            Name: <input type="text" name="screen_name" /> <br />
            Email: <input type="text" name="email" /> <br />
            Phone number: <input type="text" name="phone_number" /> <br />
            <input type="submit" value="Submit" />
            </form>'''
        

#///////////////////////////////////////////////


@route( '/error', method = "GET" )
def error( e ):
    return "error: %s" % ( e )

      
#///////////////////////////////////////////////  
    
    
def extract_user_id():
    
    cookie = request.get_cookie( EXTENSION_COOKIE )
        
    #is the user logged in? First check we have a cookie...
    if cookie:
        #and that it contains suitably formatted data
        try:
            data = json.loads( cookie )
        except:
            delete_authentication_cookie()
            raise LoginException( "Your login data is corrupted. Resetting." )
        
        #and then that it contains a valid user_id
        try:
            user_id =  data[ "user_id" ]
            return user_id
        except:
            delete_authentication_cookie()
            raise LoginException( "You are logged in but have no user_id. Resetting." )
    else:
        return None

  
#///////////////////////////////////////////////  
    
    
def check_login():

    #first try and extract the user_id from the cookie. 
    #n.b. this can generate LoginExceptions
    user_id = extract_user_id()
    
    if ( user_id ) :
        
        #we should have a record of this id, from when it was authenticated
        user = safetyCall( lambda: viphydb.fetch_user_by_id( user_id ) )
        if ( not user ):
            delete_authentication_cookie()
            raise LoginException( "We have no record of the id supplied. Resetting." )
        
        #and finally lets check to see if the user has registered their details
        if ( user[ "screen_name" ] is None ):
            raise RegisterException()
        return user_id
        
    #if the user has made it this far, their page can be processed accordingly
    else:
        return False   
    
    
#///////////////////////////////////////////////


#TODO: this should be part of the (private) database api
def safetyCall( func ):
    
    """
        This is a function that protects from mysqldb timeout,
        performing one reconnection attempt if the provided lambda 
        function generates a mysqldb error. This is necessary because
        mysqldb does not currently provided an auto_reconnect facility.
    """
    try:
        return func();
    except MySQLdb.Error, e:
        logging.error( "%s: db error %s" % ( "viphy", e.args[ 0 ] ) )
        viphydb.reconnect()
        return func();

    
#///////////////////////////////////////////////


@route( '/submit', method = "POST" )
def submit():
    """ 
        A log Entry is packaged as a json message of the form
        {userId, identifier, timestamp, location, data}    
    """
    
    try:
        #extract the gzip file
#        f = request.files.get('userfile')
#        raw = f.file.read()
#        stream = StringIO.StringIO(raw)
        
        stream = request.body
        gzipper = gzip.GzipFile( fileobj = stream )
        content = gzipper.read()
        
        lines = content.split('\n')
        line = lines[0]
        
        #convert the data into a json object
        data = json.loads(line)
        user_id = data.get('userId')
                
    except ValueError, e:
        logging.error( 
            "%s: JSON validation error - %s" 
            % ( "viphy", e ) 
        )          
        return "{'success':false,'cause':'JSON error'}"
    
    # Log that we have received package
    logging.debug( 
        "%s: Message from '%s' successfully unpacked" 
        % ( "viphy", user_id ) 
    )
    
       
    try:    
        # First db interaction of this method so safety check in case 
        # a mysql timeout has occurred since we last accessed the db.
        user = safetyCall( lambda: viphydb.fetch_user_by_id( user_id ) )
    except Exception, e: 
        logging.error( 
            "%s: User Lookup Error for Message from '%s'" 
            % ( "viphy", e ) 
        )          
        return "{'success':false,'cause':'User Lookup error'}"   
    
    
    # Authenticate the user, using the supplied key
    if user:
        
        logging.debug( 
            "%s: Message successfully authenticated as belonging to '%s'" 
            % ( "viphy", user[ "screen_name" ]  ) 
        )

        # And finally process it into the database
        try:
            processContent(content);
            return "{'success':true}"
        except:
            logging.info( 
                "%s: Processing Failure for message from '%s'" 
                % ( "viphy", user )
            ) 
            return "{'success':false,'cause':'Processing error'}"
    
    else:
        logging.warning( 
            "%s: Identification Failure for message from '%s'" 
            % ( "viphy", user_id ) 
        )
        return "{'success':false,'cause':'Authentication error'}"

    
#///////////////////////////////////////////////


def processContent(content):
    for line in content.split('\n'):
        if line != "":
            data = json.loads(line)
            user_id = data.get('userId')
            processEntry(user_id, data)
    viphydb.commit()
    logging.info( "Process package successfully for '%s'" % (user_id) )


#///////////////////////////////////////////////


def processEntry(user_id, data):
    # separate timestamp
    timestamp = data.get('timestamp')
    systemTime = timestamp.get('systemTime')
    timezone = timestamp.get('timezone')
    
    #insert new entry
    entry_id = viphydb.insert_entry(user_id, systemTime, timezone);
    
    decodedData = data.get('data')
    decodedData = base64.b64decode(decodedData)
    decodedData = json.loads(decodedData)
    
    identifier = data.get('identifier').get('uniqueID')
    
    if (identifier == "aethers.notebook.logger.managed.callhistory.CallHistoryLogger"):
        insertCallHistory(decodedData, entry_id, user_id)
    elif (identifier == "aethers.notebook.logger.managed.message.SMSLogger"):
        insertSMS(decodedData, entry_id, user_id)
    elif (identifier == "aethers.notebook.logger.managed.position.PositionLogger"):
        insertLocation(decodedData, entry_id)

    
#///////////////////////////////////////////////


def insertCallHistory(data, entry_id, user_id):
    contact = data.get('contact')[0]
    contact_id = insertContact(contact, user_id)
    number = data.get('number')
    name = data.get('name')
    numberType = data.get('numberType')
    callType = data.get('callType')
    date = data.get('date')
    duration = data.get('duration')
    viphydb.insert_call(number , name, numberType, callType, 
                     date, duration, contact_id, entry_id)

    
#///////////////////////////////////////////////


def insertSMS(data, entry_id, user_id):
    contact = data.get('contact')[0]
    contact_id = insertContact(contact, user_id)
    type = data.get('type')
    address = data.get('address')
    time = data.get('time')
    length = data.get('length')
    viphydb.insert_sms(type, address, time, length, contact_id, entry_id);

    
#///////////////////////////////////////////////


def insertContact(contact, user_id):
    displayName = contact.get('displayName')
    givenName = contact.get('givenName')
    familyName = contact.get('familyName')
    phoneNo = contact.get('phoneNo')
    phoneType = contact.get('phoneType')
    emailAddress = contact.get('emailAddress')
    emailType = contact.get('emailType')
    street = contact.get('street')
    city = contact.get('city')
    region = contact.get('region')
    postalCode = contact.get('postalCode')
    country = contact.get('country')
    return viphydb.insert_contact(user_id, displayName, givenName, familyName, 
                                  phoneNo, phoneType, emailAddress, emailType, 
                                  street, city, region, postalCode, country)

    
#///////////////////////////////////////////////


def insertLocation(data, entry_id):
    event = data.get('event')
    provider = data.get('provider')
    locationId1 = viphydb.insert_location(event, provider, entry_id)
    
    if event == "statusChanged":
        status1 = data.get('status')
        extras1 = data.get('extras')
        viphydb.update_location(status = status1, extras = extras1, locationId = locationId1)
    elif event == "locationChanged":
        data = data.get('location')
        accuracy1 = data.get('accuracy')
        altutube1 = data.get('altitude')
        bearing1 = data.get('bearing')
        latitude1 = data.get('latitude')
        longitude1 = data.get('longitude')
        speed1 = data.get('speed')
        time1 = data.get('time')
        extras1 = data.get('extras')
        extras1 = json.dumps(extras1)
        viphydb.update_location(accuracy = accuracy1, altitude = altutube1, bearing = bearing1, 
                                latitude = latitude1, longitude = longitude1, speed = speed1, 
                                time = time1, extras = extras1, locationId = locationId1)

           
#//////////////////////////////////////////////////////////
# MAIN FUNCTION
#//////////////////////////////////////////////////////////


if __name__ == '__main__' :
    
    #-------------------------------
    # setup logging
    #-------------------------------
    
    logging.basicConfig( 
        format= '%(asctime)s [%(levelname)s] %(message)s', 
        datefmt='%Y-%m-%d %I:%M:%S',
        #filename='logs/viphy.log',
        level=logging.DEBUG 
    )

    #-------------------------------
    # constants
    #-------------------------------
    EXTENSION_COOKIE = "logged_in"
    PORT = 8080
#    REALM = "http://79.125.109.229:8080"
    REALM = "http://0.0.0.0:8080"
    ROOT_PAGE = "/"
        
    #-------------------------------
    # initialization
    #-------------------------------
    try:    
        pm = ProcessingModule.ProcessingModule()
    except Exception, e:
        logging.error( "Processing Module failure: %s" % ( e, ) )
        exit()

    viphydb = ViPhyDB.ViPhyDB()  
    viphydb.connect()
    viphydb.check_tables()
    
    logging.info( "database initialisation completed... [SUCCESS]" );
        
    try:
        debug( True )
        run( host='0.0.0.0', port=PORT )
    except Exception, e:
        logging.error( "Web Server Startup failed: %s" % ( e, ) )
        exit()
