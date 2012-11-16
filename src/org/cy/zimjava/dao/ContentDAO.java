package org.cy.zimjava.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.cy.zimjava.Path;
import org.cy.zimjava.entity.Content;

/**
 * Class used to handle data files.
 * @author sigh
 *
 */
public class ContentDAO {

	private String root;
	
	/**
	 * @param root Path to filesystem root for contents. 
	 */
	public ContentDAO(String root) {
		this.root = root;
	}
	
	/**
	 * Creates a new Content object and load data from filesystem.
	 * @param path Path to content.
	 * @param host The object containing the content.
	 * @return Returns null on error.
	 */
	public Content load(Path path) {
		Content res = null;
		String fpath = path.toFilePath(this.root);
		
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new FileReader(fpath), 8192);
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n"); // TODO End Of Line
			}
			res = new Content();
			res.parseText(sb.toString());
			br.close();
		} catch (FileNotFoundException e) {
			// Do nothing if file not found, simply return null
		} catch (IOException e) {
			// Do nothing on error, simply return null.
		}
		
		return res;
	}
	
	/**
	 * Create or update content to specified path.
	 * @param content
	 * @param path
	 * @return
	 */
	public boolean save(Content content, Path path) {
		if (content == null) {
			return false;
		}
		String fpath = path.toFilePath(this.root);
		try {
			File f = new File(fpath);
			if (!f.exists()) {
				f.mkdirs();
				f.delete();
				f.createNewFile();
			}
			FileWriter fw = new FileWriter(f);
			fw.write(content.generateText());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Delete content file.
	 * @param path
	 * @return
	 */
	public boolean delete(Path path) {
		String fpath = path.toFilePath(this.root);
		File f = new File(fpath);
		if (f.isFile())
			return f.delete();
		return false;
	}
}
