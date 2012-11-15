package org.cy.zimjava.dao;

import java.util.Collection;

import org.cy.util.IRecordDAO;
import org.cy.zimjava.record.PageRecord;

public interface IPageRecordDAO extends IRecordDAO<PageRecord> {
	public PageRecord findById(long id);
	public boolean deleteById(long id);
	public Collection<PageRecord> findByParentId(long parent);
	public Collection<PageRecord> findByParentId(long parent, boolean ordered);
}
