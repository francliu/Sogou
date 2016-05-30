package com.sogou.cm.pa.maincontent;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;








import com.sogou.cm.pa.multipage.maincontent.PageSimilarityCalculator;
import com.sogou.web.selector.urllib.URLUtils;

class PathNode {
	String attr_node;
	String index_node;
	PathNode() {
		attr_node = "";
		index_node = "";
	}
}

public class HtmlContentHandler implements ContentHandler {
	
	Locator locator;
	ArrayList<Element> blocks;
	HashMap<Integer, ArrayList<Element>> type_blocks;
	HashSet<Element> candidate_blocks;
	StringBuffer xpath;
	StringBuffer full_xpath;
	ArrayList<Element> path;
	LinkedList<PathNode> new_xpath;
	HashMap<String, ArrayList<Element> > sub_blocks;
	String url;
	String domain;
	String title;
	int page_height;
	int mc_limit;
	boolean has_video_block;
	ArrayList<Element> candidate_contract_blocks;
	Element content_title;
	Element content_title_candidate;
	boolean is_abnormal_page;
	String test_xpath = "/html/body/div[@id='blog-163-com-main' and @class='nb-wrap wsy']/div[@class='nb-are nb-cnt']/div[@class='wkg']/div[@id='blog-163-com-container' and @class='c wc h clearfix']/div[@id='-3' and @class='nb-mdl lcr m-3']/div[@class='nb-mc lcr']/div[@class='c cc lcr nb-jsc']/div[@class='nbw-ryt ztag clearfix']/div[@class='left']/div[@class='lcnt bdwr bds0 bdc0']/div[@class='mcnt ztag']/div[@class='snl']";
	
	//rule parms
	String[] mergeable_tag = {"li", "dt", "dd", "p", "span", "strong", "font"};
	HashSet<String> mergeable_tag_set;
	int min_area = 10000;
	int mergeable_child_area = 30000;
	HashSet<String> title_tags;
	HashMap<Element, ArrayList<Element>> parent2children;
	
	boolean last_text_is_anchor;
	String last_anchor;
	
	
	HtmlContentHandler() {
		blocks = new ArrayList<Element>();
		type_blocks = new HashMap<Integer, ArrayList<Element>>();
		candidate_blocks = new HashSet<Element>();
		xpath = new StringBuffer();
		full_xpath = new StringBuffer();
		path = new ArrayList<Element>();
		sub_blocks = new HashMap<String, ArrayList<Element> >();
		candidate_contract_blocks = new ArrayList<Element>();
		url = "";
		domain = "";
		page_height = 0;
		has_video_block = false;
		is_abnormal_page = false;
		new_xpath = new LinkedList<PathNode>();
		parent2children = new HashMap<Element, ArrayList<Element>>();
		mergeable_tag_set = new HashSet<String>();
		mergeable_tag_set.add("p");
		mergeable_tag_set.add("span");
		title_tags = new HashSet<String>();
		title_tags.add("h1");
		title_tags.add("h2");
		title_tags.add("h3");
		title_tags.add("h4");
		last_text_is_anchor = false;
	}
	
	public void clear() {
		blocks.clear();
		page_height = 0;
		mc_limit = 0;
		has_video_block = false;
		type_blocks.clear();
		candidate_blocks.clear();
		xpath.setLength(0);
		xpath.trimToSize();
		path.clear();
		full_xpath.setLength(0);
		full_xpath.trimToSize();
		sub_blocks.clear();
		title = "";
		content_title = null;
		content_title_candidate = null;
		is_abnormal_page = false;
		new_xpath.clear();
		parent2children.clear();
		last_text_is_anchor = false;
		candidate_contract_blocks.clear();
	//	url = "";
	//	domain="";
	}
	
	public void setUrl(String input_url) {
		url = input_url;
		domain = URLUtils.getMainDomain(url);
		if (domain == null) {
			domain = "1234";
		}
	}

	
	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
		this.locator = locator;
	}

	
	public void startDocument() throws SAXException {
		clear();
	}
	
	private boolean isOverlap(Element e1, Element e2) {
		int e1_center_x = e1.left + e1.width/2;
		int e1_center_y = e1.top + e1.height/2;
		int e2_center_x = e2.left + e2.width/2;
		int e2_center_y = e2.top + e2.height/2;
		if (Math.abs(e1_center_x-e2_center_x) < Math.abs(e1.width/2+e2.width/2)) {
			if (Math.abs(e1_center_y-e2_center_y) < Math.abs(e1.height/2+e2.height/2)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isContain(Element e1, Element e2) {
		if (e2.left >= e1.left 
				&& e2.top >= e1.top
				&& e2.left + e2.width <= e1.left + e1.width
				&& e2.top + e2.height <= e1.top + e1.height) {
			return true;
		} else {
			return false;
		}
	}

	
	public void endDocument() throws SAXException {
		if (!type_blocks.containsKey(4) && content_title_candidate != null) {
			content_title_candidate.block_type = 4;
			ArrayList<Element> tmp = new ArrayList<Element>();
			tmp.add(content_title_candidate);
			type_blocks.put(4, tmp);
		}
						
		for (Element e: candidate_blocks) {
			blocks.add(e);
		}
		if (blocks.size() == 0 && sub_blocks.size() > 0) {
			Iterator iter = sub_blocks.values().iterator();
			while (iter.hasNext()) {
				blocks.addAll((ArrayList<Element>)iter.next());
			}
		}
		
		for (int i = 0; i < blocks.size(); ++i) {
			Element e = blocks.get(i);

			if (e.xy_modified) {
				for (int j = 0; j < blocks.size(); ++j) {
					if (j == i) {
						continue;
					}
					Element e2 = blocks.get(j);
					if (isOverlap(e, e2)) {
							e.left = e.child_left;
							e.width = e.child_right - e.child_left;
							e.top = e.child_top;
							e.height = e.child_bottom - e.child_top;

						break;
					}
				}
			}
		}
		
		ArrayList<Element> temp = new ArrayList<Element>();
		for (int i = 0; i < blocks.size(); ++i) {
			Element e = blocks.get(i);
			int area = e.width * e.height;
			if (area > 500000 && area > e.img_iframe_area * 10 && e.text_len < 20) {
				int j = 0;
				int contain_area = 0;
				for (; j < blocks.size(); ++j) {
					if (j == i) {
						continue;
					}
					Element e2 = blocks.get(j);
					if (isContain(e, e2)) {
						contain_area += e2.width*e2.height;
						if (contain_area > 60000)
							break;
					}
				}
				if (j >= blocks.size()) {
					temp.add(e);
				}
			} else {
				temp.add(e);
			}
		}
		blocks.clear();
		blocks = temp;
		
		HashSet<String> error_contracts = new HashSet<String>();
		if (candidate_contract_blocks.size() >= 5) {
			HashMap<String, Integer> xpath2num = new HashMap<String, Integer>();
			for (Element e: candidate_contract_blocks) {
				if (e.text_len > 8) {
					if (xpath2num.containsKey(e.full_xpath)) {
						xpath2num.put(e.full_xpath, xpath2num.get(e.full_xpath)+1);
					} else {
						xpath2num.put(e.full_xpath, 1);
					}
				}
			}
			for (Entry<String, Integer> entry: xpath2num.entrySet()) {
				if (entry.getValue() >= 5) {
					error_contracts.add(entry.getKey());
				}
			}
			ArrayList<Element> temp_blocks = new ArrayList<Element>();
			for (Element e: candidate_contract_blocks) {
				if (!error_contracts.contains(e.full_xpath)) {
					temp_blocks.add(e);
				}
			}
			candidate_contract_blocks = temp_blocks;
		}
		
		if (candidate_contract_blocks.size() > 0) {
			Tool.quickSortY(candidate_contract_blocks, 0, candidate_contract_blocks.size()-1);
			temp = new ArrayList<Element>();
			Element e = candidate_contract_blocks.get(0);
			e.block_type = 10;
			temp.add(e);
			for (int i = 1; i < candidate_contract_blocks.size(); ++i) {
				e = candidate_contract_blocks.get(i);
				boolean flag = false;
				for (Element j: temp) {
					if (j.left <= e.left && j.width+j.left >= e.width+e.left && j.top <= e.top && j.top+j.height>=e.top+e.height) {
						flag = true;
						break;
					}
				}
				if (!flag) {
					e.block_type = 10;
					temp.add(e);
				}
			}
			if (temp.size() > 0) {
				type_blocks.put(10, temp);
			}
		}
		
		List<Element> candidate_disclaimers = type_blocks.get(11);
		if(candidate_disclaimers != null && candidate_disclaimers.size() > 0) {
			HashSet<String> disclaimer_xpath_set = new HashSet<String>();
			HashSet<String> error_xpath_set = new HashSet<String>();
			for (Element e: candidate_disclaimers) {
				if (disclaimer_xpath_set.contains(e.full_xpath)) {
					error_xpath_set.add(e.full_xpath);
				} else {
					disclaimer_xpath_set.add(e.full_xpath);
				}
			}
			ArrayList<Element> temp_disclaimers = new ArrayList<Element>();
			for (Element e: candidate_disclaimers) {
				if (!error_xpath_set.contains(e.full_xpath)) {
					temp_disclaimers.add(e);
				}
			}
			type_blocks.put(11, temp_disclaimers);
		}
		
		
		
		
	//	Tool.quickSortY(blocks, 0, blocks.size()-1);
	}

	
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// TODO Auto-generated method stub
	}

	
	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub
	}
	
	public void setFeatureFromAtts(Attributes atts, Element e) {
		String id = atts.getValue("id");
		if (id != null) {
		//	id = id.replaceAll("[ \t\r\n]?(&nbsp;)?(&gt;)?", "");
			if (id.length() > 0) {
				e.id = id;
				e.ids_classes.append(id);
			}
		}
		String class_attr = atts.getValue("class");
		if (class_attr != null) {
		//	class_attr = class_attr.replaceAll("[ \t\r\n]?(&nbsp;)?(&gt;)?", "");
			if (class_attr.length() > 0) {
				e.class_attr = class_attr;
				e.ids_classes.append(class_attr);
			}
		}
		String style_attr = atts.getValue("style");
		if (style_attr != null) {
			style_attr = style_attr.replaceAll("[ \t\r\n]?(&nbsp;)?(&gt;)?", "");
			if (style_attr.length() > 0)
				e.style = style_attr;
		}
		String href_attr = atts.getValue("href");
		if (href_attr != null) {
			//style_attr = style_attr.replaceAll("[ \t\r\n]?(&nbsp;)?(&gt;)?", "");
			if (href_attr.length() > 0)
				e.src = href_attr;
		}
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
	
	public static String normalizeAttr(String name, String attr) {
		boolean is_re = false;
		if (attr == null || attr.length() == 0) {
			return null;
		}
		if (attr.toLowerCase().matches("col(umn)?[-_]?[1-3]")) {
			return "@" + name + "='" + attr + "'";
		}
		/*
		int pos = attr.indexOf(" ");
		if (pos > 0) {
			is_re = true;
			attr = attr.substring(0, pos);
		}
		*/
		int limit = 30;
		if (name.equalsIgnoreCase("id")) {
			limit = 15;
		}
		String temp = attr;
		temp = temp.replaceAll("[-_]", " ");
		String[] segs = temp.split(" ");
		int max_len = 0;
		for (String seg: segs) {
			if (seg.length() > max_len) {
				max_len = seg.length();
			}
		}
		if (max_len > limit) {
			return null;
		}
		if (attr.length() > 60) {
			attr = segs[0];
			is_re = true;
		}
		int i = 0;
		for (; i < attr.length(); ++i) {
			if (attr.charAt(i)>='0' && attr.charAt(i)<='9') {
				break;
			}
		}
		if (i == 0) {
			return null;
		}
		if (i < attr.length()) {
			is_re = true;
			attr = attr.substring(0, i);
		}
		if (!is_re) {
			return "@" + name + "='" + attr + "'";
		} else {
			return "starts-with(@" + name + ", '" + attr + "')";
		}
	}
	
	public static String getFulltag(String qName, String id, String class_attr) {
		if (qName.equalsIgnoreCase("html") || qName.equalsIgnoreCase("body")) {
			return qName;
		}
		String id_r = normalizeAttr("id" , id);
		String class_attr_r = normalizeAttr("class", class_attr);
		StringBuffer full_tag = new StringBuffer();
		full_tag.append(qName);
		String full_attr = "";
		if (id_r != null) {
			full_attr = id_r;
		}
		if (class_attr_r != null) {
			if (full_attr.length() > 0) {
				full_attr = full_attr + " and ";
			}
			full_attr += class_attr_r;
		}
		if (full_attr != null && full_attr.length() != 0) {
			full_tag.append("[" + full_attr + "]"); 
		}
		return full_tag.toString();
		
	}
	
	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		//System.out.println(qName);
		if (is_abnormal_page) {
			return;
		}
		xpath.append("/"+qName);
		if (xpath.length() > 2000) {
			is_abnormal_page = true;
		}
		
		Element e = new Element();
		e.tag = qName;
		e.xpath = xpath.toString();
		setFeatureFromAtts(atts, e);
		if (qName.equals("html")) {
			page_height = e.height;
		}
		
		
		
		String full_tag = this.getFulltag(qName, e.id, e.class_attr);
		full_xpath.append("/" + full_tag);
		e.full_xpath = full_xpath.toString();
		
	//	System.out.println("begin: " + e.full_xpath );
		
		Element parent = null;
		if (path.size()>0)
			parent = path.get(path.size()-1);
		Integer num = 1;
		if (parent != null) {
			if (parent.tag2num.containsKey(qName)) {
				num = parent.tag2num.get(qName);
				++num;
				parent.tag2num.put(qName, num);
			} else {
				num = 1;
				parent.tag2num.put(qName, num);
			}
		}
		if (parent == null || (parent != null && !parent.is_float)) {
			if (e.style.indexOf("z-index")>=0) {
				e.is_float = true;
			}
		}
		
		String attr_node = "";
		String num_node = "";
		if (!qName.equalsIgnoreCase("body") && !qName.equalsIgnoreCase("html")) {
			attr_node = full_tag;
			num_node = qName+"["+num+"]";
		} else {
			attr_node = qName;
			num_node = qName;
		}
		
		PathNode path_node = new PathNode();
		path_node.attr_node = attr_node;
		path_node.index_node = num_node;
		new_xpath.add(path_node);
		
		StringBuffer num_xpath = new StringBuffer();
		int xpath_depth = new_xpath.size();
		for (int i = 0; i < xpath_depth-2; ++i) {
			num_xpath.append("/"+new_xpath.get(i).attr_node);
		}
		if ( xpath_depth > 1) {
			num_xpath.append("/" + new_xpath.get(xpath_depth-2).index_node);
		}
		num_xpath.append("/" + new_xpath.get(xpath_depth-1).index_node);
		e.num_xpath = num_xpath.toString();
	//	System.out.println(e.full_xpath + "\t" + e.num_xpath);
		
		if (e.tag.equalsIgnoreCase("a")) {
			String e_domain = URLUtils.getMainDomain(e.src);
			if ((!domain.equals("alibaba.com")) && e_domain != null && !e_domain.equalsIgnoreCase(domain)) {
			//	System.out.println(domain + "\t" + e_domain);
				e.is_out_domain = true;
			}
		}
		path.add(e);
	}
	
	private boolean isSmall(Element e) {
		if (e.is_single_block == false) {
			return false;
		}
		if (e.tag.equalsIgnoreCase("ignore_js_op")) {
			return true;
		}
		if (e.width * e.height < mergeable_child_area) {
			return true;
		}
		if (e.width < 30 || e.height < 30) {
			return true;
		}
		if (e.text_len < 4 && e.width * e.height < 200000 ) {
			return true;
		}
		if (e.text_len == 0 && e.img_iframe_area == 0 && e.is_video == false && e.width * e.height < 300000) {
			return true;
		}
		if (e.tag.equalsIgnoreCase("img")) {
			if (e.width * e.height < 40000)
				return true;
		}
		return false;
	}
	
	private boolean isNormalBlock(Element e) {
		if (e.is_video) {
			return true;
		}
		if(e.is_tag) {
			return true;
		}
		if (e.width <= 10 || e.height <= 10) {
			return false;
		}
		if (e.tag.equalsIgnoreCase("br")) {
			return false;
		}
		//System.out.println("1: " + e.xpath + "\t" + e.left + "\t" + e.width);
		if (e.left < 0 || e.top < 0 || e.left > 1500 || e.left + e.width > 1500) {
			return false;
		}
	//	System.out.println("2: " + e.xpath);
		// filter block of tag_soup bug
		if (e.left < 100 && e.top <100 && e.width > 800 && e.height > 1000 && e.xpath.endsWith("body")) {
			return false;
		}
		int area = e.width * e.height;
		if (e.text_len < 4 && e.width * e.height < 100000 && e.height > 200) {
	//		return false;
		}
		if (e.tag.equalsIgnoreCase("img")) {
			if (e.width * e.height < 40000)
				return false;
		}
		return true;
	}
	
	private void isList(Element e, List<Element> children) {
	//	System.out.println(e.text);
		int area1 = 0;
		int area2 = 0;
		int text1 = 0;
		int text2 = 0;
		for (int i = 0; i < children.size(); ++i) {
			
			Element cur_e = children.get(i);
			if (cur_e.is_list) {
				area1 += cur_e.width * cur_e.height;
				text1 += cur_e.text_len;
			} else {
				area2 += cur_e.width * cur_e.height;
				text2 += cur_e.text_len;
			}
		}
	//	System.out.println(e.xpath + "\t" + area1 + "\t" + area2 + "\t" + e.text);
		if (area1 > area2 * 5) {
			e.is_list = true;
			return;
		}
		if (area1 > area2 * 3 && text1 > text2*8) {
			e.is_list = true;
			return;
		}
		
		int list_num = 0; 
		int strict_list_num = 0;
		for (Element child: children) {
			if (child.xpath.endsWith("li") || child.xpath.endsWith("dd") || child.xpath.endsWith("dt")) {
				if (child.anchor_len > 0) {
					++list_num;
				} else if (child.text_len == 0 && child.img_area > 0 && child.anchor_num > 0) {
					++list_num;
				}
				if (child.anchor_len*2>child.text_len) {
					++strict_list_num;
				}
			}
		}
		if (list_num >= 4 && list_num + 2 >= children.size()) {
			e.is_list = true;
			return;
		}
		if (strict_list_num >= 2 && children.size() == strict_list_num) {
			e.is_list = true;
			return;
		}
		String s = e.id + e.class_attr;
		if (s.toLowerCase().indexOf("list")>=0 && children.size() > 0) {
			int max_len = 0;
			int cnt = 0;
			Element before = children.get(0);

			for (int i = 1; i < children.size(); ++i) {
				Element child = children.get(i);
				if (before.height < 400 && child.height < 400) {
					if (before.tag.equals(child.tag) 
							&& before.left == child.left && before.width == child.width
							&& before.anchor_len > 0 && child.anchor_len > 0) {
						if (Math.abs(before.height - child.height) < 50) {
							cnt ++;
						} else {
							if (max_len < cnt) {
								max_len = cnt;
							}
							cnt = 0;
						}
					} else {
						if (max_len < cnt) {
							max_len = cnt;
						}
						cnt = 0;
					}
					before = child;
				} else {
					max_len = 0;
					break;
				}
			}
			if (max_len < cnt) {
				max_len = cnt;
			}
			if (max_len + 3 >= children.size() && max_len >= 4) {
				e.is_list = true;
				return;
			}
		}
		
		if (children.size() > 0) {
			int max_len = 0;
			int cnt = 0;
			Element before = children.get(0);

			for (int i = 1; i < children.size(); ++i) {
				Element child = children.get(i);

				if (before.height < 300 && child.height < 300) {
					if (before.tag.equals(child.tag) && before.struct_hash == child.struct_hash
							&& child.struct_hash != 0
							&& before.left == child.left && before.width == child.width
							&& before.anchor_len > 0 && child.anchor_len > 0) {
						if (Math.abs(before.height - child.height) < 60) {
							cnt ++;
						} else {
							if (max_len < cnt) {
								max_len = cnt;
							}
							cnt = 0;
						}
					} else {
						if (max_len < cnt) {
							max_len = cnt;
						}
						cnt = 0;
					}
					before = child;
				} else {
					max_len = 0;
					break;
				}
			}
			if (max_len < cnt) {
				max_len = cnt;
			}
			if (max_len + 3 >= children.size() && max_len >= 4) {
				e.is_list = true;
				return;
			}
		}
		
		if (children.size() > 0) {
			int max_len = 0;
			int cnt = 0;
			Element before = children.get(0);

			for (int i = 1; i < children.size(); ++i) {
				Element child = children.get(i);
				if (child.height < 600 && child.width < 350) {
					if (
				before.tag.equals(child.tag) && before.struct_hash == child.struct_hash
							&& child.struct_hash != 0
							&& child.width == before.width
							&& before.class_attr.equals(child.class_attr)
							&& child.img_area*3>child.width*child.height
							&& before.anchor_len > 0 && child.anchor_len > 0
							) {
							cnt++;
					} else {
						if (max_len < cnt) {
							max_len = cnt;
						}
						cnt = 0;
					}
					before = child;
				} else {
					max_len = 0;
					break;
				}
			}
			if (max_len < cnt) {
				max_len = cnt;
			}
			if (max_len + 2 >= children.size() && max_len >= 9) {
				e.is_list = true;
				e.contain_big_pic = false;
				return;
			}
		}
		
		if (children.size() > 0) {
			int max_len = 0;
			int cnt = 0;
			Element before = children.get(0);

			for (int i = 1; i < children.size(); ++i) {
				Element child = children.get(i);
				if (before.height < 120 && child.height < 120) {
					if (before.tag.equals(child.tag)
							&& before.left == child.left && before.width == child.width && before.width > 550
							&& before.anchor_len > 0 && child.anchor_len > 0) {
						if (before.height == child.height) {
							cnt ++;
						} else {
							if (max_len < cnt) {
								max_len = cnt;
							}
							cnt = 0;
						}
					} else {
						if (max_len < cnt) {
							max_len = cnt;
						}
						cnt = 0;
					}
					before = child;
				} else {
					max_len = 0;
					break;
				}
			}
			if (max_len < cnt) {
				max_len = cnt;
			}
			if (max_len >= children.size()-2 && max_len >= 5) {
				e.is_list = true;
				return;
			}
		}
		
		if (children.size() > 20) {
			int cnt = 0;
			Element before = children.get(0);

			for (int i = 1; i < children.size(); ++i) {
				Element child = children.get(i);
				if (before.tag.equals(child.tag)
						&& (( before.struct_hash == child.struct_hash && child.struct_hash != 0)
								|| (before.width == child.width && before.height == child.height))
						&& before.anchor_num > 0 
						&& child.anchor_num > 0) {
					cnt ++;
				}
				before = child;

			}

			
			if (cnt >= children.size()-2) {
				e.is_list = true;
				return;
			}
		}
	}
	
	private void containBigPic(Element e, List<Element> children) {
		if (e.tag.toLowerCase().equals("img") && e.width*e.height>100000 && e.height*3 > e.width) {
			e.contain_big_pic = true;
		} else {
			for (Element child: children) {
				if (child.contain_big_pic) {
					e.contain_big_pic = true;
					return;
				}
			}
		}
		
		if (e.tag.toLowerCase().equals("iframe") && e.width >= 500 && e.height>=500) {
			e.contain_big_iframe = true;
		} else {
			for (Element child: children) {
				if (child.contain_big_iframe) {
					e.contain_big_iframe = true;
					return;
				}
			}
		}
		
	}
	
	private int getDomType(Element e, List<Element> children) {
		int type = 2;
	//	System.out.println(e.text_len + "\t" + e.anchor_len + "\t" + e.visible_dom_num + "\t" + e.height + "\t" + e.text);
		if (e.text_len > 20 && e.text_len > (e.visible_dom_num-e.anchor_num) * 15 && e.text_len > e.anchor_len * 4) {
			return 0;
		}
		if (e.tag.equalsIgnoreCase("p")) {
			return 0;
		}
		/*
		if (e.text_len > e.anchor_len*3 && e.text_len < e.anchor_len*4 && e.text_len*2 > e.height && e.text_len > (e.visible_dom_num-e.anchor_num) * 15) {
			return 0;
		}
		*/

		if (e.text_len < e.anchor_len * 2 && e.anchor_len > 20 && !e.contain_big_pic && !e.is_video && !e.contain_big_iframe) {
			if (e.height > e.width) {
				return 1;
			}
			/*
			if (e.text.indexOf("欧洲中东部地区遭遇严")>= 0 ) {
				System.out.println(e.text_len + "\t" + e.anchor_len + "\t" + e.text);
			}
			*/
	//		System.out.println("here:  " + e.text);
			if ((e.text_len - e.anchor_len)*3 < e.anchor_len) {
			//	System.out.println(e.full_xpath + "\t" +e.is_video + "\t" +  e.text);

				return 1;
			}

			/*
			if ((e.text_len - e.anchor_len)*2 < e.anchor_len && e.anchor_num >= 2) {
				return 1;
			}
			
			if (((double)(e.text_len - e.anchor_len))*1.5 < e.anchor_len && e.anchor_num >= 5 && e.visible_dom_num > e.anchor_num*2) {
				return 1;
			}
			*/
			
		}

		int area1 = 0, area2 = 0;
		int text1 = 0, text2 = 0;
		for (int i = 0; i < children.size(); ++i) {
			
			Element cur_e = children.get(i);
			if (cur_e.type == 1) {
			//	System.out.println("1: " + cur_e.width + "\t" + cur_e.height);
			//	area1 += cur_e.width * cur_e.height - cur_e.img_iframe_area;
				area1 += cur_e.width * cur_e.height;
				text1 += cur_e.text_len;
			} else {
			//	System.out.println("2: " + cur_e.width + "\t" + cur_e.height);
				area2 += cur_e.width * cur_e.height - (cur_e.img_iframe_area-cur_e.img_area);
				text2 += cur_e.text_len;
			}
		}
	//	if (e.full_xpath.equals("/html/body/div[@class='wrapper clearfix']/div[@class='content']/div[@id='J_ProduceNew' and @class='section']"))
		//	System.out.println(text2 + "\t" + text1 + "\t" + area2 +"\t" + area1);
		

		
		if (area1 > area2 * 4 && text2*2 < text1 && area1*3 > e.width*e.height*2  && !e.contain_big_pic && !e.is_video) {
			return 1;
		}
		if ((double)area1 > (double)area2 * 2.8 && text2 < 30 && text2 * 2 < text1 && area1*3 > e.width*e.height*2  && !e.contain_big_pic && !e.is_video) {
			return 1;
		}

		if (children.size() >= 3) {

			ArrayList<Integer> similarity_list = new ArrayList<Integer>();
			Element last_e = children.get(0);

			boolean last_is_valid = true;
			int link_sub_block_num = 0;

			//if (last_e.height * 2 < last_e.width && last_e.anchor_len * 4 > last_e.text_len) {
			if (last_e.anchor_len * 8 > last_e.text_len) {
				last_is_valid = true;
				if (last_e.anchor_len * 4 > last_e.text_len) {
					++link_sub_block_num;

				}
		//		System.out.println("4  " + last_e.text);
			} else {
				last_is_valid = false;
		//		System.out.println("5  " + last_e.text);
			}
			
			
			for (int i = 1; i < children.size(); ++i) {
				Element cur_e = children.get(i);
				boolean cur_is_valid = false;
			//	System.out.println(cur_e.anchor_len + "\t" + cur_e.text_len + "\t" + cur_e.text);
				//if ((cur_e.height * 2 < cur_e.width) && cur_e.anchor_len * 4 > cur_e.text_len) {
				if (cur_e.anchor_len * 8 > cur_e.text_len) {
					if (cur_e.anchor_len * 4 > cur_e.text_len) {
						++link_sub_block_num;

					}
					cur_is_valid = true;
				}
				if (cur_is_valid && last_is_valid) {
					int max_len = last_e.text_len;
					int min_len = cur_e.text_len;
					if (max_len < min_len) {
						max_len = min_len;
						min_len = last_e.text_len;
					}
					if (min_len*3 > max_len && cur_e.tag == last_e.tag 
							&& ((Math.abs(cur_e.left-last_e.left)< 15 && Math.abs(cur_e.width - last_e.width)<50)
							|| (cur_e.height == last_e.height))) {
						similarity_list.add(1);
				//		System.out.println("1  " + cur_e.text);
					} else {
						similarity_list.add(0);
				//		System.out.println("0  " + cur_e.text);
					}
				} else {
					similarity_list.add(0);
				//	System.out.println("2  " + cur_e.text_len + "\t" + cur_e.anchor_len + "\t" + cur_e.text);
				}
				last_e = cur_e;
				last_is_valid = cur_is_valid;
			}
		//	System.out.println(children.size() + "\t" + similarity_list.size() + "\t" + e.text);
			if ((children.size()-link_sub_block_num)*4 > link_sub_block_num ) {
				return type;
			}
			
			int longest_length = 0;
			int longest_start = -1;
			int cur_length = 0;
			int cur_start = 0;
			for (int i = 0; i < similarity_list.size(); ++i) {
				int t = similarity_list.get(i);
				if (t == 1) {
					if (cur_length == 0) {
						cur_start = i;
					}
					++cur_length;
				} else {
					if (cur_length > longest_length) {
						longest_length = cur_length;
						longest_start = cur_start;
					}
					cur_length = 0;
				}
			}
			if (cur_length > longest_length) {
				longest_length = cur_length;
				longest_start = cur_start;
			}
		//	System.out.println(e.anchor_len + "\t" + e.text_len + "\t" + e.text);
		//	System.out.println(children.size() + "\t" + longest_length + "\t" + longest_start + "\t" + e.text);
			if (longest_length > 0) {
				int start = longest_start;
				int end = longest_start + longest_length;
				area1 = 0; area2 = 0;
				for (int i = 0; i < children.size(); ++i) {
					Element cur_e = children.get(i);
					if (i >= start && i <= end) {
						area1 += cur_e.width * cur_e.height - cur_e.img_iframe_area;
					} else {
						area2 += cur_e.width * cur_e.height - cur_e.img_iframe_area;
					}
				}
				if (area1 > area2 * 4 && !e.contain_big_pic && !e.is_video) {
					return 1;
				}
			}
			

		}

	//	System.out.println(e.text_len + "\t" + e.anchor_len + "\t" + e.visible_dom_num + "\t" + e.text);
		return type;
	}

	private void isVideoBlock(Element e, List<Element> children) {
		if (e.tag.equalsIgnoreCase("object") || e.tag.equalsIgnoreCase("video") || e.tag.equalsIgnoreCase("embed")) {
			Element parent = path.get(path.size() - 1);
			parent.has_media_child = true;
			if (e.left >= 10 && e.left <= 400 && e.top >= 50 && e.top <= 500
					&& e.width >= 300) {
				if (e.height > 400) {
					e.is_video = true;
					has_video_block = true;
				}

				if (e.height == 0) {
					e.height = 1;
				}
		//	System.out.println("here:  " + e.full_xpath);
				return;
			}
		}

		if (e.has_media_child) {
			boolean flag = false;
			if (children.size() == 0) {
				flag = true;
			}
			if (children.size() == 1) {

				Element tmp = children.get(0);

				if (tmp.tag.equals("object") || tmp.tag.equals("video") || e.tag.equals("embed")) {
					flag = true;
				//	System.out.println("asdf  " + e.left + "\t" + e.width + "\t" + e.height);
				}
			}
			if (flag && e.left >= 10 && e.left <= 400 && e.top >= 50 && e.top <= 500
					&& e.width >= 500 && e.height > 400) {
				e.is_video = true;
				has_video_block = true;
			//	System.out.println("asdf");
				//System.out.println("here:  " + e.full_xpath);
				return;
			}
		}
		String attr = e.class_attr + e.id;
		if (attr.indexOf("player")>=0 && (e.text.toString().toLowerCase().indexOf("flash播放器")>=0|| e.text.toString().toLowerCase().indexOf("flashplayer")>=0)) {
			if (e.left >= 10 && e.left <= 400 && e.top >= 50 && e.top <= 500
					&& e.width >= 300) {
				
				e.is_video = true;
				has_video_block = true;
				if (e.height == 0) {
					e.height = 1;
				}
			//	System.out.println("here:  " + e.full_xpath);
				return;
			}
		}
		if (e.text.toString().toLowerCase().indexOf("您还没有安装flash播放器")>=0
				|| e.text.toString().toLowerCase().indexOf("您没有安装flash播放器")>=0) {
		//	System.out.println("hh " + e.full_xpath);
			if (e.left >= 10 && e.left <= 400 && e.top >= 50 && e.top <= 500
					&& e.width >= 500 && e.height >= 300 && e.text_len < 50) {
				e.is_video = true;
				has_video_block = true;
			//	System.out.println("here:  " + e.full_xpath);
				return;
			}
		}
		for (Element child: children) {
			if (child.is_video) {
				e.is_video = true;
		//		System.out.println("here:  " + e.full_xpath);
				return;
			}
		}
	}

	private void isTag(Element e, List<Element> children) {
		if (e.height <= 50 && (e.text.toString().startsWith("标签：") || e.text.toString().startsWith("标签:") ) && e.anchor_len > 0 && e.text_len < 100) {
			e.is_tag = true;
		} else {
			if (!type_blocks.containsKey(1) && children != null && children.size() > 0) {
				for (Element child: children) {
					if (child.is_tag) {
						child.block_type = 2;
						ArrayList<Element> tmp = new ArrayList<Element>();
						tmp.add(child);
						type_blocks.put(2, tmp);
						break;
					}
				}
			}
		}
	}
	
	private void isBreadCrumb(Element e, List<Element> children) {
		if (e.breadcrumb_flag == 0) {
			for (Element child: children) {
				e.breadcrumb_flag += child.breadcrumb_flag;
			}
		}
		if (e.left < 600 && e.top < 700 && e.height <= 45 && e.text_len < 100 && !e.text.toString().startsWith("更多")) {
	//		System.out.println("here.  " + e.text + '\t' + last_anchor + "\t" + e.breadcrumb_flag);
			if (e.breadcrumb_flag > 0 && e.text.indexOf(last_anchor) >= 0 && e.anchor_num < 10) {
				
				if (!type_blocks.containsKey(3)) {
					e.block_type = 3;
					ArrayList<Element> tmp = new ArrayList<Element>();
					tmp.add(e);
					type_blocks.put(3, tmp);
				} else {
					ArrayList<Element> tmp = type_blocks.get(3);
					Element last_bc = tmp.get(0);
					if (e.breadcrumb_flag > last_bc.breadcrumb_flag) {
					//	System.out.println("hereeeeeeeeee.");
						e.block_type = 3;
						tmp.clear();
						tmp.add(e);
						last_bc.block_type = 0;
						type_blocks.put(3, tmp);
					}
				}
			}
		}
	}
	
	private void isReplyTips(Element e, List<Element> children) {
		if (e.height > 40 || e.text_len > 50) {
		//	if (!type_blocks.containsKey(5)) {
				for (Element child: children) {
					if (child.reply_tips_flag == 1) {
						ArrayList<Element> tmp = type_blocks.get(5);
						if (tmp == null) {
							tmp = new ArrayList<Element>();
							type_blocks.put(5, tmp);
						}
						child.block_type = 5;
						tmp.add(child);
					//	type_blocks.put(5, child);
					//	break;
					}
				}
		//	}
		} else {
			for (Element child: children) {
				if (child.reply_tips_flag == 1) {
					e.reply_tips_flag = 1;
				} else if (child.reply_tips_flag == 2) {
					e.reply_tips_flag = 2;
					break;
				}
			}
		}
	}
	
	private void isUserInfo(Element e, List<Element> children) {
		if (e.width >= 700 && children.size() == 2 && e.height > 120) {
			Element c1 = children.get(0);
			Element c2 = children.get(1);
			Element left = null, right = null;
			if (c1.left < c2.left) {
				left = c1;
				right = c2;
			} else {
				left = c2;
				right = c1;
			}
			if (left.left+left.width-50<right.left && left.is_userinfo) {
				ArrayList<Element> tmp = type_blocks.get(6);
				if (tmp == null) {
					tmp = new ArrayList<Element>();
					type_blocks.put(6, tmp);
				}
				left.block_type = 6;
				tmp.add(left);
			}
		} else {
			if (e.left >= 150 && e.left <= 450 && e.height > 120 && e.height < 600) {
				if (e.img_area > 0 && e.anchor_len > 0 && e.text_len > e.anchor_len && e.text_len < 70) {
					e.is_userinfo = true;
				}
			}
		}
		String attr = e.id + e.class_attr;
		if (attr.indexOf("profile")>=0 || attr.indexOf("user")>=0) {
			if (e.img_area > 0 && e.anchor_len > 0 && e.text_len > e.anchor_len && e.text.toString().indexOf("登录")<0) {
				if (e.width > 550 && e.height > 50 && e.top < 300 && e.height < 300) {
				//	System.out.println(e.text + "\t" + url + "\t" + e.full_xpath);
					e.is_userinfo2 = true;
					return;
				}
			}
		}
		
		if (attr.indexOf("profile")>=0 || attr.indexOf("user")>=0) {
			if (e.img_area > 0 && e.anchor_len > 0 && e.text_len > e.anchor_len && e.text.toString().indexOf("登录")<0) {
				if ((e.left > 900 || e.left < 400) && e.top < 1000 && e.height < 400 && e.width < 350 && e.height > 60) {
					e.is_userinfo2 = true;
			//		System.out.println(e.text + "\t" + url + "\t" + e.full_xpath);
					return;
				}
			}
		}
		if (!e.is_userinfo2) {
			for (Element child: children) {
				if (child.is_userinfo2) {
					ArrayList<Element> tmp = type_blocks.get(6);
					if (tmp == null) {
						tmp = new ArrayList<Element>();
						type_blocks.put(6, tmp);
					}
					child.block_type = 6;
					tmp.add(child);
				}
			}
		}
	}
	
	private void isBottomContact(Element e, List<Element> children) {
		
	}
	
	private void isShareBlock(Element e, List<Element> children) {
		String[] shared_keywords = {"新浪微博", "腾讯微博", "空间", "人人网", "开心网"};
		if (e.height > 70) {
			for (Element child: children) {

				if (child.height < 70 && child.tag_num >= 5 && child.text.indexOf("分享") >= 0) {
					int cnt = 0;
					for (String keyword: shared_keywords) {
						if (child.text.indexOf(keyword) >= 0) {
							++cnt;
						}
					}
					if (cnt >= 2) {
						ArrayList<Element> tmp = type_blocks.get(12);
						if (tmp == null) {
							tmp = new ArrayList<Element>();
							type_blocks.put(12, tmp);
						}
						child.block_type = 12;
						tmp.add(child);
						break;
					}
				}
			}
		}
	}
	
	private void containContentTitle(Element e, List<Element> children) {
		if (e.tag.equalsIgnoreCase("h1") || e.tag.equalsIgnoreCase("h2") || e.tag.equalsIgnoreCase("h3")) {
			if (e.left < 600 && e.top < 650 && e.height < 150 && e.text_len < 100 && e.text_len > 1) {
				if (title.length() > 0 && ((e.text.toString().length() > 10 && title.indexOf(e.text.toString())>=0)
						|| title.startsWith(e.text.toString()))) {

				//	System.out.println("hhh: " + e.text);
					if (!type_blocks.containsKey(4)) {
					//	System.out.println(e.text);
						e.block_type = 4;
						ArrayList<Element> tmp = new ArrayList<Element>();
						tmp.add(e);
						type_blocks.put(4, tmp);
					}
				}
			}
		}
		if (!type_blocks.containsKey(4)) {
			if (e.tag.equalsIgnoreCase("h1") || e.tag.equalsIgnoreCase("h2")) {
				if (e.left < 900 && e.top < 650 && e.height < 150 && e.text_len < 100 && e.text_len >= 5) {
					if (title.length() > 0 && ((e.text.toString().length() > 10 && title.indexOf(e.text.toString())>=0)
							|| title.startsWith(e.text.toString()))) {
					//	System.out.println("hhh: " + e.text);
						if (!type_blocks.containsKey(4)) {
						//	System.out.println(e.text);
							e.block_type = 4;
							ArrayList<Element> tmp = new ArrayList<Element>();
							tmp.add(e);
							type_blocks.put(4, tmp);
						}
					}
				}
			}
		}
		if (e.tag.equalsIgnoreCase("h1") || e.tag.equalsIgnoreCase("h2")) {
			if (e.left < 600 && e.top < 650 && e.height < 100 && e.height > 20 && e.text_len < 50 && e.text_len > 10) {
				if (title.length() > 0 && title.startsWith(e.text.toString())) {
				//	System.out.println("hhh: " + e.text);
					if (!type_blocks.containsKey(4)) {
					//	System.out.println(e.text);
						e.block_type = 4;
						ArrayList<Element> tmp = new ArrayList<Element>();
						tmp.add(e);
						type_blocks.put(4, tmp);
					} else {
						ArrayList<Element> tmp = type_blocks.get(4);
						tmp.get(0).block_type = 0;
						tmp.clear();
						e.block_type = 4;
						tmp.add(e);
						type_blocks.put(4, tmp);
					}
				}
			}
		}
		if (e.tag.equalsIgnoreCase("h1") && !type_blocks.containsKey(4)) {
			if (e.left < 600 && e.top < 800 && e.height < 150 && e.text_len < 50 && e.text_len > 10) {
		//		System.out.println(e.text);
			//	System.out.println("eee: " + e.text);
				if (title.length() > 0 &&title.indexOf(e.text.toString())>=0) {
					if (!type_blocks.containsKey(4)) {
						e.block_type = 4;
						ArrayList<Element> tmp = new ArrayList<Element>();
						tmp.add(e);
						type_blocks.put(4, tmp);
					}
				}
			}
		}
		if ((e.tag.equalsIgnoreCase("h1") || e.tag.equalsIgnoreCase("h2") || e.tag.equalsIgnoreCase("h3"))
				&& !type_blocks.containsKey(4)) {
			
			if (e.left < 600 && e.top < 500 && e.height < 100 && e.text_len < 50 && e.text_len > 10) {
			//	System.out.println(e.text + "\t" + title);
				String sub_t = e.text.substring(0,10);
				if (title.length() > e.text.length() && title.indexOf(sub_t)>=0) {
					e.block_type = 4;
					ArrayList<Element> tmp = new ArrayList<Element>();
					tmp.add(e);
					type_blocks.put(4, tmp);
				}
			}
		}
	//	System.out.println(e.text + "\t" + e.full_xpath);
		if ((e.tag.equalsIgnoreCase("h1") || e.tag.equalsIgnoreCase("h2")) && !type_blocks.containsKey(4)) {
			
			if (e.left < 600 && e.top < 500 && e.height < 100 && e.height > 20 && e.text_len < 50 && e.text_len > 10) {
			//	System.out.println(e.text + "\t" + title);
				if (title.length() > e.text.length()) {
					
					int distance = PageSimilarityCalculator.LCS(title, e.text.toString());
					//System.out.println("hhh " + e.text  + "\t" + title + "\t" + distance + "\t" + e.text.length());
					if (distance+2>=e.text.length() || distance*1.2>=e.text.length()) {
						
						e.block_type = 4;
						ArrayList<Element> tmp = new ArrayList<Element>();
						tmp.add(e);
						type_blocks.put(4, tmp);
					}
				}
			}
		}

		if (!type_blocks.containsKey(4)) {
			if (e.tag.equalsIgnoreCase("div") || e.tag.equalsIgnoreCase("td")) {
				if (e.left < 600 && e.top < 500 && e.height < 100 && e.height > 20 && e.text_len < 30 && e.text_len >= 9) {
			//		System.out.println("hhh:  " + e.text + "\t" + title);
					if (title.length() > 0 && (title.startsWith(e.text.toString()) || (title.indexOf(e.text.toString())>=0&&title.length()-2*e.text.length()<0))) {
			//			System.out.println("eee:  " + e.text);
						/*
						e.block_type = 4;
						ArrayList<Element> tmp = new ArrayList<Element>();
						tmp.add(e);
						type_blocks.put(4, tmp);
						*/
						content_title_candidate = e;
					}
				}
			}
		}
		if (!type_blocks.containsKey(4)) {
			if (e.tag.equalsIgnoreCase("h1") || e.tag.equalsIgnoreCase("h2")) {
				if (e.left < 600 && e.top < 500 && e.height < 100 && e.height > 15 && e.text_len < 30 && e.text_len >= 9) {
			//		System.out.println("hhh:  " + e.text + "\t" + title);
					int pos = e.text.toString().indexOf('-');
					if (title.length() > 0 && pos > 0) {
						String sub_t = e.text.toString().substring(0, pos);
						if (sub_t.length()>=4 && title.startsWith(sub_t) && content_title_candidate == null) {
							content_title_candidate = e;
					//		System.out.println("hhh " + e.text  + "\t" + title + "\t" + url);
						}
					}
				}
			}
		}
		if (!type_blocks.containsKey(4)) {
			if (e.tag.equalsIgnoreCase("h1")) {
				if (e.left < 600 && e.top < 600 && e.height < 100 && e.height > 15 && e.text_len < 30 && e.text_len >= 5) {
					String sub_t = e.text.substring(0, 2);
					int pos = title.indexOf(sub_t);
					if (pos >= 0 && pos <= 3 && content_title_candidate == null) {
							content_title_candidate = e;
					//		System.out.println("hhh " + e.text  + "\t" + title + "\t" + url);
						}
					
				}
			}
		}
		if (!type_blocks.containsKey(4)) {
			if ((e.tag.equalsIgnoreCase("p") || e.tag.equalsIgnoreCase("span")) && (e.xpath.indexOf("h1")>=0 || e.xpath.indexOf("h2")>=0)) {
				if (e.left < 600 && e.top < 500 && e.height < 100 && e.height > 15 && e.text_len < 30 && e.text_len >= 9) {
					if (title.length() > 0 && title.startsWith(e.text.toString())) {
						content_title_candidate = e;
					//	System.out.println("hhh " + e.text  + "\t" + title + "\t" + url);
					}
				}
			}
		}
		if (e.tag.equalsIgnoreCase("h1")) {
			//System.out.println("hrere."  + e.text + "\t" + title);
			if (title.length() > 0 && title.startsWith(e.text.toString())) {
			//	System.out.println("hrere."  + e.text + "\t" + title);
				e.contain_contenttitle = true;
				return;
			}
		}
		for (Element child: children) {
			if (child.contain_contenttitle) {
				e.contain_contenttitle = true;
				return;
			}
		}
	}
	
	private void getBlockTitle(Element e, List<Element> children) {
		for (Element child: children) {
			if (child.top-20<=e.top && child.height <= 50) {
				if (title_tags.contains(child.tag)) {
					e.block_title = child.text.toString();
					return;
				} else if (child.block_title.length() > 0) {
					e.block_title = child.block_title;
					return;
				}
			}
		}
	}
	
	
	private void isRecommendBlock(Element e, List<Element> children) {
		String[] recommend_keys = {"相关", "其他相关", "类似", "猜你喜欢", "热门", "最新", "附近的"};

		if (e.text_len < 30 && e.height < 65 && e.text.toString().indexOf("相关介绍")<0 && e.text.toString().indexOf("热门回复")<0 && e.tag_num <= 5) {
			String text = e.text.toString().replaceAll("[\"”“>与和:：]?(更多)?", "");
			//text = text.replaceAll("[a-zA-Z]+", "a");
			if (title != null) {
				int i = 0;
				for (; i < title.length() && i < text.length(); ++i) {
					if (title.charAt(i) != text.charAt(i)) {
						break;
					}
				}
				if (i >= 5) {
				//	System.out.println(text);
					text = text.substring(i);
				} else if (i == 0 && title.length() >= 5) {
					String sub_t = title.substring(0, 5);
					int j = text.indexOf(sub_t);
					if (j > 0 && title.startsWith(text.substring(j))) {
						text = text.substring(0, j);
					}
				}
			}
			if (text.length() < 10) {
				
				boolean flag = false;
				for (String key: recommend_keys) {
					if (text.startsWith(key)) {
					//	System.out.println(text + "\t" + e.full_xpath);
						flag = true;
						break;
					}
				}
				if (flag) {
					e.is_recommend_tips = true;
				//	return;
				}
			}
			if (text.length() < 10 && (text.endsWith("推荐")|| text.endsWith("都在看"))) {
				e.is_recommend_tips = true;
				return;
			}
			if (text.length() < 18) {
				if ((text.startsWith("看过") && text.indexOf("还看过")>0)
						|| (text.startsWith("看了") && text.indexOf("还看")>0)
						|| (text.startsWith("浏览") && text.indexOf("还浏览")>0)
						||  (text.startsWith("关注") && text.indexOf("还关注")>0)
						|| (text.startsWith("咨询") && text.indexOf("还咨询")>0)
						|| (text.length() < 12 && text.startsWith("您可能感兴趣"))
						|| (text.length() < 12 && text.startsWith("你可能感兴趣"))
						|| (text.equals("你可能还喜欢"))
						|| (text.equals("每日热榜"))
						|| (text.startsWith("大家都在"))
						|| (text.equals("延伸阅读"))) {
					//System.out.println("hehhhh");
					e.is_recommend_tips = true;
					return;
				}
			}
			if (text.length() < 20) {
				if (e.text.toString().matches("[与和].*(相关|类似).*的.*")) {
				//	System.out.println("hhh: ") ;
					e.is_recommend_tips = true;
					return;
				}
			}

			if (e.class_attr.indexOf("title")>=0 || e.tag.startsWith("h")) {
				if (e.text_len < 18 && e.height < 60) {
					boolean flag = false;
					for (String key: recommend_keys) {
						if (text.indexOf(key)>=0) {
							flag = true;
							break;
						}
					}
					if (flag) {
						e.is_recommend_tips = true;
			//			System.out.println(e.full_xpath);
				//		return;
					}
				}
			}
			
			if (e.is_recommend_tips) {
				if (text.indexOf("最新章节")>=0) {
					e.is_recommend_tips = false;
				} else {
					return;
				}
			}
		}

		if (children.size() == 1) {
			Element child = children.get(0);
			if (child.is_recommend_tips && e.text_len == child.text_len && e.height < 70) {
				e.is_recommend_tips = true;
				return;
			}
		}

		if (children.size() > 0) {

			Element child = children.get(0);
			if (child.is_recommend_tips) {

				ArrayList<Element> tmp = type_blocks.get(7);
				if (tmp == null) {
					tmp = new ArrayList<Element>();
					type_blocks.put(7, tmp);
				}
				e.block_type = 7;
				tmp.add(e);
			//	System.out.println(e.full_xpath);
				return;
			}
		}
		{
			for (int i = 0; i < children.size(); ++i) {

				Element child = children.get(i);
				if (child.is_recommend_tips && i < children.size()-1) {
					i++;
			//		System.out.println("hh: " + e.full_xpath);
					Element t = children.get(i);
					if ((t.type == 1 || t.is_list ) && (t.height > 80 || i+1 == children.size())) {
						ArrayList<Element> tmp = type_blocks.get(7);
						if (tmp == null) {
							tmp = new ArrayList<Element>();
							type_blocks.put(7, tmp);
						}
						child.block_type = 7;
						tmp.add(child);
					//	System.out.println(t.full_xpath);
						t.block_type = 7;
						tmp.add(t);
					} else if (i+2<children.size()){
						int j = i;
						for (; j < children.size();++j) {
							t = children.get(j);
							if ((t.anchor_len*2>t.text_len && t.height < 80 && t.width >= 200)||(t.anchor_len > 0 && t.anchor_len==t.text_len && t.height < 60 && t.width >= 20)) {
								continue;
							} else {
								break;
							}
						}
						if (j > i+2) {
						//	System.out.println(i + "\t" + j + "\t" + children.size());
							if (i == 1 && j >= children.size()) {
								ArrayList<Element> tmp = type_blocks.get(7);
								if (tmp == null) {
									tmp = new ArrayList<Element>();
									type_blocks.put(7, tmp);
								}
							//	System.out.println(e.full_xpath);
								e.block_type = 7;
								tmp.add(e);
							} else {
								for (int k = i-1; k < j; ++k) {
									t = children.get(k);
									ArrayList<Element> tmp = type_blocks.get(7);
									if (tmp == null) {
										tmp = new ArrayList<Element>();
										type_blocks.put(7, tmp);
									}
								//	System.out.println("hhh   " + t.text);
								//	System.out.println(e.full_xpath);
									t.block_type = 7;
									tmp.add(t);
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	private void isDisclaimer(Element e, List<Element> children) {
		for (Element child: children) {
			if (child.has_disclaimer) {
				e.has_disclaimer = true;
				return;
			}
		}
		if (children == null || children.size() <= 4) {
			if (e.width >= 500 && e.height < 200 && e.top > 600) {
				if (e.anchor_len <= 5 && e.text_len > 30) {
					int i = e.text.indexOf("免责声明");
					if (i < 0) {
						i = e.text.indexOf("免责申明");
					}
					if (i < 0 || i > 10) {
						i = e.text.indexOf("提示:");
					}
					if (i < 0 || i > 10) {
						i = e.text.indexOf("提示：");
					}
					if (i >= 0 && i <= 10) {
						ArrayList<Element> tmp = type_blocks.get(11);
						if (tmp == null) {
							tmp = new ArrayList<Element>();
							type_blocks.put(11, tmp);
						}
						e.block_type = 11;
						e.has_disclaimer = true;
					//	System.out.println(e.text);
						tmp.add(e);
					} else if (children.size() > 0) {
						Element child = children.get(0);
						i = child.text.indexOf("免责声明");
						if (i < 0) {
							i = child.text.indexOf("免责申明");
						}
						if (i < 0 || i > 10) {
							i = child.text.indexOf("提示:");
						}
						if (i < 0 || i > 10) {
							i = child.text.indexOf("提示：");
						}
						if (i >= 0 && i <= 10) {
							ArrayList<Element> tmp = type_blocks.get(11);
							if (tmp == null) {
								tmp = new ArrayList<Element>();
								type_blocks.put(11, tmp);
							}
							e.block_type = 11;
							e.has_disclaimer = true;
							tmp.add(e);
						}
					}
				}
			}
		}
	}
	
	
	
	private void isLastAndNext(Element e, List<Element> children) {
		for (Element child: children) {
			if (child.has_last_and_next) {
				e.has_last_and_next = true;
				return;
			}
		}
		if (e.height < 100 && e.text_len < 60 && e.anchor_len > 0) {
			if ((e.text.toString().indexOf("上一")>=0 && e.text.toString().indexOf("下一")>=0)
					|| (e.text.toString().indexOf("前一")>=0 && e.text.toString().indexOf("后一")>=0)) {
				ArrayList<Element> tmp = type_blocks.get(13);
				if (tmp == null) {
					tmp = new ArrayList<Element>();
					type_blocks.put(13, tmp);
				}
				e.block_type = 13;
				e.has_last_and_next = true;
				tmp.add(e);
				return;
		//	System.out.println(e.text + "\t" + url + "\t" +  e.full_xpath);
			}
		}
		
		//page list
		if (e.pagelist_flag >= 3 && e.height < 120 && e.text_len < 40 && (e.text_len-e.anchor_len) < 10) {
			ArrayList<Element> tmp = type_blocks.get(13);
			if (tmp == null) {
				tmp = new ArrayList<Element>();
				type_blocks.put(13, tmp);
			}
			e.block_type = 13;
			e.has_last_and_next = true;
			tmp.add(e);
	//		System.out.println(e.text + "\t" + url + "\t" +  e.full_xpath);
			return;
		}
		if (children.size() == 0 && e.tag.equals("a") && e.text.length() < 5 && path.size() >= 1) {
			if (e.text.toString().matches("第[0-9]{1,4}页") || e.text.toString().matches("[0-9]{1,4}")
					|| e.text.toString().matches("\\[[0-9]{1,4}\\]") || e.text.toString().equals("上一页")
					|| e.text.toString().equals("下一页")) {
				Element parent = path.get(path.size()-1);
				parent.pagelist_flag++;
			//	System.out.println(e.full_xpath);
			}
		}
	}
	
	 
	
	private void isMcLimit(Element e, List<Element> children) {
		if (e.xpath.indexOf("/h1")>=0 || e.xpath.indexOf("/h2")>=0 || e.xpath.indexOf("/h3")>=0) {
			int center = e.left + e.width/2;
		//	System.out.println(e.own_text + "\t" + url + "\t" +  e.full_xpath);
			if (e.width > 0 && e.height > 0 && e.height < 80 && center < 900 && ( (title.indexOf("评论") < 0 && title.indexOf("点评") < 0) || title.indexOf("报价")>=0 ||title.indexOf("参数")>=0) && e.text_len < 30 && e.anchor_len < 15 && (e.own_text.indexOf("评论")>=0 || e.own_text.indexOf("点评")>=0 || e.own_text.indexOf("短评")>=0 )) {
				if (e.own_text.toString().equals("网友评论：") || e.own_text.toString().equals("网友评论:")) {
			//		System.out.println(e.text + "\t" + url + "\t" +  e.full_xpath);
					mc_limit = e.top;
					return;
				}
			//	System.out.println(e.text + "\t" + url + "\t" +  e.full_xpath);
				int max_len = -1;
				int start = 0;
				while (start < title.length()) {
					int i = 0;
					for (;i < e.own_text.length() && i+start < title.length(); i++) {
						if (e.own_text.charAt(i) != title.charAt(i+start)) {
							break;
						}
					}
					if (i > max_len) {
						max_len = i;
					}
					++start;
				}
				String tail = e.own_text.substring(max_len);
				if (max_len >= 2 && tail.length() <= 5) {
					mc_limit = e.top;
			//		System.out.println(e.text + "\t" + url + "\t" +  e.full_xpath);
				}
			}
		}
	}
	
	private void isHidden(Element e, List<Element> children) {
		if (e.xpath.startsWith("/html/body")) {
			if (e.width == 0 && e.height == 0) {
				e.hidden_text_len += e.text_len;
				if (path.size()>0) {
					Element p = path.get(path.size() - 1);
					p.hidden_text_len += e.hidden_text_len;
					if (p.width > 0 && p.height > 0 && e.hidden_text_len > 20) {
						ArrayList<Element> tmp = type_blocks.get(14);
						if (tmp == null) {
							tmp = new ArrayList<Element>();
							type_blocks.put(14, tmp);
						}
						e.block_type = 14;
						tmp.add(e);
						//System.out.println(e.full_xpath + "\t" + url);
					}
				}
	
			}
		}
	}
	
	private void isComment(Element e, List<Element> children) {
		String attr = e.id + e.class_attr;
		if (attr.toLowerCase().indexOf("comm")>=0 || e.tag.equals("form")) {
		//	System.out.println("asdfasdf:   " + e.full_xpath + "\t" + children.get(0).text);
			if (e.text.indexOf("评论")>=0 && e.text.indexOf("评论")<=4 ) {
				e.block_type = 15;
				
				return;
			} else if (children.size() > 0) {
				Element child = children.get(0);
				if (child.text.indexOf("评论")>=0 && child.text.indexOf("评论")<=4) {
					e.block_type = 15;
					return;
				}
			}
		}
		if (e.block_type != 15) {
			for (Element child: children) {
				if (child.block_type == 15) {
					ArrayList<Element> tmp = type_blocks.get(15);
					if (tmp == null) {
						tmp = new ArrayList<Element>();
						type_blocks.put(15, tmp);
					}
					child.block_type = 15;
					tmp.add(child);
					return;
				}
			}
		}
	}

	
	private void isPublicTime(Element e, List<Element> children) {
		if (url.indexOf("bbs")>=0 || url.indexOf("thread-")>=0 || url.indexOf("viewthread")>=0) {
			return;
		}
		List<Element> content_title_list = type_blocks.get(4);
		Element content_title = null;
		if (content_title_list == null || content_title_list.size() == 0) {
			if (content_title_candidate == null) {
				return;
			} else {
				content_title = content_title_candidate;
			}
		} else {
			content_title = content_title_list.get(0);
		}
		int content_title_bottom = content_title.top + content_title.height;
	//	System.out.println(content_title_bottom);
		
		ArrayList<Element> tmp = type_blocks.get(16);

		if (tmp == null) {
			if (e.height < 80 && e.width >= 10 && e.left >= 0 && e.top < 500 && e.text_len < 50 && e.top < content_title_bottom+50 && e.top > content_title_bottom-10) {
			//	System.out.println("3  "+ e.text + "\t" + url + "\t" + e.full_xpath);
				if (e.width >= 150) {
					int cnt = 0;
					Element temp_child = null;
					for (Element child: children) {
						if (child.contain_publictime) {
							cnt++;
							temp_child = child;
						}
					}
					if (cnt > 1 || (children.size() == 1 && cnt == 1)) {
						//System.out.println("3  "+ e.text + "\t" + url + "\t" + e.full_xpath);
						e.contain_publictime = true;
						return;
					} else if (cnt == 1) {
						if (e.text.toString().indexOf("楼主")>=0) {
							return;
						}
						if (temp_child.width < 150) {
							e.contain_publictime = true;
							return;
						}
						tmp = new ArrayList<Element>();
						type_blocks.put(16, tmp);
						temp_child.block_type = 16;
						tmp.add(temp_child);
				//		System.out.println("1\t"+ temp_child.text + "\t" + url + "\t" + temp_child.full_xpath);
						return;
					}
				}
				
				if (e.text.toString().matches("(.*[^0-9]|^)[12][0-9]{3}年[0-9]{1,2}月[0-9]{1,2}日.*")) {
					
					e.contain_publictime = true;
					return;
				}
				if (e.text.toString().matches("(.*[^0-9]|^)[12][0-9]{3}[-/][0-9]{1,2}[-/][0-9]{1,2}.*")) {
					e.contain_publictime = true;
					return;
				}
				if (e.text.toString().indexOf("来源:")>=0 || e.text.toString().indexOf("来源：")>=0) {
					e.contain_publictime = true;
					return;
				}
				if (e.text.toString().indexOf("编辑:")>=0 || e.text.toString().indexOf("编辑：")>=0) {
					e.contain_publictime = true;
					return;
				}
				if (e.text.toString().indexOf("作者:")>=0 || e.text.toString().indexOf("作者：")>=0) {
					e.contain_publictime = true;
					return;
				}

			} else if (e.width >= 500 && e.left >= 0 && e.top < 500) {
				if (e.text.toString().indexOf("楼主")>=0) {
					return;
				}
				for (Element child: children) {
					if (child.contain_publictime) {
						tmp = new ArrayList<Element>();
						type_blocks.put(16, tmp);
						child.block_type = 16;
						tmp.add(child);
				//		System.out.println("2\t" + child.text + "\t" + url + "\t" + child.full_xpath);
						return;
					}
				}
			}
			
		}
	}
	
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		
		Element e = path.get(path.size() - 1);
		if (qName.equalsIgnoreCase(e.tag)) {

		//	System.out.println("end: " + e.xpath + "\t" + e.height);
		//	System.out.println("end: " + e.full_xpath + "\t" + e.anchor_num);
			
			xpath.delete(xpath.lastIndexOf("/"), xpath.length());
			full_xpath.delete(full_xpath.lastIndexOf("/"), full_xpath.length());
			path.remove(path.size() - 1);

			new_xpath.pollLast();
			if (qName.equalsIgnoreCase("script") || qName.equalsIgnoreCase("style")) {
				return;
			}
			if (path.size() > 0) {
				Element p = path.get(path.size() - 1);
				p.child_tags.append(qName);
			}
			ArrayList<Element> children = sub_blocks.get(e.xpath);
			e.tag_num = 1;
			if (e.tag.equals("img")) {
				e.img_num = 1;
			}
			if (children != null) {

			//	System.out.println(xpath);
				boolean all_child_is_small = true;
				if (e.text_len > 0) {
					e.text_num = 1;
				}
				if (e.anchor_len > 0) {
					e.anchor_num = 1;
				}
				if (e.own_text.length() > 0) {
					e.text_anchor_flag.append(0);
				}
				for (int i = 0; i < children.size(); ++i) {
					Element child = children.get(i);
					if (child.xy_modified) {
						for (int j = 0; j < children.size(); ++j) {
							if (j == i) {
								continue;
							}
							Element e2 = children.get(j);
							if (isOverlap(child, e2)) {
								child.left = child.child_left;
								child.width = child.child_right - child.child_left;
								child.top = child.child_top;
								child.height = child.child_bottom - child.child_top;
								child.xy_modified = false;
								break;
							}
						}
					}
				}

				for (Element child: children) {

					if(child.text_len > 0 || child.tag.equalsIgnoreCase("iframe")) {
						if (child.left < e.child_left) {
							e.child_left = child.left;
						}
						if (child.top < e.child_top) {
							e.child_top = child.top;
						}
						if (child.left + child.width > e.child_right) {
							e.child_right = child.left + child.width;
						}
						if (child.top + child.height > e.child_bottom) {
							e.child_bottom = child.top + child.height;
						}
					}
					e.tag_num += child.tag_num;
					e.ids_classes.append(child.ids_classes);
					e.visible_dom_num += child.visible_dom_num;
					e.img_iframe_area += child.img_iframe_area;
					e.img_area += child.img_area;
					e.img_num += child.img_num;
					if (child.text_area > e.text_area) {
						e.text_area = child.text_area;
					}
					e.form_area += child.form_area;
					e.text_len += child.text_len;
					e.anchor_len += child.anchor_len;
					e.anchor_num += child.anchor_num;
					e.outdomain_anchor_num += child.outdomain_anchor_num;
					e.outdomain_anchor_len += child.outdomain_anchor_len;
					e.text_num += child.text_num;
					e.hidden_text_len += child.hidden_text_len;
					e.text_anchor_flag.append(child.text_anchor_flag);
					if (child.has_download) {
						e.has_download = true;
					}
					if (child.max_text_len > e.max_text_len) {
						e.max_text_len = child.max_text_len;
					}
					e.text.append(child.text);
				//	System.out.println(child.text);
					if (!isSmall(child)) {
						all_child_is_small = false;
					}
				}
			//	System.out.println(e.full_xpath + "\t" + e.text);
			//	if (e.full_xpath.startsWith("/html/body/div[@id='content' and @class='clearfix']/div[@class='wrapper mt']/div[@class='cnt mt']/div[@class='layout grid article']/div[@class='col-main']/div[@class='main-wrap atd']/div[@class='relnews']"))
			//		System.out.println(e.full_xpath + "\t" + e.text + "\t" + children.size());
				if (children.size() == 1) {
					e.struct_hash = children.get(0).struct_hash;
				} else {
					e.struct_hash = e.child_tags.toString().hashCode();
				}
				if (e.tag.equalsIgnoreCase("a")) {
					e.anchor_len = e.text_len;
					if (e.is_out_domain) {
						e.outdomain_anchor_len = e.anchor_len;
						e.outdomain_anchor_num = e.anchor_num;
					}
					e.text_anchor_flag.setLength(0);
					e.text_anchor_flag.append(1);
				}
				if (e.tag.equalsIgnoreCase("textarea")) {
					e.text_area = e.width * e.height;
				}
				if (e.tag.equalsIgnoreCase("form")) {
					e.form_area = e.width * e.height;
				}
				if (e.tag.equalsIgnoreCase("img")) {
					e.img_area = e.width * e.height;
				}
			//	System.out.println(e.xpath + "\t"  + e.text);
			//	System.out.println(e.xpath + "\t"+(e.left+e.width) + "\t" + e.child_right + "\t" + e.own_text.length() + "\t" + e.text);
				int child_width = e.child_right - e.child_left;
				int child_height = e.child_bottom - e.child_top;
		//		System.out.println("1  " + e.xpath + "\t" + e.left + "\t" + e.top  + "\t" + e.width + "\t" + e.height + "\t" + e.text);
				if (e.child_left >= 0 && e.child_right <= 1440 && e.child_top >= 0 && child_width > 0 && child_height > 0) {
					if (e.width == 0) {
						e.left = e.child_left;
						e.width = child_width;
						if (e.height > child_height * 2) {
							e.top = e.child_top;
							e.height = child_height;
						}
					}
					if (e.height == 0) {
						e.top = e.child_top;
						e.height = child_height;
						if (e.left + e.width > 1440 || e.width > child_width * 2) {
							e.left = e.child_left;
							e.width = child_width;
						}
					}
					//if (e.width * 1.5 < child_width || (e.width > child_width * 1.5 && e.own_text.length() == 0)) {
					if ((double)e.width * 1.5 < child_width || (double)e.height * 1.5 < child_height) {
					//	System.out.println("here: " + e.xpath + "\t" + e.child_bottom + "\t" + e.child_top + "\t" + e.text);
					//	System.out.println("here.");
						int t = e.left;
						e.left = e.child_left;
						e.child_left = t;
						t = e.width;
						e.width = child_width;
						e.child_right = e.child_left + t;
						e.xy_modified = true;
				//	}
					//if (e.height * 1.5 < child_height || (e.height > child_height * 1.5 && e.own_text.length() == 0)) {
				//	if (e.height * 1.5 < child_height) {
						t = e.top;
						e.top = e.child_top;
						e.child_top = t;
						t = e.height;
						e.height = child_height;
						e.child_bottom = e.child_top + t;
						e.xy_modified = true;
					}
					
					int e_area = e.width * e.height;
					if (e_area > 800000 && (child_width * child_height + e.img_iframe_area) * 5 < e_area && e.text_len < 500 && !e.has_media_child) {
						e.left = e.child_left;
						e.width = child_width;
						e.top = e.child_top;
						e.height = child_height;
					} else if (e_area > 500000 && (child_width * child_height + e.img_iframe_area) * 4 < e_area  && e.text_len < 500) {
						e.xy_modified = true;
					} else if (e_area > 1000000 && (child_width * child_height + e.img_iframe_area) * 2 < e_area && e.text_len < 600 && children.size() == 1) {
						e.xy_modified = true;
					}
				}
		//		System.out.println("2  " + e.xpath + "\t" + e.left + "\t" + e.top  + "\t" + e.width + "\t" + e.height + "\t" + e.text);
				
				if (e.width > 10 && e.height > 10) {
					e.visible_dom_num++;
				}
				if (e.tag.equalsIgnoreCase("img") || e.tag.equalsIgnoreCase("iframe")) {
					e.img_iframe_area += e.width * e.height;
					//System.out.println("here.");
				}
				if (e.tag.equalsIgnoreCase("a")) {
					++e.anchor_num;
				}
				
				isVideoBlock(e, children);
				containBigPic(e, children);
				e.type = getDomType(e, children);

				isList(e, children);
				
				isTag(e, children);
				isBreadCrumb(e, children);
				isReplyTips(e, children);
				isUserInfo(e, children);
				containContentTitle(e, children);
				isRecommendBlock(e, children);
				isDisclaimer(e, children);
				isShareBlock(e, children);
				isLastAndNext(e, children);
				getBlockTitle(e, children);
				isMcLimit(e,children);
				isComment(e,children);
				isPublicTime(e, children);
			//	if (e.full_xpath.startsWith("/html/body/div[@class='wrapper clearfix']/div[@class='content']/div[@id='J_ProduceNew' and @class='section']"))
			//		System.out.println(e.full_xpath + "\t" + e.block_title);
			//	System.out.println("2: " + e.xpath + "\t" + e.text);

				
				for (String tag: mergeable_tag) {
					if (e.xpath.indexOf("/" + tag + "/") >= 0 || e.xpath.endsWith("/" + tag)) {
						e.is_single_block = true;
						break;
					}
				}


				for (Element child: children) {
					if ((child.text_len> 100 && mergeable_tag_set.contains(child.tag.toLowerCase()) && child.text.indexOf("免责声明")<0)) {
					//	System.out.println("hhhasdf");

						//e.is_single_block = true;
						ArrayList<Element> temp = parent2children.get(e);
						if (temp != null) {
						//	System.out.println("hereee: \t" + e.full_xpath);
							for (Element tt: temp) {
					//			System.out.println(tt.full_xpath);
								candidate_blocks.remove(tt);
							}
						}
						break;
					}
				}
				
				if (all_child_is_small) {
					e.is_single_block = true;
				}
				if (e.width * e.height < min_area) {
					e.is_single_block = true;
				}
				if (e.own_text.length() > 100) {
					ArrayList<Element> temp = parent2children.get(e);
					if (temp != null) {
					//	System.out.println("hereee: \t" + e.full_xpath);
						for (Element tt: temp) {
				//			System.out.println(tt.full_xpath);
							candidate_blocks.remove(tt);
						}
					}
					e.is_single_block = true;
				}
				/*
				if (e.own_text.length()*2>e.text.length()) {
					e.is_single_block = true;
				}
				*/

			//	System.out.println(e.is_single_block + "\t" + e.full_xpath + "\t" + e.width + "\t" + e.height);
				if (!e.is_single_block) {

					Element child_left = null;
					boolean all_child_can_merage = true;
					boolean can_merage = true;
					if (children.size() == 1) {
						if (children.get(0).is_single_block == false) {
							all_child_can_merage = false;
							can_merage = false;
						}

					} else {
						ArrayList<Integer> similarity_list = new ArrayList<Integer>();
						for (Element child: children) {
						//	System.out.println(child.width + "\t" + child.height + "\t" + isSmall(child));
							if (!child.is_single_block) {
								all_child_can_merage = false;
								can_merage = false;
								break;
							}
							if (child_left == null) {
								if (!isSmall(child))
									child_left = child;
							} else {
								//	System.out.println("1: " + child_left.xpath + "\t" + child_left.text);
								//	System.out.println("2: " + child.xpath + "\t" + child.text);
									if (isSmall(child)) {
										continue;
									}
							//		System.out.println("1: " + child_left.xpath + "\t" + child_left.text);
							//		System.out.println("2: " + child.xpath + "\t" + child.text);
							//		if (e.full_xpath.equalsIgnoreCase(test_xpath))	
							//			System.out.println("hhh");
							//		System.out.println("hhh");
									boolean similarity = getSimilarity(child_left, child);
									if (similarity) {
										similarity_list.add(1);
									} else {
										similarity_list.add(0);
									}

									
									if (!similarity) {
										
									//	System.out.println("here1 " + child_left.width + "\t" + child_left.height);
									//	System.out.println("here " + child.width + "\t" + child.height);
										can_merage = false;
									//	break;
									}
								child_left = child;
							}
						}
				//		if (e.full_xpath.equalsIgnoreCase(test_xpath))						
				//			System.out.println(e.xpath + "\t" + can_merage + "\t"+ e.text);

						if (!can_merage && all_child_can_merage) {
							int max_1_len = 0;
							int cur_1_len = 0;
							for (Integer t : similarity_list) {
								if (t == 1) {
									++cur_1_len;
								} else {
									if (cur_1_len > max_1_len) {
										max_1_len = cur_1_len;
									}
									cur_1_len = 0;
								}
							}
							if (cur_1_len > max_1_len) {
								max_1_len = cur_1_len;
							}
							if (max_1_len * 6 >= similarity_list.size() * 5) {
								can_merage = true;
							}
						}
					}

					if (!can_merage && all_child_can_merage) {
						if (canMerage(e, children)) {
							can_merage = true;
						}
					}

				//	System.out.println(e.full_xpath + "\t" + can_merage);
					if (!can_merage) {
						if (canMerage2(e, children)) {
							can_merage = true;
							ArrayList<Element> temp = parent2children.get(e);
							if (temp != null) {
							//	System.out.println("hereee: \t" + e.full_xpath);
								for (Element tt: temp) {
						//			System.out.println(tt.full_xpath);
									candidate_blocks.remove(tt);
								}
							}
						}
					}
					
				
					// for bbs. not meraged
					if (can_merage && children.size() == 2) {
						
						Element e1 = children.get(0);
						Element e2 = children.get(1);
					//	System.out.println("here.  " + e1.width + "  " + e1.height + "\t" + e2.width + "\t" + e2.height);
					//	System.out.println(e1.top + "\t" + e2.text);
					//	System.out.println(isSmall(e1));
						if ((isSmall(e1) || isSmall(e2)) && e2.left >= e1.left + e1.width && e2.top >= e1.top && e2.top <= e1.top + e1.height
								&& e2.width > 550 && e1.height > e1.width
								&& e1.width > 100 && e1.width < 250) {
							can_merage = false;
						}
					}
					
					

					if (can_merage) {
						e.is_single_block = true;
					} else {
						for (Element child: children) {
				//			System.out.println("3: " + child.full_xpath + "\t" + child.left + "\t" + child.top + "\t" + isNormalBlock(child) + "\t" + child.text);
							if (child.is_single_block && isNormalBlock(child)) {
								candidate_blocks.add(child);
								ArrayList<Element> temp = parent2children.get(e);
								if (temp == null) {
									temp = new ArrayList<Element>();
									temp.add(child);
									parent2children.put(e, temp);
								} else {
									temp.add(child);
								}
							}
						}
					}
					//System.out.println("" + e.is_single_block + " " +  e.xpath + " " + e.text);
				}
		   // 	System.out.println(e.xpath + "\t" + e.is_single_block + "\t"+ e.type + "\t" + e.text);
				ArrayList<Element> temp = sub_blocks.get(e.xpath);
				if (temp != null) {
					for (Element ee: temp) {
						ee = null;
					}
				}
				temp = null;
				sub_blocks.remove(e.xpath);
				ArrayList<Element> es = sub_blocks.get(xpath.toString());
				if (es == null) {
					es = new ArrayList<Element>();
					es.add(e);
					sub_blocks.put(xpath.toString(), es);
				} else {
					es.add(e);
				}

			} else {
			//	if (e.full_xpath.startsWith("/html/body/div[@id='content' and @class='clearfix']/div[@class='wrapper mt']/div[@class='cnt mt']/div[@class='layout grid article']/div[@class='col-main']/div[@class='main-wrap atd']/div[@class='relnews']"))
			//		System.out.println(e.full_xpath + "\t" + e.text);
			//	System.out.println("leaf:  " + e.xpath + "\t" + e.width + "\t" + e.height + "\t" + (e.child_right - e.child_left) + "\t" + (e.child_bottom - e.child_top) + "\t" + e.text);
				if (e.left >= 0 && e.top >= 0 && e.width >= 0 && e.height >= 0) {
					e.child_left = e.left;
					e.child_top = e.top;
					e.child_right = e.left + e.width;
					e.child_bottom = e.top + e.height;
				}
				if (e.width > 10 && e.height > 10) {
					e.visible_dom_num = 1;
				}
				if (e.tag.equalsIgnoreCase("a")) {
					++e.anchor_num;
					if (e.is_out_domain) {
						++e.outdomain_anchor_num;
					}
					if (e.anchor_len > 0) {
						e.text_anchor_flag.append(1);
					}
					if (e.text.indexOf("下载") >= 0 && e.text_len < 10) {
						e.has_download = true;
					}
				} else if (e.text_len > 0) {
					e.text_anchor_flag.append(0);
				}
				if (e.text_len > 0) {
					e.text_num = 1;
					
				}
				
				isVideoBlock(e, new ArrayList<Element>());
				containBigPic(e, new ArrayList<Element>());
				e.type = getDomType(e, new ArrayList<Element>());
				
				isTag(e, new ArrayList<Element>());
				isRecommendBlock(e, new ArrayList<Element>());
				isDisclaimer(e, new ArrayList<Element>());
				isLastAndNext(e, new ArrayList<Element>());
				containContentTitle(e, new ArrayList<Element>());
				isMcLimit(e, new ArrayList<Element>());
				isHidden(e,new ArrayList<Element>());
				isComment(e,new ArrayList<Element>());
				isPublicTime(e,new ArrayList<Element>());
			//	System.out.println("ss:  " + e.xpath + "\t" + e.own_text + "\t" + e.text);
				if (e.width > 0 && e.height > 0) {
					if (e.tag.equalsIgnoreCase("img") || e.tag.equalsIgnoreCase("iframe")) {
						e.img_iframe_area = e.width * e.height;
					//	System.out.println("here.");
					}
					if (e.tag.equalsIgnoreCase("textarea")) {
						e.text_area = e.width * e.height;
					}
					if (e.tag.equalsIgnoreCase("form")) {
						e.form_area = e.width * e.height;
					}
					if (e.tag.equalsIgnoreCase("img")) {
						e.img_area = e.width * e.height;
					}
					e.is_single_block = true;
					if (e.text_len == 0 && e.img_iframe_area < 50 && e.width*e.height < 10000 && !e.is_video) {
						return;
					}
					if (e.width == 0 || e.height == 0) {
						return;
					}
					ArrayList<Element> es = sub_blocks.get(xpath.toString());
					
					if (es == null) {
						es = new ArrayList<Element>();
						es.add(e);
						sub_blocks.put(xpath.toString(), es);
					} else {
						es.add(e);
					}
				}
				if (e.tag.equalsIgnoreCase("title") && title.length() == 0) {
				//	System.out.println(e.full_xpath + "\t" + e.text);
					title = e.text.toString();
				}
				if (e.tag.equalsIgnoreCase("h1") && content_title == null) {
					String t = e.text.toString();
					if (title.indexOf(t) == 0) {
						if (title.length() == t.length()) {
							content_title = e;
						} else {
							char c = title.charAt(t.length());
							if (c == ' ' || c == '-' || c == '_') {
								content_title = e;
							}
						}
					}
				}
			}
			ArrayList<Element> temp = parent2children.get(e);
			if (temp != null) {
				if (path.size() > 0) {
					Element parent = path.get(path.size() - 1);
					if (parent != null) {
						ArrayList<Element> temp2 = parent2children.get(parent);
						if (temp2 == null) {
							parent2children.put(parent, temp);
						} else {
							temp2.addAll(temp);
						}
					}
				}
			}
		}
	}
	
	private boolean canMerage(Element e, List<Element> children) {

		if (children.size() >= 4) {
			
			Element t = children.get(0);
			String tag = t.tag;
			int left = t.left;
			int i = 1;
			if (t.height > 130 || t.width < t.height * 3) {
				;
			} else {
				for (; i < children.size(); ++i) {
					Element child = children.get(i);

					if (!(child.left < left + 15 && child.left + 15 > left)) {
				//		System.out.println("here.");
						break;
					}
					if (!(child.width < t.width + 15 && child.width + 15 > t.width)) {
				//		System.out.println("here2.");
						break;
					}
					if (child.height > 130) {
				//		System.out.println("here3.  " + child.height + "\t" +child.text);
						break;
					}
					
				}
			}
		//	System.out.println(e.xpath + "\t" + i + "\t" + e.text);
			if (i >= children.size())
				return true;
		}
	//	System.out.println(e.full_xpath + "\t" + "here");
		//System.out.println(e.full_xpath);
		//shopping detail page
		if (children.size() >= 2) {
		//	System.out.println(e.text);
			Element t = children.get(0);
			int left = t.left;
			int i = 1;
			int invalid_count = 0;
			if (!(t.height < 220 && t.width > 300 && t.left > 450 && t.left < 800 && t.top < 1000&& t.width > t.height * 2)) {
				;
			} else {
				for (; i < children.size(); ++i) {
					Element child = children.get(i);

					if (!(child.left < left + 100 && child.left + 100 > left)) {
						++invalid_count;
						if (invalid_count > 1)
							break;
					}
					if (!(child.width < t.width + 100&& child.width + 100 > t.width)) {
						++invalid_count;
						if (invalid_count > 1)
							break;
					}
					if (child.text_area > 40000 || (child.type == 0 && child.text_len > 200)) {
						break;
					}
					if (!(t.height < 220 && t.width > 300 && t.left > 450 && t.left < 800 && t.top < 1000&& t.width > t.height * 2)) {
						++invalid_count;
						if (invalid_count > 1)
							break;
					}
					
				}
			}
			if (i >= children.size())
				return true;
		}
	//	System.out.println(e.full_xpath + "\t" + "here2");
		//shopping detail page for amazon
		if (children.size() >= 2) {
		//	System.out.println(e.text);
			Element t = children.get(0);
			int left = t.left;
			int i = 1;
			int invalid_count = 0;
			if (!(t.height < 220 && t.width > 600 && t.left > 300 && t.left < 600 && t.top < 1000&& t.width > t.height * 3)) {
				;
			} else {
				for (; i < children.size(); ++i) {
					Element child = children.get(i);
					if (child.width*child.height < 5000) {
						continue;
					}
					if (!(child.left < left + 10 && child.left + 10 > left)) {

							break;
					}
					if (!(child.width < t.width + 10&& child.width + 10 > t.width)) {

							break;
					}
					if (child.type == 1) {
						break;
					}
					if (!(t.height < 220 && t.width > 600 && t.left > 300 && t.left < 600 && t.top < 1000&& t.width > t.height * 3)) {

							break;
					}
					
				}
			}
			if (i >= children.size())
				return true;
		}
		
	//	System.out.println(e.full_xpath);
		if (children.size() >= 10) {
			int invalid_num = 0;
			Element left = null;
			int i = 0;
			for (; i < children.size(); ++i) {
				Element child = children.get(i);
				if (child.height > 80) {
					++invalid_num;
				}
				if (left == null) {
					left = child;
				} else {
					if (child.top <= left.top) {
						break;
					}
					left = child;
				}
			}
			if (i >= children.size() && invalid_num < 2)
				return true;
		}

	//	System.out.println(e.full_xpath);
		if (children.size() >= 2) {
		//	System.out.println(e.text);
			Element left = null;
			int i = 0;
			Element t = children.get(0);
			if (t.width > 600 && t.height <= 60 && t.width * t.height * 3 < e.width * e.height
					&& t.text_len < 20 && children.size() >= 3) {
				++i;
			}
			for (; i < children.size(); ++i) {
				
				Element child = children.get(i);
			//	System.out.println("1:  " + child.width + "\t" + child.text);
				//if (child.text_len == 0) continue;
				if (child.width > 500 || child.height > 400) {
					break;
				}
				if (left == null) {
					left = child;
				} else {
					if (child.top != left.top) {
						break;
					}
					left = child;
				}
			}
			if (i >= children.size())
				return true;
		}

	//	System.out.println(e.full_xpath);
		if (children.size() > 0) {
			boolean can_merage = true;
			int text_block_num = 0;
			int link_block_num = 0;
			int other_block_num = 0;
			int other_big_block_num = 0;
			int flag = 0;
			for (Element child: children) {
			//	System.out.println(child.full_xpath + "\t" + child.img_iframe_area + "\t" + child.type + "\t" + child.text);
				if (child.type == 0) {
					++text_block_num;
					++flag;
				} else if (child.type == 1) {
					++link_block_num;
				} else if (child.text_len > 0 || (child.img_iframe_area-child.img_area)>150000) {
					++other_block_num;
				//	System.out.println(child.full_xpath + "\t" + child.img_iframe_area);
					if (child.text_len > 20 || (child.img_iframe_area-child.img_area) > 150000) {
						++other_big_block_num;
					}
					--flag;
				}
				if (flag < -1) {
					can_merage = false;
					break;
				}
			}
			if (can_merage && text_block_num > other_big_block_num*3 && text_block_num > other_block_num && link_block_num == 0) {
				return true;
			}
		}
		
	//	System.out.println(e.full_xpath);
		// middle link
		int i = 0;
		if (children.get(i).height <= 50 && children.get(i).type != 1 && children.get(i).text_len < 20) {
			i++;
		}
		if (children.size() - i >= 2) {
			Element last_child = null;
			for (; i < children.size(); ++i) {
				Element child = children.get(i);
				if (child.height <= 15) {
					continue;
				}
				if ((child.left + child.width/2 > 500 && child.left + child.width/2 < 800
						&& child.width >= 400) || child.width > 600) {
					if (child.type == 1) {
						if (last_child != null) {
							if (last_child.left == child.left) {
								last_child = child;
								continue;
							} else {
								break;
							}
						} else {
							last_child = child;
						}
					} else {
						break;
					}
				} else {
					break;
				}
			}
			if (i >= children.size()) {
				return true;
			}
		}
		
		if (children.size() > 3 && e.width > 500) {
			Element first_child = children.get(0);
			if (first_child.width < 200 && first_child.height < 280) {
				int t = 1;
				for (; t < children.size(); ++t) {
					Element temp = children.get(t);
					if (first_child.width == temp.width && first_child.height == temp.height) {
						continue;
					} else {
						break;
					}
				}
				if (t == children.size()) {
					return true;
				}
			}
		}

		//System.out.println(e.text);
		return false;
	}
	
	private boolean canMerage2(Element e, List<Element> children) {
		//text block
		if (children.size() > 0) {
			int textnode_len = 0;
			int other_text = 0;
			int text_area = 0;
			int other_area = 0;
			int text_node_num = 0;
			for (Element child: children) {
				if (child.type == 0) {
					textnode_len += child.text_len;
					text_area += child.width*child.height;
					text_node_num++;
				} else {
					other_text += child.text_len;
					if (child.text_len > 20) {
						other_area += child.width*child.height;
						/*
						if (e.full_xpath.equalsIgnoreCase("/html/body/div[@id='page']/div[@id='main']/div[@id='content' and @class='fl']/div[@class='article']/div[@id='artibody']/div[@id='ftcg' and @class='content']")) {
							System.out.println("asdfasdf: \t" + child.height + "\t" + child.text);
						}
						*/
					}
				}
			}
			/*
			if (e.full_xpath.equalsIgnoreCase("/html/body/div[@id='page']/div[@id='main']/div[@id='content' and @class='fl']/div[@class='article']/div[@id='artibody']/div[@id='ftcg' and @class='content']")) {
				System.out.println("eeee\t" + text_node_num + "\t" + text_area + "\t" + other_area + "\t" + textnode_len + "\t" + other_text);
			}
			*/
			if (text_node_num >= 5 && textnode_len > other_text*5 && text_area > other_area*3) {
			//	System.out.println("ddddddddddd");
				return true;
			}
		}
		return false;
	}
	
	private boolean getSimilarity(Element e1, Element e2) {
		int similarity = 0;
		if (!e1.tag.equalsIgnoreCase(e2.tag)) {
			return false;
		} else {
			++similarity;
			for (String tag : mergeable_tag) {
				if (e1.tag.equalsIgnoreCase(tag)) {
					similarity += 5;
					if (e1.tag.equals("li") && e1.top == e2.top && e1.width+e2.width>800 && e1.height>400&&e2.height > 400) {
						similarity -= 5;
					} else if (e1.tag.equals("dd") && e1.height > 1000 && e1.width > 600 && e1.height>e2.height*3) {
					//	System.out.println("here.");
						similarity -= 6;
					} else {
						return true;
					}
				}
			}
		}
		if (e1.id.length() > 0 && e1.id.equalsIgnoreCase(e2.id)) {
			++similarity;
		}
		if (e1.class_attr.length() > 0 && e1.class_attr.equalsIgnoreCase(e2.class_attr)){
			++similarity;
		}
		if (e1.type == 0 && e2.type == 0) {
			++similarity;
		}
		
	//	System.out.println(similarity);
	//	System.out.println("1:  " + e1.text);
	//	System.out.println("2:  " + e2.text);
	//	System.out.println("type: " + e1.type + " " + e2.type + "\t" + e2.text_len + "\t" + e2.visible_dom_num);
		if (e1.type != e2.type && e1.type != 2 && e2.type != 2) {
			return false;
		}
		if (e1.id.length()>0 && e1.class_attr.length()>0 && e2.id.length()>0 && e2.class_attr.length()>0) {
			String attr1 = (e1.id+e1.class_attr);attr1=attr1.replaceAll("[0-9]+", "0");
			String attr2 = (e2.id+e2.class_attr);attr2=attr2.replaceAll("[0-9]+", "0");
			if (e1.type != e2.type && !attr1.equals(attr2) && (e1.type == 1 || e2.type == 1)) {
				--similarity;
			}
		}
		if ((e1.width < 500 && e2.width < 500) && e1.type == 1 && e1.text_len > 20) {
			return false;
		}
	//	System.out.println(similarity);
		if (e1.height <= 50 && e1.width  > e1.height * 5 && e2.height <= 50 && e2.width > e2.height * 5) {
		//	System.out.println("here");
			++similarity;
		}
	//	System.out.println("a: " + similarity);
		if (e1.type == e2.type && e2.type == 2 && e1.top == e2.top) {
			if (e1.width < 500 && e2.width < 500 && e1.height < 500 && e2.height < 500) {
				++similarity;
			}
		}
		
//		if (e1.type == 1 && e2.type == 1 && e1.width)
	//	System.out.println(similarity);
		if (similarity >= 2) {
			return true;
		} else {
			return false;
		}
		/*
		if (e1.left == e2.left && e1.width == e2.width) {
			++similarity;
		} else {
			if (e1.top == e2.top && e1.height == e2.height) {
				++similarity;
			} else {
				return 0;
			}
		}
		*/
		/*
		if (((double)Math.abs(e1.tag_num - e2.tag_num))/(e2.tag_num + e1.tag_num) < 0.15) {
			++similarity;
		} else {
			
			if (((double)Math.abs(e1.tag_num - e2.tag_num))/(e2.tag_num + e1.tag_num) > 0.59) {
				return 0;
			}
			
		}
		if (((double)Math.abs(e1.text_len - e2.text_len))/(e2.text_len + e1.text_len) < 0.15) {
			++similarity;
		} else {
			
			if (((double)Math.abs(e1.text_len - e2.text_len))/(e2.text_len + e1.text_len) > 0.59) {
				return 0;
			}
			
		}
		int area1 = e1.width * e1.height;
		int area2 = e2.width * e2.height;
		if (((double)Math.abs(area1 - area2))/(area2 + area1) < 0.15) {
			++similarity;
		} else {
			
			if (((double)Math.abs(area1 - area2))/(area2 + area1) > 0.59) {
				return 0;
			}
			
		}
		return similarity;
		*/
	}
	
	public static boolean containContract_backup(String s) {
		s = s.replaceAll("[ \u00a0\u3000]", "");
		if (s.length() > 500) {
			return false;
		}
		if (s.toLowerCase().matches(".*((地址)|(传真)|(电话)|(qq)|(群)|(热线))(:|：).*")) {
		//	System.out.println("here.");
			return true;
		} else if (s.indexOf("@")>=0 && (s.indexOf(".com")>=0 || s.indexOf(".cn")>=0 || s.indexOf(".org")>=0)) {
		//	System.out.println("here2.  " + s);
			return true;
		} else if (s.matches(".*(:|：)[0-9]+-[0-9]+.*")) {
		//	System.out.println("here3.");
			return true;
		} else {
			return false;
		}
	}

	public static boolean containContract(String seg) {
		seg = seg.replaceAll("[ \u00a0\u3000]", "");
		String[] keyword = {"地址:","传真:","电话:","qq:","群:","热线:","手机:","邮箱:","e-mail:","地址：","传真：","电话：","qq：","群：","热线：","手机：","邮箱：","e-mail：",};
		for (String k: keyword) {
			if (seg.toLowerCase().indexOf(k)>=0) {
				return true;
			}
		}
		if (seg.indexOf("@")>=0 && (seg.indexOf(".com")>=0 || seg.indexOf(".cn")>=0 || seg.indexOf(".org")>=0 || seg.indexOf(".net")>=0)) {
			return true;
		}
		int cnt1 = 0;
		int cnt2 = 0;
		int state = 0;
		for (int i = 0; i < seg.length() && (state > 0 || i <= seg.length() - 9); ++i) {
			char c = seg.charAt(i);
			switch (state)
			{
				case 0:
					if (c == ':' || c == '：') {
						state = 1;
						cnt1 = 0;
						cnt2 = 0;
					}
					break;
				case 1:
					if (c >='0' && c<='9') {
						++cnt1;
					} else if (c == '-' || c == '－') {
						if (cnt1 >=2) {
							state = 2;
						} else {
							state = 0;
						}
					} else {
						state = 0;
						if (c == ':' || c == '：') {
							--i;
						}
					}
					break;
				case 2:
					if (c >='0' && c<='9') {
						++cnt2;
					} else {
						if (cnt2 >= 5) {
							return true;
						} else {
							state = 0;
							if (c == ':' || c == '：') {				
								--i;
							}
						}
					}
			}

		}
		if (cnt2>=5) {
			return true;
		}
		return false;
	}

	
	
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		Element e = path.get(path.size() - 1);
		String tag = e.tag;
		if (tag.equalsIgnoreCase("script") || tag.equalsIgnoreCase("style")) {
			return;
		}
		
		String s = String.valueOf(ch, start, length);

	//	System.out.println("content: " + s);
		s = s.replaceAll("[\t\r\n]?(&nbsp;)?(&gt;)?", "");
		if (!e.contain_contract && e.top + 300 > page_height && containContract(s)) {
			e.contain_contract = true;
		//	System.out.println("asdfasdf: " + e.full_xpath + "\t" + e.top + "\t" + page_height + "\t" + s);
			candidate_contract_blocks.add(e);
		}
		String s2 = s.replaceAll("[0-9-:/()]+", "0");
		s2 = s2.replaceAll("[a-zA-Z]+", "a");
		s = s.replaceAll(" ", "");
		s = s.replaceAll("　", "");
		s = s.replaceAll(" ", "");
		s2 = s2.replaceAll(" ", "");
		s2 = s2.replaceAll("　", "");
		s2 = s2.replaceAll(" ", "");
	//	System.out.println("^"+tag + "\t" + s.length() + "\t" + s);
		if (tag.equalsIgnoreCase("a")) {
			e.anchor_len += s2.length();
			if (e.is_out_domain) {
				e.outdomain_anchor_len += s2.length();
			//	System.out.println(s);
			}
		}
		e.text_len += s2.length();
		e.text.append(s);
		e.own_text += s;
		if (s2.length() > e.max_text_len) {
			e.max_text_len = s2.length();
		}
		if ((s.startsWith(">") || s.startsWith("»") || s.startsWith("→")) && s2.length() < 20) {
		//	System.out.println(s + "\t" + e.text + "\t" + e.full_xpath);
			if (!last_text_is_anchor || e.tag.equalsIgnoreCase("a")) {
				e.breadcrumb_flag = -10000;
			//	System.out.println("sdfa  " + e.text + "\t" + e.tag);
			} else {
		//		System.out.println("hhh  " + e.full_xpath);
				e.breadcrumb_flag++;
				
			}
			
		}
		if (e.tag.equalsIgnoreCase("a")) {
			if (s.equals("回复") && e.height <= 30) {
				e.reply_tips_flag = 1;
			}
		}
		
		if (s.length() > 0) {
			if (e.tag.equalsIgnoreCase("a")) {
				if (e.width>0 && e.height>0) {
					last_anchor = s;
					last_text_is_anchor = true;
				}
			//	System.out.println("a " + e.tag + "\t" + s);
			} else if (!s.equals("-") && !s.equals(">")){
				
				last_text_is_anchor = false;
			//	System.out.println("b " + e.tag + "\t" + s);
			}
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
	
	
	public static void main(String[] args) throws IOException {
		FileInputStream f_stream = new FileInputStream(new File("t.html"));
		byte[] bs = new byte[1024*1024*4];
		int len = f_stream.read(bs);
		String s = new String(bs, 0, len, "UTF-8");

	//	String s2="<html><body><div>test<div>0</div></div><a>test<div>asdf</div></body></html>";
	//	String s2 = "<div>test<div>  0 </div></div>";
		HtmlContentHandler		htmlContentHandler		= new HtmlContentHandler();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		
		String out = "";
		try {
			htmlContentHandler.setUrl("http://www.iqiyi.com/yinyue/20121214/10453bddc18b141b.html");
			parser.parse(new InputSource(new StringReader(s)));
			//List list = htmlContentHandler.getInvalidTexts();
			ArrayList<Element> strs = htmlContentHandler.blocks;
			Tool.quickSortY(strs, 0, strs.size()-1);
			//Tool.quickSort(strs, 0, strs.size()-1);
			int area = 0;
			for (int i = 0; i < strs.size(); ++i) {
				Element f = strs.get(i);
				//System.out.println(f.full_xpath + "\t" + f.is_video);
		        out += String.format("<div style=\"position: absolute; left: %dpx; top: %dpx; width: %dpx; height: %dpx;  border:3px solid red;\">%s</div>\n",f.left, f.top, f.width, f.height, f.full_xpath + "\t" + f.text.toString());
//		        out += String.format("<div style=\"position: absolute; left: %dpx; top: %dpx; width: %dpx; height: %dpx;  border:3px solid red;\">%s</div>\n",f.left, f.top, f.width, f.height, "");

		//		System.out.println(f.text_len + "\t" + f.text);
			}
			
			for (Integer block_type: htmlContentHandler.type_blocks.keySet()) {
				ArrayList<Element> fs = htmlContentHandler.type_blocks.get(block_type);
				for (Element f: fs)
					out += String.format("<div style=\"position: absolute; left: %dpx; top: %dpx; width: %dpx; height: %dpx;  border:3px solid red;\">%s</div>\n",f.left, f.top, f.width, f.height, block_type + "\t" + f.full_xpath + "\t" + f.text.toString());
			}
			System.out.println(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
} 
