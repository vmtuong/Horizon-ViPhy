"""
Created on 12 April 2011
@author: jog
"""
import urllib
import re

#///////////////////////////////////////////////


support_providers = [ "google", "yahoo", "aol", "myopenid" ]


#///////////////////////////////////////////////


def process( realm, return_to, provider, username=None ):
    
    if ( realm is None or return_to is None or provider is None ) :
        raise Exception( "Incorrect OpenID parameters supplied" )
        
    provider_list = {
        "google" : "https://www.google.com/accounts/o8/id",
        "yahoo" : "https://me.yahoo.com",
        "aol" : "https://openid.aol.com/%s" % ( username, ),
        "myopenid" : "https://%s.myopenid.com" % ( username, ),            
    }
    
    #Attempt to find the actual discovery 
    #URL from our list
    discovery_url = provider_list.get( provider )
    if ( discovery_url is None ) :
        raise Exception( "Specified OpenID provider could not be found" )
    
    #Attempt to contact that URL and discover the 
    #provider's OpenID endpoint     
    try:
        endpoint_url = discover( discovery_url )
    except:
        raise Exception( "OpenID discovery has failed" )
    
    #Create an association between us and the 
    #provider, getting a shared key    
    try:
        assoc_handle = createAssociation( endpoint_url )
    except:
        raise Exception( "OpenID association has failed" )
    
    #Finally build the login URL that will redirect
    #the user to     
    try:
        redirect_url = buildRedirectURL(  
            endpoint_url, 
            assoc_handle, 
            return_to, 
            realm 
        )
        
    except:
        raise Exception( "Failed to generate OpenID redirect URL" )
    
    return redirect_url

   
#///////////////////////////////////////////////

     
def discover( discovery_url ):
    """
        Attempts to discover the openID endpoint for the
        provider (first by xdrs and then by html discovery).
        If this fails and exception will be thrown by the regex.
    """
    result = urllib.urlopen( discovery_url ).read()
    m = re.search( "<URI>(.*)</URI>", result )
    if m is None :
        m = re.search( 
            "rel=[\"']openid2.provider[\"'] href=[\"'](.*?)[\"']", 
            result ) 
    return m.group( 1 )    


#///////////////////////////////////////////////

     
def createAssociation( endpoint_url ):
    """
        Attempts to setup an association with the openID 
        provider. If this fails and exception will be 
        thrown by the regex extraction.
    """
    url = endpoint_url + \
        "?openid.ns=http://specs.openid.net/auth/2.0" + \
        "&openid.mode=associate" + \
        "&openid.assoc_type=HMAC-SHA1" + \
        "&openid.session_type=no-encryption"
        
    result = urllib.urlopen( url ).read()
    m = re.search( "assoc_handle:(.*)\n", result )
    return m.group( 1 ); 

    
#///////////////////////////////////////////////
     
     
def buildRedirectURL( endpoint_url, assoc_handle, return_to, realm ):
    """
       Builds the redirect endpoint ready to send to the user.
    """
    
    if ( endpoint_url and 
         assoc_handle and
         return_to and
         realm ):

        return endpoint_url + \
            "?openid.ns=http://specs.openid.net/auth/2.0" + \
            "&openid.mode=checkid_setup" + \
            "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select" + \
            "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" + \
            "&openid.return_to=" + return_to + \
            "&openid.realm=" + realm + \
            "&openid.assoc_handle=" + assoc_handle
    else:
        raise Exception()


#///////////////////////////////////////////////
     
     
def getRedirectURL( self ):
    """
       Builds the redirect endpoint ready to send to the user.
    """
    if ( self.endpoint and 
         self.assoc_handle and
         self.return_to and
         self.realm ):

        return self.endpoint + \
            "?openid.ns=http://specs.openid.net/auth/2.0" + \
            "&openid.mode=checkid_setup" + \
            "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select" + \
            "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" + \
            "&openid.return_to=" + self.return_to + \
            "&openid.realm=" + self.realm + \
            "&openid.assoc_handle=" + self.assoc_handle
    else:
        raise Exception()


#///////////////////////////////////////////////


class Response( object ):
          
          
    #///////////////////////////////////////////////

            
    def __init__( self, params ):
        self.__params = params
        
        result = self.__params[ "openid.mode" ]
        if result == "cancel" :
            self.success = False
        elif result == "id_res" :
            self.success = True
        else:
            raise Exception( "unrecognized openid response" )
    
    
    #///////////////////////////////////////////////
    
    
    def get( self, key ):
        return self.__params[ key ]
    
    
    #///////////////////////////////////////////////
    
    
    def get_user_id( self ):
        try:
            return self.__params[ "openid.claimed_id" ]
        except:
            return None
    
    
    #///////////////////////////////////////////////
    
    def is_success( self ):
        return self.success
    