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
import com.sogou.cm.pa.pagecluster.PageSegmentation;

class DomNode {
	String name;
	String full_name;
	StringBuffer text;
	StringBuffer contract_text;
	StringBuffer contract_text_full;
	int anchor_len;
	int type;
	boolean is_displaynone;
	boolean is_maincontent;
	boolean parent_is_maincontent;
	
	boolean is_belong_to_content;
	boolean not_belong_to_content;
	DomNode() {
		is_displaynone = false;
		is_maincontent = false;
		parent_is_maincontent = false;
		is_belong_to_content = false;
		not_belong_to_content = false;
		type = 0;
		anchor_len = 0;
		name = "";
		full_name = "";
		text = new StringBuffer();
		contract_text = new StringBuffer();
		contract_text_full = new StringBuffer();
	}
}

public class MainContentExtractorFromFlagPage implements ContentHandler {
	

	LinkedList<DomNode> path;
	StringBuffer full_xpath;

	HashSet<String> xpathes;
	ArrayList<DomNode> results;
	boolean is_abnormal_page;

	String clusterid;
	StringBuffer main_content;
	public String main_content_out;
	
	HashSet<String> not_mc_tags;
	
	DomNode current_contract_node;
	
	public MainContentExtractorFromFlagPage() {

		path = new LinkedList<DomNode>();
		full_xpath = new StringBuffer();

		is_abnormal_page = false;
		xpathes = new HashSet<String>();
		results = new ArrayList<DomNode>();

		clusterid = "";
		main_content = new StringBuffer();
		not_mc_tags = new HashSet<String>();
		not_mc_tags.add("option");
		current_contract_node = null;
	}
	
	public void clear() {

		path.clear();
		full_xpath.setLength(0);
		full_xpath.trimToSize();

		is_abnormal_page = false;
		results.clear();
		main_content.setLength(0);
		main_content.trimToSize();
		current_contract_node = null;
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
		main_content_out = main_content.toString().replaceAll("( )+", "");
	}

	
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// TODO Auto-generated method stub
	}

	
	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub
	}
	
	private int getBlockType(String attr) {
		int type = 0;
		int i1 = attr.indexOf("blocktype");
		if (i1 >= 0) {
			int i2 = attr.indexOf(";", i1+10);
			if (i2 > 0) {
				try {
					type = Integer.valueOf(attr.substring(i1+10, i2));
				} catch (Exception e) {
					return 0;
				}
			}
		}
		return type;
	}
	

	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		if (full_xpath.length() > 4000) {
			is_abnormal_page = true;
			return;
		}
		DomNode e = path.peekLast();

		
		String attr_str = "";
		String id = atts.getValue("id");
		String class_attr = atts.getValue("class");
		


		String full_name = HtmlContentHandler.getFulltag(qName, id, class_attr);
		full_xpath.append("/" + full_name);

		
		
		DomNode new_e = new DomNode();
		new_e.name = qName;
		new_e.full_name = full_name;
		
		String style = atts.getValue("style");
		if (style != null) {
			style = style.toLowerCase().replaceAll(" ", "");
		}
		if (style != null && (style.indexOf("display:none")>= 0 || style.indexOf("display：none")>=0)) {
			new_e.is_displaynone = true;
		}
		if (e != null && e.is_displaynone) {
			new_e.is_displaynone = true;
		}
		
		if (e != null && (e.is_maincontent || e.parent_is_maincontent)) {
			new_e.parent_is_maincontent = true;
		}
		String block_feature = atts.getValue("blockfeature");
		if (block_feature != null && block_feature.length() > 0) {
	//		System.out.println("hhhh   " + block_feature);
			if (block_feature.indexOf("ismaincontent") >= 0) {
				new_e.is_maincontent = true;
			}
			int type = getBlockType(block_feature);
			new_e.type = type;
			if (type == 2 || type == 6) {
				new_e.is_belong_to_content = true;
			} else if (type != 1 && type != 0) {
				new_e.not_belong_to_content = true;
			}
		}
		if (e != null && e.is_belong_to_content) {
			new_e.is_belong_to_content = true;
		}
		if (e != null && e.not_belong_to_content) {
			new_e.not_belong_to_content = true;
		}
		path.add(new_e);
		
		if (new_e.type == 10) {
			current_contract_node = new_e;
		}

	}

	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		DomNode e = path.peekLast();
		full_xpath.delete(full_xpath.lastIndexOf("/"), full_xpath.length());
	//	System.out.println(full_xpath);
	//	System.out.println(qName);

		if (qName.equalsIgnoreCase(e.name)) {
			path.pollLast();
			if (qName.equals("a")) {
				e.anchor_len = e.text.length();
			}
			DomNode parent = path.peekLast();
			if (parent != null && e.text.length() > 0) {
				parent.text.append(e.text);
				parent.anchor_len += e.anchor_len;
			}
			if (e.type == 10) {
				current_contract_node = null;
				//System.out.println(e.contract_text_full.length() + "\t" + e.contract_text);
				if (e.text.length() > 200 || e.anchor_len > 20
						|| (e.contract_text_full.length() > e.contract_text.length()*2 && e.contract_text_full.length() > 100)) {
					main_content.append(e.contract_text + " ");
				} else {
					main_content.append(e.contract_text_full + " ");
				}
			}
		}
		/*
		if (e.is_maincontent) {
			System.out.println("mc: " + e.text);
		}
		*/
		/*
		if (e.is_maincontent && !e.parent_is_maincontent) {
			main_content.append(e.text + " ");
		}
		*/

	}

	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (is_abnormal_page) {
			return;
		}

		
		
		
		DomNode element = path.peekLast();
		if (element.name.equalsIgnoreCase("script") || element.name.equalsIgnoreCase("style")) {
			return;
		}
		String s = String.valueOf(ch, start, length);
		//s = s.replaceAll("[ \t\r\n\u00a0\u3000]?(&nbsp;)?(&gt;)?", "");
		//s = s.replaceAll(" ", "");
		StringBuffer out = new StringBuffer();
		PageSegmentation.cleanString(s, out);
		s = BCConvert.bj2qj(out.toString());
		
		if (element.type == 10) {
		//	System.out.println("ddd: " + out.toString());
			if (HtmlContentHandler.containContract(out.toString())) {
				element.contract_text.append(s);
			}
		}
		
		if (!not_mc_tags.contains(element.name) && !element.is_displaynone) {
			//System.out.println("here.");
			if (element.is_maincontent || element.parent_is_maincontent || element.is_belong_to_content) {
			//	System.out.println("here.");
				if (!element.not_belong_to_content) {
				//	System.out.println("here.");
					main_content.append(s + " ");
				}
			}
		}
		element.text.append(s);
		if (current_contract_node != null) {
			current_contract_node.contract_text_full.append(s);
		}
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
		FileInputStream f_stream = new FileInputStream(new File("tt.html"));
		byte[] bs = new byte[1024*1024*4];
		int len = f_stream.read(bs);
		String s = new String(bs, 0, len, "UTF-8");

		MainContentExtractorFromFlagPage		htmlContentHandler		= new MainContentExtractorFromFlagPage();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		
		String out = "";
		try {
			HashSet<String> xpathes = new HashSet<String>();
		//	xpathes.add("/html/body/div[@class='main']/div[@class='content']/div[@class='contentR']");
			//xpathes.add("/html/body/div[@id='wp' and @class='wp']/div[@id='ct' and @class='wp cl']/table/tbody/tr/td[@class='plc']/div[@class='pct']");
		//	htmlContentHandler.setXpathes(xpathes);
			parser.parse(new InputSource(new StringReader(s)));
			String o = htmlContentHandler.main_content_out;

			System.out.println(o);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
} 

