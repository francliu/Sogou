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
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.sogou.cm.pa.maincontent.HtmlContentHandler;



public class BlockExtractorFromXpath implements ContentHandler {
	

	LinkedList<Element> path;
	StringBuffer full_xpath;
	LinkedList<PathNode> new_xpath;
	HashSet<String> xpathes;
	ArrayList<Element> results;
	boolean is_abnormal_page;
	public TreeNode dom_tree;
	LinkedList<TreeNode> tree_path;
	String clusterid;
	
	public BlockExtractorFromXpath() {

		path = new LinkedList<Element>();
		full_xpath = new StringBuffer();
		new_xpath = new LinkedList<PathNode>();
		is_abnormal_page = false;
		xpathes = new HashSet<String>();
		results = new ArrayList<Element>();
		dom_tree = new TreeNode();
		tree_path = new LinkedList<TreeNode>();
		clusterid = "";
	}
	
	public void clear() {

		path.clear();
		full_xpath.setLength(0);
		full_xpath.trimToSize();
		new_xpath.clear();
		is_abnormal_page = false;
		results.clear();
		dom_tree = null;
		tree_path.clear();
	}
	
	public void setXpathes(HashSet<String> input) {
		xpathes.clear();
		for (String s: input) {
			xpathes.add(s);
		}
	}
	
	public void setClusterId(String id) {
		clusterid = id;
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
		Integer num = 1;
		Element e = path.peekLast();
		if (e != null) {
			if (e.tag2num.containsKey(qName)) {
				num = e.tag2num.get(qName);
				++num;
				e.tag2num.put(qName, num);
			} else {
				num = 1;
				e.tag2num.put(qName, num);
			}
		}
		
		String attr_str = "";
		String id = atts.getValue("id");
		String class_attr = atts.getValue("class");

		String full_name = HtmlContentHandler.getFulltag(qName, id, class_attr);
		full_xpath.append("/" + full_name);
	//	System.out.println(full_xpath);
		
		
		String attr_node = "";
		String num_node = "";
		if (!qName.equalsIgnoreCase("body") && !qName.equalsIgnoreCase("html")) {
			attr_node = full_name;
			num_node = qName+"["+num+"]";
		} else {
			attr_node = qName;
			num_node = qName;
		}
		
		PathNode path_node = new PathNode();
		path_node.attr_node = attr_node;
		path_node.index_node = num_node;
		new_xpath.add(path_node);
		
		
		Element new_e = new Element();
		new_e.name = qName;
		new_e.full_name = full_name;
		new_e.xpath = full_xpath.toString();
		path.add(new_e);
		
		TreeNode tn = new TreeNode();
		tn.tag = qName;
		tn.atts = atts;
		TreeNode parent = tree_path.peekLast();
		if (parent != null) {
			parent.children.add(tn);
		}
		tree_path.add(tn);
		if (dom_tree == null) {
			dom_tree = tn;
			//System.out.println("here.");
		}
		if (tn.tag.equalsIgnoreCase("html")) {
			tn.add_atts.put("clusterid", clusterid);
		}
	}

	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		Element e = path.peekLast();
	//	System.out.println(e.xpath + "\t" + e.text.length());
	//	System.out.println(qName);
		if (qName.equalsIgnoreCase(e.name)) {
			path.pollLast();
			Element parent = path.peekLast();
			if (parent != null && e.text.length() > 0) {
				parent.text.append(e.text);
			}

			StringBuffer num_xpath = new StringBuffer();
			int xpath_depth = new_xpath.size();
			for (int i = 0; i < xpath_depth-2; ++i) {
				num_xpath.append("/"+new_xpath.get(i).attr_node);
			}
			if ( xpath_depth > 1) {
				num_xpath.append("/" + new_xpath.get(xpath_depth-2).index_node);
			}
			num_xpath.append("/" + new_xpath.get(xpath_depth-1).index_node);
	//		System.out.println(num_xpath);
		//	System.out.println(num_xpath + "\t" + e.text.length());
			if (xpathes.contains(num_xpath.toString()) || xpathes.contains(full_xpath.toString())) {
				results.add(e);
				TreeNode tn = tree_path.peekLast();
				tn.add_atts.put("ismaincontent", "true");
			}
			
			
			
			full_xpath.delete(full_xpath.lastIndexOf("/"), full_xpath.length());
			new_xpath.pollLast();
			tree_path.pollLast();
		}

	}

	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (is_abnormal_page) {
			return;
		}

		
		String s = String.valueOf(ch, start, length);
		TreeNode tn = new TreeNode();
		tn.text = s;
		TreeNode p = tree_path.peekLast();
		if (p != null) {
			p.children.add(tn);
		}
		
		Element element = path.peekLast();
		if (element.name.equalsIgnoreCase("script") || element.name.equalsIgnoreCase("style")) {
			return;
		}
		//if (length > 0)
		//	System.out.println("content: " + ch[start] + "\ta\t" + s);
		s = s.replaceAll("[ \t\r\n]?(&nbsp;)?(&gt;)?", "");

		
		element.text.append(s.toLowerCase());
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
		FileInputStream f_stream = new FileInputStream(new File("t.html"));
		byte[] bs = new byte[1024*1024*4];
		int len = f_stream.read(bs);
		String s = new String(bs, 0, len, "UTF-8");

		BlockExtractorFromXpath		htmlContentHandler		= new BlockExtractorFromXpath();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		
		String out = "";
		try {
			HashSet<String> xpathes = new HashSet<String>();
			xpathes.add("/html/body/div[@id='page']/div[@class='page_wrap']/div[@class='page_body clear']/div[@class='main']/div[@class='doc_main']/div[@class='doc_header_mod']");
			//xpathes.add("/html/body/div[@id='wp' and @class='wp']/div[@id='ct' and @class='wp cl']/table/tbody/tr/td[@class='plc']/div[@class='pct']");
			htmlContentHandler.setXpathes(xpathes);
			parser.parse(new InputSource(new StringReader(s)));
			for (Element e: htmlContentHandler.results) {
			//	System.out.println(e.xpath + "\t" + e.text);
			}
			System.out.println(htmlContentHandler.dom_tree.traverse_debug());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
} 

