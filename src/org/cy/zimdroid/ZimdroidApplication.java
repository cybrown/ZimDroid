package org.cy.zimdroid;

import org.cy.zimjava.entity.Notebook;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

public class ZimdroidApplication extends Application {
	private SQLiteDatabase db;
	private Notebook notebook;
	
	public Notebook getNotebook() {
		return notebook;
	}

	public void setNotebook(Notebook notebook) {
		this.notebook = notebook;
	}

	public void setDb(SQLiteDatabase db) {
		this.db = db;
	}
	
	public SQLiteDatabase getDb() {
		return this.db;
	}
}
