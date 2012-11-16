package org.cy.zimjava.dao;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.cy.zimjava.Path;
import org.cy.zimjava.entity.Content;
import org.cy.zimjava.entity.Page;
import org.cy.zimjava.record.PageRecord;

import android.util.Log;

/**
 * Fetch pages from persistence.
 * Pages can use this class to fetch lazy loaded information.
 * @author sigh
 *
 */
public class PageDAO {

	private IPageRecordDAO pageRecordDAO;
	private ContentDAO contentDAO;
	private String rootPath;
	private HashMap<Long, Page> cache;
	
	public PageDAO(String root, IPageRecordDAO prdao) {
		this.rootPath = root;
		File f = new File(root);
		if (!f.isDirectory()) {
			f.mkdirs();
		}
		this.pageRecordDAO = prdao;
		this.contentDAO = new ContentDAO(this.rootPath);
		this.cache = new HashMap<Long, Page>();
	}
	
	public Page findById(long id) {
		if (id == 0)
			return null;
		Page res;
		res = this.cache.get(id);
		if (res == null) {
			PageRecord pr = this.pageRecordDAO.findById(id);
			if (pr != null) {
				res = new Page(this).hydrate(pr.getId(), pr.getBasename(), pr.getParent());
				this.cache.put(id, res);
			}
			System.out.println("Loading (" + id + ") " + res.getPath().toString());
		}
		return res;
	}
	
	public Page findRoot() {
		return this.findById(1);
	}
	
	public List<Page> findByParentId(long parent) {

		// Looking into cache
		LinkedList<Page> list = new LinkedList<Page>();
		for (Page p: this.cache.values()) {
			if ((p.getParent() != null) && (p.getParent().getId() == parent)) {
				list.add(p);
			}
		}
		
		// Looking in persistence layer (do not add already cached objects)
		Page tmpPage;
		for (PageRecord pr: this.pageRecordDAO.findByParentId(parent, true)) {
			tmpPage = this.findById(pr.getId());
			if (list.contains(tmpPage) || ((tmpPage != null) && (tmpPage.getParent() != null) && tmpPage.getParent().getId() != parent)) {
				continue;
			}
			if (tmpPage == null) {
				tmpPage = new Page(this).hydrate(pr.getId(), pr.getBasename(), pr.getParent());
			}
			list.add(tmpPage);
		}
		return list;
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
			cur = new Page(this).hydrate(0, name, prec.getId());
			lst.remove(0);
			prec = cur;
		}
		
		return cur;
	}
	
	public boolean saveAll() {
		LinkedList<Page> tmp = new LinkedList<Page>();
		boolean somethingWasSaved = false;
		for (Page i: this.cache.values()) {
			tmp.add(i);
		}
		for (Page i: tmp) {
			somethingWasSaved = somethingWasSaved | this.save(i);
		}
		return somethingWasSaved;	// TODO Verify output...
	}
	
	public boolean save(Page page) {
		if (!page.isModified())
			return false;
		System.out.println("Saving " + page.getPath().toString());
		PageRecord pr = page.getId() == 0
			? new PageRecord()
			: this.pageRecordDAO.findById(page.getId());
		Page2Pagerecord(pr, page);
		if (this.pageRecordDAO.save(pr)) {
			// The page is new, the id was just generated in pagerecord
			Log.d("SAVE", "Put page " + page.getBasename() + " in cache.");
			if (page.getId() == 0) {
				page.setId(pr.getId());
			}
			this.cache.put(page.getId(), page);
			if (page.isChildrenLoaded() && !page.hasChildren())
				this.deleteFolder(page);
			this.saveContent(page);
			page.setModified(false);
			return true;
		}
		Log.e("CY", "Failed to save page record.");
		return false;
	}
	
	public Content loadContent(Path path) {
		Content c = this.contentDAO.load(path);
		return c;
	}
	
	public boolean delete(Page page) {
		Log.d("DELETE", "Delete: " + page.getBasename());
		if (page.getParent() != null) {
			page.getParent().invalidateChildren();
		}
		if (page.hasChildren()) {
			for (Page p: page.getChildren()) {
				this.delete(p);
			}
		}
		this.pageRecordDAO.deleteById(page.getId());
		this.contentDAO.delete(page.getPath());
		this.deleteFolder(page);
		this.cache.remove(page.getId());
		return true;
	}

	public boolean moveFiles(Page page, Path newPath) {
		// Compute old and new path
		String old_content_path = page.getPath().toFilePath(this.rootPath);
		String new_content_path = newPath.toFilePath(this.rootPath);
		String old_children_path = page.getPath().toDirPath(this.rootPath);
		String new_children_path = newPath.toDirPath(this.rootPath);

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
	
	private boolean saveContent(Page page) {
		return this.contentDAO.save(page.getContent(), page.getPath());
	}
		
	private boolean deleteFolder(Page page) {
		String path = page.getPath().toDirPath(this.rootPath);
		File f = new File(path);
		if (f.isDirectory())
			return f.delete();
		return false;
	}
	
	private void Page2Pagerecord(PageRecord pr, Page p) {
		pr.setId(p.getId());
		pr.setBasename(p.getBasename());
		
		if (p.getParent() != null)
			pr.setParent(p.getParent().getId());
		
		if ((p.getChildren() != null) && (p.getChildren().size() != 0)) {
			File f = new File(p.getPath().toDirPath(this.rootPath));
			Double childrenkey = f.lastModified()/1000.0d;
			pr.setChildrenkey(childrenkey);
			pr.setHaschildren(p.getChildren().size() != 0);
		}
		else {
			pr.setChildrenkey(null);
		}
		
		if (p.hasContent()) {
			File f = new File(p.getPath().toFilePath(this.rootPath));
			Double contentkey = f.lastModified()/1000.0d;
			pr.setContentkey(contentkey);
			pr.setHascontent(true);
		}
		else {
			pr.setHascontent(false);
			pr.setContentkey(null);
		}
	}

}
