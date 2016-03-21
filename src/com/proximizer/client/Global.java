package com.proximizer.client;

import java.util.ArrayList;


import android.app.Application;
import android.content.Context;


public class Global extends Application{

	public static ArrayList<Contact> contacts = new ArrayList<Contact>();
//	public static ArrayList<String> contactnames = new ArrayList<String>() ;
	public static Contact myself = new Contact() ;
	public static final String NAMESPACE = "http://proximizer.com/";
	
//	public static final String URL = "http://10.0.2.2:8888/proximizer/processorSoapServerServlet" ;
//	public static final String SOAP_ACTION="http://l0.0.2.2:8888/proximizer/processorSoapServerServlet" ;
	
	public static final String URL = "http://proximizer.appspot.com/proximizer/processorSoapServerServlet" ;
	public static final String SOAP_ACTION="http://proximizer.appspot.com/proximizer/processorSoapServerServlet" ;
		static final String TAG ="Gobal" ;
	
	public static boolean IntializeContactsData(Context ctx) {
		
		//
	
		return true ;
}
}
