package org.cy.zimjava.util;

import java.util.LinkedList;

public class ZimSyntax {
	private static final String STYLE = "a {text-decoration: none } a:hover {text-decoration: underline } a:active {text-decoration: underline } strike {color: grey } u {text-decoration: none; background-color: yellow } tt {color: #2e3436; } pre {color: #2e3436; margin-left: 20px } h1 {text-decoration: underline; color: #4e9a06 } h2 {color: #4e9a06 } h3 {color: #4e9a06 } h4 {color: #4e9a06 } h5 {color: #4e9a06 }";
	static class Replacement {
		public Replacement(String rg, String rp) {
			this.regex = rg;
			this.replace = rp;
		}
		public String regex;
		public String replace;
	}
	
	public static String toHtml(String in) {
		String res;
		
		LinkedList<Replacement> r = new LinkedList<Replacement>();
		r.add(new Replacement("&", "&amp;"));
		r.add(new Replacement("<", "&lt;"));
		r.add(new Replacement(">", "&gt;"));
        r.add(new Replacement("__((.|\r|\n)*?)__", "<u>$1</u>"));
        r.add(new Replacement("\\*\\*((.|\r|\n)*?)\\*\\*", "<b>$1</b>"));
        r.add(new Replacement("//((.|\r|\n)*?)//", "<i>$1</i>"));
        r.add(new Replacement("(^|\n)\\* (.*)", "$1<li>$2</li>"));
        r.add(new Replacement("(^|\n)======(.*?)(======)?((\r?\n)|$)", "$1<h1>$2</h1>"));
        r.add(new Replacement("(^|\n)=====(.*?)(=====)?((\r?\n)|$)", "$1<h2>$2</h2>"));
        r.add(new Replacement("(^|\n)====(.*?)(====)?((\r?\n)|$)", "$1<h3>$2</h3>"));
        r.add(new Replacement("(^|\n)===(.*?)(===)?((\r?\n)|$)", "$1<h4>$2</h4>"));
        r.add(new Replacement("(^|\n)==(.*?)(==)?((\r?\n)|$)", "$1<h5>$2</h5>"));
        r.add(new Replacement("(^|\n)=(.*?)(=)?((\r?\n)|$)", "$1<h6>$2</h6>"));
        r.add(new Replacement("\\[\\[(.*?)\\|(.*?)\\]\\]", "<a href=\"$1\">$2</a>"));
        r.add(new Replacement("\\[\\[(.*?)\\]\\]", "<a href=\"$1\">$1</a>"));
        r.add(new Replacement("\r?\n", "<br>"));
		
		res = in;
		for (Replacement rpl: r) {
			res = res.replaceAll(rpl.regex, rpl.replace);
		}
		
		res = "<html><head><style>" + STYLE + "</style></head><body>" + res + "</body></html>";
		
		return res;
	}
	
}
