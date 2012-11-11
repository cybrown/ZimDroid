package org.cy.zimdroid.androiddb;

import java.io.File;
import java.io.IOException;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ZimdroidSQLiteHelper extends SQLiteOpenHelper {

	private SQLiteDatabase database;
	private String path;
	private boolean mustCreate;
	
	public ZimdroidSQLiteHelper(String path) {
		super(null, null, null, 1);
		this.path = path;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE meta (key TEXT,value TEXT)");
		db.execSQL("CREATE TABLE pages (id INTEGER PRIMARY KEY, basename TEXT, parent INTEGER DEFAULT '0', hascontent BOOLEAN, haschildren BOOLEAN, type INTEGER, ctime TIMESTAMP, mtime TIMESTAMP, contentkey FLOAT, childrenkey FLOAT)");
		db.execSQL("CREATE TABLE pagetypes (id INTEGER PRIMARY KEY,label TEXT)");
		db.execSQL("CREATE TABLE links (source INTEGER,href INTEGER,type INTEGER,CONSTRAINT uc_LinkOnce UNIQUE (source, href, type))");
		db.execSQL("CREATE TABLE linktypes (id INTEGER PRIMARY KEY,label TEXT)");
		db.execSQL("CREATE TABLE tags (id INTEGER PRIMARY KEY,name TEXT)");
		db.execSQL("CREATE TABLE tagsources (source INTEGER,tag INTEGER,CONSTRAINT uc_TagOnce UNIQUE (source, tag))");
		ContentValues cv = new ContentValues();
		cv.put("id", 1);
		cv.put("basename", "");
		cv.put("parent", 0);
		cv.put("hascontent", 0);
		cv.put("haschildren", 1);
		db.insert("pages", null, cv);
		cv.remove("id");
		cv.put("basename", "Home");
		cv.put("parent", 1);
		cv.put("hascontent", 0);
		cv.put("haschildren", 0);
		db.insert("pages", null, cv);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// Will be necessary in the future
	}
	
	@Override
	public SQLiteDatabase getReadableDatabase()
	{
		verifyDbFile();
	    database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.CREATE_IF_NECESSARY);
	    if (this.mustCreate) {
	    	this.onCreate(this.database);
			this.mustCreate = false;
	    }
	    return database;
	}
	
	@Override
	public SQLiteDatabase getWritableDatabase()
	{
		verifyDbFile();
	    database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);
	    if (this.mustCreate) {
	    	this.onCreate(this.database);
			this.mustCreate = false;
	    }
	    return database;
	}

	private void verifyDbFile() {
		File f = new File(this.path);
		if (!f.isFile()) {
			try {
				f.createNewFile();
				this.mustCreate = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
