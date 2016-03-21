package com.proximizer.client;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.util.Log;
public class RemoteServices {

	private static final String NAMESPACE = "http://server.proximizer.com";
	private static final String URL = 
		"http://10.0.0.2:8080/Proximizer_server1/services/processor?wsdl";	
	private static final String SOAP_ACTION = "http://10.0.0.2:8080/Proximizer_server1/services/processor";
	private static final String TAG = "RemoteService";
	
	public RemoteServices() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Represents an Register task for registering new users
	 */
	public static boolean register(String username,String password)
	{
		boolean status =false ; 
		
		return status ;
	}
	/**
	 * 
	 This method calls the external web service for authentication
	 */
	
	
	
	
	
	
	/**
	 * 
	 * Adds friends for the user
	 */
	 public static boolean addFriend(String username,String friendName)
	 {
		 boolean isAdded = false ;
		 
		 return isAdded ;
	 }
	
	
	
	/**
	 * 
	 * This deletes the friend for the particular user.
	 * @return
	 */
	
	public static boolean deleteFriend(String username, String friendName)
	{
		boolean isDeleted =false ;
		
		return isDeleted ;
	}
	
		
	/**
	 * 
	 * Get location of a users.
	 * Returns Location object
	 */
/*	
	public static Location getLocation(String username)
	{
		Location loc =null ;
		
		return loc ;
	}*/
	
	
	
	
	
	

}
