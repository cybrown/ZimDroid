package org.cy.zimdroid;

import org.cy.zimjava.entity.Content;
import org.cy.zimjava.entity.Notebook;
import org.cy.zimjava.entity.Page;
import org.cy.zimjava.util.Path;
import org.cy.zimjava.util.ZimSyntax;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * View and modify a page.
 * 
 * Caller Activity: PageBrowser.
 * @author sigh
 *
 */
public class ContentPad extends Activity {

	// Intent
	private Page page;	// AS long pageId
	
	// Bundle
	private boolean showSource;
	private boolean bodyIsModified;
	private String body;	// FROM this.getBody()
	
	WebView wv;
	
	private Notebook notebook;
	private Notebook getNotebook() {
		if (this.notebook == null) {
			try {
				this.notebook = ((ZimdroidApplication)this.getApplication()).getNotebook();
			}
			catch (Exception ex) {
	        	Log.w("CY", "Notebook is null in ContentPad activity.");
	        	Toast.makeText(this, "Notebook not definied", Toast.LENGTH_LONG).show();
	        	this.finish();
			}
		}
		return this.notebook;
	}
	
	// Activities Life Cycle
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("LIFECYCLE", "Activity ContentPad onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_pad);
        TextView tvContentPadPath = (TextView)findViewById(R.id.tvContentPadPath);
        long page_id = this.getIntent().getLongExtra("pageId", 0);
        if (page_id == 0) {
        	Log.e("CY", "Page Id from Intant for ContentPad activity must not be 0.");
        	finish();
        }
    	this.page = this.getNotebook().findById(page_id);

        
        // Restore state from bundle
        if (savedInstanceState == null) {
            this.showSource = false;
            this.body = this.page.hasContent() ? this.page.getContent().getBody() : "";
            this.showBody();
        }
        else {
            this.showSource = savedInstanceState.getBoolean("showSource");
            this.body = savedInstanceState.getString("body");
            this.showBody();
            this.bodyIsModified = savedInstanceState.getBoolean("bodyIsModified");
            ((ToggleButton)findViewById(R.id.btnSource)).setChecked(this.showSource);
        }
        
        tvContentPadPath.setText(this.page.getPath().toString());
    	this.wv = new WebView(this);
    	final ContentPad that = this;
    	this.wv.setWebViewClient(new WebViewClient() {
    		@Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
    			Path path = page.getPath().clone().fromZimPath(url);
    			Log.d("CY", "Go to page: " + path.toString());
    			Page pageToGo = that.getNotebook().createByPath(path);
    			if (pageToGo != null) {
        			Intent intent = new Intent(that, ContentPad.class);
        			intent.putExtra("pageId", pageToGo.getId());
        			startActivity(intent);
    			}
    			return true;
    		}
    	});
    }
    
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("showSource", this.showSource);
        savedInstanceState.putString("body", this.getCurrentBody());
        savedInstanceState.putBoolean("bodyIsModified", this.bodyIsModified);
    }
    
    @Override
    public void onPause() {
        Log.d("LIFECYCLE", "Activity ContentPad onPause");
    	this.saveBodyToPage();
    	super.onPause();
    }
    
    @Override
    public void onDestroy() {
        Log.d("LIFECYCLE", "Activity ContentPad onDestroy");
        ((ZimdroidApplication)this.getApplication()).releaseNotebook();
    	super.onDestroy();
    }

    // Hard buttons
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_content_pad, menu);
        return true;
    }
    
    // Events
    
    public void btn_source_click(View v) {
    	this.showSource = ((ToggleButton) v).isChecked();
    	this.showBody();
    }
    
    // Private methods
    
    protected String getCurrentBody() {
		LinearLayout lytViewer = (LinearLayout)findViewById(R.id.lytViewer);
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
    	LinearLayout lytViewer = (LinearLayout)findViewById(R.id.lytViewer);
        View viewToAdd = null;
        if (this.showSource) {
        	EditText txtBody = new EditText(this);
            txtBody.setText(this.body);
            viewToAdd = txtBody;
            this.bodyIsModified = true;
        }
        else {
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
    	if (!this.page.hasContent() && (!this.body.equals(""))) {
    		this.page.setContent(new Content(this.page));
    	}
    	// If page has a content, set it's body
    	if (this.page.hasContent()) {
    		this.page.setBody(this.getCurrentBody());
    	}
    }
}
