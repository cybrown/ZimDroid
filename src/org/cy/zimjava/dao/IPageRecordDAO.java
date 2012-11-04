package org.cy.zimjava.dao;

import java.util.Collection;

import org.cy.util.IRecordDAO;
import org.cy.zimjava.record.PageRecord;

public interface IPageRecordDAO extends IRecordDAO<PageRecord> {
	public Collection<PageRecord> findByParentId(long parent);
}
