package org.cy.zimdroid;


import java.util.LinkedList;

import org.cy.zimjava.entity.Notebook;
import org.cy.zimjava.entity.Page;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * View pages in one folder.
 * Caller: PageBrowser, NotebookChooser.
 * @author sigh
 *
 */
public class PageBrowser extends Activity implements OnItemClickListener {
	
	// Persisted
	private Page currentPage;
	private Notebook notebook;	// to intent
	
	// Generated
	private LinkedList<String> basenames;
	private ListView lstNotes;
	private ArrayAdapter<String> adapter;
	
	// Activity Life Cycle
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LIFECYCLE", "Activity MainActivity onCreate");
        setContentView(R.layout.activity_page_browser);
        lstNotes = (ListView)(findViewById(R.id.lstNotes));
        lstNotes.setOnItemClickListener(this);
        
        // Var initialization
        long current_id = 0;
        basenames = new LinkedList<String>();
        
        // Get Path to Notebook
        String notebook_uri = null;
        if (savedInstanceState != null) {
        	current_id = savedInstanceState.getLong("currentPageId");
        	notebook_uri = savedInstanceState.getString("notebookUri");
        }
        
        // Create default path to notebook
        if (notebook_uri == null) {
        	String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
            notebook_uri = sdcard_path + "/zim";
        }
        // If app notebook is null, create it
		if (((ZimdroidApplication)this.getApplication()).getNotebook() == null) {
			((ZimdroidApplication)this.getApplication()).setNotebook(new Notebook(notebook_uri));
			((ZimdroidApplication)this.getApplication()).getNotebook().open();
		}
		this.notebook = ((ZimdroidApplication)this.getApplication()).getNotebook();
		if (!this.notebook.isOpened())
			this.notebook.open();
		
		// Open last opened page
		Page current_page;
		if (current_id == 0) {
			current_page = this.notebook.findRoot();
		}
		else {
			current_page = this.notebook.findById(current_id);
			if (current_page == null) {
				current_page = this.notebook.findRoot();
			}
		}
		
		this.setCurrentPage(current_id == 0 ? this.notebook.findRoot() : this.notebook.findById(current_id));
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d("LIFECYCLE", "Activity MainActivity onRestoreInstanceState");
    }
    
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d("LIFECYCLE", "Activity MainActivity onSaveInstanceState");
        savedInstanceState.putString("notebookUri", this.notebook.getUri());
    	savedInstanceState.putLong("currentPageId", this.currentPage.getId());
    }
    
    @Override
    public void onDestroy() {
        Log.d("LIFECYCLE", "Activity MainActivity onDestroy");
    	super.onDestroy();
    }

    // Hard buttons
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_page_browser, menu);
        return true;
    }
	
	@Override
	public void onBackPressed() {
		if (this.currentPage == null)
			return;
		if (this.currentPage.getParent() == null) {
			this.notebook.close();
			this.finish();
		}
		this.setCurrentPage(this.currentPage.getParent());
	}
	
	// Events
	
	public void btn_open_click(View v) {
		Intent intent = new Intent(this, ContentPad.class);
		intent.putExtra("pageId", this.currentPage.getId());
		startActivity(intent);
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Log.d("arg2", Integer.toString(arg2));
		Log.d("arg3", Long.toString(arg3));
		this.setCurrentPage(this.currentPage.getChild(this.basenames.get(arg2)));
	}
	
	// Private methods
	
	protected void setCurrentPage(Page p) {
		if (p == null) {
			Log.w("CY", "Trying to set a null page...");
			return;
		}
		Log.d("CY", "Changing page to: " + p.getBasename());
		this.currentPage = p;
		this.basenames.clear();
		for (Page tmp: this.currentPage.getChildren()) {
			basenames.add(tmp.getBasename());
		}
		((TextView)findViewById(R.id.tvPath)).setText(p.getPath().toString());
		((Button)findViewById(R.id.btnOpen)).setVisibility(p.hasContent() ? View.VISIBLE : View.GONE);
		this.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, basenames.toArray(new String[0]));
		lstNotes.setAdapter(this.adapter);
	}
}
