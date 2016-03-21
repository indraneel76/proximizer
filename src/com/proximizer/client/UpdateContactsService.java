package com.proximizer.client;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class UpdateContactsService extends Service implements LocationListener {

	 public static final String BROADCAST_ACTION = "com.proximizer.client.UpdateContactsService.UPDATE_FRIENDS";
	private static final String TAG = "com.proximizer.client.UpdateContactsService";
     LocalBroadcastManager broadcaster ;
     private LocationManager locationManager;
     private String provider;
     private static final long MIN_TIME_BW_UPDATES = 60000; // 1 minute
 	// The minimum distance to change Updates in meters
 	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 5 meters
	TimerTask doAsynchronousTask;
	Timer timer = new Timer();

     @Override
     public void onCreate() {
             // Called on service created
    	 broadcaster = LocalBroadcastManager.getInstance(this);
    	 
    	 //getting LocationManger intialized
    		locationManager = (LocationManager) this
					.getSystemService(Context.LOCATION_SERVICE);
     }

     
   


	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Get your friends list
		GetFriends getFriends = new GetFriends();
		getFriends.execute(com.proximizer.client.Global.myself.username);
		// setups up the GPS and gets the first location
		SetupLocationService();
				
		//update the contacts list on regular interval and send your location info too
				final Handler handler = new Handler();

				doAsynchronousTask = new TimerTask() {
					@Override
					public void run() {
						handler.post(new Runnable() {
							public void run() {
								try {
									new GetFriends().execute(com.proximizer.client.Global.myself.username);
									new GetNearContact()
											.execute(com.proximizer.client.Global.myself);

								} catch (Exception e) {

									e.printStackTrace();
								}
							}
						});
					}
				};

				timer.schedule(doAsynchronousTask, 0, 60000); // execute in every 60000
																// msec (60sec).
				
				
		return START_NOT_STICKY ;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	

	
	@Override
	public void onDestroy() {
		timer.cancel();
		super.onDestroy();
	}





	public boolean SetupLocationService()
	
	{
	
		Log.d(TAG, "Inside SetupGPS");
		boolean isGPSSetup = false;
	
		try {
	
			//see which one is enabled GPS or network
			boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean networkIsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			android.location.Location location =null ;
			
			if (gpsIsEnabled)
			{
			Log.d(TAG, "LocationManager = " + locationManager.toString());
	
			Log.d(TAG, "Setting up the criteria");
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			provider = locationManager.getBestProvider(criteria, false);
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
					MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
		  location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); 
			}
			else if (networkIsEnabled) 
			{
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
					MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				
			}
			if (location != null) {
	
				Log.d(TAG, "Location = " + location.getLatitude() + ","
						+ location.getLongitude() + "," + location.getTime());
				com.proximizer.client.Global.myself.loc = location;
	
				new SubmitLocation()
						.execute(com.proximizer.client.Global.myself);
			} else {
				Log.d(TAG, "Location not available");
			}
			isGPSSetup = true;
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Exception inside setupGPS");
			isGPSSetup = false;
		}
	
		return isGPSSetup;
	
	}

	@Override
	public void onLocationChanged(android.location.Location location) {
		Log.d(TAG, "Inside OnLocationChange");
		Log.d(TAG, "Location retreived " + location.getLatitude() + ","
				+ location.getLongitude());
		Log.d(TAG, "before submiting the location to RemoteServices");
		com.proximizer.client.Global.myself.loc = location;
		new SubmitLocation().execute(com.proximizer.client.Global.myself);
	
	}





	@Override
	public void onProviderDisabled(String provider) {
	
		Log.d(TAG, "Inside onProviderDisabled ");
		Log.d(TAG, "Provider = " + provider);
	
	}





	@Override
	public void onProviderEnabled(String provider) {
	
		Log.d(TAG, "Inside onProviderEnabled ");
		Log.d(TAG, "Provider = " + provider);
	}





	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "Inside onStatusChanged ");
		Log.d(TAG, "Provider = " + provider);
		Log.d(TAG, "Status = " + status);
	
	}


	/**
	 * Get Contact of the user who is near to me
	 */

	

	private double Seperation(Contact myself, Contact myfriend) {

		double theta = Math.abs(myself.loc.getLongitude()
				- myfriend.loc.getLongitude());
		double dist = Math.sin(deg2rad(myself.loc.getLatitude()))
				* Math.sin(deg2rad(myfriend.loc.getLatitude()))
				+ Math.cos(deg2rad(myself.loc.getLatitude()))
				* Math.cos(deg2rad(myfriend.loc.getLatitude()))
				* Math.cos(deg2rad(theta));
		Log.d(TAG, "dist1 = " + dist);
		dist = Math.acos(dist);
		Log.d(TAG, "dist2 = " + dist);
		dist = rad2deg(dist);
		Log.d(TAG, "dist3 = " + dist);
		dist = dist * 60 * 1.1515;
		Log.d(TAG, "dist4 = " + dist);
		// seperation distance in meters
		dist = dist * 1609.344;
		Log.d(TAG, "dist5 = " + dist);
		Log.d(TAG, "seperation " + dist);

		return dist;
	}

	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	class GetFriends extends AsyncTask<String, Integer, ArrayList<Contact>> {
		
		private static final String TAG = "GetFriends";

		@Override
		protected ArrayList<Contact> doInBackground(String... params) {
	
			ArrayList<Contact> friends = new ArrayList<Contact>();
	
			final String METHOD_NAME = "getFriends";
	
			SoapObject request = new SoapObject(
					com.proximizer.client.Global.NAMESPACE, METHOD_NAME);
			request.addProperty("arg0", params[0]);
	
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.setOutputSoapObject(request);
	
			// Make the soap call.
			HttpTransportSE androidHttpTransport = new HttpTransportSE(
					com.proximizer.client.Global.URL);
			androidHttpTransport.debug = true;
			try {
	
				// this is the actual part that will call the webservice
				androidHttpTransport.call(
						com.proximizer.client.Global.SOAP_ACTION, envelope);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				String inputRequest = androidHttpTransport.requestDump;
				Log.d(TAG, "Input XML request " + inputRequest);
	
				String outputResponse = androidHttpTransport.responseDump;
				Log.d(TAG, "Output XML response " + outputResponse);
			}
	
			// Get the SoapResult from the envelope body.
	
			SoapObject resultObj = (SoapObject) envelope.bodyIn;
			Log.d("Soap response", "result  is ....." + resultObj);
			int count = resultObj.getPropertyCount();
			Log.d(TAG, "soap array count " + count);
	
			for (int i = 0; i < count; i++) {
				friends.add(new Contact(resultObj.getProperty(i).toString(),
						new Location("GPS"), false));
			}
	
			return friends;
	
		}
	
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(ArrayList<Contact> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			com.proximizer.client.Global.contacts = result;
	
			System.out.println("on post execute...........");
			// update the UI
			Intent updateFriendListIntent = new Intent("com.proximizer.client.UpdateContactsService.UPDATE_FRIENDS");
			updateFriendListIntent.putExtra("contacts", result) ;
			broadcaster.sendBroadcast(updateFriendListIntent);
		}
	
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			System.out.println("on pre execute......");
		}
	
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
	
	}
	
	
	/**
	 * 
	 * Submits the location information for the user
	 */
	class SubmitLocation extends AsyncTask<Contact, Integer, Boolean>  {

		@Override
		protected Boolean doInBackground(Contact... contact) {

			boolean isSubmitted = false;
			Log.d(TAG, "Inside submitLocation");
			final String METHOD_NAME = "submitLocation";

			SoapObject request = new SoapObject(
					com.proximizer.client.Global.NAMESPACE, METHOD_NAME);
			request.addProperty("arg0",
					com.proximizer.client.Global.myself.username);

			// SoapObject soapLocation = new
			// SoapObject(com.proximizer.client.Global.NAMESPACE, "location");
			SoapObject soapLocation = new SoapObject("", "location");

			soapLocation.addProperty("longitude",
					"" + contact[0].loc.getLongitude());
			soapLocation.addProperty("latitude",
					"" + contact[0].loc.getLatitude());
			soapLocation.addProperty("time", "" + contact[0].loc.getTime());
			request.addProperty("arg1", soapLocation);

			// request.addSoapObject(soapLocation) ;
			Log.d(TAG, "Soap Request sent " + request.toString());
			// request.addProperty("location",location);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.setOutputSoapObject(request);
			envelope.implicitTypes = true;
			envelope.setAddAdornments(false);
			Log.d(TAG, "Input Envelop contains " + envelope.bodyOut);
			// Make the soap call.
			HttpTransportSE androidHttpTransport = new HttpTransportSE(
					com.proximizer.client.Global.URL);
			androidHttpTransport.debug = true;
			try {

				// this is the actual part that will call the webservice
				androidHttpTransport.call(
						com.proximizer.client.Global.SOAP_ACTION, envelope);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				String inputRequest = androidHttpTransport.requestDump;
				Log.d(TAG, "Input XML request " + inputRequest);

				String outputResponse = androidHttpTransport.responseDump;
				Log.d(TAG, "Output XML response " + outputResponse);
			}

			// Get the SoapResult from the envelope body.
			SoapObject result = (SoapObject) envelope.bodyIn;
			Log.d("Soap response", "result  is ....." + result);

			if (result != null) {
				String submitted = result.getProperty(0).toString();
				if (submitted.equals("true"))
					isSubmitted = true;

			}

			return isSubmitted;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		
		}


}


	/**
	 * Get Contact of the user who is near to me
	 */
	
	class GetNearContact extends
			AsyncTask<Contact, Integer, ArrayList<Contact>> {
	
		@Override
		protected ArrayList<Contact> doInBackground(Contact... params) {
	
			ArrayList<Contact> nearFriendList = new ArrayList<Contact>();
	
			Contact myself = params[0];
			final String METHOD_NAME = "getNearFriends";
	
			SoapObject request = new SoapObject(
					com.proximizer.client.Global.NAMESPACE, METHOD_NAME);
			request.addProperty("arg0", myself.username);
	
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.setOutputSoapObject(request);
	
			// Make the soap call.
			HttpTransportSE androidHttpTransport = new HttpTransportSE(
					com.proximizer.client.Global.URL);
			androidHttpTransport.debug = true;
			try {
	
				// this is the actual part that will call the webservice
				androidHttpTransport.call(
						com.proximizer.client.Global.SOAP_ACTION, envelope);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				String inputRequest = androidHttpTransport.requestDump;
				Log.d(TAG, "Input XML request " + inputRequest);
	
				String outputResponse = androidHttpTransport.responseDump;
				Log.d(TAG, "Output XML response " + outputResponse);
			}
			// Get the SoapResult from the envelope body.
	
			SoapObject resultObj = (SoapObject) envelope.bodyIn;
			Log.d("Soap response", "result  is ....." + resultObj);
			int count = resultObj.getPropertyCount();
			Log.d(TAG, "soap array count " + count);
	
			for (int i = 0; i < count; i++) {
				// Take the item
				SoapObject itemObj = (SoapObject) resultObj.getProperty(i);
				Log.d(TAG, "Soap itemObj " + itemObj);
	
				String friendsName = itemObj.getProperty("username").toString();
				Log.d(TAG, "friendsName " + friendsName);
	
				SoapObject locObj = (SoapObject) itemObj.getProperty("loc");
				String friendsLatitude = locObj.getProperty("latitude")
						.toString();
				Log.d(TAG, "FriendsLatitude " + friendsLatitude);
				String friendsLongitude = locObj.getProperty("longitude")
						.toString();
				Log.d(TAG, "friendsLongitude " + friendsLongitude);
				String friendsTime = locObj.getProperty("time").toString();
				Log.d(TAG, "friendsTime " + friendsTime);
				Log.d(TAG, "NearFriends name: " + friendsName + " Latitude: "
						+ friendsLatitude + " Longitude: " + friendsLongitude
						+ " Time: " + friendsTime);
				Log.d(TAG, "Creating a location");
				Location loc = new Location("GPS");
	
				Log.d(TAG, "Adding latitude to  location");
				loc.setLatitude(Double.parseDouble(friendsLatitude));
				//LoLogAG, "Adding longitude to  location");
				loc.setLongitude(Double.parseDouble(friendsLongitude));
	
				Log.d(TAG, "Creating a new contact");
				Contact nearFriendContact = new Contact(friendsName, loc, true);
	
				Log.d(TAG, "Adding contact to  nearFriendLis t");
				nearFriendList.add(nearFriendContact);
	
			}
			return nearFriendList;
		}
	
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		//<TobeDone>
		@Override
		protected void onPostExecute(ArrayList<Contact> nearFriendList) {
			// TODO Auto-generated method stub
			super.onPostExecute(nearFriendList);
			// Alerting that there is a friend which is near to you
			
			//refresh the friends list
			//new GetFriends().execute(com.proximizer.client.Global.myself.username);
			//resetting the contactlist 
			for (Contact contact:com.proximizer.client.Global.contacts)
				contact.isNear=false;
			
			if (nearFriendList.size()!=0)
			{
			for (Contact contact : nearFriendList) {
	
				boolean isRemoved =com.proximizer.client.Global.contacts.remove(contact);
				Log.d(TAG,"Is Removed = "+isRemoved);
				contact.isNear=true ;
				com.proximizer.client.Global.contacts.add(contact);
				// updating the contact list
			//	Intent updateFriendListIntent = new Intent("com.proximizer.client.UpdateContactsService.UPDATE_FRIENDS");
			//	broadcaster.sendBroadcast(updateFriendListIntent);
				
				// To show the alert pop up
				double sep = Seperation(com.proximizer.client.Global.myself,contact);
				int dsep = (int) sep;
				contact.seperation=dsep ;
				Intent alertNearFriendIntent = new Intent("com.proximizer.client.UpdateContactsService.UPDATE_FRIENDS");
				alertNearFriendIntent.putExtra("nearcontact",contact);
				broadcaster.sendBroadcast(alertNearFriendIntent);
				
				//create a notification on the slider
				NotificationManager notificationManager = (NotificationManager) 
						  getSystemService(NOTIFICATION_SERVICE); 
				Intent intent = new Intent(UpdateContactsService.this,ContactsActivity.class);
				Notification notification = new Notification(R.drawable.ic_launcher,"Your friend "+contact.username+" is near",System.currentTimeMillis());
				PendingIntent pendingIntent=PendingIntent.getActivity(UpdateContactsService.this, 0, intent, Context.BIND_AUTO_CREATE);
				notification.setLatestEventInfo(UpdateContactsService.this, "Your friend "+contact.username+" is near", "seperation = "+dsep+" m",pendingIntent );
				notificationManager.notify(1, notification);
				
				notification = new Notification();
				
				notification.vibrate = new long[] { 1000, 1000, 1000, 1000,
						1000 };
				notificationManager.notify(0, notification);
	
			
			}
			}
			
		}
	
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
	
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
	
	}
	
	
	
}



	
