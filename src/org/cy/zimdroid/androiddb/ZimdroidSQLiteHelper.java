package org.cy.zimdroid.androiddb;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ZimdroidSQLiteHelper extends SQLiteOpenHelper {

	public ZimdroidSQLiteHelper(String path) {
		super(null, null, null, 1);
		this.path = path;
		this.database = SQLiteDatabase.openOrCreateDatabase(path, null);
	}

	private SQLiteDatabase database;
	private String path;
	
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public SQLiteDatabase getReadableDatabase()
	{
	    database = SQLiteDatabase.openDatabase(path, null,
	            SQLiteDatabase.OPEN_READONLY);
	    return database;
	}

	@Override
	public SQLiteDatabase getWritableDatabase()
	{
	    database = SQLiteDatabase.openDatabase(path, null,
	            SQLiteDatabase.OPEN_READWRITE);
	    return database;
	}

}
