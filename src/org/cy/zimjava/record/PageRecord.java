package org.cy.zimjava.record;

public class PageRecord {
	private long id;
	private String basename;
	private long parent;
	private boolean hascontent;
	private boolean haschildren;
	private Double contentkey;
	private Double childrenkey;
	
	public Double getContentkey() {
		return contentkey;
	}

	public void setContentkey(Double contentkey) {
		this.contentkey = contentkey;
	}

	public Double getChildrenkey() {
		return childrenkey;
	}

	public void setChildrenkey(Double childrenkey) {
		this.childrenkey = childrenkey;
	}

	public String toString() {
		return this.id + ": " + this.basename;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getBasename() {
		return basename;
	}
	public void setBasename(String basename) {
		this.basename = basename;
	}
	public long getParent() {
		return parent;
	}
	public void setParent(long parent) {
		this.parent = parent;
	}
	public boolean isHascontent() {
		return hascontent;
	}
	public void setHascontent(boolean hascontent) {
		this.hascontent = hascontent;
	}
	public boolean isHaschildren() {
		return haschildren;
	}
	public void setHaschildren(boolean haschildren) {
		this.haschildren = haschildren;
	}
}
