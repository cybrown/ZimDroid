package org.cy.zimdroid;


import java.util.LinkedList;

import org.cy.zimjava.entity.Content;
import org.cy.zimjava.entity.Notebook;
import org.cy.zimjava.entity.Page;
import org.cy.zimjava.util.Path;
import org.cy.zimjava.util.ZimSyntax;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * View pages in one folder.
 * Caller: PageBrowser, NotebookChooser.
 * @author sigh
 *
 */
public class PageBrowser extends Activity implements OnItemClickListener {
	
	// Application global
	private Notebook notebook;
	private Notebook getNotebook() {
		if (this.notebook == null) {
			try {
				this.notebook = ((ZimdroidApplication)this.getApplication()).getNotebook();
			}
			catch (Exception ex) {
				this.notebook = ((ZimdroidApplication)this.getApplication()).createNotebook(this.notebook_uri);
			}
		}
		return this.notebook;
	}
	
	// Bundle
	private Page currentBrowserPage;
	private boolean showSource;
	private boolean bodyIsModified;
	private String body;	// FROM this.getBody()
	private Page currentViewerPage;
	
	// Generated
	private LinkedList<String> basenames;
	private ListView lstNotes;
	private ArrayAdapter<String> adapter;
	private WebView wv;
	
	//???
	private String notebook_uri;
	
	// Activity Life Cycle
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LIFECYCLE", "Activity MainActivity onCreate");
        setContentView(R.layout.activity_main);
        lstNotes = (ListView)(findViewById(R.id.lstNotes));
        lstNotes.setOnItemClickListener(this);
        
    	// Initializing WebView
    	this.wv = new WebView(this);
    	final PageBrowser that = this;
    	this.wv.setWebViewClient(new WebViewClient() {
    		@Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
    			Path path = currentViewerPage.getPath().clone().fromZimPath(url);
    			Log.d("CY", "Go to page: " + path.toString());
    			Page pageToGo = that.getNotebook().createByPath(path);
    			if (pageToGo != null) {
    				that.viewContent(pageToGo);
    			}
    			return true;
    		}
    	});
        
        // Var initialization
        long current_id = 0;
        basenames = new LinkedList<String>();
        
        // Initialize from bundle
        if (savedInstanceState != null) {
        	current_id = savedInstanceState.getLong("currentBrowserPageId");
        	this.notebook_uri = savedInstanceState.getString("notebookUri");

	            this.showSource = savedInstanceState.getBoolean("showSource");
	            this.body = savedInstanceState.getString("body");
	            this.showBody();
	            this.bodyIsModified = savedInstanceState.getBoolean("bodyIsModified");
	            ((ToggleButton)findViewById(R.id.btnSource)).setChecked(this.showSource);
	            long page_id = savedInstanceState.getLong("currentViewerPageId", 0);
	        	this.currentViewerPage = this.getNotebook().findById(page_id);
        }
        else {
        	// Remove this part ???
        	// TODO Create activity to choose notebook
        	String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
        	this.notebook_uri = sdcard_path + "/zim";
        }
		
		// Open last open page
		Page current_page;
		if (current_id == 0) {
			current_page = this.getNotebook().findRoot();
		}
		else {
			current_page = this.getNotebook().findById(current_id);
			if (current_page == null) {
				current_page = this.getNotebook().findRoot();
			}
		}
		
		this.setCurrentBrowserPage(current_id == 0 ? this.getNotebook().findRoot() : this.getNotebook().findById(current_id));
		this.showBody();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d("LIFECYCLE", "Activity MainActivity onSaveInstanceState");
        savedInstanceState.putString("notebookUri", this.notebook_uri);
    	savedInstanceState.putLong("currentBrowserPageId", this.currentBrowserPage.getId());
    	savedInstanceState.putLong("currentViewerPageId", this.currentViewerPage.getId());
        savedInstanceState.putBoolean("showSource", this.showSource);
        savedInstanceState.putString("body", this.getCurrentBody());
        savedInstanceState.putBoolean("bodyIsModified", this.bodyIsModified);
    }
    
    @Override
    public void onDestroy() {
        Log.d("LIFECYCLE", "Activity MainActivity onDestroy");
		((ZimdroidApplication)this.getApplication()).releaseNotebook();
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
		// TODO Press twice to quit app
		if (!this.goParent()) {
			this.finish();
		}
	}
	
	// Events

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Page page = this.currentBrowserPage.getChild(this.basenames.get(arg2));
		if (page.hasChildren()) {
			this.setCurrentBrowserPage(page);
		}
		this.viewContent(page);
	}
	
	public void btn_parent_click(View v) {
		this.goParent();
	}
	
	public void btn_nav_click(View v) {
		View lytNav = (View)findViewById(R.id.lytNav);
		lytNav.setVisibility(lytNav.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
	}
    
	public void btn_source_click(View v) {
    	this.showSource = ((ToggleButton) v).isChecked();
    	this.showBody();
    }

	// Private methods
	
	protected boolean goParent() {
		if (this.currentBrowserPage == null)
			return true;
		if (this.currentBrowserPage.getParent() == null) {
			return false;
		}
		this.setCurrentBrowserPage(this.currentBrowserPage.getParent());
		return true;
	}
	
	protected void viewContent(Page page) {
		// Initialize visualizer state
        this.showSource = false;
        this.body = page.hasContent() ? page.getContent().getBody() : "";
        this.bodyIsModified = false;
        this.currentViewerPage = page;
        this.showBody();
	}
	
	protected void setCurrentBrowserPage(Page p) {
		if (p == null) {
			Log.w("CY", "Trying to set a null page...");
			return;
		}
		Log.d("CY", "Changing page to: " + p.getBasename());
		this.currentBrowserPage = p;
		this.basenames.clear();
		for (Page tmp: this.currentBrowserPage.getChildren()) {
			basenames.add(tmp.getBasename());
		}
		((TextView)findViewById(R.id.tvPath)).setText(p.getPath().toString());
		this.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, basenames.toArray(new String[0]));
		lstNotes.setAdapter(this.adapter);
	}

    protected String getCurrentBody() {
    	FrameLayout lytViewer = (FrameLayout)findViewById(R.id.lytViewer);
		if (lytViewer == null) {
			Log.w("CY", "Should not be executed...");
			return this.body;
		}
		else if (this.bodyIsModified) {
    		return ((EditText)lytViewer.getChildAt(0)).getText().toString();
    	}
    	else {
    		return this.body;
    	}
    }
    
    protected void showBody() {
    	if (this.body == null)
    		return;
    	FrameLayout lytViewer = (FrameLayout)findViewById(R.id.lytViewer);
        View viewToAdd = null;
        if (this.showSource) {
        	EditText txtBody = new EditText(this);
            txtBody.setText(this.body);
            viewToAdd = txtBody;
            this.bodyIsModified = true;
        }
        else {
        	if (this.bodyIsModified) {
        		this.saveBodyToPage();
        	}
        	this.body = this.getCurrentBody();
        	this.bodyIsModified = false;
            this.wv.loadDataWithBaseURL(null, ZimSyntax.toHtml(this.body), "text/html", "utf-8", null);
            viewToAdd = this.wv;
        }

    	lytViewer.removeAllViews();
    	lytViewer.addView(viewToAdd);
    }
    
    protected void saveBodyToPage() {
    	// If page doesn't has a content and body is not empty, create a new content
    	if (!this.currentViewerPage.hasContent() && (!this.body.equals(""))) {
    		this.currentViewerPage.setContent(new Content(this.currentViewerPage));
    	}
    	// If page has a content, set it's body
    	if (this.currentViewerPage.hasContent()) {
    		this.currentViewerPage.setBody(this.getCurrentBody());
    	}
    }
}
