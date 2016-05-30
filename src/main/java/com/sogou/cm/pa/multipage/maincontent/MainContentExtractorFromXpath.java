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

import com.sogou.cm.pa.pagecluster.PageSegmentation;
import com.sogou.cm.pa.maincontent.HtmlContentHandler;
import com.sogou.cm.pa.maincontent.UrlUtils;

class CandidateNodeInfo {
	HashSet<String> original_xpathes;
	TreeNode treenode;
	int text_len;
	boolean in_use;
	HashSet<String> undetermined_xpathes;
	CandidateNodeInfo() {
		original_xpathes = new HashSet<String>();
		treenode = null;
		text_len = -1;
		in_use = false;
		undetermined_xpathes = new HashSet<String>();
	}
}

class ErrorCorrection {
	TreeNode treenode;
	String xpath;
	int text_len;
	int second_text_len;
	ErrorCorrection() {
		treenode = null;
		xpath = "";
		text_len = -1;
		second_text_len = -1;
	}
}

public class MainContentExtractorFromXpath implements ContentHandler {
	

	LinkedList<Element> path;
	StringBuffer full_xpath;
	LinkedList<PathNode> new_xpath;
//	HashSet<String> xpathes;
	HashMap<String, Integer> xpath2type;
	ArrayList<Element> results;
	boolean is_abnormal_page;
	public TreeNode dom_tree;
	LinkedList<TreeNode> tree_path;
	String clusterid;
	HashMap<String, CandidateNodeInfo> candidate_nodes;
	HashSet<String> mcs;
	HashSet<String> tags;
	HashSet<String> media_tags;
	HashSet<String> merageable_tags;
	HashSet<String> mc_blocktype;
	ErrorCorrection error_correction;
	int all_mc_len = 0;
	boolean is_homepage;
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
	
	public MainContentExtractorFromXpath() {

		path = new LinkedList<Element>();
		full_xpath = new StringBuffer();
		new_xpath = new LinkedList<PathNode>();
		is_abnormal_page = false;
	//	xpathes = new HashSet<String>();
		xpath2type = new HashMap<String, Integer>();
		results = new ArrayList<Element>();
		dom_tree = new TreeNode();
		tree_path = new LinkedList<TreeNode>();
		mcs = new HashSet<String>();
		clusterid = "";
		candidate_nodes = new HashMap<String, CandidateNodeInfo>();
		tags = new HashSet<String>();
		tags.add("tr");
		tags.add("td");
		tags.add("table");
		tags.add("div");
		media_tags = new HashSet<String>();
		media_tags.add("img");
		media_tags.add("object");
		media_tags.add("video");
		media_tags.add("embed");
		merageable_tags = new HashSet<String>();
		merageable_tags.add("p");
		merageable_tags.add("br");
		merageable_tags.add("a");
		merageable_tags.add("img");
		merageable_tags.add("font");
		merageable_tags.add("i");
		merageable_tags.add("u");
		merageable_tags.add("ignore_js_op");
		merageable_tags.add("strong");
		mc_blocktype = new HashSet<String>();
		mc_blocktype.add("0");
		mc_blocktype.add("1");
		mc_blocktype.add("2");
		mc_blocktype.add("6");
		mc_blocktype.add("10");
		mc_blocktype.add("14");
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
		error_correction = new ErrorCorrection();
		all_mc_len = 0;
	//	is_homepage = false;
	}
	
	public void setUrl(String url) {
		if (UrlUtils.urlIsHomepage(url)) {
			is_homepage = true;
		} else {
			is_homepage = false;
		}
	}
	
	public void setXpathes(HashMap<String, Integer> input, HashSet<String> input_mcs) {
//		xpathes.clear();
		xpath2type.clear();
		mcs.clear();
		candidate_nodes.clear();
		Iterator iter = input.entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			String s = (String) entry.getKey();
			Integer type = (Integer) entry.getValue();
		//	System.out.println(type + "\t" + s);
			xpath2type.put(s, type);
		}
		for (String s: input_mcs) {
			int pos = s.lastIndexOf("[");
			int pos2 = s.lastIndexOf("/");
			if (pos < pos2) {
				pos = s.length();
			}
			String tag = "";
			if (pos2<pos && pos2>=0)
				tag = s.substring(pos2+1, pos).toLowerCase();
			
			if (!s.endsWith("']") && !s.endsWith(")]") && s.endsWith("]") && tags.contains(tag)) {
				String xpath2 = s.substring(0, pos);
				
				CandidateNodeInfo cni = candidate_nodes.get(xpath2);
				if (cni == null) {
					cni = new CandidateNodeInfo();
					cni.original_xpathes.add(s);
					candidate_nodes.put(xpath2, cni);
				//	System.out.println(xpath2);
				} else {
				//	System.out.println("here.");
				//	cni.in_use = false;
					cni.original_xpathes.add(s);
				//	xpathes.add(cni.original_xpath);
				}
			} else {
			//	System.out.println("asdf");
			//	xpathes.add(s);
				mcs.add(s);
			}
		}
	}
	
	public void setClusterId(String id) {
		clusterid = id;
	}
	
	public String getMainContent() {
		return BCConvert.bj2qj(dom_tree.toMaincontentString());
	}

	
	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
	}

	
	public void startDocument() throws SAXException {
		clear();
	}

	
	public void endDocument() throws SAXException {
		for (String xpath: candidate_nodes.keySet()) {
			CandidateNodeInfo cni = candidate_nodes.get(xpath);
			if (cni.undetermined_xpathes.size() > 0 && cni.treenode != null) {
			//	System.out.println("here.  " + cni.undetermined_xpathes.size());
				cni.treenode.add_atts.put("ismaincontent", "true");
				all_mc_len += cni.text_len;
				cni.treenode.add_atts.put("blocktype", "1");
				cni.treenode.add_atts.put("xpathkey", String.valueOf(cni.undetermined_xpathes.toArray()[0].hashCode()));
			//	System.out.println("h1:  " + cni.undetermined_xpathes.toArray()[0]);
			}
		}
		//System.out.println(all_mc_len);
		if (all_mc_len*2 < error_correction.text_len && error_correction.text_len > 500) {
		//	System.out.println("hhh");
			error_correction.treenode.add_atts.put("ismaincontent", "true");
			error_correction.treenode.add_atts.put("blocktype", "1");
			error_correction.treenode.add_atts.put("mccorrection", "true");
			error_correction.treenode.add_atts.put("xpathkey", String.valueOf(error_correction.xpath.hashCode()));
		}
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

	//	System.out.println(e.xpath + "\t" + e.text);
	//	System.out.println(qName);
		if (qName.equalsIgnoreCase(e.name)) {
			path.pollLast();
			if (qName.equalsIgnoreCase("a")) {
				e.anchor_len = e.text_len;
			}

			Element parent = path.peekLast();
			if (parent != null && e.text_len > 0) {
				//parent.text.append(e.text);
				parent.text_len += e.text_len;
				parent.anchor_len += e.anchor_len;
				if (qName.equalsIgnoreCase("p")) {
					parent.p_tag_num++;
				}
			}

			if (parent != null) {
				if (media_tags.contains(qName.toLowerCase()) && !is_homepage) {
				//	parent.text.append("this node contain media tags.");
					parent.text_len += 30;
				}
				if (!merageable_tags.contains(qName.toLowerCase())) {
					parent.child_is_merageable = false;
				}
				if (e.text_len>0) {
					parent.child_num++;
				}
				if (e.text_len >e.anchor_len*5 && !qName.equals("li")) {
					parent.text_child_num++;
				}
			}
			
			TreeNode tn = tree_path.peekLast();
			tn.text_len = e.text_len;
			tn.anchor_len = e.anchor_len;
			

			StringBuffer num_xpath = new StringBuffer();
			int xpath_depth = new_xpath.size();
			for (int i = 0; i < xpath_depth-2; ++i) {
				num_xpath.append("/"+new_xpath.get(i).attr_node);
			}
			if ( xpath_depth > 1) {
				num_xpath.append("/" + new_xpath.get(xpath_depth-2).index_node);
			}
			String xpath2 = num_xpath.toString() + "/" + qName;
			//System.out.println(full_xpath);
			num_xpath.append("/" + new_xpath.get(xpath_depth-1).index_node);
	//		System.out.println(num_xpath);
		//	System.out.println(num_xpath + "\t" + e.text);
			String xpathkey = "";
			int type = 0;
			
			if (xpath2type.containsKey(num_xpath.toString())) {
				xpathkey = num_xpath.toString();
				type = xpath2type.get(num_xpath.toString());
			} else if (xpath2type.containsKey(full_xpath.toString())) {
			//	System.out.println(full_xpath);
				xpathkey = full_xpath.toString();
				type = xpath2type.get(full_xpath.toString());
			}
			if (xpathkey.length() > 0) {
				results.add(e);
				//TreeNode tn = tree_path.peekLast();
				/*
				if (type == 1) {
					tn.add_atts.put("ismaincontent", "true");
					all_mc_len += e.text.length();
				}
				*/
				//System.out.println(type + "\t" + xpathkey);
				if (!((type == 4 || type == 3 || type == 12|| type == 13 || type == 16) && e.text_len > 150) 
						&& !((type == 10) && e.text_len > 800) && !(type == 6 && e.text.toString().toLowerCase().indexOf("copyright©")>=0)) {
					tn.add_atts.put("blocktype", String.valueOf(type));
					tn.add_atts.put("xpathkey", String.valueOf(xpathkey.hashCode()));
				}
			//	System.out.println("here");
			}
			
			if ((e.child_is_merageable || (e.own_text_len>500 && (e.text_len-e.own_text_len)*4<e.own_text_len) || (e.p_tag_num > 5 && e.text_len > 500) || (e.text_child_num*1.2>e.child_num && e.text_child_num > 10 && e.text_len>1000 && type != 9)) && e.text_len >e.anchor_len*4) {
				if (e.text_len > error_correction.text_len) {
				//	System.out.println(e.xpath + "\t" + e.text_len);
					error_correction.second_text_len = error_correction.text_len;
					error_correction.text_len = e.text_len;
					error_correction.treenode = tree_path.peekLast();
					error_correction.xpath = full_xpath.toString();
				}
			}
			
			if (mcs.contains(num_xpath.toString()) || mcs.contains(full_xpath.toString())) {
			//	System.out.println("asdf");
				if (!(is_homepage && (e.anchor_len*2>e.text_len || e.text_len < 10))) {
				//	System.out.println("hhh");
					results.add(e);
				//	TreeNode tn = tree_path.peekLast();
					tn.add_atts.put("ismaincontent", "true");
					String blocktype = tn.add_atts.get("blocktype");
					if (blocktype == null || mc_blocktype.contains(blocktype)) {
						all_mc_len += e.text_len;
					}
					
					tn.add_atts.put("xpathkey", String.valueOf(xpathkey.hashCode()));
					/*
					int article_text = 0;
					int article_anchor = 0;
					int p_num = 0;
					int other_text = 0;
					for (TreeNode child: tn.children) {
						if (child.tag.equals("p")) {
							++p_num;
						} else {
							other_text += child.text_len;
							if (other_text > 10) {
								break;
							}
						}
						article_text += child.text_len;
						article_anchor += child.anchor_len;
						if (child.)
					} 
					*/
				}
			} else {
				CandidateNodeInfo cni = candidate_nodes.get(xpath2);
				if (cni != null) {
					if (cni.original_xpathes.contains(num_xpath.toString()) && e.text_len >= 10) {
						if (!(is_homepage && (e.anchor_len*2>e.text_len || e.text_len < 10))) {
						//	System.out.println("hhh  " + e.text_len + "\t" + e.anchor_len);
							results.add(e);
						//	TreeNode tn = tree_path.peekLast();
							tn.add_atts.put("ismaincontent", "true");
							all_mc_len += e.text_len;
							tn.add_atts.put("blocktype", "1");
							tn.add_atts.put("xpathkey", String.valueOf(num_xpath.toString().hashCode()));
						}
					//	System.out.println("hereee.  " + num_xpath.toString() + "\t" + e.text_len);
					/*
						if (num_xpath.toString().equals("/html/body/table/tbody[1]/tr[2]")) {
							System.out.println("h2:  " + e.text.length() + "\ta" + e.text + "a");
						}
						*/
						/*
						cni.original_xpathes.remove(num_xpath.toString());
						if (cni.original_xpathes.size() == 0) {
							cni.in_use = false;
						}
						*/
					} else {
						if (cni.original_xpathes.contains(num_xpath.toString())) {
					//		System.out.println("here.  " + num_xpath.toString() + "\t" + e.text_len);
							cni.undetermined_xpathes.add(num_xpath.toString());
						}
						if ((e.text_len > e.anchor_len*2) && e.text_len > cni.text_len) {
						//	System.out.println("here  " + num_xpath + "\t" + e.text_len);
						//	TreeNode tn = tree_path.peekLast();
							cni.text_len = e.text_len;
							cni.treenode = tn;
						}
					}
				}
			}
			
			
			
			full_xpath.delete(full_xpath.lastIndexOf("/"), full_xpath.length());
			new_xpath.pollLast();
			tree_path.pollLast();
		}

	}
	
	public static int cleanString(String s, StringBuffer sb) {
		StringBuffer tmp = new StringBuffer();
	//	StringBuffer sb = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (cleaned_chars.contains(c)) {
				tmp.append(' ');
				continue;
			}
			if (c=='\u00a0' || c== '\u3000') {
				tmp.append(' ');
				continue;
			}
			if (c == '&') {
				if (s.length() >= i+6) {
					String s2 = s.substring(i, i+6);
					if (s2.equalsIgnoreCase("&nbsp;")) {
						i=i+5;
						tmp.append(' ');
						continue;
					}
			    }
				if (s.length() >= i+4) {
					String s2 = s.substring(i, i+4);
					if (s2.equalsIgnoreCase("&gt;")) {
						i=i+3;
						tmp.append(' ');
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
			if (c == ' ') {
				while (i+1<tmp.length()) {
					c = tmp.charAt(i+1);
					if (c == ' ') {
						++i;
					} else {
						break;
					}
				}
				if (sb.length() > 0 && i+1<tmp.length()) {
					sb.append(' ');
				}
				continue;
			}
			sb.append(c);
			sb2.append(c);
		}
		return sb2.length();
	}
	

	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		Element element = path.peekLast();
		if (element.name.equalsIgnoreCase("script") || element.name.equalsIgnoreCase("style") || element.name.equalsIgnoreCase("iframe")) {
			return;
		}
		
		String s = String.valueOf(ch, start, length);
		TreeNode tn = new TreeNode();
		tn.text = s;
		TreeNode p = tree_path.peekLast();
		if (p != null) {
			p.children.add(tn);
		}
		

		StringBuffer out = new StringBuffer();
		int n = MainContentExtractorFromXpath.cleanString(s, out);
		s = out.toString();
		//System.out.println("asdf " + s);
		/*
		s = s.replaceAll("[\t\r\n>]?(&nbsp;)?(&gt;)?", "");
		s = s.replaceAll("[0-9-:/()]+", "0");
		s = s.replaceAll("[a-zA-Z]+", "a");
		s = s.replaceAll("[ ]?", "");
		s = s.replaceAll("　", "");
		*/
		if (!element.is_displaynone && !element.name.equalsIgnoreCase("select") && !element.name.equalsIgnoreCase("option") && !element.name.equalsIgnoreCase("textarea")) {
			element.text_len += n;
			element.own_text_len += n;
			tn.clean_text = s;
		//	element.text.append(s.toLowerCase());
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
		FileInputStream f_stream = new FileInputStream(new File("t.html"));
		byte[] bs = new byte[1024*1024*4];
		int len = f_stream.read(bs);
		String s = new String(bs, 0, len, "UTF-8");

		MainContentExtractorFromXpath		htmlContentHandler		= new MainContentExtractorFromXpath();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		
		String out = "";
		try {
			HashMap<String, Integer> xpathes = new HashMap<String, Integer>();
			HashSet<String> mcs = new HashSet<String>();
			BufferedReader reader = new BufferedReader(new FileReader(new File("C:\\Users\\sunjian\\Downloads\\tt.txt")));
			
			String line;
			while ((line = reader.readLine()) != null) {
				String[] segs = line.split("\t");
				for (int i = 1; i < segs.length; ++i) {
					int type = Integer.valueOf(segs[i+1]);
					if (type != 1) {
						xpathes.put(segs[i], type);
					} else {
					//	System.out.println(segs[i]);
						mcs.add(segs[i]);	
					}
					++i;
				}
			}
			htmlContentHandler.setUrl("http://www.hunt007.com/guangxi-waimao-zhaopin.htm");
			htmlContentHandler.setXpathes(xpathes, mcs);
			parser.parse(new InputSource(new StringReader(s)));
			for (Element e: htmlContentHandler.results) {
			//	System.out.println(e.xpath + "\t" + e.text);
			}
		//	System.out.println(htmlContentHandler.dom_tree.traverse_debug());
			System.out.println(htmlContentHandler.getMainContent());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
} 

