package com.sogou.cm.pa.multipage.maincontent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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





public class MaincontentExtractor implements ContentHandler {


	StringBuffer full_xpath;
	HashSet<String> xpathes;
	LinkedList<PathNode> new_xpath;
	LinkedList<Element> path;
	ArrayList<Element> mces;
	
	MaincontentExtractor() {

		full_xpath = new StringBuffer();
		xpathes = new HashSet<String>();
		new_xpath = new LinkedList<PathNode>();
		path = new LinkedList<Element>();
		mces = new ArrayList<Element>();
	}
	
	public void clear() {

	//	xpath2num.clear();
		full_xpath.setLength(0);
		full_xpath.trimToSize();
		new_xpath.clear();
		path.clear();
		mces.clear();
	}
	
	public void setXpath(ArrayList<HtmlXpath> xpathes2) {
		for (HtmlXpath hx: xpathes2) {
			if (hx.use_num) {
				xpathes.add(hx.num_xpath);
			} else {
				xpathes.add(hx.class_xpath);
			}
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
		if (cnt >= 4) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setFeatureFromAtts(Attributes atts, Element e) {
		String style = atts.getValue("webkit_style");
		if (style == null || style.length() == 0) {
			style = atts.getValue("style");
			if (style != null && style.indexOf("position:fixed;") < 0) {
				style = null;
			}
		}
		//String style = atts.getValue("style");
		if (style != null) {
			String[] segs = style.split(";");
			for (int i = 0; i < segs.length; ++i) {
				String t = segs[i].replace(" ", "");
				String[] sub_segs = t.split(":");
				if (sub_segs.length < 2) {
					continue;
				}
				
				String num_str = sub_segs[1];
				num_str = num_str.replace("\"", "");
				int index = sub_segs[1].indexOf("px");
				if (index >= 0) {
					num_str = sub_segs[1].substring(0, index);
				}
			//	System.out.println(num_str);
				int num = 0;
				try {
					num = Integer.valueOf(num_str);
				} catch (Exception ex) {
					
				}
				if (sub_segs[0].equalsIgnoreCase("top")) {
					e.top = num;
				} else if (sub_segs[0].equalsIgnoreCase("left")) {
					e.left = num;
				} else if (sub_segs[0].equalsIgnoreCase("width")) {
					e.width = num;
				} else if (sub_segs[0].equalsIgnoreCase("height")) {
					e.height = num;
				} 
			}
		}
		//System.out.println("set feaure internal: " +  feature.width + " " + feature.height);
	}

	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		
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
		String class_attr = atts.getValue("class");
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
		full_xpath.append("/" + full_name);

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
		this.setFeatureFromAtts(atts, new_e);
		path.add(new_e);

	}

	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		Element e = path.peekLast();
		//System.out.println(e.text);
	//	System.out.println(qName);
		if (qName.equalsIgnoreCase(e.name)) {
			String xpath_s = full_xpath.toString();
			StringBuffer num_xpath = new StringBuffer();
			int xpath_depth = new_xpath.size();
			for (int i = 0; i < xpath_depth-2; ++i) {
				num_xpath.append("/"+new_xpath.get(i).attr_node);
			}
			if ( xpath_depth > 1) {
				num_xpath.append("/" + new_xpath.get(xpath_depth-2).index_node);
			}
			num_xpath.append("/" + new_xpath.get(xpath_depth-1).index_node);
			String num_xpath_s = num_xpath.toString();
			// System.out.println(num_xpath_s);
			if (xpathes.contains(xpath_s) || xpathes.contains(num_xpath_s)) {
				mces.add(e);
			//	System.out.println(e.text);
			}
			
			
			path.pollLast();
			Element parent = path.peekLast();
			if (parent != null) {
				parent.text.append(e.text);
			}
			full_xpath.delete(full_xpath.lastIndexOf("/"), full_xpath.length());
			new_xpath.pollLast();
		}
			

	}

	
	public void characters(char[] ch, int start, int length) throws SAXException {
		Element element = path.peekLast();
		if (element.name.equals("script") || element.name.equals("style")) {
			return;
		}
		
		String s = String.valueOf(ch, start, length);
		//System.out.println("content: " + s);
		s = s.replaceAll("[ \t\r\n]?(&nbsp;)?(&gt;)?", "");
	//	String s2 = s.replaceAll("[0-9-:/()]+", "0");
	//	s2 = s2.replaceAll("[a-zA-Z]+", "a");
	//	s = s.replaceAll("[ ]?", "");
	//	s2 = s2.replaceAll("[ ]?", "");
		
		element.text.append(s.toLowerCase());
		//System.out.println(element.text);
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
		
		String url = "http://detail.1688.com/offer/44513447035.html";
		HashMap<String, String> url2cluster = new HashMap<String, String>();
		HashMap<String, ArrayList<HtmlXpath>> cluster2mc = new HashMap<String, ArrayList<HtmlXpath>>();
		BufferedReader reader2 = new BufferedReader(new FileReader(new File("sample_url.txt")));
		String line;
		
		while ((line = reader2.readLine()) != null) {
			String[] segs = line.split("\t");
			url2cluster.put(segs[0], segs[1]);
		}
		reader2.close();
		
		reader2 = new BufferedReader(new FileReader(new File("cluster_maincontent.txt")));
		
		while ((line = reader2.readLine()) != null) {
			String[] segs = line.split("\t");
			ArrayList<HtmlXpath> mcs = new ArrayList<HtmlXpath>();
			for (int i = 1; i < segs.length; ++i) {
				HtmlXpath hx = new HtmlXpath();
				hx.fromString(segs[i]);
				mcs.add(hx);
			}
			if (mcs.size() > 0)
				cluster2mc.put(segs[0], mcs);
		}
		reader2.close();
		String clusterid = url2cluster.get(url);
		if (clusterid == null) {
			return;
		}
		
		ArrayList<HtmlXpath> mcs = cluster2mc.get(clusterid);
		if (mcs == null) {
			return;
		}
		
		FileInputStream f_stream = new FileInputStream(new File("tt.html"));
		byte[] bs = new byte[1024*1024*4];
		int len = f_stream.read(bs);
		String s = new String(bs, 0, len, "UTF-8");
		
		MaincontentExtractor		htmlContentHandler		= new MaincontentExtractor();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		//try {
			ArrayList<HtmlXpath> xpathes = new ArrayList<HtmlXpath>();
			HtmlXpath hx = new HtmlXpath();
			htmlContentHandler.setXpath(mcs);

			parser.parse(new InputSource(new StringReader(s)));
			ArrayList<Element> blocks = htmlContentHandler.mces;
			String out = "";
			for (int i = 0; i < blocks.size(); ++i) {
				Element f = blocks.get(i);
			//	System.out.println(f.xpath + "\t" + f.full_xpath + "\t" +  f.text);
			//	System.out.println(f.text_len  + "\t" +  f.anchor_len + "\t" + f.text);
			//	System.out.println(f.anchor_len + "\t" + (f.text_len-f.anchor_len) + "\t" + f.max_text_len + "\t" + f.text_num + "\t" + f.text);
		        out += String.format("<div style=\"position: absolute; left: %dpx; top: %dpx; width: %dpx; height: %dpx;  border:3px solid red;\">%s</div>\n",f.left, f.top, f.width, f.height, f.xpath +"\t" +  f.text);

			//	System.out.println(f.toPrintString());
			}
			System.out.println(out);

	}
} 

