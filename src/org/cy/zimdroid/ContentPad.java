package org.cy.zimdroid;

import org.cy.zimjava.entity.Notebook;
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
	Notebook notebook;
	Page page;
	
	// Activities Life Cycle
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("LIFECYCLE", "Activity ContentPad onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_pad);
        Log.d("CY", this.getIntent().getStringExtra("notebookPath"));
        Log.d("CY", Long.toString(this.getIntent().getLongExtra("pageId", 42)));
        this.notebook = new Notebook(this.getIntent().getStringExtra("notebookPath"));
        this.notebook.open();
        this.page = this.notebook.findById(this.getIntent().getLongExtra("pageId", 42));
        this.showBody();
    }
    
    @Override
    public void onStart() {
        Log.d("LIFECYCLE", "Activity ContentPad onStart");
        super.onStart();
    }
    
    @Override
    protected void onDestroy() {
        Log.d("LIFECYCLE", "Activity ContentPad onDestroy");
    	this.notebook.close();
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
    	boolean on = ((ToggleButton) v).isChecked();
    	if (on) {
    		
    	}
    	else {
    		
    	}
    }
    
    // Private methods
    
    protected void showBody() {
    	EditText txtBody = new EditText(this);
    	LinearLayout lytViewer = (LinearLayout)findViewById(R.id.lytViewer);
        txtBody.setText(this.page.getContent().getBody());
        
        WebView wv = new WebView(this);
        Log.i("CY", ZimSyntax.toHtml(this.page.getContent().getBody()));
        wv.loadData(ZimSyntax.toHtml(this.page.getContent().getBody()), "text/html", "utf-16");
        
        View viewToAdd;
        
        //viewToAdd = txtBody;
        viewToAdd = wv;
        
    	lytViewer.removeAllViews();
    	lytViewer.addView(viewToAdd);
    }
    
}
