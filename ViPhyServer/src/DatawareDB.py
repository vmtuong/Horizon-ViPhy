'''    
Created on 12 April 2011
@author: jog
'''

import MySQLdb
import logging
import ConfigParser
import hashlib

class DatawareDB( object ):
    ''' classdocs '''
    
    DB_NAME = 'ViPhyDB'
    TBL_DATAWARE_QUERIES = 'tblDatawareQueries'
    TBL_DATAWARE_SECRETS = 'tblDatawareSecrets'
    CONFIG_FILE = "viphytravel.cfg"
    SECTION_NAME = "DatawareDB"
    
    
    #///////////////////////////////////////

  
    createQueries = { 
               
        TBL_DATAWARE_QUERIES : """
            CREATE TABLE %s.%s (
            access_token varchar(256) NOT NULL,
            client_id varchar(256) NOT NULL,
            user_id varchar(256) NOT NULL,
            expiry_time int(11) unsigned NOT NULL,
            query text NOT NULL,
            checksum varchar(256) NOT NULL,
            PRIMARY KEY (access_token) USING BTREE,
            UNIQUE KEY (client_id,user_id,checksum) )
            ENGINE=InnoDB DEFAULT CHARSET=latin1;
        """  % ( DB_NAME, TBL_DATAWARE_QUERIES ),
       
        TBL_DATAWARE_SECRETS : """ 
            CREATE TABLE %s.%s (
            user_id varchar(256) NOT NULL,
            catalog_secret varchar(256) NOT NULL,
            PRIMARY KEY (user_id) ) 
            ENGINE=InnoDB DEFAULT CHARSET=latin1;
        """  % ( DB_NAME, TBL_DATAWARE_SECRETS ),            
    } 
    

    #///////////////////////////////////////
    
    
    def __init__( self, name = "DatawareDB" ):
            
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
                     
   
    #///////////////////////////////////////
    
        
    def checkTables( self ):
        
        logging.info( "%s: checking system table integrity..." % self.name );
        
        self.cursor.execute ( """
            SELECT table_name
            FROM information_schema.`TABLES`
            WHERE table_schema='%s'
        """ % self.DB_NAME )
        
        tables = [ row[ "table_name" ] for row in self.cursor.fetchall() ]
 
        if not self.TBL_DATAWARE_QUERIES in tables : 
            self.createTable( self.TBL_DATAWARE_QUERIES )
        if not self.TBL_DATAWARE_SECRETS in tables : 
            self.createTable( self.TBL_DATAWARE_SECRETS )
        self.commit();
        
        
    #///////////////////////////////////////
    
               
    def createTable( self, tableName ):
        
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

      
    #////////////////////////////////////////////////////////////////////////////////////////////


    def insert_request( self, access_token, client_id, user_id, expiry_time, query_code ):
       
        #create a SHA checksum for the file
        checksum = hashlib.sha1( query_code ).hexdigest()
        
        query = """
             INSERT INTO %s.%s VALUES ( %s, %s, %s, %s, %s, %s )
        """  % ( self.DB_NAME, self.TBL_DATAWARE_QUERIES, '%s', '%s', '%s', '%s', '%s', '%s' ) 
        
        #client_id, user_id and checksum must be unique, to prevent duplicate queries
        self.cursor.execute( 
            query, ( 
                access_token, 
                client_id, 
                user_id, 
                expiry_time, 
                query_code, 
                checksum 
            ) 
        )
        self.commit()
        
        
    
    #///////////////////////////////////////////////
    
    
    def delete_request( self, access_token, user_id ):

        query = """
             DELETE FROM %s.%s WHERE access_token = %s AND user_id = %s 
        """  % ( self.DB_NAME, self.TBL_DATAWARE_QUERIES, '%s', '%s' ) 

        self.cursor.execute( query, ( access_token, user_id ) )
        self.commit()
                
        #how many rows have been affected?
        if ( self.cursor.rowcount == 0 ) : 
            return False
        else :
            return True 
        
    
    
    #///////////////////////////////////////////////
    
    
    def fetch_request( self, access_token ):
        
        query = """
            SELECT * FROM %s.%s WHERE access_token = %s
        """  % ( self.DB_NAME, self.TBL_DATAWARE_QUERIES, '%s' ) 
        self.cursor.execute( query, access_token )
        row = self.cursor.fetchone()
        return row
    
    
    #///////////////////////////////////////////////


    def update_tfidf( self ):
        
        f = open( 'doc_similarity.py', 'r' )
        code = f.read()
        
        query = """
            UPDATE %s.%s SET query=%s WHERE access_token=4444
        """  % ( self.DB_NAME, self.TBL_DATAWARE_QUERIES, '%s' ) 
        self.cursor.execute( query, code )
        self.commit()


    #////////////////////////////////////////////////////////////////////////////////////////////
    
    
    def authenticate( self, user_id, catalog_secret ) :
        
        if user_id and catalog_secret:
            query = """
                SELECT 1 FROM %s.%s WHERE user_id = %s AND catalog_secret = %s  
            """  % ( self.DB_NAME, self.TBL_DATAWARE_SECRETS, '%s', '%s' ) 


            self.cursor.execute( query, ( user_id, catalog_secret ) )
            row = self.cursor.fetchone()

            if ( row is None ):
                return False
            else:    
                return True
        else:    
            return False

