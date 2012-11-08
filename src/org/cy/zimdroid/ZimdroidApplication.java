package org.cy.zimdroid;

import org.cy.zimjava.entity.Notebook;

import android.app.Application;

public class ZimdroidApplication extends Application {
	private Notebook notebook;
	
	public Notebook getNotebook() {
		return notebook;
	}

	public void setNotebook(Notebook notebook) {
		this.notebook = notebook;
	}
}
