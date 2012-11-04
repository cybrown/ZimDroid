package org.cy.zimjava.entity;

import org.cy.zimjava.dao.PageDAO;

public class Notebook {

	private String uri;
	private PageDAO pdao;
	private boolean opened;
	
	public Notebook(String uri) {
		this.uri = uri;
		this.opened = false;
	}
	
	public boolean isOpened() {
		return this.opened;
	}
	
	public void open() {
		this.pdao = new PageDAO(this.uri);	// TODO Verify URI
	}
	
	public void close() {
		this.pdao.saveAll();
		this.pdao = null;
	}
	
	public String getUri() {
		return this.uri;
	}
	
	public Page findRoot() {
		return this.pdao.findRoot();
	}
	
	public Page findById(Long id) {
		return this.pdao.findById(id);
	}
	
	public void delete(Page p) {
		this.pdao.delete(p);
	}
	
	public Page createPageFor(Page parent) {
		Page p = new Page(this.pdao);
		p.setParent(parent);
		return p;
	}
	
	public Page createPage() {
		return this.createPageFor(this.findRoot());
	}
}
