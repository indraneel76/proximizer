package com.proximizer.client;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class UserDetailsDataSource {

  // Database fields
  private SQLiteDatabase database;
  private MySQLiteHelper dbHelper;
  private String[] allColumns = { MySQLiteHelper.MY_PHONENO};

  public UserDetailsDataSource(Context context) {
    dbHelper = new MySQLiteHelper(context);
  }

  public void open() throws SQLException {
    database = dbHelper.getWritableDatabase();
  }

  public void close() {
    dbHelper.close();
  }

  public boolean createUserDetail(String phoneNo) {
    boolean status =false ;
	System.out.println("phoneNo passed = "+phoneNo);  
    ContentValues values = new ContentValues();
    values.put(MySQLiteHelper.MY_PHONENO, phoneNo);
    long insertId = database.insert(MySQLiteHelper.USER_DETAIL, null,
        values);
    status=true;
    return status ;
      
  }

  public void deleteComment(String phoneNo) {
    
    System.out.println("Comment deleted with id: " + phoneNo);
    database.delete(MySQLiteHelper.USER_DETAIL, MySQLiteHelper.MY_PHONENO
        + " = " + phoneNo, null);
  }
  public void deleteUserDetail() {
	    
	    System.out.println("Deleteing user details");
	    database.delete(MySQLiteHelper.USER_DETAIL, null, null);
	  }

  public String getMyPhoneNo() {
    String myPhoneNo = new String();
   myPhoneNo="" ;
    try {
    Cursor cursor = database.query(MySQLiteHelper.USER_DETAIL,
        allColumns, null, null, null, null, null);

    cursor.moveToFirst();
   
     myPhoneNo  = cursorToPhoneNumber(cursor);
    
    // make sure to close the cursor
    cursor.close();
    }
    catch (Exception e)
    {
    	System.out.println("Exception occurred = "+e);
    }
    return myPhoneNo;
  }

  private String cursorToPhoneNumber(Cursor cursor) {
    String phoneNo = new String();
    
     phoneNo = (cursor.getString(0));
    return phoneNo;
  }
} 
