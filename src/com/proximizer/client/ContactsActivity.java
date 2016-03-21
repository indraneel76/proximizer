package com.proximizer.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ResourceBundle.Control;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.proximizer.client.R.id;


import android.R.drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Settings.Global;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.sax.StartElementListener;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//added for monitoring

public class ContactsActivity extends Activity {
	private static final String TAG = "ContactsActivity";

	ListView ContactListView;
	LocalBroadcastManager broadcaster;
	CustomAdapter customadapter;
	TimerTask doAsynchronousTask;
	Timer timer = new Timer();
	private LocationManager locationManager;
	private String provider;

	private static final long MIN_TIME_BW_UPDATES = 60000; // 1 minute
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 5 meters
	private static final int REQ_GET_CONTACT = 0;
	static final int PICK_CONTACT_REQUEST = 1;  // The request code

	//ProgressDialog pd;
	//Handler handler;

	ImageView proximityicon;

	BroadcastReceiver receiver;
	Intent startServiceIntent;
	

	
	 private SharedPreferences userDetails;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		// for monitoring
		/*NewRelic.withApplicationToken(
				"AAbb4540c5b5bec76b136de6a36d07db59b67c0a02"
				).start(this.getApplication());
		*/
		Log.d(TAG,"OnCreate called");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		
		userDetails = getSharedPreferences("userdetails", MODE_PRIVATE);
		
		
		
		// Get the logged in user
		Intent intent = getIntent();
		String LoggedUser = intent.getStringExtra("LoggedUser");
		Log.d(TAG,"LoggedUser = "+LoggedUser);
		//if LoggedUser is not empty use fill shared preference
			if(!(LoggedUser==null))
			{
				Log.d(TAG,"LoggedUser is not empty");
				Editor edit = userDetails.edit();
				edit.clear();
				edit.putString("username", LoggedUser.trim());
				edit.commit();
				
			}
			else
			{
				Log.d(TAG,"LoggedUser is  empty");
				//LoggedUser is empty and we need to get the value using SharedPreferences
				LoggedUser = userDetails.getString("username", "");
			}
			com.proximizer.client.Global.myself.username = LoggedUser;
			
			Log.d(TAG,"Inside Create-- com.proximizer.client.Global.myself.username "+com.proximizer.client.Global.myself.username);
		
			//still username is empty go to login screen
			
			if (com.proximizer.client.Global.myself.username==null)
			{	Intent logoutIntent = new Intent(ContactsActivity.this,
						RegisterActivity.class);
				startActivity(logoutIntent);
			}
		
		
		// Load the friend List
		ContactListView = (ListView) findViewById(R.id.contactlist);
		ContactListView.setOnItemClickListener(new OnItemClickListener()

		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub

			}
		});

		//setting its broadcaster
		broadcaster = LocalBroadcastManager.getInstance(this);
    	 
    	 //getting LocationManger intialized
    		locationManager = (LocationManager) this
					.getSystemService(Context.LOCATION_SERVICE);
		
		/**
		 * Start the background service which will get the contact list and
		 * update it regularly
		 * 
		 */
		startServiceIntent = new Intent(this, UpdateContactsService.class);

		startService(startServiceIntent);

		/**
		 * Handler for updating the contact list
		 */

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent
						.getAction()
						.equalsIgnoreCase(
								"com.proximizer.client.UpdateContactsService.UPDATE_FRIENDS")) {
					Bundle bundle = intent.getExtras();
					// ArrayList <Contact> contacts = (ArrayList<Contact>)
					// bundle.get("contacts");
					ArrayList<String> contactlist = new ArrayList<String>() ;
					
					for (Contact contact : com.proximizer.client.Global.contacts)
					{
						
					}
					
					customadapter = new CustomAdapter(ContactsActivity.this,
							com.proximizer.client.Global.contacts);
					ContactListView.setAdapter(customadapter);
				}

			}

		};
	}
	
	public String getNameFromPhoneNumber(String phoneNumber)
	{
		Log.d(TAG, "phonenumber passed for name = "+phoneNumber);
		String cName=  phoneNumber ;
		Cursor phones = getContentResolver().query( 
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, 
                ContactsContract.CommonDataKinds.Phone.NUMBER +" = "+ "'"+phoneNumber+"'", 
                null, null);
		Log.d(TAG, "phones.getCount() = "+phones.getCount());
		if (phones.getCount() >0)
{
		 phones.moveToFirst();
		 
		 cName = phones.getString(phones.getColumnIndex(Phone.DISPLAY_NAME_PRIMARY));
}
		 return  cName ;
	}

	@Override
	protected void onStart() {
		Log.d(TAG,"OnStart called");
		super.onStart();
		LocalBroadcastManager
				.getInstance(this)
				.registerReceiver(
						(receiver),
						new IntentFilter(
								"com.proximizer.client.UpdateContactsService.UPDATE_FRIENDS"));
		// registering for google analytics
		EasyTracker.getInstance().activityStart(this);
	}
	
	
	

	@Override
	protected void onResume() {
		Log.d(TAG,"OnResume called");
		new GetFriends()
		.execute(com.proximizer.client.Global.myself.username) ;
		super.onResume();
	}
	
	
	

	@Override
	protected void onRestart() {
		Log.d(TAG,"OnRestart called");
		new GetFriends()
		.execute(com.proximizer.client.Global.myself.username);
		super.onRestart();
	}

	@Override
	@Deprecated
	public Object onRetainNonConfigurationInstance() {
		// TODO Auto-generated method stub
		return super.onRetainNonConfigurationInstance();
	}

	@Override
	protected void onStop() {
		Log.d(TAG,"OnStop called");
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Log.d(TAG,"OnDestroy called");
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_contacts, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		// return super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.addContact:
			Log.d(TAG, "Inside AddContact Menu");
			/**
			 * 
			 * This use to open a add contact UI, I have removed it as we don't
			 * want phone numbers to go to server due to privacy issues
			 */
			 Uri uri= ContactsContract.Contacts.CONTENT_URI; Intent intent = new Intent(Intent.ACTION_PICK, uri);
			 
			 startActivityForResult(intent, 1);
			 
		
			
			
			//AlertDialog.Builder alert = new AlertDialog.Builder(this);
			//alert.setTitle("Add Contact");
			//alert.setMessage("Enter contact's username :");

			// Set an EditText view to get user input
			//final EditText input = new EditText(this);
			//alert.setView(input);

			/*alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String friendUserName = input.getText().toString();
							Log.d(TAG, "friendUserName : " + friendUserName);
							addContact(friendUserName);
							return;
						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							return;
						}
					});
			alert.show();*/

			break;

/*		case R.id.deleteContact:
			Log.d(TAG, "Inside DeleteContact Menu");
			
			 * Uri uri1 = ContactsContract.Contacts.CONTENT_URI; Intent intent1
			 * = new Intent(Intent.ACTION_PICK, uri1);
			 * 
			 * startActivityForResult(intent1, 2);
			 
			AlertDialog.Builder alertdelete = new AlertDialog.Builder(this);
			alertdelete.setTitle("Delete Contact");
			alertdelete.setMessage("Enter contact's username :");

			// Set an EditText view to get user input
			final EditText inputdelete = new EditText(this);
			alertdelete.setView(inputdelete);

			alertdelete.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String friendUserName = inputdelete.getText()
									.toString();
							Log.d(TAG, "friendUserName : " + friendUserName);
							deleteContact(friendUserName);
							return;
						}
					});

			alertdelete.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							return;
						}
					});
			alertdelete.show();

			break;
*/			/*
		case R.id.menu_settings:
			Log.d(TAG, "Inside setting menu");
			Intent SettingActivityIntent = new Intent(ContactsActivity.this,
			SettingsActivity.class);
			startActivity(SettingActivityIntent);

			break;
*/
		case R.id.reset:
			Log.d(TAG, "Inside Inside Logout menu");
			// stopping the service
			stopService(startServiceIntent);
			
			//clear the sharedPreferences
			
			Editor edit = userDetails.edit();
			edit.clear();
			
			edit.commit();
			
			UserDetailsDataSource userDetailsDataSource = new UserDetailsDataSource(getApplicationContext());
			userDetailsDataSource.open();
			userDetailsDataSource.deleteUserDetail() ;
			userDetailsDataSource.close();
			
			// Go to login screen
			Intent logoutIntent = new Intent(ContactsActivity.this,
					RegisterActivity.class);
			startActivity(logoutIntent);
			
			

			break;
		}
		return true;
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request it is that we're responding to
	   Log.d(TAG, "Inside OnActivityResult");
		if (requestCode == PICK_CONTACT_REQUEST) {
	        // Make sure the request was successful
	        if (resultCode == RESULT_OK) {
	            // Get the URI that points to the selected contact
	            Log.d(TAG, "data to string "+data.toString()) ;
	        	Uri contactUri = data.getData();
	        	
	        	
	        	Cursor c =  managedQuery(contactUri, null, null, null, null);
	            
	            
	            c.moveToFirst();
	            String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
	            Log.d(TAG, "id = "+id);
	            String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

	              if (hasPhone.equalsIgnoreCase("1")) {
	             Cursor phones = getContentResolver().query( 
	                          ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, 
	                          ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id, 
	                          null, null);
	             
	             phones.moveToFirst();
	           String   cNumber = phones.getString(phones.getColumnIndex("data1"));
	            Log.d(TAG,"number is:"+cNumber);
	            //phoneNumberAdded=cNumber ;
	            addContact(cNumber);
	              } // end of if condition
	            
	          
	        } // end of result okay.
	    }
	}
	
	private void addContact(String friendsUserName) {
		String[] params = new String[2];
		params[0] = com.proximizer.client.Global.myself.username;
		params[1] = friendsUserName;

		new AddFriend().execute(params);

		Toast.makeText(this, "Selected Contacted Added", Toast.LENGTH_LONG)
				.show();

	}

	private void deleteContact(String friendUserName) {
		String[] params = new String[2];
		params[0] = com.proximizer.client.Global.myself.username;
		params[1] = friendUserName;

		new DeleteFriend().execute(params);

		Toast.makeText(this, "Selected Contacted Deleted", Toast.LENGTH_LONG)
				.show();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		Log.d(TAG,"OnPause called");
		super.onPause();
		// locationManager.removeUpdates(this);
	}

	class AddFriend extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			boolean isAdded = false;
			final String METHOD_NAME = "addFriend";

			SoapObject request = new SoapObject(
					com.proximizer.client.Global.NAMESPACE, METHOD_NAME);
			request.addProperty("arg0", params[0]);
			request.addProperty("arg1", params[1]);

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
				isAdded = false;
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
					isAdded = true;

			}
			
			return isAdded;

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
		/*	if (result)
				handler.sendEmptyMessage(456);*/

		}
	}

	class DeleteFriend extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			boolean isDeleted = false;
			final String METHOD_NAME = "deleteFriend";

			SoapObject request = new SoapObject(
					com.proximizer.client.Global.NAMESPACE, METHOD_NAME);
			request.addProperty("arg0", params[0]);
			request.addProperty("arg1", params[1]);

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

			SoapObject result = (SoapObject) envelope.bodyIn;
			Log.d("Soap response", "result  is ....." + result);

			if (result != null) {
				String submitted = result.getProperty(0).toString();
				if (submitted.equals("true"))
					isDeleted = true;

			}

			// update the list
			/*new GetFriends()
					.execute(com.proximizer.client.Global.myself.username);*/
			return isDeleted;

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
			/*if (result)
				handler.sendEmptyMessage(456);*/

		}
	}

	class CustomAdapter extends BaseAdapter {
		private static final String TAG = "Inside CustomAdapter";
		Context ctx;
		ArrayList<Contact> contacts;
		TextView tv_username;
		// TextView tv_latitude;
		TextView tv_info;
		// TextView tv_longitude;
		// TextView tv_datetime;
		ImageView im_proximity_icon;
		Button btnShowAddress;
		Button btnDeleteContact;

		public CustomAdapter(Context ctx, ArrayList<Contact> contacts) {
			this.ctx = ctx;
			this.contacts = contacts;

		}

		@Override
		public int getCount() {
			return contacts.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			if (convertView == null) {
				convertView = View.inflate(ctx, R.layout.layout_contact_item,
						null);

			}

			tv_username = (TextView) convertView
					.findViewById(R.id.textview_contact_name);

			im_proximity_icon = (ImageView) convertView
					.findViewById(R.id.imageview_contact_proxmity);

			// im_proximity_icon.setImageResource(R.drawable.redstar);
			tv_info = (TextView) convertView.findViewById(R.id.info);
			btnShowAddress = (Button) convertView
					.findViewById(R.id.btn_showAddress);
			btnShowAddress.setEnabled(false);
			// tv_longitude = (TextView) convertView
			// .findViewById(R.id.textview_longitude);
			// tv_latitude = (TextView) convertView
			// .findViewById(R.id.textview_latitude);
			// tv_datetime = (TextView) convertView
			// .findViewById(R.id.textview_datetime);
			// Commenting this out as we are no longer using phone number
			// tv_username.setText(getContactName(ctx,
			// contacts.get(position).username));
			
			String contactNameToDisplay =getNameFromPhoneNumber(contacts.get(position).username);
			
			tv_username.setText(contactNameToDisplay);
			Log.d(TAG, "Contact = " + contacts.get(position).username);
			
			btnDeleteContact = (Button) convertView
					.findViewById(R.id.btn_deletecontact);

			btnDeleteContact.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String usertoDelete=contacts.get(position).username;
					deleteContact(usertoDelete);
				}
			});

			
			

			if (contacts.get(position).isNear) {
				Log.d(TAG, "Contact is near");
				im_proximity_icon.setImageResource(R.drawable.greenstar);

				tv_info.setText("Your friend is near to you !! "+contacts.get(position).seperation);
				// tv_longitude
				// .setText("" + contacts.get(position).loc.getLongitude());
				Log.d(TAG,
						"Longitude = "
								+ contacts.get(position).loc.getLongitude());

				// tv_latitude.setText("" +
				// contacts.get(position).loc.getLatitude());
				Log.d(TAG,
						"Latitude = "
								+ contacts.get(position).loc.getLatitude());

				Log.d(TAG, "Datetime = " + contacts.get(position).loc.getTime());
				// tv_datetime.setText("" +
				// contacts.get(position).loc.getTime());

				// setting listerner for showAddress button
				btnShowAddress.setEnabled(true);
				btnShowAddress.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Double[] params = new Double[2];
						params[0] = contacts.get(position).loc.getLatitude();
						params[1] = contacts.get(position).loc.getLongitude();
						new GetAddress().execute(params);
					}
				});

				// setting up listerner
				/*
				 * tv_longitude.setOnClickListener(new OnClickListener() {
				 * 
				 * @Override public void onClick(View v) { Intent intent = new
				 * Intent(ctx, ShowMapActivity.class) ; Bundle b = new Bundle()
				 * ; b.putDouble("latitude",
				 * contacts.get(position).loc.getLatitude());
				 * b.putDouble("longitude",
				 * contacts.get(position).loc.getLongitude());
				 * intent.putExtras(b) ; startActivity(intent) ;
				 * 
				 * 
				 * } }) ;
				 */

			} else {
				im_proximity_icon.setImageResource(R.drawable.redstar);
				tv_info.setText("Your friend is far away from you");
				// tv_longitude.setText("N/A");
				Log.d(TAG, "Longitude = N/A");

				// tv_latitude.setText("N/A");
				Log.d(TAG, "Latitude = N/A");

				Log.d(TAG, "Datetime = N/A");
				// tv_datetime.setText("N/A");
			}

			return convertView;
		}

	}

	@SuppressLint("NewApi")
	class GetAddress extends AsyncTask<Double, Integer, String> {

		@Override
		protected String doInBackground(Double... params) {

			Double latitude = params[0];
			Double longitude = params[1];
			String strAddress = "No address found";
			Geocoder gc = new Geocoder(getApplicationContext());

			if (gc.isPresent()) {
				Address address;
				try {
					List<Address> addresses = gc.getFromLocation(latitude,
							longitude, 1);
					address = addresses.get(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return "no address found";
				}
				StringBuffer str = new StringBuffer();
				str.append(address.getAddressLine(0)+",");
				str.append(address.getFeatureName()+",");	
				str.append(address.getLocality() + ",");
				str.append(address.getSubAdminArea() + ",");
				str.append(address.getAdminArea() + ",");
				str.append(address.getCountryName());
				strAddress = str.toString();
			}
			
			return strAddress;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String address) {
			// TODO Auto-generated method stub
			super.onPostExecute(address);
			// creating alert
			AlertDialog alertDialog = new AlertDialog.Builder(
					ContactsActivity.this).create();
			alertDialog.setTitle("Address of your friend");

			alertDialog.setMessage("Address: " + address);

			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// here you can add functions
				}
			});
			alertDialog.setIcon(R.drawable.ic_launcher);
			alertDialog.show();

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
			Intent updateFriendListIntent = new Intent(
					"com.proximizer.client.UpdateContactsService.UPDATE_FRIENDS");
			updateFriendListIntent.putExtra("contacts", result);
			broadcaster.sendBroadcast(updateFriendListIntent);
			// pd.dismiss();
			// ContactsActivity.this.handler.sendEmptyMessage(123);
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

}
