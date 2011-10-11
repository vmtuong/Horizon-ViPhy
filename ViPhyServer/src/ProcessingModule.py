"""
Created on 12 April 2011
@author: jog
"""

from new import * #@UnusedWildImport
import json
import base64
import random
import __builtin__
import sys
import DatawareDB
import MySQLdb
import hashlib
import logging
 
    
#TODO: SAFETY CALLS ANYONE?
    
#///////////////////////////////////////////////


class ProcessingModule( object ) :

    #set of commands that are permissable within the sandbox's e
    #excecution environment (list borrowed from the "CAPE" sandbox )
    ALLOWED_BUILTINS = [
        'abs', 'bool', 'callable', 'chr', 'cmp', 'coerce', 'complex', 
        'dict', 'divmod', 'enumerate', 'filter', 'float', 'getattr', 
        'hasattr', 'hash', 'hex', 'int', 'intern', 'isinstance',
        'issubclass', 'iter', 'len', 'list', 'locals', 'long', 'map',
        'max', 'min', 'oct', 'ord', 'pow', 'range', 'reduce', 'repr',
        'round', 'slice', 'str', 'sum', 'tuple', 'type', 'unichr',
        'unicode', 'xrange', 'zip'
    ]
    
    #///////////////////////////////////////////////
    
    
    def __init__( self ):
        self.db = DatawareDB.DatawareDB()
        self.db.connect()
        self.db.checkTables()
        
        self.sandbox_builtins = __builtin__.__dict__.copy()
        
        for command in __builtin__.__dict__ :
            if command not in self.ALLOWED_BUILTINS :
                del self.sandbox_builtins[ command ]
        
        
    
    #///////////////////////////////////////////////
        
        
    def __del__(self):
        if self.db.connected: 
            self.db.close(); 
        
        
    #///////////////////////////////////////////////


    def format_process_failure( self, error, msg ):
        
        json_response = { 
            'success': False,
            'error':{
                'type':error,
                'message':msg
            }
        } 
        
        return json.dumps( json_response );
        
        
    #///////////////////////////////////////////////
    
    
    def format_process_success( self, result = None ):
        
        if ( result ) :
            json_response = { 'success': True, 'return': result }
        else : 
            json_response = { 'success': True }
        
        #TODO: make sure this ignores an empty return
        return json.dumps( json_response );
    
     
    #///////////////////////////////////////////////
    
    
    def invoke_request( self, access_token, jsonParams ):
        
        if access_token is None :
            return self.format_process_failure(
                "AccessException",
                "Access token has not been supplied"
            ) 

        try:
            parameters = json.loads( jsonParams ) if jsonParams else {}
        except:
            return self.format_process_failure(
                "AccessException",
                "incorrectly formatted JSON parameters"
            ) 
    
        try:
            request = self.db.fetch_request( access_token )
            if request is None:
                return self.format_process_failure(
                    "AccessException",
                    "Invalid access token"
                )
             
            #obtain relevent info from the request object    
            user = request[ "user_id" ]
            query = request[ "query" ]
                        
        except:
            return self.format_process_failure(
                "AccessException",
                "Database problems are currently being experienced"
            ) 

        try:
            sandbox = self.setup_sandbox( user, query )

        #TODO: Should probably log exceptions like these                           
        except Exception, e:
            return self.format_process_failure(
                "ProcessingException",
                "Compile-time failure - %s:%s" % 
                ( type( e ).__name__,  e )
            ) 

        #finally invoke the function
        try:
            result = sandbox.run( parameters )
            return self.format_process_success( result )
        
        #and catch any problems that occur in processing
        except:
           
            return self.format_process_failure(
                "ProcessingException",
                "Run-time failure - %s " % str( sys.exc_info() )
            ) 

            
    #///////////////////////////////////////////////
    
    
    def setup_sandbox( self, user, query ) :
        
        #setup sandbox module for running query in
        #TODO: THIS NEEDS A LOT MORE TIGHTENING UP! 
        sandbox = module( "sandbox" )
        #sandbox.__dict__[ '__builtins__' ] = self.sandbox_builtins
        sandbox.db = self.db
        
        #setup constants available to the query
        sandbox.TOTAL_WEB_DOCUMENTS = 2000000000
        sandbox.user = user

        #load the query function into memory
        exec query in sandbox.__dict__
        
        return sandbox
        
            
    #///////////////////////////////////////////////
    
        
    def permit_request( self, catalog_secret, client_id, user_id, jsonScope ):
        
        #check that the catalog_secret is correct for this user_id
        try:
            if ( not self.db.authenticate( user_id, catalog_secret ) ) :
                return self.format_process_failure(
                    "PermitException",
                    "incorrect user_id or catalog_secret"
                ) 
        except:    
            return self.format_process_failure(
                "PermitException",
                "Database problems are currently being experienced"
            ) 
        
        
        #check that the client_id exists and is valid
        if not ( client_id ):
            return self.format_process_failure(
                "PermitException",
                "A valid client ID has not been provided"
            )  
        
        #check that the scope unpacks
        try:
            scope = json.loads( 
                jsonScope.replace( '\r\n','\n' ), 
                strict=False 
            )
            target_resource = scope[ "target_resource" ]
            expiry_time = scope[ "expiry_time" ]
            query = scope[ "query" ] 
        except Exception, e:
            return self.format_process_failure(
                "PermitException",
                "incorrectly formatted JSON scope"
            ) 
        
        #check that the requested query is syntactically correct
        try:
            compile(query, '', 'exec')
        except:
            return self.format_process_failure(
                "PermitException",
                "Compilation error occurred: %s" % 
                ( str( sys.exc_info() ) )
            ) 
            
        #TODO: check that the expiry time is valid
        #TODO: check that the target_resource is correct (i.e. us)
        #TODO: should check code here to confirm that it is valid 
        #TODO: this could be done by comparisng the checksum for acceptable queries?
        #TODO: this will require sandboxing, and all sorts...
       
        #so far so good. Time to generate an access token
        access_token = self.generateAccessToken();
        try:
            self.db.insert_request( 
                access_token, 
                client_id, 
                user_id, 
                expiry_time, 
                query 
            )
                       
            return self.format_process_success(
                { "access_token" : access_token } 
            ) 
        
        #if the user access_token already exists an Integrity Error will be thrown
        except MySQLdb.IntegrityError:
            return self.format_process_failure(
                "PermitException",
                "An identical request already exists"
            ) 
              
        except:    
            return self.format_process_failure(
                "PermitException",
                "Database problems are currently being experienced"
            ) 
            
                
    #///////////////////////////////////////////////
    
    
    def revoke_request( self, user_id, catalog_secret, access_token ):
 
        #check that the catalog_secret is correct for this user_id
        try:
            if ( not self.db.authenticate( user_id, catalog_secret ) ) :
                return self.format_process_failure(
                    "RevokeException",
                    "Incorrect user_id or catalog_secret"
                ) 
    
            if ( self.db.delete_request( access_token, user_id ) ) :
                return self.format_process_success() 
            else :
                return self.format_process_failure(
                    "RevokeException",
                    "Deletion failed because request object not found"
                ) 
        except:    
            return self.format_process_failure(
                "RevokeException",
                "Database problems are currently being experienced"
            ) 
            
            
    #///////////////////////////////////////////////

             
    def generateAccessToken( self ):
        
        token = base64.b64encode(  
            hashlib.sha256( 
                str( random.getrandbits(256) ) 
            ).digest() 
        )  
            
        #replace plus signs with asterisks. Plus signs are reserved
        #characters in ajax transmissions, so just cause problems
        return token.replace( '+', '*' ) 

        
    #///////////////////////////////////////////////

 
    def safetyCall( self, func ):
        """
            This is a function that protects from mysqldb timeout,
            performing one reconnection attempt if the provided lambda 
            function generates a mysqldb error. This is necessary because
            mysqldb does not currently provided an auto_reconnect facility.
        """
        try:
            return func();
        except MySQLdb.Error, e:
            logging.error( "%s: db error %s" % ( "ProcessingModule", e.args[ 0 ] ) )
            self.db.reconnect()
            return func();
    
    