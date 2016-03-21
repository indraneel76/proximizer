package com.proximizer.client;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class RegisterActivity extends Activity {
	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

//
	public static final String TAG = "RegistryActivity";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */

	private UserRegisterTask mRegiTask = null ;
	private UserLoginTask mLoginTask = null ;

	// Values for email and password at the time of the login attempt.
	private String mphoneString;
	

	// UI references.
	private EditText mPhoneView;
	private TextView mRegisterStatusMessageView;
	private View mRegistrationFormView;
	private View mRegistrationStatusView;
	
	
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	
	String myPhoneNo ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "inside on Create");
		super.onCreate(savedInstanceState);
		
		UserDetailsDataSource userDetailsDataSource = new UserDetailsDataSource(getApplicationContext());
		userDetailsDataSource.open();
		myPhoneNo =userDetailsDataSource.getMyPhoneNo() ;
		Log.d(TAG,"Check phoneNo = "+myPhoneNo) ;
		userDetailsDataSource.close();
		
		
		if (myPhoneNo.equals(""))
		{
		
			setContentView(R.layout.activity_register);
			mRegistrationFormView = findViewById(R.id.register_form);
			mRegistrationStatusView = findViewById(R.id.register_status);
			mRegisterStatusMessageView = (TextView) findViewById(R.id.register_status_message);
			mPhoneView = (EditText) findViewById(R.id.yourphonenumber);
			

		// Set up the login form.
		
		

	//	mPhoneView.setText("Enter your phone number");

		

		

		
		findViewById(R.id.register_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptRegister();
						//successful registration , lets login
						
						
					}
				});
		
		}
		else
		{
			// lets login
		Log.d(TAG, "inside Login section");
		setContentView(R.layout.activity_login);
		
		mLoginStatusView = findViewById(R.id.login_status);
		
		mLoginStatusMessageView= (TextView) findViewById(R.id.login_status_message);
		
		mLoginStatusMessageView.setText("Login under progress");
			showLoginProgress(true);
			mLoginTask = new UserLoginTask(myPhoneNo);
			mLoginTask.execute((Void) null);
			
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		// return super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.menu_reset:
			Log.d(TAG, "Inside Reset Menu");
			
			UserDetailsDataSource userDetailsDataSource = new UserDetailsDataSource(getApplicationContext());
			userDetailsDataSource.open();
			userDetailsDataSource.deleteUserDetail() ;
			userDetailsDataSource.close();
			
			break;
		}
			
		return true;
	}
	
	

	/**register
	 * 
	 */
	
	public void attemptRegister() {
		if (mRegiTask != null) {
			return;
		}

		// Reset errors.
		mPhoneView.setError(null);
		
		// Store values at the time of the login attempt.
		mphoneString = mPhoneView.getText().toString();
	

		boolean cancel = false;
		View focusView = null;

		
		// Check for a valid email address.
		if (TextUtils.isEmpty(mphoneString)) {
			mPhoneView.setError(getString(R.string.error_field_required));
			focusView = mPhoneView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mRegisterStatusMessageView.setText(R.string.login_progress_signing_in);
			showRegistrationProgress(true);
			mRegiTask = new UserRegisterTask(mphoneString);
			mRegiTask.execute((Void) null);
		}
	}


	
	
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showRegistrationProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mPhoneView.setVisibility(View.VISIBLE);
			mRegistrationStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mRegistrationStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mPhoneView.setVisibility(View.VISIBLE);
			mPhoneView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mPhoneView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mRegistrationStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mPhoneView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showLoginProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

		//	mPhoneView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});


		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			//mPhoneView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	
	public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {
		String mphoneNumber ;
		public UserRegisterTask(String phoneNumber) {
			this.mphoneNumber=phoneNumber ;
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			boolean isRegister =false ;
			try {
			
			//	isAuthenticated=RemoteServices.authenticate(mEmail, mPassword) ;
				
				 final String  METHOD_NAME ="register" ; 
				
				SoapObject request = new SoapObject(com.proximizer.client.Global.NAMESPACE, METHOD_NAME);
				 request.addProperty("arg0",mphoneNumber);
				 request.addProperty("arg1","password");
		        
		         SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		         envelope.setOutputSoapObject(request);
		        
		         // Make the soap call.
		         HttpTransportSE androidHttpTransport = new HttpTransportSE(com.proximizer.client.Global.URL);
		         androidHttpTransport.debug=true ;
		         try {

		             //this is the actual part that will call the webservice
		             androidHttpTransport.call(com.proximizer.client.Global.SOAP_ACTION, envelope);        
		         } catch (Exception e) {
		             e.printStackTrace(); 
		         }
		         finally
		         {
		        	 String inputRequest =androidHttpTransport.requestDump ;
		             //Log.d(TAG,"Input XML request "+inputRequest);
		             
		             String outputResponse =androidHttpTransport.responseDump ;
		             //Log.d(TAG,"Output XML response "+outputResponse);
		         }
		         
		      // Get the SoapResult from the envelope body.       
		         SoapObject result = (SoapObject)envelope.bodyIn;
		         //Log.d("Soap response","result  is ....."+result);
		         
		         if (result !=null)
		         {
		        	 String authresult =result.getProperty(0).toString();
		        	 if (authresult.equals("true"))
		        	 {
		        			 isRegister=true;
		        			 
		        	 }
		         }
		         
				return isRegister ;
				
			} catch (Exception e) {
				return false;
			}

		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mRegiTask = null;
			showRegistrationProgress(false);

			if (success) {
				//finish();
			//need to insert userphonenumber to mysqldatabase 
			
				UserDetailsDataSource userDetailsDataSource = new UserDetailsDataSource(getApplicationContext());
				userDetailsDataSource.open();
				userDetailsDataSource.createUserDetail(mphoneString);
				userDetailsDataSource.close();
				Toast.makeText(getApplicationContext(),"Registration successful", Toast.LENGTH_LONG).show();
				Toast.makeText(getApplicationContext(),"Logging in Now", Toast.LENGTH_LONG).show();
				
				setContentView(R.layout.activity_login);
				
				mLoginStatusView = findViewById(R.id.login_status);
				
				mLoginStatusMessageView= (TextView) findViewById(R.id.login_status_message);
				
				mLoginStatusMessageView.setText("Login under progress");
					showLoginProgress(true);
					mLoginTask = new UserLoginTask(mphoneNumber);
					mLoginTask.execute((Void) null);

				
			} else {
				Toast.makeText(getApplicationContext(),"Registration Unsuccessful. Userid already exists", Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			mRegiTask = null;
			showRegistrationProgress(false);
		}
	}
	
	
	class UserLoginTask extends AsyncTask<Void, Void, String> {
		
		String mPhoneNumber ;
		public UserLoginTask(String phoneNumber) {
			this.mPhoneNumber=phoneNumber ;
		}
		@Override
		protected String doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			String status ="unknown" ;
			try {
			
			//	isAuthenticated=RemoteServices.authenticate(mEmail, mPassword) ;
				
				 final String  METHOD_NAME ="authenticate" ; 
				
				SoapObject request = new SoapObject(com.proximizer.client.Global.NAMESPACE, METHOD_NAME);
				 request.addProperty("arg0",mPhoneNumber);
		         request.addProperty("arg1","password");
		         SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		         envelope.setOutputSoapObject(request);
		        
		         // Make the soap call.
		         HttpTransportSE androidHttpTransport = new HttpTransportSE(com.proximizer.client.Global.URL);
		         androidHttpTransport.debug=true ;
		         Log.d(TAG, "inside UserLoginTask doBackground with phone "+mphoneString);
		         try {

		             //this is the actual part that will call the webservice
		             androidHttpTransport.call(com.proximizer.client.Global.SOAP_ACTION, envelope);        
		         } catch (Exception e) {
		             e.printStackTrace();
		             Log.d(TAG, "Exception occurred in Login "+e);
		             status ="network_error";
		         }
		         finally
		         {
		        	 String inputRequest =androidHttpTransport.requestDump ;
		             Log.d(TAG,"Input XML request "+inputRequest);
		             
		             String outputResponse =androidHttpTransport.responseDump ;
		             Log.d(TAG,"Output XML response "+outputResponse);
		         }
		         
		      // Get the SoapResult from the envelope body.       
		         SoapObject result = (SoapObject)envelope.bodyIn;
		         //Log.d("Soap response","result  is ....."+result);
		         
		         if (result !=null)
		         {
		        	 String authresult =result.getProperty(0).toString();
		        	 if (authresult.equals("true"))
		        			 status="pass";
		        	 else
		        		 status="fail";
		        	 
		         }
		//         
		         
				return status ;
				
			} catch (Exception e) {
				Log.d(TAG, "Exception occurred "+e);
				return "error";
			}

		}

		@Override
		protected void onPostExecute(final String status) {
			mLoginTask = null;
			showLoginProgress(false);

			if (status.equalsIgnoreCase("pass")) {
				//finish();
				//Start the ContactsActivity
				Intent intent = new Intent(RegisterActivity.this,ContactsActivity.class);
				intent.putExtra("LoggedUser",mphoneString );
				startActivity(intent);
			}
				else if(status.equalsIgnoreCase("network_error"))
				{
					AlertDialog alertDialog = new AlertDialog.Builder(
							RegisterActivity.this).create();
					alertDialog.setTitle("Problem");

					alertDialog.setMessage("Looks like there is a connectivity issue !!");

					alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// here you can add functions
						}
					});
					alertDialog.setIcon(R.drawable.ic_launcher);
					alertDialog.show();
				}
			 else if(status.equalsIgnoreCase("fail")) {
				 AlertDialog alertDialog = new AlertDialog.Builder(
							RegisterActivity.this).create();
					alertDialog.setTitle("Problem");

					alertDialog.setMessage("Something is wrong !!");

					alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// here you can add functions
						}
					});
					alertDialog.setIcon(R.drawable.ic_launcher);
					alertDialog.show();
			}
		}

		@Override
		protected void onCancelled() {
			mLoginTask = null;
			showLoginProgress(false);
		}
	}
	
	
	

	
	
}



