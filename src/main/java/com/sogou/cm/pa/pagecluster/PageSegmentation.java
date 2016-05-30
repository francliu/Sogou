package com.sogou.cm.pa.pagecluster;

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
import java.util.Random;
import java.util.Set;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class Element {
	String tag_name;
	boolean is_leaf;
	boolean is_display;
	HashMap<String, Integer> tag2num;
	int text_len;
	int anchor_len;
	StringBuffer text;
	boolean is_sidebar = false;
	Element() {
		tag_name = "";
		is_leaf = true;
		text_len = 0;
		is_display = false;
		tag2num = new HashMap<String, Integer>();
		text = new StringBuffer();
		is_sidebar = false;
		anchor_len = 0;
	}
}

class PathNode {
	String attr_node;
	String index_node;
	PathNode() {
		attr_node = "";
		index_node = "";
	}
}

public class PageSegmentation implements ContentHandler {
	
	StringBuffer s;
	String html_code;
	StringBuffer xpath;
	StringBuffer full_xpath;
	LinkedList<PathNode> new_xpath;
//	HashMap<String, Integer> xpath2num;
	HtmlPage html_page;
	int html_line_num;
	boolean is_abnormal_page;
	LinkedList<Element> path;
	boolean in_head;
	Locator locator;
	String[] merge_tags = {"/span/", "/strong/", "/font/", "/p/", "/a/"};
	String[] bottom_block_keyword = {"copyright", "关于我们", "联系我们", "©", "网站地图"};
	MessageDigest md;
	byte[] dig_id;
	HashSet<String> sidebar_keyword;
	int text_len;
	int max_node_len;
	boolean in_a;
	Random ran;
	public static HashSet<Character>  cleaned_chars = new HashSet<Character>() {{
		add('\t');
		add('\r');
		add('\n');
	 }};

	public static HashSet<Character> num_chars  = new HashSet<Character>() {{
		add('-');
		add(':');
		add('/');
		add('(');
		add(')');
	 }};;
	
	PageSegmentation() {
		s = new StringBuffer();
		xpath = new StringBuffer();
		full_xpath = new StringBuffer();
		is_abnormal_page = false;
		path = new LinkedList<Element>();
		new_xpath = new LinkedList<PathNode>();
	//	xpath2num = new HashMap<String, Integer>();
		in_head = false;
		try {
			md = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dig_id = new byte[16];
		sidebar_keyword = new HashSet<String>();
		sidebar_keyword.add("header");
		sidebar_keyword.add("footer");
		sidebar_keyword.add("sidebar");
		sidebar_keyword.add("sidebox");
		sidebar_keyword.add("cm_seoarea");
		ran = new Random();
		/*
		cleaned_chars = new HashSet<Character>();
		cleaned_chars.add('\t');
		cleaned_chars.add('\r');
		cleaned_chars.add('\n');
		num_chars = new HashSet<Character>();
		num_chars.add('-');
		num_chars.add(':');
		num_chars.add('/');
		num_chars.add('(');
		num_chars.add(')');
		*/
	}
	
	public void clear() {
		s.setLength(0);
		s.trimToSize();
		xpath.setLength(0);
		xpath.trimToSize();
		is_abnormal_page = false;
		path.clear();
		full_xpath.setLength(0);
		full_xpath.trimToSize();
		new_xpath.clear();
		in_head = false;
//		xpath2num.clear();
		html_line_num = 0;
		html_page = new HtmlPage();
		text_len = 0;
		max_node_len = 0;
		in_a = false;
		for (int i = 0; i < html_code.length(); ++i) {
			if (html_code.charAt(i) == '\n') {
				++html_line_num;
			}
		}
	}
	
	public void setHtmlCode(String html) {
		
		html_code = html;

	//	System.out.println(html_line_num);
	}

	
	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
		this.locator = locator;
	}

	
	public void startDocument() throws SAXException {
		clear();
	}

	
	public void endDocument() throws SAXException {
		html_page.tags = s.toString();
		Set<String> xpathes = html_page.xpath2text.keySet();
		for (String xpath: xpathes) {
			md.reset();
			String text = html_page.xpath2text.get(xpath).toString();
			
			Long docid= new Long(0);
			try {
				md.update(text.getBytes("utf8"));
				md.digest(dig_id, 0, 16);
				for (int j = 0; j < dig_id.length; ++j) {
					docid=docid<<8;
					docid += dig_id[j]&0xff;
				}
			} catch (Exception e) {
				docid = Long.valueOf(html_page.xpath2info.get(xpath).num);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (html_page.xpath2info.containsKey(xpath)) {
				html_page.xpath2info.get(xpath).hash = docid.hashCode();
			//	html_page.xpath2info.get(xpath).num = html_page.xpath2num.get(xpath);
			}	
		}
		HashMap<String, XpathInfo> temp = new HashMap<String, XpathInfo>();
		for (Entry<String, XpathInfo> entry: html_page.xpath2info.entrySet()) {
			temp.put(String.valueOf(entry.getKey().hashCode()), entry.getValue());
		}
		html_page.xpath2info = temp;
	//	html_page.tags2Shingles();
	}

	
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// TODO Auto-generated method stub
	}

	
	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub
	}
	
	public boolean isSidebar(String id_attr, String  class_attr) {
		if (id_attr != null && sidebar_keyword.contains(id_attr.toLowerCase()) ) {
			return true;
		}
		if (class_attr != null && sidebar_keyword.contains(class_attr.toLowerCase())) {
			return true;
		}
		return false;
	}
	

	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
	//	System.out.println(qName + " start.");
	//	s.append("<"+qName+">");
	//	System.out.println( qName);
		if (is_abnormal_page) {
			return;
		}
		if (xpath.length() > 2000) {
			is_abnormal_page = true;
			return;
		}
		if (qName.equalsIgnoreCase("a")) {
			in_a = true;
		}
		Integer num = 1;
		Element e = path.peekLast();
		if (e != null) {
			e.is_leaf = false;
			if (e.tag2num.containsKey(qName)) {
				num = e.tag2num.get(qName);
				++num;
				e.tag2num.put(qName, num);
			} else {
				num = 1;
				e.tag2num.put(qName, num);
			}
		}
		String full_tag = qName;
		
		String id = atts.getValue("id");
		
		if (id != null) {
			id = id.replaceAll("[0-9]+", "0");
			if (id.length() >= 15) {
				id = "";
			}
			if (id != null) {
				full_tag = full_tag + "(" + id + ")";
			}
		}
		
		
		String class_attr = atts.getValue("class");
		if (class_attr != null) {
	//		class_attr = class_attr.replaceAll("[ \t\r\n]?(&nbsp;)?(&gt;)?", "");
			int t = class_attr.indexOf(" ");
			if (t > 30 || (t < 0 && class_attr.length() > 30)) {
				class_attr = "";
			}
			class_attr = class_attr.replaceAll("[0-9]+", "0");
			t = class_attr.indexOf(" ");
			if (class_attr.length() > 50) {
				if (t > 0) {
					class_attr = class_attr.substring(0, t);
				}
			}
			if (class_attr != null) {
				full_tag = full_tag + "(" + class_attr + ")";
			}
		}
		
		
		
		Element e_new = new Element();
		e_new.tag_name = qName;
		
		String style_attr = atts.getValue("style");
		if (style_attr != null) {
			if (style_attr.toLowerCase().indexOf("display") >= 0 && style_attr.toLowerCase().indexOf("none") >= 0) {
				e_new.is_display = true;
			}
		}
		if (e != null && e.is_display) {
			e_new.is_display = true;
		}
		
		if (isSidebar(id, class_attr)) {
			e_new.is_sidebar = true;
		}
		if (e != null && e.is_sidebar) {
			e_new.is_sidebar = true;
		}
		
		// to be fixed e-->new_e
		if (qName.equalsIgnoreCase("img")) {
			String href_attr = atts.getValue("src");
			if (href_attr != null && href_attr.length() > 0 && e != null) {
				e.text.append(href_attr);
				e.text_len = 20;
			}
		}
		
		// to be fixed e-->new_e
		if (qName.equalsIgnoreCase("video")
				|| qName.equalsIgnoreCase("object")
				|| qName.equalsIgnoreCase("embed")) {
			if (e != null) {
				e.text_len = 300;
				e.text.append(ran.nextLong());
			}
		}
		
		path.add(e_new);
		
		xpath.append(qName + "/");
		
		String attr_node = "";
		String num_node = "";
		if (!qName.equalsIgnoreCase("body") && !qName.equalsIgnoreCase("html")) {
			attr_node = full_tag;
			num_node = full_tag+"["+num+"]";
		} else {
			attr_node = qName;
			num_node = qName;
		}
		full_xpath.append(attr_node + "/");
		
		
		
		PathNode path_node = new PathNode();
		path_node.attr_node = attr_node;
		path_node.index_node = num_node;
		new_xpath.add(path_node);
		
		
		if (qName.equalsIgnoreCase("head")) {
			in_head = true;
		}
		if (!in_head) {
			if (!qName.equalsIgnoreCase("script") && !qName.equalsIgnoreCase("style")) {
				
			//	String tag2 = qName+"["+num+"]";
				s.append(""+full_tag+" ");
			}
		}
	}

	
	public void endElement(String uri, String localName, String qName) throws SAXException {
	//	System.out.println(qName + " end.");
	//	s.append("</" + qName + ">");
		if (is_abnormal_page) {
			return;
		}
		if (qName.equalsIgnoreCase("a")) {
			in_a = false;
		}
		Element e = path.pollLast();
	//	path.removeLast();
 		
	//	System.out.println(full_xpath + "\t" + e.text);
		
		if (!in_head) {
		//	System.out.println(full_xpath + "\t" + e.text);
			if (!qName.equalsIgnoreCase("script") && !qName.equalsIgnoreCase("style")) {
			//	System.out.println(full_xpath + "\t" + e.text);
				s.append("/" + qName + " ");
				if (e.text_len > 0 && !e.is_display) {
					boolean in_merge_tag = false;
					for (String merge_tag: merge_tags) {
						if (xpath.indexOf(merge_tag)>=0) {
							in_merge_tag = true;
							break;
						}
					}
					if (in_merge_tag) {
						Element parent = path.peekLast();
						parent.text_len += e.text_len;
						parent.anchor_len += e.anchor_len;
						parent.text.append(e.text);
					} else {
						boolean in_bottom = false;
						
						int line = locator.getLineNumber();
						if (line*2 > html_line_num) {
							for (int i = 0; i < bottom_block_keyword.length; ++i) {
								if (e.text.indexOf(bottom_block_keyword[i]) >= 0) {
									in_bottom = true;
								//	System.out.println("line number: " + locator.getLineNumber() + "\t" + html_line_num);
									break;
								}
							}
						}
						
						/*
						if (in_bottom) {
							e.is_sidebar = true;
						}
						*/
						
						if (!in_bottom) {
						//	System.out.println(e.text);
							int dom_weight = e.text_len;
							if (qName.equalsIgnoreCase("option")) {
								dom_weight = 1;
							}

							Integer n = dom_weight;
							int num_index = 6;
							if (new_xpath.size() < 10) {
								num_index = new_xpath.size() - 4;
								if (num_index < 3) {
									num_index = 3;
								}
							}
							StringBuffer xpath_temp = new StringBuffer();
							int cnt = 0;
							for (PathNode pn: new_xpath) {
								++cnt;
								if (cnt <= num_index) {
									xpath_temp.append(pn.index_node + "/");
								} else {
									xpath_temp.append(pn.attr_node + "/");
								}
							}
							
							
							String xpath_used = xpath_temp.toString();
//							if (xpath_used.toString().equals("html/body/div(main)[2]/div(bright)/div(position0_content)/"))
//								System.out.println("eeeeeeee: " + full_xpath + "\t" + e.text);

							StringBuffer path_text;
							if (html_page.xpath2text.containsKey(xpath_used)) {
								path_text = html_page.xpath2text.get(xpath_used);
								path_text.append(e.text.toString());
							} else {
								path_text = new StringBuffer();
								path_text.append(e.text.toString());
							}
							html_page.xpath2text.put(xpath_used, path_text);
							if (html_page.xpath2info.containsKey(xpath_used)) {
								XpathInfo xi = html_page.xpath2info.get(xpath_used);
								xi.num += dom_weight;
								xi.anchor_len += e.anchor_len;
								if (xi.before_max_node_len > max_node_len) {
									xi.before_max_node_len = max_node_len;
								}
								if (xi.before_text_len > text_len) {
									xi.before_text_len = text_len;
								}
								if (!e.is_sidebar) {
									xi.is_sidebar = false;
								}
								
							} else {
								XpathInfo xi = new XpathInfo();
								xi.is_sidebar = e.is_sidebar;
								xi.num = dom_weight;
								xi.anchor_len = e.anchor_len;
								xi.before_max_node_len = max_node_len;
								xi.before_text_len = text_len;
								html_page.xpath2info.put(xpath_used, xi);
							}
							text_len += e.text_len-e.anchor_len;
							if (max_node_len < e.text_len-e.anchor_len) {
								max_node_len = e.text_len-e.anchor_len;
							}
						}
					}
				}
			}
		}
		if (qName.equalsIgnoreCase("head")) {
			in_head = false;
		}
		xpath.delete(xpath.lastIndexOf("/"), xpath.length());
		if (xpath.lastIndexOf("/")>=0) {
			xpath.delete(xpath.lastIndexOf("/")+1, xpath.length());
		} else {
			xpath.delete(0, xpath.length());
		}
		full_xpath.delete(full_xpath.lastIndexOf("/"), full_xpath.length());
		if (full_xpath.lastIndexOf("/")>=0) {
			full_xpath.delete(full_xpath.lastIndexOf("/")+1, full_xpath.length());
		} else {
			full_xpath.delete(0, full_xpath.length());
		}
		new_xpath.pollLast();
		
	}
	
	
	public static int cleanString(String s, StringBuffer sb) {
		StringBuffer tmp = new StringBuffer();
	//	StringBuffer sb = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (cleaned_chars.contains(c)) {
				continue;
			}
			if (c == '&') {
				if (s.length() >= i+6) {
					String s2 = s.substring(i, i+6);
					if (s2.equalsIgnoreCase("&nbsp;")) {
						i=i+5;
						continue;
					}
			    }
				if (s.length() >= i+4) {
					String s2 = s.substring(i, i+4);
					if (s2.equalsIgnoreCase("&gt;")) {
						i=i+3;
						continue;
					}
				}
			}
			tmp.append(c);
		}
		for (int i = 0; i < tmp.length(); ++i) {
			char c = tmp.charAt(i);
			if ((c>='a' && c<='z')||(c>='A'&&c<='Z')) {
				int j = i+1;
				sb.append(c);
				for (; j < tmp.length(); ++j) {
					char c2 = tmp.charAt(j);
					if ((c2>='a' && c2<='z')||(c2>='A'&&c2<='Z')) {
						sb.append(c2);
					} else {
						break;
					}
				}
				sb2.append('a');
				i=j-1;
				continue;
			}
			if ((c>='0'&&c<='9') || num_chars.contains(c)) {
				int j = i+1;
				sb.append(c);
				for (; j < tmp.length(); ++j) {
					char c2 = tmp.charAt(j);
					if ((c2>='0'&&c2<='9') || num_chars.contains(c2)) {
						sb.append(c2);
					} else {
						break;
					}
				}
				sb2.append('0');
				i=j-1;
				continue;
			}
			if (c == ' ' || c=='\u00a0' || c== '\u3000') {
				continue;
			}
			sb.append(c);
			sb2.append(c);
		}
		return sb2.length();
	}

	
	
	public void characters(char[] ch, int start, int length) throws SAXException {
	//	String s = String.valueOf(ch, start, length);
//		System.out.println(s);
		Element element = path.peekLast();
		if (element.tag_name.equalsIgnoreCase("script") || element.tag_name.equalsIgnoreCase("style")) {
			return;
		}
		String s = String.valueOf(ch, start, length);
		
		StringBuffer sb = new StringBuffer();
		int len = cleanString(s, sb);
		/*
		s = s.replaceAll("[\t\r\n]?(&nbsp;)?(&gt;)?", "");
		String s2 = s.replaceAll("[0-9-:/()]+", "0");
		s2 = s2.replaceAll("[a-zA-Z]+", "a");
		s = s.replaceAll("[ ]?", "");
		s = s.replaceAll("　", "");
		s2 = s2.replaceAll("[ ]?", "");
		s2 = s2.replaceAll("　", "");
		if (!s.equals(sb.toString()) || s2.length() != len) {
			System.out.println("here:" + String.valueOf(ch, start, length) + "\t" + s2 );
		} else {
		//	System.out.println("ffff:" + s + "\t" + sb.toString());
		}
		*/
		
		element.text_len += len;
		if (in_a) {
			element.anchor_len += len;
		}
		element.text.append(sb.toString().toLowerCase());

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
	
	public static void main(String[] args) throws IOException {

		String s2="<html><body><div><br />test<div></div><div></div></div><div><br />test<br /><br /></div></body></html>";
	//	String s2 = "<div>test<div>  0 </div></div>";
		
		FileInputStream f_stream = new FileInputStream(new File("t.html"));
		byte[] bs = new byte[1024*1024*4];
		int len = f_stream.read(bs);
		String s = new String(bs, 0, len, "UTF-8");
		
		PageSegmentation		htmlContentHandler		= new PageSegmentation();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);

		try {
			long ela = 0;
			for (int i = 0; i < 10; ++i) {
				long a = System.currentTimeMillis();
				htmlContentHandler.setHtmlCode(s);
				parser.parse(new InputSource(new StringReader(s)));
				long b = System.currentTimeMillis();
				ela += b -a ;
				System.out.println(i + "\t" + (b-a));
			}
			System.out.println(ela);
			HtmlPage html_page = htmlContentHandler.html_page;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
} 

