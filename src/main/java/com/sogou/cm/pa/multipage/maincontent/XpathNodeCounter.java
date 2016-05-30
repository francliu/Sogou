package com.sogou.cm.pa.multipage.maincontent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.sogou.cm.pa.maincontent.HtmlContentHandler;


public class XpathNodeCounter implements ContentHandler {


	StringBuffer full_xpath;
	HashMap<String, Integer> xpath2num;
	boolean is_abnormal_page;
	
	XpathNodeCounter() {

		full_xpath = new StringBuffer();
		xpath2num = new HashMap<String, Integer>();
		is_abnormal_page = false;
	}
	
	public void clear() {

	//	xpath2num.clear();
		full_xpath.setLength(0);
		full_xpath.trimToSize();
		is_abnormal_page = false;
	}
	
	public void setXpath(Set<String> xpathes) {
		for (String xpath: xpathes) {
			xpath2num.put(xpath, 0);
		}
	}
	

	
	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
	}

	
	public void startDocument() throws SAXException {
		clear();
	}

	
	public void endDocument() throws SAXException {

	}

	
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// TODO Auto-generated method stub
	}

	
	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub
	}
	

	public static boolean has_num(String s) {
		int cnt = 0;
		for (int i = s.length() - 1; i >= 0; --i) {
			if (s.charAt(i)>='0' && s.charAt(i) <= '9') {
				++cnt;
			} else {
				break;
			} 
		}
		if (cnt >= 3) {
			return true;
		} else {
			return false;
		}
	}

	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		if (full_xpath.length() > 4000) {
			is_abnormal_page = true;
			return;
		}
		String attr_str = "";
		String id = atts.getValue("id");
		String class_attr = atts.getValue("class");
		/*
		if (id != null) {
			int t = id.indexOf(" ");
			if (id.length() > 15) {
				id = "";
			}
			String[] segs = id.split(" ");
			boolean hn = false;
			for (String s: segs) {
				if (has_num(s)) {
					hn = true;
					break;
				}
			}
			if (id.length() > 0  && !hn) {
				attr_str = attr_str + "@id='" + id + "'";
			}
		}
		
		if (class_attr != null) {
			int t = class_attr.indexOf(" ");
			if (t > 30 || (t < 0 && class_attr.length() > 30)) {
				class_attr = "";
			}
			String[] segs = class_attr.split(" ");
			boolean hn = false;
			for (String s: segs) {
				if (has_num(s)) {
					hn = true;
					break;
				}
			}
			if (class_attr.length() > 0 && !hn) {
				if (attr_str.length() > 0) {
					attr_str += " and ";
				}
				attr_str = attr_str + "@class='" + class_attr + "'";
			}
		}
		String full_name = qName;
		if (!qName.equalsIgnoreCase("html") && !qName.equalsIgnoreCase("body") && attr_str.length() > 0) {
			full_name = qName + "[" + attr_str + "]";
		}
		*/
		String full_name = HtmlContentHandler.getFulltag(qName, id, class_attr);
		full_xpath.append("/" + full_name);
		Integer n = xpath2num.get(full_xpath.toString());
		if (n != null) {
			++n;
			xpath2num.put(full_xpath.toString(), n);
		}

	}

	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
			full_xpath.delete(full_xpath.lastIndexOf("/"), full_xpath.length());

	}

	
	public void characters(char[] ch, int start, int length) throws SAXException {

	}


	
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
	}

	
	public void processingInstruction(String target, String data) throws SAXException {
		// TODO Auto-generated method stub
	}

	
	public void skippedEntity(String name) throws SAXException {
		// TODO Auto-generated method stub
	}
	
	public static void main(String[] args) throws IOException, SAXException {


	}
} 

