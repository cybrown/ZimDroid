package org.cy.util;


/**
 * Simple DAO generic interface.
 * @author Cy Brown
 *
 * @param <T>
 */
public interface IRecordDAO<T> {
	public void setRoot(String root);
	public boolean delete(long id);
	public boolean delete(T pr);
	public boolean save(T pr);
	public T findById(long id);
}
