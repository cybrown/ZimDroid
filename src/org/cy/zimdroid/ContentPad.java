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
	private Page page;
	
	// TODO Set state to these properties
	private boolean show_source;
	private boolean body_is_modified;
	private String body;
	
	
	// Activities Life Cycle
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("LIFECYCLE", "Activity ContentPad onCreate");
        this.show_source = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_pad);
        TextView tvContentPadPath = (TextView)findViewById(R.id.tvContentPadPath);
        Log.d("CY", Long.toString(this.getIntent().getLongExtra("pageId", 42)));
        this.page = ((ZimdroidApplication)this.getApplication()).getNotebook().findById(this.getIntent().getLongExtra("pageId", 42));
        tvContentPadPath.setText(this.page.getPath().toString());
        this.body = this.page.hasContent() ? this.page.getContent().getBody() : "";
        this.showBody();
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
        	if (this.body_is_modified) {
        		EditText tmp = (EditText)lytViewer.getChildAt(0);
        		this.body = tmp.getText().toString();
        		this.body_is_modified = false;
        	}
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
