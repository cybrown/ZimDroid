package org.cy.zimjava.entity;

import org.cy.zimjava.Path;
import org.cy.zimjava.dao.IPageRecordDAO;
import org.cy.zimjava.dao.PageDAO;

public class Notebook {

	private String uri;
	private PageDAO pdao;
	private boolean open;
	private IPageRecordDAO pageRecordDAO;
	
	public Notebook(String uri, IPageRecordDAO prdao) {
		this.uri = uri;
		this.open = false;
		this.pageRecordDAO = prdao;
	}
	
	public boolean isOpen() {
		return this.open;
	}
	
	public void open() {
		this.pdao = new PageDAO(this.uri, this.pageRecordDAO);	// TODO Verify URI
		this.open = true;
	}
	
	public void close() {
		this.saveAll();
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
	
	public Page createByPath(Path path) {
		return this.pdao.createByPath(path);
	}
	
	public boolean delete(Page p) {
		return this.pdao.delete(p);
	}
	
	public synchronized boolean saveAll() {
		return this.pdao.saveAll();
	}
}
