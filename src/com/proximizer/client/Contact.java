package com.proximizer.client;

import java.util.Date;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Contact implements Parcelable {

	private static final String TAG = "Contact";
	public String username ;
	public boolean isNear =false ;
	public android.location.Location loc = new Location("GPS") ;
	public int seperation =9999999 ;
	public boolean toShowNear =true ;
	public Contact()
	{
		this.isNear=false ;
		this.loc = new Location("GPS");
		this.username="Default User";
		
	}
	
	public Contact(String username,Location loc,boolean isNear) 
	{

		
		this.username=username ;
		this.loc =loc ;
		this.isNear=isNear ;
		
		//Log.d(TAG, "Values set in contacts" );
		//Log.d(TAG,"username = "+this.username);
		//Log.d(TAG,"latitude = "+this.loc.getLatitude());
		//Log.d(TAG,"Longitude = "+this.loc.getLongitude());
		//Log.d(TAG,"Datetime = "+this.loc.getTime());
		//Log.d(TAG,"isNear = "+this.isNear);
	}
	
	public Contact(Contact contact)
	{
		this.username=contact.username ;
		this.loc= contact.loc;
		this.isNear=contact.isNear;
	}
	
	public Contact(Parcel in)
	{
		ReadFromParcel(in);
	}
	
	public boolean equals(Object obj) {
	    
		Contact cont = (Contact) obj;
		if (obj == null)
	    	return false;
	    if (obj == this)
	    	return true;
	    if (!(obj instanceof Contact)) 
	    	return false;
	    
	    if (this.username.equals(cont.username)) 
	    	return true ;
	    
	    return false ;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		dest.writeString(username) ; //username
		dest.writeByte((byte) (isNear ? 1 : 0));  //isNear
		dest.writeInt(seperation); //seperation
		dest.writeByte((byte) (toShowNear ? 1 : 0));  //toShowNear
		}
	
	public void ReadFromParcel(Parcel in)
	{
		username=in.readString();
		isNear= in.readByte() != 0; 
		seperation = in.readInt() ;
		toShowNear = in.readByte() != 0; 
		
	}
	

}
