package org.cy.zimjava.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.cy.zimjava.entity.Content;
import org.cy.zimjava.util.Path;

public class ContentDAO {

	private String root;
	
	public ContentDAO(String root) {
		this.root = root;
	}
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
	}
	
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
	
	public boolean delete(Path path) {
		String fpath = path.toFilePath(this.root);
		File f = new File(fpath);
		if (f.isFile())
			return f.delete();
		return false;
	}
}
