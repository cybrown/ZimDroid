package org.cy.zimdroid;


import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.cy.zimjava.entity.Content;
import org.cy.zimjava.entity.Notebook;
import org.cy.zimjava.entity.Page;
import org.cy.zimjava.util.Path;
import org.cy.zimjava.util.ZimSyntax;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * View pages in one folder.
 * Caller: PageBrowser, NotebookChooser.
 * @author sigh
 *
 */
public class ZimDroidActivity extends Activity implements OnItemClickListener {
	
	class HistoryManager implements OnClickListener {

		Map<View, Page> map;
		
		public HistoryManager() {
			this.map = new LinkedHashMap<View, Page>();
		}
		
		public void addPage(View view, Page page) {
			this.map.put(view, page);
		}
		
		@Override
		public void onClick(View view) {
			Page page = this.map.get(view);
			if (page != null) {
				viewContent(page, true, false);
			}
		}
		
		protected long[] getIds() {
			long[] ids = new long[this.map.size()];
			int i = 0;
			for (Page page: this.map.values()) {
				ids[i] = page.getId();
				i++;
			}
			return ids;
		}
		
		protected void addToHistory(Page p) {
			Button btn = new Button(getApplicationContext());
			btn.setText(p.getBasename());
			LinearLayout lytHistory = (LinearLayout)findViewById(R.id.lytHistory);
			HorizontalScrollView hsv = (HorizontalScrollView)findViewById(R.id.scrlHistory);
			lytHistory.addView(btn);
			// Scroll to end
			// TODO Bug this do not scroll to last button.
			this.addPage(btn, p);
			btn.setOnClickListener(this);
			hsv.scrollBy(hsv.getWidth(), 0);
		}
		
		protected void setIds(long[] ids) {
			for (int i = 0; i < ids.length; i++) {
				Page page = getNotebook().findById(ids[i]);
				if (page != null) {
					this.addToHistory(page);
				}
			}
		}
	}
	
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
	private boolean navShown;
	private boolean fullscreen;
	
	// Generated
	private LinkedList<String> basenames;
	private ListView lstNotes;
	private ArrayAdapter<String> adapter;
	private WebView wv;
	private long lastBackPressed;
	
	//???
	private String notebook_uri;
	private HistoryManager historyManager;
	
	// Activity Life Cycle
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LIFECYCLE", "Activity MainActivity onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        historyManager = new HistoryManager();
        lstNotes = (ListView)(findViewById(R.id.lstNotes));
        lstNotes.setOnItemClickListener(this);
        
    	// Initializing WebView
    	this.wv = new WebView(this);
    	final ZimDroidActivity that = this;
    	this.wv.setWebViewClient(new WebViewClient() {
    		@Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
    			Path path = currentViewerPage.getPath().clone().fromZimPath(url);
    			Log.d("CY", "Go to page: " + path.toString());
    			Page pageToGo = that.getNotebook().createByPath(path);
    			if (pageToGo != null) {
    				that.viewContent(pageToGo, true, true);
    			}
    			return true;
    		}
    	});
        
        // Var initialization
        long current_id = 0;
        basenames = new LinkedList<String>();
        
        // Initialize from bundle
        long[] saved_history = new long[0];
        if (savedInstanceState != null) {
        	current_id = savedInstanceState.getLong("currentBrowserPageId");
        	this.notebook_uri = savedInstanceState.getString("notebookUri");

	            this.showSource = savedInstanceState.getBoolean("showSource");
	            this.body = savedInstanceState.getString("body");
	            this.showBody();
	            this.bodyIsModified = savedInstanceState.getBoolean("bodyIsModified");
	            ((ToggleButton)findViewById(R.id.btnSource)).setChecked(this.showSource);
	            long page_id = savedInstanceState.getLong("currentViewerPageId", 0);
	            if (page_id != -1)
	            	this.currentViewerPage = this.getNotebook().findById(page_id);
	        	this.navShown = savedInstanceState.getBoolean("navShown");
	        	this.fullscreen = savedInstanceState.getBoolean("fullscreen");
	        	saved_history = savedInstanceState.getLongArray("history");
        }
        else {
        	// Remove this part ???
        	// TODO Create activity to choose notebook
        	String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
        	this.notebook_uri = sdcard_path + "/zim";
        	this.navShown = true;
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
		
		// Restore history
		this.historyManager.setIds(saved_history);
		
		// Restore state of nav panel
		((View)findViewById(R.id.lytNav)).setVisibility(this.navShown ? View.VISIBLE : View.GONE);
		
		// Restore fullscreenness
		this.applyFullscreenness();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d("LIFECYCLE", "Activity MainActivity onSaveInstanceState");
        savedInstanceState.putString("notebookUri", this.notebook_uri);
    	savedInstanceState.putLong("currentBrowserPageId", this.currentBrowserPage.getId());
    	savedInstanceState.putLong("currentViewerPageId", this.currentViewerPage == null ? -1 : this.currentViewerPage.getId());
        savedInstanceState.putBoolean("showSource", this.showSource);
        savedInstanceState.putString("body", this.getCurrentBody());
        savedInstanceState.putBoolean("bodyIsModified", this.bodyIsModified);
        savedInstanceState.putBoolean("navShown", this.navShown);
        savedInstanceState.putBoolean("fullscreen", this.fullscreen);
        savedInstanceState.putLongArray("history", this.historyManager.getIds());
    }
    
    @Override
    public void onDestroy() {
        Log.d("LIFECYCLE", "Activity MainActivity onDestroy");
		((ZimdroidApplication)this.getApplication()).releaseNotebook();
    	super.onDestroy();
    }

    // Hard buttons
	
	@Override
	public void onBackPressed() {
		// Press twice to quit app
		long now = (new Date()).getTime();
		if ((now - this.lastBackPressed) < 1000) {
			this.finish();
			return;
		}
		else {
			this.lastBackPressed = now;
			Toast.makeText(this, "Press return on more time to quit.", Toast.LENGTH_SHORT).show();
		}
	}
	
	// Events

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Page page = this.currentBrowserPage.getChild(this.basenames.get(arg2));
		if (page.hasChildren()) {
			this.setCurrentBrowserPage(page);
		}
		this.viewContent(page, false, true);
	}
	
	public void btn_parent_click(View v) {
		this.goParent();
	}
	
	public void btn_nav_click(View v) {
		View lytNav = (View)findViewById(R.id.lytNav);
		this.navShown = lytNav.getVisibility() == View.GONE;
		lytNav.setVisibility(lytNav.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
	}
    
	public void btn_source_click(View v) {
    	this.showSource = ((ToggleButton) v).isChecked();
    	this.showBody();
    }

	public void btn_fullscreen_click(View v) {
		this.fullscreen = ((ToggleButton)v).isChecked();
		this.applyFullscreenness();
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
	
	protected void viewContent(Page page, boolean setInBrowser, boolean addToHistory) {
		// Initialize visualizer state
        this.showSource = false;
        this.body = page.hasContent() ? page.getContent().getBody() : "";
        this.bodyIsModified = false;
        this.currentViewerPage = page;
        this.showBody();
        if (addToHistory) {
        	this.historyManager.addToHistory(page);
        }
        if (setInBrowser) {
        	if (page.hasChildren()) {
        		this.setCurrentBrowserPage(page);
        	}
        	else {
        		this.setCurrentBrowserPage(page.getParent());
        	}
        }
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

    protected void applyFullscreenness() {
    	if (this.fullscreen) {
    		Log.d("CY", "Go full screen.");
    		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    		findViewById(R.id.tvPath).setVisibility(View.GONE);
    		findViewById(R.id.scrlHistory).setVisibility(View.GONE);
    	}
    	else {
    		Log.d("CY", "Remove fullscreen.");
    		getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    		findViewById(R.id.tvPath).setVisibility(View.VISIBLE);
    		findViewById(R.id.scrlHistory).setVisibility(View.VISIBLE);
    	}
    	((ToggleButton)findViewById(R.id.btnFullscreen)).setChecked(this.fullscreen);
    }
}
