'''
Created on Sep 8, 2011
Based on Prefstore DB of James
'''

import MySQLdb
import logging
import ConfigParser
from time import *
import sys

class ViPhyDB( object ):
    ''' classdocs '''
    
    DB_NAME = 'ViPhyDB'
    TBL_ENTRY = 'tblEntry'
    TBL_DATA_CONNECTION = 'tblDataConneciton'
    TBL_CELL_LOCATION = 'tblCellLocation'
    TBL_WIFI = 'tblWifi'
    TBL_LOCATION = 'tblLocation'
    TBL_SIGNAL_STRENGTH = 'tblSignalStrength'
    TBL_SERVICE_STATE = 'tblServiceState'
    TBL_NEIGHBORING_CELL = 'tblNeighboringCell'
    TBL_USER_DETAILS = 'tblUserDetails'
    TBL_CALL = 'tblCall'
    TBL_SMS = 'tblSMS'
    TBL_CONTACT = 'tblContact'
    CONFIG_FILE = "viphytravel.cfg"
    SECTION_NAME = "ViPhyDB"
    
    
    #///////////////////////////////////////
    
    
    createQueries = { 
               
        TBL_ENTRY : """
            CREATE TABLE %s.%s(
            id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
            systemTime bigint NOT NULL,
            timezone varchar (256) NOT NULL,
            userId varchar(256) NOT NULL
            );
        """ % (DB_NAME, TBL_ENTRY ),
        
        TBL_DATA_CONNECTION : """
            CREATE TABLE %s.%s(
            id int AUTO_INCREMENT NOT NULL PRIMARY KEY,
            state varchar (256) NULL,
            networkType varchar(256) NULL,
            entryId bigint NOT NULL
            );
        """ % (DB_NAME, TBL_DATA_CONNECTION ),
        
        TBL_CELL_LOCATION : """
            CREATE TABLE %s.%s(
            id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
            type varchar(256) NULL,
            cid int NULL,
            lac int NULL,
            baseStationId int NULL,
            baseStationLatitude int NULL,
            baseStationLongitude int NULL,
            networkId int NULL,
            systemId int NULL,
            entryId bigint NOT NULL
            );
        """ % (DB_NAME, TBL_CELL_LOCATION ),
        
        TBL_WIFI : """
            CREATE TABLE %s.%s(
            id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
            ssid varchar(32) NULL,
            capabilities varchar(256) NULL,
            frequency int NULL,
            level int NULL,
            bssid varchar(256) NULL,
            entryId bigint NOT NULL
            );
        """ % (DB_NAME, TBL_WIFI ),
        
        TBL_LOCATION : """
            CREATE TABLE %s.%s(
            id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
            event varchar(256) NOT NULL,
            provider varchar(256) NOT NULL,
            accuracy float NULL,
            altitude float NULL,
            bearing float NULL,
            latitude float NULL,
            longitude float NULL,
            speed float NULL,
            time bigint NULL,
            extras varchar(256) NULL,
            status varchar(256) NULL,
            entryId bigint NOT NULL
            );
        """ % (DB_NAME, TBL_LOCATION ),
        
        TBL_SIGNAL_STRENGTH : """
            CREATE TABLE %s.%s(
            id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
            cdmaDbm int NULL,
            cdmaEcio int NULL,
            evdoDbm int NULL,
            evdoEcio int NULL,
            evdoSnr int NULL,
            gsmBitErrorRate int NULL,
            gsmSingalStrength int NULL,
            isGsm bit NULL,
            entryId bigint NOT NULL
            );
        """ % (DB_NAME, TBL_SIGNAL_STRENGTH ),
        
        TBL_SERVICE_STATE : """
            CREATE TABLE %s.%s(
            id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
            isManualSelection bit NULL,
            operatorAlphaLong nchar(16) NULL,
            operatorAlphaShort nchar(8) NULL,
            operatorNumeric nchar(6) NULL,
            roaming bit NULL,
            state varchar(256) NULL,
            entryID bigint NOT NULL
            );
        """ % (DB_NAME, TBL_SERVICE_STATE ),
        
        TBL_NEIGHBORING_CELL : """
            CREATE TABLE %s.%s(
            id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
            cid int NULL,
            lac int NULL,
            psc int NULL,
            rssi int NULL,
            networkType varchar(256) NULL,
            cellLocationId bigint NOT NULL,
            entryId bigint NOT NULL
            );
        """ % (DB_NAME, TBL_NEIGHBORING_CELL ),
        
        TBL_USER_DETAILS : """
            CREATE TABLE %s.%s (
            user_id varchar(256) NOT NULL,
            screen_name varchar(64),
            email varchar(256),
            phone_number varchar(20),
            PRIMARY KEY (user_id) )
            ENGINE=InnoDB DEFAULT CHARSET=latin1;
        """  % ( DB_NAME, TBL_USER_DETAILS ),   

        TBL_CALL : """
            CREATE TABLE %s.%s (
            id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
            number varchar(20),
            name varchar(256),
            numberType varchar(256),
            callType varchar(256),
            date bigint,
            duration bigint,
            contactId bigint,
            entryId bigint NOT NULL
            );
        """  % ( DB_NAME, TBL_CALL ),
        
        TBL_SMS : """
            CREATE TABLE %s.%s (
            id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
            type varchar(10),
            address varchar(20),
            time bigint,
            length bigint,
            contactId bigint,
            entryId bigint NOT NULL
            );
        """  % ( DB_NAME, TBL_SMS ),
        
        TBL_CONTACT : """
            CREATE TABLE %s.%s (
            id bigint AUTO_INCREMENT NOT NULL PRIMARY KEY,
            userId varchar(256) NOT NULL,
            displayName varchar(256),
            givenName varchar(256),
            familyName varchar(256),
            phoneNo varchar(20),
            phoneType varchar(256),
            emailAddress varchar(256),
            emailType varchar(256),
            street varchar(256),
            city varchar(256),
            region varchar(256),
            postalCode varchar(256),
            country varchar(256)
            );
        """  % ( DB_NAME, TBL_CONTACT )
        
    } 
    

    #///////////////////////////////////////
    
    
    def __init__( self, name = "ViPhyDB" ):
            
        #MysqlDb is not thread safe, so program may run more
        #than one connection. As such naming them is useful.
        self.name = name
        
        Config = ConfigParser.ConfigParser()
        Config.read( self.CONFIG_FILE )
        self.hostname = Config.get( self.SECTION_NAME, "hostname" )
        self.username =  Config.get( self.SECTION_NAME, "username" )
        self.password =  Config.get( self.SECTION_NAME, "password" )
        self.dbname = Config.get( self.SECTION_NAME, "dbname" )
        self.connected = False;

        
    #///////////////////////////////////////
    
        
    def connect( self ):
        
        logging.info( "%s: connecting to mysql database..." % self.name )

        self.conn = MySQLdb.connect( 
            host=self.hostname,
            user=self.username,
            passwd=self.password,
            db=self.dbname
        )
 
        self.cursor = self.conn.cursor( MySQLdb.cursors.DictCursor )
        self.connected = True
                    
                    
    #///////////////////////////////////////
    
    
    def reconnect( self ):
        logging.info( "%s: Database reconnection process activated:" % self.name );
        self.close()
        self.connect()
        

    #///////////////////////////////////////
          
                
    def commit( self ) : 
        self.conn.commit();
        
        
    #///////////////////////////////////////
        
          
    def close( self ) :
        
        logging.info( "%s: disconnecting from mysql database..." % self.name );
        self.cursor.close();
        self.conn.close()
                     
                            
    #////////////////////////////////////////////////////////////////////////////////////////////
    
    
    def check_tables( self ):
        
        logging.info( "%s: checking system table integrity..." % self.name );
        
        self.cursor.execute ( """
            SELECT table_name
            FROM information_schema.`TABLES`
            WHERE table_schema='%s'
        """ % self.DB_NAME )
        
        tables = [ row[ "table_name" ] for row in self.cursor.fetchall() ]
 
        if not self.TBL_ENTRY in tables : 
            self.create_table( self.TBL_ENTRY )
        if not self.TBL_DATA_CONNECTION in tables : 
            self.create_table( self.TBL_DATA_CONNECTION )
        if not self.TBL_CELL_LOCATION in tables : 
            self.create_table( self.TBL_CELL_LOCATION )
        if not self.TBL_WIFI in tables : 
            self.create_table( self.TBL_WIFI )
        if not self.TBL_LOCATION in tables : 
            self.create_table( self.TBL_LOCATION )
        if not self.TBL_SIGNAL_STRENGTH in tables : 
            self.create_table( self.TBL_SIGNAL_STRENGTH )
        if not self.TBL_SERVICE_STATE in tables : 
            self.create_table( self.TBL_SERVICE_STATE )
        if not self.TBL_NEIGHBORING_CELL in tables : 
            self.create_table( self.TBL_NEIGHBORING_CELL )
        if not self.TBL_USER_DETAILS in tables : 
            self.create_table( self.TBL_USER_DETAILS )
        if not self.TBL_CALL in tables : 
            self.create_table( self.TBL_CALL )
        if not self.TBL_SMS in tables : 
            self.create_table( self.TBL_SMS )
        if not self.TBL_CONTACT in tables : 
            self.create_table( self.TBL_CONTACT )
     
        self.commit();
        
        
    #///////////////////////////////////////
    
               
    def create_table( self, tableName ):
        logging.warning( 
            "%s: missing system table detected: '%s'" 
            % ( self.name, tableName ) 
        );
        
        if tableName in self.createQueries :
            
            logging.info( 
                "%s: --- creating system table '%s' " 
                % ( self.name, tableName )
            );  
              
            self.cursor.execute( self.createQueries[ tableName ] )


    #///////////////////////////////////////
              
                
    def insert_user( self, user_id ):
        
        if user_id:
            
            logging.info( 
                "%s %s: Adding user '%s' into database" 
                % ( self.name, "insert_user", user_id ) 
            );
            
            query = """
                INSERT INTO %s.%s 
                ( user_id, screen_name, email, phone_number ) 
                VALUES ( %s, null, null, null )
            """  % ( self.DB_NAME, self.TBL_USER_DETAILS, '%s' ) 

            self.cursor.execute( query, ( user_id ) )
            return True;
        
        else:
            logging.warning( 
                "%s %s: Was asked to add 'null' user to database" 
                % (  self.name, "insert_user", ) 
            );
            return False;

 
    #///////////////////////////////////////
    
    
    def insert_registration( self, user_id, screen_name, email, phone_number ):
            
        if ( user_id and screen_name and email and phone_number):
            
            logging.info( 
                "%s %s: Updating user '%s' registration in database" 
                % ( self.name, "insert_registration", user_id ) 
            );
            
            query = """
                UPDATE %s.%s SET screen_name = %s, email = %s, phone_number = %s WHERE user_id = %s
            """  % ( self.DB_NAME, self.TBL_USER_DETAILS, '%s', '%s', '%s', '%s' ) 

            self.cursor.execute( query, ( screen_name, email, phone_number, user_id ) )
            return True;
        
        else:
            logging.warning( 
                "%s %s: Registration requested with incomplete details" 
                % (  self.name, "insert_registration", ) 
            );
            return False;    

            
    #///////////////////////////////////////


    def fetch_user_by_id( self, user_id ) :

        if user_id :
            query = """
                SELECT * FROM %s.%s t where user_id = %s 
            """  % ( self.DB_NAME, self.TBL_USER_DETAILS, '%s' ) 
        
            self.cursor.execute( query, ( user_id, ) )
            row = self.cursor.fetchone()
            if not row is None:
                return row
            else :
                return None
        else :
            return None     
            
            
    #///////////////////////////////////////


    def fetch_user_by_email( self, email ) :

        if email :
            query = """
                SELECT * FROM %s.%s t where email = %s 
            """  % ( self.DB_NAME, self.TBL_USER_DETAILS, '%s' ) 
        
            self.cursor.execute( query, ( email, ) )
            row = self.cursor.fetchone()
            if not row is None:
                return row
            else :
                return None    
        else :
            return None     


    #///////////////////////////////////////


    def last_insert_id(self, tableName):
        if tableName:
            last_insert_id_query = """
                SELECT LAST_INSERT_ID() FROM %s.%s
                """ % ( self.DB_NAME, tableName )
            self.cursor.execute( last_insert_id_query )
            row = self.cursor.fetchone()
            insertId = row.get('LAST_INSERT_ID()')
            return insertId;
        else: return -1;


    #///////////////////////////////////////


    def insert_entry (self, userid, systemTime, timezone):
        
        if ( userid and systemTime and timezone ):
            logging.info( 
                "%s %s: Insert entry of user '%s' in database" 
                % ( self.name, "insert_entry", userid ) 
            );
            
            query = """
                INSERT INTO %s.%s
                ( userId, systemTime, timezone )
                VALUES ( %s, %s, %s )
            """  % ( self.DB_NAME, self.TBL_ENTRY, '%s', '%s', '%s' ) 

            self.cursor.execute( query, ( userid, systemTime, timezone ) )
            
            entryID = self.last_insert_id(self.TBL_ENTRY)
            return entryID;
        
        else:
            logging.warning( 
                "%s %s: Cannot insert new entry"
                % (  self.name, "insert_entry", ) 
            )
            return -1;


    #///////////////////////////////////////


    def insert_call (self, number , name, numberType, callType, 
                     date, duration, contactId, entryId):
        if ( number and name and numberType and callType and 
                     date and duration and contactId and entryId):
            logging.info( 
                "%s %s: Insert call of entry '%s' in database" 
                % ( self.name, "insert_call", entryId ) 
            );
            
            query = """
                INSERT INTO %s.%s
                (number , name, numberType, callType, 
                     date, duration, contactId, entryId)
                VALUES (%s, %s, %s, %s, 
                        %s, %s, %s, %s)
            """  % ( self.DB_NAME, self.TBL_CALL, '%s', '%s', '%s', '%s', 
                     '%s', '%s', '%s', '%s' ) 

            self.cursor.execute( query, ( number , name, numberType, callType, 
                                          date, duration, contactId, entryId ) )
            return True;
        
        else:
            logging.warning( 
                "%s %s: Cannot insert new call"
                % (  self.name, "insert_call", ) 
            );
            return False;


    #///////////////////////////////////////


    def insert_sms (self, type, address, time, length, contactId, entryId):
        if ( type and address and time and length and contactId and entryId ):
            logging.info( 
                "%s %s: Insert sms of entry '%s' in database" 
                % ( self.name, "insert_sms", entryId ) 
            );
            
            query = """
                INSERT INTO %s.%s
                (type, address, time, length, contactId, entryId)
                VALUES ( %s, %s, %s, %s, %s, %s )
            """  % ( self.DB_NAME, self.TBL_SMS, '%s', '%s', '%s', 
                     '%s', '%s', '%s' ) 

            self.cursor.execute( query, ( type, address, time, length, contactId, entryId ) )
            return True;
        
        else:
            logging.warning( 
                "%s %s: Cannot insert new sms"
                % (  self.name, "insert_sms", ) 
            );
            return False;


    #///////////////////////////////////////


    def insert_contact (self, userId, displayName, givenName, familyName, phoneNo, phoneType, 
                    emailAddress, emailType, street, city, region, postalCode, country):
        if (userId):
            logging.info( 
                "%s %s: Insert contact of user '%s' into database" 
                % ( self.name, "insert_contact", userId ) 
            );
            
            #prepare for checking duplicate, for null, use 'column is null' instead of 'column = null'
            if userId: op1='= %s'
            else: op1='is %s'
            if displayName: op2='= %s'
            else: op2='is %s'
            if givenName: op3='= %s'
            else: op3='is %s'
            if familyName: op4='= %s'
            else: op4='is %s'
            if phoneNo: op5='= %s'
            else: op5='is %s'
            if phoneType: op6='= %s'
            else: op6='is %s'
            if emailAddress: op7='= %s'
            else: op7='is %s'
            if emailType: op8='= %s'
            else: op8='is %s'
            if street: op9='= %s'
            else: op9='is %s'
            if city: op10='= %s'
            else: op10='is %s'
            if region: op11='= %s'
            else: op11='is %s'
            if postalCode: op12='= %s'
            else: op12='is %s'
            if country: op13='= %s'
            else: op13='is %s'

            #check for duplicate contact
            duplicate_check_query = """
                SELECT id FROM %s.%s
                WHERE userId %s AND displayName %s AND givenName %s 
                    AND familyName %s AND phoneNo %s AND phoneType %s 
                    AND emailAddress %s AND emailType %s AND street %s 
                    AND city %s AND region %s AND postalCode %s 
                    AND country %s
            """  % ( self.DB_NAME, self.TBL_CONTACT, op1, op2, op3, op4, op5, op6, op7,
                     op8, op9, op10, op11, op12, op13 )
            
            self.cursor.execute( duplicate_check_query, 
                                 ( userId, displayName, givenName, familyName, 
                                   phoneNo, phoneType, emailAddress, emailType, 
                                   street, city, region, postalCode, country ) )
            result = self.cursor.fetchone()
            #if duplicate, return contact id
            if not result is None:
                return result.get('id');
            
            query = """
                INSERT INTO %s.%s
                (userId, displayName, givenName, familyName, phoneNo, phoneType, 
                    emailAddress, emailType, street, city, region, postalCode, country)
                VALUES (%s, %s, %s, %s, %s, %s, 
                        %s, %s, %s, %s, %s, %s, %s)
            """  % ( self.DB_NAME, self.TBL_CONTACT, '%s', '%s', '%s', 
                     '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s' ) 
    
            self.cursor.execute( query, ( userId, displayName, givenName, familyName, 
                                          phoneNo, phoneType, emailAddress, emailType, 
                                          street, city, region, postalCode, country ) )
            
            contactID = self.last_insert_id(self.TBL_CONTACT)
            return contactID;
        else:
            logging.warning( 
                "%s %s: Cannot insert new contact"
                % (  self.name, "insert_contact", ) 
            );
            return -1;


    #///////////////////////////////////////


    def insert_location( self, event, provider, entry_id ):
        
        if event and provider and entry_id:
            
            logging.info( 
                "%s %s: Insert location of entry '%s' into database" 
                % ( self.name, "insert_location", entry_id ) 
            );
            
            query = """
                INSERT INTO %s.%s 
                ( event, provider, entryId ) 
                VALUES ( %s, %s, %s )
            """  % ( self.DB_NAME, self.TBL_LOCATION, '%s', '%s', '%s' ) 

            self.cursor.execute( query, ( event, provider, entry_id ) )
            locationId = self.last_insert_id(self.TBL_LOCATION)
            return locationId;
        
        else:
            logging.warning( 
                "%s %s: Cannot insert location to database" 
                % (  self.name, "insert_location", ) 
            );
            return -1;

 
    #///////////////////////////////////////
    
    
    def update_location(self, accuracy=None, altitude=None, bearing=None, latitude=None, longitude=None, 
                        speed=None, time=None, extras=None, status=None, locationId=None):
        if locationId:
            query = """
                UPDATE %s.%s 
                SET accuracy = %s, altitude = %s, bearing = %s, latitude = %s, 
                    longitude = %s, speed = %s, time = %s, extras = %s, status = %s
                WHERE id = %s 
            """  % ( self.DB_NAME, self.TBL_LOCATION, '%s', '%s', '%s', '%s', '%s', 
                     '%s', '%s', '%s', '%s', '%s' ) 

            self.cursor.execute( query, ( accuracy, altitude, bearing, latitude, 
                                          longitude, speed, time, extras, status, locationId ) )
            