package org.cy.zimjava.entity;

import java.util.List;

import org.cy.zimjava.dao.PageDAO;
import org.cy.zimjava.util.IContentHost;
import org.cy.zimjava.util.Path;

public class Page implements IContentHost {
	
	public String toString() {
		return this.basename + ":\n" + this.getContent();
	}
	
	private PageDAO pdao;
	
	public void setDAO(PageDAO dao) {
		this.pdao = dao;
	}
	
	public Page(PageDAO dao) {
		this.setDAO(dao);
		this.is_children_loaded = false;
		this.is_content_loaded = false;
		this.is_parent_loaded = false;
		this.is_path_loaded = false;
		this.content = new Content(this);
	}
	
	private long id;
	private String basename;
	private Page parent;
	private long parentId;
	private Content content;
	private List<Page> children;
	private Path path;
	private boolean modified;
	
	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.setModified(true);
		this.parentId = parentId;
	}

	// Lazy loading states
	private boolean is_parent_loaded;
	private boolean is_content_loaded;
	private boolean is_children_loaded;
	private boolean is_path_loaded;
	
	public boolean isChildrenLoaded() {
		return this.is_children_loaded;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.setModified(true);
		this.id = id;
	}

	public String getBasename() {
		return basename;
	}

	public void setBasename(String basename) {
		if (this.basename == null)
			this.basename = basename;
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
	
	public Page getParent() {
		if (!this.is_parent_loaded) {
			this.parent = this.pdao.findById(this.parentId);
			this.is_parent_loaded = true;
		}
		return parent;
	}

	public void setParent(Page parent) {
		this.setModified(true);
		this.is_parent_loaded = true;
		this.onBeforeChangeParent(parent);
		this.parent = parent;
		this.parentId = parent == null ? 0 : parent.getId();
		this.onAfterChangeParent(parent);
		if (this.getParent() != null)
			this.getParent().invalidateChildren();
	}

	public Content getContent() {
		if (!this.is_content_loaded) {
			this.pdao.loadContent(this);
			this.is_content_loaded = true;
		}
		return content;
	}
	
	public boolean hasContent() {
		return this.getContent() != null;
	}

	public void setContent(Content content) {
		this.setModified(true);
		this.is_content_loaded = true;
		this.content = content;
	}
	
	public void setBody(String text) {
		this.setModified(true);
		this.is_content_loaded = true;
		this.content.setBody(text);
	}

	public boolean hasChildren() {
		return (this.getChildren() != null) && (this.getChildren().size() != 0);
	}
	
	public List<Page> getChildren() {
		if (!this.is_children_loaded && (this.getId() != 0)) {	// Must be saved to have children
			this.children = this.pdao.findByParentId(this.getId());
			this.is_children_loaded = true;
		}
		return children;
	}
	
	public void invalidateChildren() {
		this.setModified(true);
		this.is_children_loaded = false;
		this.children = null;
	}
	public void invalidatePath() {
		this.path = null;
		this.is_path_loaded = false;
		for (Page p: this.getChildren()) {
			p.invalidatePath();
		}
	}
	
	public Page getChild(String basename) {
		for (Page p: this.getChildren()) {
			if (p.getBasename().equals(basename))
				return p;
		}
		return null;
	}
	
	public Path getPath() {
		if (!this.is_path_loaded) {
			this.path = this.getParent() != null
				? this.getParent().getPath().clone()
				: new Path();
			this.path.add(this.getBasename());
			this.is_path_loaded = true;
		}
		return this.path;
	}

	/**
	 * Delete any information with old path.
	 * (Content files, folder)
	 */
	private void onBeforeChangeParent(Page newParent) {
		// TODO If page has content, move content file
		// TODO If page has children, notify children
		
		// Invalidate parents children
		if (this.getParent() != null) {
			this.getParent().invalidateChildren();
		}
		
		this.getContent();
		
		// Move content file
		this.pdao.moveFiles(this, newParent.getPath().clone().add(this.getBasename()));
	}
	
	private void onAfterChangeParent(Page newParent) {
		// Invalidate parent's children
		if (this.getParent() != null) {
			this.getParent().invalidateChildren();
		}
		
		// Invalidate actual path
		this.invalidatePath();
	}

}
