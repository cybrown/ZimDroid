package org.cy.zimjava.util;

import java.util.LinkedList;

/**
 * Path utility class.
 * @author sigh
 *
 */
public class Path implements Cloneable {

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String name: this.path) {
			sb.append("/");
			sb.append(name);
		}
		return sb.toString();
	}
	
	/**
	 * Exports path to an absolute path to file from root.
	 * @param root
	 * @return
	 */
	public String toFilePath(String root) {
		StringBuilder sb = new StringBuilder();
		sb.append(root);
		for (String name: this.path) {
			sb.append("/");
			sb.append(name);
		}
		sb.append(".txt");
		return sb.toString().replace(" ", "_");
	}
	
	/**
	 * Exports path to a folder on filesystem from root.
	 * @param root
	 * @return
	 */
	public String toDirPath(String root) {
		StringBuilder sb = new StringBuilder();
		sb.append(root);
		for (String name: this.path) {
			sb.append("/");
			sb.append(name);
		}
		return sb.toString().replace(" ", "_");
	}
	
	public Path clone() {
		Path res = new Path();
		for (String name: this.path) {
			res.add(name);
		}
		return res;
	}
	
	private LinkedList<String> path;
	
	public Path() {
		this.path = new LinkedList<String>();
	}
	
	public Path add(String s) {
		this.path.add(s.replace("_", " "));
		return this;
	}
	
	public boolean equals(Path p) {
		if (this.path.size() != p.path.size())
			return false;
		for (int i = 0; i < this.path.size(); i++) {
			if (!this.path.get(i).equals(p.path.get(i)))
				return false;
		}
		return true;
	}
	
	public String getBaseName() {
		return this.path.getLast();
	}
	
	public LinkedList<String> getPath() {
		return this.path;
	}

	/**
	 * Set path to a string containing a zim path.
	 * If zimpath is absolute, the path is cleared before.
	 * If zimpath is relative, the path is used as a current path.
	 * @param zimpath
	 * @return
	 */
	public Path fromZimPath(String zimpath) {
		if (zimpath.startsWith(":")) {
			this.path.clear();
			zimpath = zimpath.substring(1);
		}
		else if (zimpath.startsWith("+")) {
			zimpath = zimpath.substring(1);
		}
		else if (zimpath.startsWith(".:")) {
			zimpath = zimpath.substring(2);
		}
		else {
			this.path.removeLast();
		}
		
		for (String name: zimpath.split(":")) {
			if ((name == null) || (name.length() == 0))
				continue;
			this.add(name);
		}
		return this;
	}
}
