package org.cy.zimdroid.dao;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.cy.zimdroid.androiddb.ZimdroidSQLiteHelper;
import org.cy.zimjava.dao.IPageRecordDAO;
import org.cy.zimjava.record.PageRecord;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// TODO Check if db and root are not null

public class AndroidSQLitePageRecordDAO implements IPageRecordDAO {

	private String root;
	SQLiteDatabase db;
	
	HashMap<Long, PageRecord> cache;
	
	public AndroidSQLitePageRecordDAO() {
		this.cache = new HashMap<Long, PageRecord>();
	}
	
	public void setRoot(String root) {
		this.root = root;
		ZimdroidSQLiteHelper zsh = new ZimdroidSQLiteHelper(this.root + "/.zim/index.db");
		this.db = zsh.getWritableDatabase();
	}
	
	public boolean delete(long id) {
		this.db.delete("pages", "id = ", new String[]{Long.toString(id)});
		return true;
	}
	
	public boolean delete(PageRecord pr) {
		boolean res = false;
		if (pr.getId() != 0) {
			res = this.delete(pr.getId());
		}
		return res;
	}
	
	public boolean save(PageRecord pr) {
		// Save to database
		ContentValues cv = new ContentValues();
		cv.put("basename", pr.getBasename());
		cv.put("parent", pr.getParent());
		cv.put("hascontent", pr.isHascontent());
		cv.put("haschildren", pr.isHaschildren());
		if (pr.getContentkey() == null)
			cv.putNull("contentkey");
		else
			cv.put("contentkey", pr.getContentkey());
		if (pr.getChildrenkey() == null)
			cv.putNull("childrenkey");
		else
			cv.put("childrenkey", pr.getChildrenkey());

		if (pr.getId() == 0) {
			long last_id = this.db.insert("pages", null, cv);
			if (last_id == -1)
				return false;
			pr.setId(last_id);
		}
		else {
			this.db.update("pages", cv, "id = ?", new String[]{Long.toString(pr.getId())});
		}
		
		return true;
	}
	
	public PageRecord findById(long id) {
		if (id == 0)
			return null;
		PageRecord pr;
		pr = this.cache.get(id);
		if (pr == null) {
			Cursor cursor = this.db.query("pages", new String[]{"basename", "parent", "haschildren", "hascontent", "contentkey", "childrenkey"}, "id = ?", new String[]{Long.toString(id)}, null, null, null, "1");
			if (cursor.moveToFirst()) {
				pr = new PageRecord();
				pr.setId(id);
				pr.setBasename(cursor.getString(0));
				pr.setParent(cursor.getLong(1));
				pr.setHaschildren(cursor.getInt(2) == 1);
				pr.setHascontent(cursor.getInt(3) == 1);
				pr.setContentkey(cursor.getDouble(4));
				pr.setChildrenkey(cursor.getDouble(5));
				this.cache.put(id, pr);
			}
			cursor.close();
		}
		return pr;
	}
	
	public Collection<PageRecord> findByParentId(long parent) {
		return this.backendFindByParentId(parent, null);
	}
	
	@Override
	public Collection<PageRecord> findByParentId(long parent, boolean ordered) {
		return this.backendFindByParentId(parent, ordered ? "basename" : "basename DESC");
	}
		
	protected Collection<PageRecord> backendFindByParentId(long parent, String order) {
		LinkedList<PageRecord> prl = new LinkedList<PageRecord>();
		// If id is 0, do not verify parents properties
		if (parent != 0) {
			PageRecord pr_parent = this.findById(parent);
			if (!pr_parent.isHaschildren())
				return prl;
		}
		
		PageRecord pr;
		long id;
		
		for (PageRecord i_pr: this.cache.values()) {
			if (i_pr.getParent() == parent) {
				prl.add(i_pr);
			}
		}
		
		Cursor cursor = this.db.query("pages", new String[]{"id", "basename", "haschildren", "hascontent", "contentkey", "childrenkey"}, "parent = ?", new String[]{Long.toString(parent)}, null, null, order);
		if (cursor.moveToFirst()) {
			do {
				id = cursor.getLong(0);
				pr = this.cache.get(id);
				if (pr == null)
					pr = new PageRecord();
				else if (pr.getParent() != parent)	// parents id has changed since database version
					continue;
				if (prl.contains(pr))	// record is already in result set
					continue;
				pr.setId(id);
				pr.setParent(parent);
				pr.setBasename(cursor.getString(1));
				pr.setHaschildren(cursor.getInt(2) == 1);
				pr.setHascontent(cursor.getLong(3) == 1);
				pr.setContentkey(cursor.getDouble(4));
				pr.setChildrenkey(cursor.getDouble(5));
				this.cache.put(id, pr);
				prl.add(pr);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return prl;
	}

	public void close() {
		this.db.close();
	}
}
