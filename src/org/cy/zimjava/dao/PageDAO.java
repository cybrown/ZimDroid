package org.cy.zimjava.dao;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.cy.zimdroid.dao.AndroidSQLitePageRecordDAO;
import org.cy.zimjava.entity.Content;
import org.cy.zimjava.entity.Page;
import org.cy.zimjava.record.PageRecord;
import org.cy.zimjava.util.Path;

/**
 * Fetch pages from persistence.
 * Pages can use this class to fetch lazy loaded information.
 * @author sigh
 *
 */
public class PageDAO {

	private IPageRecordDAO prd;
	private ContentDAO cdao;
	private String root;
	private HashMap<Long, Page> cache;
	
	public PageDAO(String root) {
		this.root = root;
		this.prd = new AndroidSQLitePageRecordDAO(this.root);
		this.cdao = new ContentDAO(this.root);
		this.cache = new HashMap<Long, Page>();
	}
	
	private void Pagerecord2Page(PageRecord pr, Page p) {
		p.setId(pr.getId());
		p.setBasename(pr.getBasename());
		p.setParentId(pr.getParent());	// Set only parent id, for lazy loading
		// Other properties are lazy loaded...
	}
	
	private void Page2Pagerecord(PageRecord pr, Page p) {
		pr.setId(p.getId());
		pr.setBasename(p.getBasename());
		
		if (p.getParent() != null)
			pr.setParent(p.getParent().getId());
		
		if ((p.getChildren() != null) && (p.getChildren().size() != 0)) {
			File f = new File(p.getPath().toDirPath(this.root));
			Double childrenkey = f.lastModified()/1000.0d;
			pr.setChildrenkey(childrenkey);
			pr.setHaschildren(p.getChildren().size() != 0);
		}
		else {
			pr.setChildrenkey(null);
		}
		
		if (p.hasContent()) {
			File f = new File(p.getPath().toFilePath(this.root));
			Double contentkey = f.lastModified()/1000.0d;
			pr.setContentkey(contentkey);
			pr.setHascontent(true);
		}
		else {
			pr.setHascontent(false);
			pr.setContentkey(null);
		}
	}
	
	public Page findById(long id) {
		if (id == 0)
			return null;
		Page res;
		res = this.cache.get(id);
		if (res == null) {
			res = new Page(this);
			PageRecord pr = this.prd.findById(id);
			if (pr != null) {
				Pagerecord2Page(pr, res);
				res.setModified(false);
			}
			System.out.println("Loading (" + id + ") " + res.getPath().toString());
		}
		this.cache.put(id, res);
		return res;
	}
	
	public Page findRoot() {
		return this.findById(1);
	}
	
	public List<Page> findByParentId(long parent) {
		LinkedList<Page> list = new LinkedList<Page>();
		Page tmp;

		LinkedList<Page> tmp2 = new LinkedList<Page>();
		for (Page pr: this.cache.values()) {
			tmp2.add(pr);
		}
		for (Page p: tmp2) {
			if ((p.getParent() != null) && (p.getParent().getId() == parent)) {
				list.add(p);
			}
		}
		for (PageRecord pr: this.prd.findByParentId(parent, true)) {
			tmp = this.findById(pr.getId());
			if (tmp == null) {
				tmp = new Page(this);
			}
			else if (tmp.getParent().getId() != parent)	{	// Parent's id has changed since database write
				continue;
			}
			if (list.contains(tmp)) {
				continue;
			}
			Pagerecord2Page(pr, tmp);
			tmp.setModified(false);
			list.add(tmp);
		}
		return list;
	}
	
	public Page findByPath(Path path) {
		Page cur = this.findRoot();
		for (String name: path.getPath()) {
			cur = cur.getChild(name);
		}
		return cur;
	}
	
	public Page createByPath(Path path) {
		List<String> lst = path.getPath();
		Page cur = this.findRoot();
		Page prec = this.findRoot();
		String name = "";
		
		// While page exists
		while (!lst.isEmpty()) {
			name = lst.get(0);
			cur = cur.getChild(name);
			if (cur == null) {
				break;
			}
			lst.remove(0);
			prec = cur;
		}
		
		// While list is not empty, create page
		while (!lst.isEmpty()) {
			name = lst.get(0);
			cur = new Page(this);
			cur.setBasename(name);
			cur.setParent(prec);
			this.save(cur);
			lst.remove(0);
			prec = cur;
		}
		
		return cur;
	}
	
	public boolean saveAll() {
		LinkedList<Page> tmp = new LinkedList<Page>();
		for (Page i: this.cache.values()) {
			tmp.add(i);
		}
		for (Page i: tmp) {
			this.save(i);
		}
		return true;	// TODO Verify output...
	}
	
	public boolean save(Page page) {
		if (!page.isModified())
			return false;
		System.out.println("Saving " + page.getPath().toString());
		PageRecord pr = page.getId() == 0
			? new PageRecord()
			: this.prd.findById(page.getId());
		Page2Pagerecord(pr, page);
		if (this.prd.save(pr)) {
			// The page is new, the id was just generated in pagerecord
			if (page.getId() == 0)
				page.setId(pr.getId());
			if (page.isChildrenLoaded() && !page.hasChildren())
				this.deleteFolder(page);
			this.saveContent(page);
			return true;
		}
		page.setModified(false);
		return false;
	}
	
	public Content loadContent(Path path) {
		Content c = this.cdao.load(path);
		return c;
	}
	
	public boolean delete(Page page) {
		this.prd.delete(page.getId());
		this.cdao.delete(page.getPath());
		this.deleteFolder(page);
		this.cache.remove(page);
		return true;
	}
	
	public boolean saveContent(Page page) {
		return this.cdao.save(page.getContent(), page.getPath());
	}
	
	public boolean deleteContent(Page page) {
		return this.cdao.delete(page.getPath());
	}
	
	public boolean moveFiles(Page page, Path newPath) {
		// Compute old and new path
		String old_content_path = page.getPath().toFilePath(this.root);
		String new_content_path = newPath.toFilePath(this.root);
		String old_children_path = page.getPath().toDirPath(this.root);
		String new_children_path = newPath.toDirPath(this.root);

		// Create java files
		File content_source = new File(old_content_path);
		File content_dest = new File(new_content_path);
		File children_source = new File(old_children_path);
		File children_dest = new File(new_children_path);
		
		boolean success = false;
		
		// Move content file
		if (content_source.isFile()) {
			content_dest.mkdirs();
			content_dest.delete();
			success = content_source.renameTo(content_dest);
		}
		
		if (!success) {
			return false;
		}
		
		// Move children folder
		if (children_source.isDirectory()) {
			children_source.mkdirs();
			success = success & children_source.renameTo(children_dest);
		}
		return success;
	}
	
	public boolean deleteFolder(Page page) {
		String path = page.getPath().toDirPath(this.root);
		File f = new File(path);
		if (f.isDirectory())
			return f.delete();
		return false;
	}
	
	public void close() {
		this.prd.close();
	}
}
