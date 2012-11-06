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
	private Page page;	// long pageId
	
	// Bundle
	private boolean show_source;
	private boolean body_is_modified;
	private String body;
	
	
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
        this.page = ((ZimdroidApplication)this.getApplication()).getNotebook().findById(page_id);
        
        // Restore state from bundle
        if (savedInstanceState == null) {
            this.show_source = false;
            this.body = this.page.hasContent() ? this.page.getContent().getBody() : "";
            this.showBody();
        }
        else {
            this.show_source = savedInstanceState.getBoolean("show_source");
            this.body = savedInstanceState.getString("body");
            this.showBody();
            this.body_is_modified = savedInstanceState.getBoolean("body_is_modified");
            ((ToggleButton)findViewById(R.id.btnSource)).setChecked(this.show_source);
        }
        
        tvContentPadPath.setText(this.page.getPath().toString());
    }
    
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("show_source", this.show_source);
        savedInstanceState.putString("body", this.getCurrentBody());
        savedInstanceState.putBoolean("body_is_modified", this.body_is_modified);
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
    	this.show_source = ((ToggleButton) v).isChecked();
    	this.showBody();
    }
    
    // Private methods
    
    protected String getCurrentBody() {
		LinearLayout lytViewer = (LinearLayout)findViewById(R.id.lytViewer);
		if (lytViewer == null) {
			Log.w("CY", "Should not be executed...");
			return this.body;
		}
		else if (this.body_is_modified) {
    		return ((EditText)lytViewer.getChildAt(0)).getText().toString();
    	}
    	else {
    		return this.body;
    	}
    }
    
    protected void showBody() {
    	LinearLayout lytViewer = (LinearLayout)findViewById(R.id.lytViewer);
        View viewToAdd = null;
        if (this.show_source) {
        	EditText txtBody = new EditText(this);
            txtBody.setText(this.body);
            viewToAdd = txtBody;
            this.body_is_modified = true;
        }
        else {
        	this.body_is_modified = false;
        	this.body = this.getCurrentBody();
        	WebView wv = new WebView(this);
            wv.loadData(ZimSyntax.toHtml(this.body), "text/html", "ISO-8859-1");
            viewToAdd = wv;
        }

    	lytViewer.removeAllViews();
    	lytViewer.addView(viewToAdd);
    }
    
    protected void saveBodyToPage() {
    	if (!this.page.hasContent() && (!this.body.equals(""))) {
    		this.page.setContent(new Content());
    	}
    	else if (this.page.hasContent()) {
    		this.page.getContent().setBody(this.body);
    	}
    }
}
