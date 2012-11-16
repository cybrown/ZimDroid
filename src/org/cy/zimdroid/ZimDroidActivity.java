package org.cy.zimdroid;


import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.cy.zimjava.Path;
import org.cy.zimjava.ZimSyntax;
import org.cy.zimjava.entity.Notebook;
import org.cy.zimjava.entity.Page;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * View pages in one folder.
 * Caller: PageBrowser, NotebookChooser.
 * @author sigh
 *
 */
public class ZimDroidActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {
	
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
	private String body;							// From this.getCurrentBody()
	private Page currentViewerPage;
	private boolean fullscreen;
	private HistoryManager historyManager;
	
	// Generated
	private LinkedList<String> listOfChildrenName;	// From currentBrowserPage.getParent()
	private ArrayAdapter<String> adapter;			// From listOfChildrenName
	private long lastBackPressed;
	PathManager pathManager;
	boolean savingInProgress;
	
	// Views
	private EditText txtBody;
	private View lytNav;
	private ListView lstNotes;
	private WebView wvBody;
	private LinearLayout lytPathbtn;
	
	//???
	private String notebook_uri;	// Get from intent
	Timer autoSave;
	
	// Activity Life Cycle
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LIFECYCLE", "Activity MainActivity onCreate");
        
        // Initializing Window
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        lstNotes = (ListView)(findViewById(R.id.lstNotes));
        lstNotes.setOnItemClickListener(this);
        lstNotes.setOnItemLongClickListener(this);
		this.lytNav = (View)findViewById(R.id.lytNav);
    	this.txtBody = (EditText)findViewById(R.id.txtBody);
    	this.wvBody = (WebView)findViewById(R.id.wvBody);
    	this.lytPathbtn = (LinearLayout)findViewById(R.id.lytPathbtn);
    	final ZimDroidActivity that = this;
    		// Initializing clicking on a link in webview
    	this.wvBody.setWebViewClient(new WebViewClient() {
    		@Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
    			Path path = currentViewerPage.getPath().clone().fromZimPath(url);
    			Log.d("CY", "Go to page: " + path.toString());
    			Page pageToGo = that.getNotebook().createByPath(path);
    			if (pageToGo != null) {
    				that.viewPage(pageToGo);
    			}
    			return true;
    		}
    	});
        
        // Initialize default state
        long current_id = 0;
        this.listOfChildrenName = new LinkedList<String>();
        this.historyManager = new HistoryManager();
		this.pathManager = new PathManager();
    	
        // Initialize state from bundle
        long[] saved_history = new long[0];
        if (savedInstanceState != null) {
        	current_id = savedInstanceState.getLong("currentBrowserPageId");
        	this.notebook_uri = savedInstanceState.getString("notebookUri");
            this.showSource = savedInstanceState.getBoolean("showSource");
            this.body = savedInstanceState.getString("body");
            if (this.showSource) {
            	this.txtBody.setText(this.body);
            }
            this.bodyIsModified = savedInstanceState.getBoolean("bodyIsModified");
            ((ToggleButton)findViewById(R.id.btnSource)).setChecked(this.showSource);
            long page_id = savedInstanceState.getLong("currentViewerPageId", 0);
            if (page_id != -1)
            	this.currentViewerPage = this.getNotebook().findById(page_id);

            // Restore state of nav panel
    		((View)findViewById(R.id.lytNav)).setVisibility(
    				savedInstanceState.getBoolean("navShown") ? View.VISIBLE : View.GONE);
        	this.fullscreen = savedInstanceState.getBoolean("fullscreen");
        	saved_history = savedInstanceState.getLongArray("history");
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
		
		this.browsePage(current_id == 0 ? this.getNotebook().findRoot() : this.getNotebook().findById(current_id));
		this.applyBody();
		
		// Restore history
		this.historyManager.setState(saved_history);
		
		// Restore fullscreenness
		this.applyFullscreen();
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
        savedInstanceState.putBoolean("navShown", this.lytNav.getVisibility() == View.VISIBLE);
        savedInstanceState.putBoolean("fullscreen", this.fullscreen);
        savedInstanceState.putLongArray("history", this.historyManager.getState());
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	Log.d("CY", "Remove autosave timer.");
    	this.autoSave.cancel();
    }
    
    public void onResume() {
    	super.onResume();
    	Log.d("CY", "Set up autosave timer.");
    	this.autoSave = new Timer();
    	final ZimDroidActivity that = this;
    	this.autoSave.schedule(new TimerTask() {
			@Override
			public void run() {
				if (savingInProgress)
					return;
				savingInProgress = true;
				if (that.getNotebook().saveAll()) {
					Log.d("CY", "Autosave...");
				}
				savingInProgress = false;
			}
        }, 1000, 5000);
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

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Page page = this.currentBrowserPage.getChild(this.listOfChildrenName.get(position));
		this.browsePage(page);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		final Page page = this.currentBrowserPage.getChild(this.listOfChildrenName.get(position));
		final ZimDroidActivity that = this;
		new AlertDialog.Builder(this)
		.setTitle("Delete")
		.setMessage("Do you really want to delete page " + page.getBasename() + "?")
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Toast.makeText(ZimDroidActivity.this, page.getBasename() + " deleted...", Toast.LENGTH_SHORT).show();
				that.getNotebook().delete(page);
				that.browsePage(that.currentBrowserPage);
			}
		})
		.setNegativeButton(android.R.string.no, null).show();
		return false;
	}
	
	public void btn_parent_click(View v) {
		this.browsePage(this.currentBrowserPage.getParent());
	}
	
	public void btn_nav_click(View v) {
		this.lytNav.setVisibility(this.lytNav.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
	}
    
	public void btn_source_click(View v) {
    	this.showSource = ((ToggleButton) v).isChecked();
    	this.applyBody();
    }

	public void btn_fullscreen_click(View v) {
		this.fullscreen = ((ToggleButton)v).isChecked();
		this.applyFullscreen();
	}
	
	// Private methods
	
	/**
	 * Put a page in the viewer and browser.
	 * @param page
	 */
	protected void viewPage(Page page) {
		this.viewPage(page, true);
	}
	
	/**
	 * Put a page in the viewer, and in the browser if it has children, otherwise its parent.
	 * @param page
	 * @param addToHistory false if do not add to history.
	 */
	protected void viewPage(Page page, boolean addToHistory) {
		if (page.getId() == 1)	// We can not view the root page
			return;
        this.showSource = false;
        this.body = page.hasContent() ? page.getContent().getBody() : "======" + page.getBasename() + ":\n";
        this.bodyIsModified = false;
        this.currentViewerPage = page;
        if (addToHistory) {
        	this.historyManager.addToHistory(page);
        }
        this.browsePage(page);
        this.applyBody();
	}
	
	/**
	 * Put a page in the browser, loading its children in the list.
	 * @param p
	 */
	protected void browsePage(Page p) {
		if (p == null) {
			Log.w("CY", "Trying to set a null page...");
			return;
		}
		this.currentBrowserPage = p;
		this.listOfChildrenName.clear();
		for (Page tmp: this.currentBrowserPage.getChildren()) {
			listOfChildrenName.add(tmp.getBasename());
		}
		Collections.sort(this.listOfChildrenName, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return arg0.compareToIgnoreCase(arg1);
			}
		});
		
		// Refresh path bar
		this.lytPathbtn.removeAllViews();
		this.pathManager.clear();
		Page cur = p;
		while (cur != null) {
			Button b = new Button(this);
			b.setText(cur.getBasename().length() == 0 ? "ROOT" : cur.getBasename());
			b.setOnClickListener(this.pathManager);
			this.pathManager.addPage(b, cur);
			this.lytPathbtn.addView(b, 0);
			cur = cur.getParent();
		}
		
		this.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listOfChildrenName.toArray(new String[0]));
		lstNotes.setAdapter(this.adapter);
	}

	/**
	 * Return the state of the body in the viewer, get it from EditText if in modification.
	 * @return
	 */
    protected String getCurrentBody() {
    	return this.bodyIsModified
    		? this.txtBody.getText().toString()
    		: this.body;
    }
    
    /**
     * Put current page's body in correct View, if we are editing its sources or viewing in WebView.
     */
    protected void applyBody() {
    	if (this.body == null)
    		return;
    	
    	// Save body to current page, if was modified
    	if (this.bodyIsModified) {
    		this.body = this.txtBody.getText().toString();
    		// Save body in editor into the current viewer's page
        	if (this.currentViewerPage.hasContent() || (this.body.length() != 0)) {
        		this.currentViewerPage.setBody(this.body);
        	}
    		this.bodyIsModified = false;
    	}
    	
    	// Put current body value in correct view
        if (this.showSource) {
            txtBody.setText(this.body);
            this.bodyIsModified = true;	// this.body will be modified in EditText
            this.txtBody.setVisibility(View.VISIBLE);
            this.wvBody.setVisibility(View.GONE);
        }
        else {
        	this.wvBody.loadDataWithBaseURL(null, ZimSyntax.toHtml(this.body), "text/html", "utf-8", null);
            this.txtBody.setVisibility(View.GONE);
            this.wvBody.setVisibility(View.VISIBLE);
        }

        // Show correct view
//        FrameLayout lytViewer = (FrameLayout)findViewById(R.id.lytViewer);
//    	lytViewer.removeAllViews();
//    	lytViewer.addView(this.showSource ? this.txtBody : this.wvBody);
    }

    /**
     * Apply current fullscreen setting.
     */
    protected void applyFullscreen() {
    	getWindow().setFlags(this.fullscreen ? WindowManager.LayoutParams.FLAG_FULLSCREEN : 0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		findViewById(R.id.scrlHistory).setVisibility(this.fullscreen ? View.GONE : View.VISIBLE);
    	((ToggleButton)findViewById(R.id.btnFullscreen)).setChecked(this.fullscreen);
    }

    // Nested classes
    
    /**
     * This class has the responsability of the path bar.
     * @author sigh
     *
     */
    class PathManager implements OnClickListener {

    	private HashMap<View, Page> pathBar;
    	
    	public PathManager() {
    		this.pathBar = new HashMap<View, Page>();
    	}
    	
    	public void addPage(View view, Page page) {
    		this.pathBar.put(view, page);
    	}
    	
    	public void clear() {
    		this.pathBar.clear();
    	}
    	
		@Override
		public void onClick(View view) {
			Page goTo = pathBar.get(view);
			if (goTo != null) {
				// If the page to browse is already the current one, view it
				if (goTo == currentBrowserPage)
					viewPage(goTo);
				else
					browsePage(goTo);
			}
		}
    	
    }
    
	/**
	 * This class has the responsability of create new history buttons, listen when
	 * we click on them, get and set the state to a bundle (array of long).
	 * @author sigh
	 *
	 */
	class HistoryManager implements OnClickListener {

		Map<View, Page> map;
		LinkedList<Page> last5;
		
		public HistoryManager() {
			this.map = new LinkedHashMap<View, Page>();
			this.last5 = new LinkedList<Page>();
		}
		
		/**
		 * Only add page to the history if not in the last 5 visited pages.
		 * @param view
		 * @param page
		 */
		protected void addPage(View view, Page page) {
			this.map.put(view, page);
		}
		
		@Override
		public void onClick(View view) {
			Page page = this.map.get(view);
			if (page != null) {
				viewPage(page, false);	// false parameter not to add page in history
			}
		}
		
		public void addToHistory(Page page) {
			if (!this.last5.contains(page)) {
				Button btn = new Button(getApplicationContext());
				btn.setText(page.getBasename());
				LinearLayout lytHistory = (LinearLayout)findViewById(R.id.lytHistory);
				HorizontalScrollView hsv = (HorizontalScrollView)findViewById(R.id.scrlHistory);
				lytHistory.addView(btn);
				// Scroll to end
				// TODO Bug this does not scroll to last button.
				this.addPage(btn, page);
				btn.setOnClickListener(this);
				hsv.scrollBy(hsv.getWidth(), 0);
				this.last5.add(page);
				if (this.last5.size() > 5) {
					this.last5.removeFirst();
				}
			}
		}
		
		public long[] getState() {
			long[] ids = new long[this.map.size()];
			int i = 0;
			for (Page page: this.map.values()) {
				ids[i] = page.getId();
				i++;
			}
			return ids;
		}
		
		public void setState(long[] ids) {
			if (ids == null)
				return;
			for (int i = 0; i < ids.length; i++) {
				Page page = getNotebook().findById(ids[i]);
				if (page != null) {
					this.addToHistory(page);
				}
			}
		}
	}

}
