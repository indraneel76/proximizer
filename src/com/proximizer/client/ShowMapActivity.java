package com.proximizer.client;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

import com.google.android.maps.GeoPoint;

import com.google.android.maps.MapActivity ;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class ShowMapActivity extends FragmentActivity {

	 private GoogleMap mMap;
	private static final String TAG = "ShowMapActivity";
	private MyLocationOverlay myLocationOverLay;
	static private double Longitude ;
	static private double Latitude ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_show_map);
		MapView mapview = (MapView) findViewById(R.id.mapview);
		mapview.setBuiltInZoomControls(true) ;
		mapview.setSatellite(true);
		myLocationOverLay = new MyLocationOverlay(this, mapview);
		
		//Get the intent extra params
		Intent intent1 = getIntent() ;
		Bundle b = intent1.getExtras();
		//Log.d(TAG, "Inside ShowMapActivity") ;
		Latitude = b.getDouble("latitude") ;
		Longitude = b.getDouble("longitude") ;
		//Log.d(TAG,"Latitude ="+Latitude);
		//Log.d(TAG,"Longitude ="+Longitude);
		//Creating a geoPoint
		
	//	GeoPoint geoPoint = new GeoPoint((int)(latitude*1E6), (int)(longitude*1E6));
	//	MapController mc = mapview.getController() ;
	//	mc.setZoom(18);
	//	mc.animateTo(geoPoint) ;
		setUpMapIfNeeded();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_show_map, menu);
		return true;
	}



	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
	
		super.onResume();
		setUpMapIfNeeded();
	}
	
	private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap(Latitude,Longitude);
            }
        }
    }
	
    private void setUpMap(double latitude,double longitude) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("Marker"));
    }
}



