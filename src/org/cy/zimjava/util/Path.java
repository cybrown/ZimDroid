package org.cy.zimjava.util;

import java.util.LinkedList;

public class Path implements Cloneable {

	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String name: this.path) {
			if (!first)
				sb.append("/");
			sb.append(name);
			first = false;
		}
		return sb.toString();
	}
	
	public String toFilePath(String root) {
		StringBuilder sb = new StringBuilder();
		sb.append(root);
		boolean first = true;
		for (String name: this.path) {
			if (!first)
				sb.append("/");
			sb.append(name);
			first = false;
		}
		sb.append(".txt");
		return sb.toString().replace(" ", "_");
	}
	
	public String toDirPath(String root) {
		StringBuilder sb = new StringBuilder();
		sb.append(root);
		boolean first = true;
		for (String name: this.path) {
			if (!first)
				sb.append("/");
			sb.append(name);
			first = false;
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
		this.path.add(s);
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
		return this.path.get(this.path.size() - 1);
	}
	
	public LinkedList<String> getPath() {
		return this.path;
	}
}
