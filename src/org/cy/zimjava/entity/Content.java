package org.cy.zimjava.entity;

import java.util.HashMap;
import java.util.Map;

public class Content {

	private Map<String, String> headers;
	private String body;
	
	public Content() {
		this.headers = new HashMap<String, String>();
		this.body = "";
	}
	
	public void addHeader(String key, String value) {
		this.headers.put(key, value);
	}
	
	public String getHeader(String key) {
		return this.headers.get(key);
	}
	
	public Map<String, String> getHeaders() {
		return this.headers;
	}
	
	public boolean hasHeader(String key) {
		return this.headers.containsKey(key);
	}
	
	public boolean hasHeaders() {
		return this.headers.size() != 0;
	}
	
	public void setBody(String param) {
		this.body = param;
	}
	
	public String getBody() {
		return this.body;
	}
	
	public boolean parseText(String text) {
		StringBuilder sb = new StringBuilder();
		boolean mode_header = true;
		int pos;
		this.headers.clear();
		for (String line: text.split("\n")) {	// TODO End Of Line
			if (mode_header) {
					pos = line.indexOf(":");
				if (pos != -1) {
					this.addHeader(line.substring(0, pos).trim(), line.substring(pos+1).trim());
				}
				else if (line.length() == 0) {
					mode_header = false;
				}
				else {
					mode_header = false;
					sb.append(line);
					sb.append("\n");	// TODO End Of Line
				}
			}
			else {
				sb.append(line);
				sb.append("\n");	// TODO End Of Line
			}
		}
		this.body = sb.toString();
		return true;
	}
	
	public String generateText() {
		StringBuilder sb = new StringBuilder();
		
		boolean at_least_once = false;
		for (String key: this.headers.keySet()){
			sb.append(key).append(": ").append(this.headers.get(key)).append("\n"); // TODO End Of Line
			at_least_once = true;
		}
		if (at_least_once)
			sb.append("\n");	// TODO End Of Line
		sb.append(this.body);
		
		return sb.toString();
	}
}
