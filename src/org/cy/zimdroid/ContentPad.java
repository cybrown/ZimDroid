package org.cy.zimdroid;

import org.cy.zimjava.entity.Content;
import org.cy.zimjava.entity.Page;
import org.cy.zimjava.util.ZimSyntax;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
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
        try {
        	this.page = ((ZimdroidApplication)this.getApplication()).getNotebook().findById(page_id);
        }
        catch (Exception ex) {
        	Log.w("CY", "Notebook is null in ContentPad activity.");
        	Toast.makeText(this, "Notebook not definied", Toast.LENGTH_LONG).show();
        	this.finish();
        }
        
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
        	WebView wv = new WebView(this);
            wv.loadDataWithBaseURL(null, ZimSyntax.toHtml(this.body), "text/html", "utf-8", null);
            viewToAdd = wv;
        }

    	lytViewer.removeAllViews();
    	lytViewer.addView(viewToAdd);
    }
    
    protected void saveBodyToPage() {
    	if (!this.page.hasContent() && (!this.body.equals(""))) {
    		this.page.setContent(new Content(this.page));
    	}
    	else if (this.page.hasContent()) {
    		this.page.getContent().setBody(this.body);
    	}
    }
}
