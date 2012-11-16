package org.cy.zimdroid;

import java.io.File;

import org.cy.zimdroid.androiddb.ZimdroidSQLiteHelper;
import org.cy.zimdroid.dao.AndroidSQLitePageRecordDAO;
import org.cy.zimjava.entity.Notebook;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ZimdroidApplication extends Application {
	private Notebook notebook;
	private String notebook_uri;
	private int isNotebookNeeded;
	private SQLiteDatabase db;
	
	public Notebook createNotebook(String uri) {
		this.notebook_uri = uri;
		
		// Create or fetch database
		File f = new File(this.notebook_uri + "/.zim");
		if (!f.isDirectory()) {
			f.mkdirs();
		}
		ZimdroidSQLiteHelper zsh = new ZimdroidSQLiteHelper(this.notebook_uri + "/.zim/index.db");
		this.db = zsh.getWritableDatabase();
		
		// Create necessary DAO objects
		AndroidSQLitePageRecordDAO prdao = new AndroidSQLitePageRecordDAO(this.db);
		
		// Create notebook
		this.notebook = new Notebook(this.notebook_uri, prdao);
		this.notebook.open();
		
		this.isNotebookNeeded++;
		Log.d("CY", "Creating Notebook, remaining uses: " + Integer.toString(this.isNotebookNeeded));
		return this.notebook;
	}
	
	/**
	 * Each activity must use this method once per lifecycle, and use release on destroy.
	 * @return
	 * @throws Exception
	 */
	public Notebook getNotebook() throws Exception {
		// TODO Review condition of recreation
		if ((this.notebook_uri == null) && (this.notebook == null))
			throw new Exception("Notebook not created.");
		if (this.isNotebookNeeded == 0) {
			return this.createNotebook(this.notebook_uri);
		}
		this.isNotebookNeeded++;
		Log.d("CY", "Getting Notebook, remaining uses: " + Integer.toString(this.isNotebookNeeded));
		return this.notebook;
	}
	
	public void releaseNotebook() {
		this.isNotebookNeeded--;
		Log.d("CY", "Releasing Notebook, remaining uses: " + Integer.toString(this.isNotebookNeeded));
		if (this.isNotebookNeeded == 0) {
			// Do something to destroy notebook
			this.notebook.close();
			this.notebook = null;
			Log.d("CY", "Notebook destroyed.");
		}
	}
}
