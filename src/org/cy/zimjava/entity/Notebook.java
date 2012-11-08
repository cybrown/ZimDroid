package org.cy.zimjava.entity;

import org.cy.zimjava.dao.PageDAO;
import org.cy.zimjava.util.Path;

public class Notebook {

	private String uri;
	private PageDAO pdao;
	private boolean open;
	
	public Notebook(String uri) {
		this.uri = uri;
		this.open = false;
	}
	
	public boolean isOpen() {
		return this.open;
	}
	
	public void open() {
		this.pdao = new PageDAO(this.uri);	// TODO Verify URI
		this.open = true;
	}
	
	public void close() {
		this.pdao.saveAll();
		this.pdao = null;
		this.open = false;
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
	
	public Page findByPath(Path path) {
		return this.pdao.findByPath(path);
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
