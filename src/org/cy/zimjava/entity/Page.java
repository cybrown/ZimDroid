package org.cy.zimjava.entity;

import java.util.List;

import org.cy.zimjava.dao.PageDAO;
import org.cy.zimjava.util.Path;

import android.util.Log;

public class Page {

	private PageDAO pdao;
	
	// Properties
	private long id;
	private String basename;
	private boolean modified;
	private boolean created;
	
	// Lazy loaded properties
	private Path path;
	private Page parent;
	private Content content;
	private List<Page> children;
	
	// Low level information for lazy loading
	private long parentId;
	
	// Lazy loading states
	private boolean is_parent_loaded;
	private boolean is_content_loaded;
	private boolean is_children_loaded;
	private boolean is_path_loaded;
	
	
	public void hydrate(long id, String basename, long parentId) {
		this.setId(id);
		this.setBasename(basename);
		this.setParentId(parentId);
		this.setModified(false);
		this.setCreated(true);
	}
	
	public Page(PageDAO dao) {
		this.setDAO(dao);
	}
	
	public String getBasename() {
		return basename;
	}
	
	public Page getChild(String basename) {
		for (Page p: this.getChildren()) {
			if (p.getBasename().equals(basename))
				return p;
		}
		return null;
	}
	
	public List<Page> getChildren() {
		if (!this.is_children_loaded && (this.getId() != 0)) {	// Must be saved to have children
			this.children = this.pdao.findByParentId(this.getId());
			this.is_children_loaded = true;
		}
		return children;
	}

	public Content getContent() {
		if (!this.is_content_loaded) {
			this.content = this.pdao.loadContent(this.getPath());
			this.is_content_loaded = true;
		}
		return content;
	}

	public long getId() {
		return id;
	}

	public Page getParent() {
		if (!this.is_parent_loaded) {
			this.parent = this.pdao.findById(this.parentId);
			this.is_parent_loaded = true;
		}
		return parent;
	}

	public long getParentId() {
		return parentId;
	}

	public Path getPath() {
		if (!this.is_path_loaded) {
			this.path = this.getParent() != null
				? this.getParent().getPath().clone()
				: new Path();
			if (this.getBasename().length() != 0)
				this.path.add(this.getBasename());
			this.is_path_loaded = true;
		}
		return this.path;
	}

	public boolean hasChildren() {
		return (this.getChildren() != null) && (this.getChildren().size() != 0);
	}

	public boolean hasContent() {
		return this.getContent() != null;
	}

	public void invalidateChildren() {
		this.setModified(true);
		this.is_children_loaded = false;
		this.children = null;
	}

	public void invalidatePath() {
		this.path = null;
		this.is_path_loaded = false;
		if (this.hasChildren()) {
			for (Page p: this.getChildren()) {
				p.invalidatePath();
			}
		}
	}

	public boolean isChildrenLoaded() {
		return this.is_children_loaded;
	}
	
	public boolean isCreated() {
		return created;
	}

	public boolean isModified() {
		return modified;
	}
	
	public void setBasename(String basename) {
		if (this.basename == null) {
			this.setModified(true);
			this.basename = basename;
		}
		else if (!this.basename.equals(basename)) {
			this.setModified(true);
			Path new_path = this.getPath().clone();
			new_path.getPath().removeLast();
			new_path.add(basename);
			this.pdao.moveFiles(this, new_path);
			this.basename = basename;
			this.invalidatePath();
		}
	}

	public void setBody(String text) {
		this.setModified(true);
		if (!this.hasContent()) {
			this.setContent(new Content());
		}
		this.content.setBody(text);
	}
	
	public void setContent(Content content) {
		if (this.is_content_loaded)
			Log.e("CY", "Loading new Content in Page, but last content is not null");
		this.setModified(true);
		this.is_content_loaded = true;
		this.content = content;
	}

	public void setCreated(boolean value) {
		this.created = value;
	}
	
	public void setDAO(PageDAO dao) {
		this.pdao = dao;
	}

	public void setId(long id) {
		this.setModified(true);
		this.id = id;
	}
	
	public void setModified(boolean modified) {
		if (!this.created)
			return;
		this.modified = modified;
	}
	
	public void setParent(Page parent) {
		this.setParent(parent, true);
	}

	public void setParent(Page parent, boolean doEvents) {
		this.setModified(true);
		this.is_parent_loaded = true;
		// Invalidate old parent's children
		if (this.getParent() != null) {
			this.getParent().invalidateChildren();
		}
		if (doEvents) {
			this.getContent();
			// Move content file
			this.pdao.moveFiles(this, parent.getPath().clone().add(this.getBasename()));
		}
		this.parent = parent;
		this.parentId = parent == null ? 0 : parent.getId();
		// Invalidate new parent's children
		if (this.getParent() != null) {
			this.getParent().invalidateChildren();
		}
		if (doEvents) {
			// Invalidate actual path
			this.invalidatePath();
		}
		if (this.getParent() != null)
			this.getParent().invalidateChildren();
	}
	
	public void setParentId(long parentId) {
		this.setModified(true);
		this.parentId = parentId;
	}
	
	public String toString() {
		return "[" + this.id + "] " + this.basename;
	}
}
