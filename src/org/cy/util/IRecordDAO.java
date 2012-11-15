package org.cy.util;


/**
 * Simple DAO generic interface.
 * @author Cy Brown
 *
 * @param <T>
 */
public interface IRecordDAO<T> {
	public boolean delete(T pr);
	public boolean save(T pr);
	public void close();
}
