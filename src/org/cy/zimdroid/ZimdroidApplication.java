package org.cy.zimdroid;

import org.cy.zimjava.entity.Notebook;

import android.app.Application;
import android.util.Log;

public class ZimdroidApplication extends Application {
	private Notebook notebook;
	private String notebook_uri;
	private int isNotebookNeeded;
	
	public Notebook createNotebook(String uri) {
		this.notebook_uri = uri;
		this.notebook = new Notebook(this.notebook_uri);
		this.isNotebookNeeded++;
		return this.notebook;
	}
	
	/**
	 * Each activity must use this method once per lifecycle, and use release on destroy.
	 * @return
	 * @throws Exception
	 */
	public Notebook getNotebook() throws Exception {
		if ((this.notebook_uri == null) && (this.notebook == null))
			throw new Exception("Notebook not created.");
		Log.d("CY", "getNotebook: " + Integer.toString(this.isNotebookNeeded));
		if (this.isNotebookNeeded == 0) {
			return this.createNotebook(this.notebook_uri);
		}
		this.isNotebookNeeded++;
		return notebook;
	}
	
	public void releaseNotebook() {
		Log.d("CY", "releaseNotebook: " + Integer.toString(this.isNotebookNeeded));
		this.isNotebookNeeded--;
		if (this.isNotebookNeeded == 0) {
			// Do something to destroy notebook
			this.notebook.close();
			this.notebook = null;
			Log.d("CY", "Notebook destroyed.");
		}
	}
}
