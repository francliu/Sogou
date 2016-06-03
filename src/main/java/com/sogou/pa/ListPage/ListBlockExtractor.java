package com.sogou.pa.ListPage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import com.sogou.cm.pa.maincontent.HtmlContentHandler;

public class ListBlockExtractor implements ContentHandler {
	

	LinkedList<Element> path;
	StringBuffer xpath;
	StringBuffer full_xpath;

	HashSet<String> xpathes;
	boolean is_abnormal_page;
	
	LinkedList<PrintElement> print_path;
	PrintElement root;
	Element dom_root;
	String[] list_tag = {"table", "ul", "div", "tr", "td", "a", "dl", "i", "u", "p", "b", "figure", "span", "article"};
	HashSet<String> list_tag_set;
	HashSet<Element> list_blocks;
	HashSet<Element> text_blocks;
	HtmlPage page;
	Element red;
	Element green;
	Instances data_set;
	Classifier cls;
	boolean isListPage;
	public ListBlockExtractor() {
		isListPage = false;
		path = new LinkedList<Element>();
		print_path = new LinkedList<PrintElement>();
		xpath = new StringBuffer();
		full_xpath = new StringBuffer();

		is_abnormal_page = false;
		xpathes = new HashSet<String>();
		list_tag_set = new HashSet<String>();
		for (String tag: list_tag) {
			list_tag_set.add(tag);
		}
		list_blocks = new HashSet<Element>();
		text_blocks = new HashSet<Element>();
		page = new HtmlPage();
		FastVector temp = new FastVector();
		temp.addElement("true");
		temp.addElement("false");
		FastVector temp2 = new FastVector();
		temp2.addElement("0");
		temp2.addElement("1");
		FastVector attributes = new FastVector();
	//	attributes.addElement(new Attribute("index"));
	//	attributes.addElement(new Attribute("index2"));
	//	attributes.addElement(new Attribute("index3", temp2));
		attributes.addElement(new Attribute("is_displaynone", temp));

		for (int i = 0; i < 39;i++) {
			attributes.addElement(new Attribute(new Integer(i).toString()));
		}
		for (int i = 39; i < 51;i++) {
			attributes.addElement(new Attribute(new Integer(i).toString(), temp));
		}
		attributes.addElement(new Attribute("is_list", temp2));
		
		data_set = new Instances("dataset", attributes, 0);
		data_set.setClassIndex(data_set.numAttributes() - 1);
		String model_path = "list_block_cv.model";
		try {
			cls =  (Classifier) SerializationHelper.read(model_path);
		}  catch (Exception e) {
			// TODO Auto-generated catch block
			cls = null;
			e.printStackTrace();
		}
	}
	
	public void clear() {
		isListPage = false;
		path.clear();
		print_path.clear();
		xpath.setLength(0);
		xpath.trimToSize();
		full_xpath.setLength(0);
		full_xpath.trimToSize();
		root = null;
		dom_root = null;

		is_abnormal_page = false;
		list_blocks.clear();
		text_blocks.clear();
		page = new HtmlPage();
		red = null;
		green = null;
	}
	
	public void setXpathes(HashSet<String> input) {
		xpathes.clear();
		for (String s: input) {
			xpathes.add(s);
		}
	}

	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
	}

	public void startDocument() throws SAXException {
		clear();
	}

	private int boolean2Int(boolean b) {
		if (b) {
			return 0;
		} else {
			return 1;
		}
	}
	
	private void traverse_domtree(Element e) {
		if (cls == null || e == null) {
			return;
		}
		if (e.is_list) {
			Element t = e;
			PrintElement roots = root;
			t.tobottom = page.height-(t.top+t.height);
			double[] att_vals = {boolean2Int(t.is_displaynone),Math.atan(t.top) , Math.atan(t.left), Math.atan(t.height), Math.atan(t.width), Math.atan(t.area), Math.atan(t.text_len), Math.atan(t.own_text_len), Math.atan(t.anchor_len), Math.atan(t.anchor_num), Math.atan((double)t.anchor_len/(t.text_len-t.anchor_len)), Math.atan(t.img_num), Math.atan(t.child_num), Math.atan(t.fake_child_num), Math.atan(t.input_area), Math.atan(t.area_list), Math.atan(t.area_other), Math.atan(t.list_num1), Math.atan(t.strict_list_num1), Math.atan(t.indiffent_child_num1), Math.atan(t.list_num2), Math.atan(t.strict_list_num2), Math.atan(t.indiffent_child_num2), Math.atan(t.list_num3), Math.atan(t.indiffent_child_num3), Math.atan(t.big_list_num), Math.atan(t.biggest_child_area), Math.atan(t.biggest_img_area), Math.atan(t.biggest_img_width), Math.atan(t.most_img_num), Math.atan(t.child_arrange_style), Math.atan(t.first_second_child_ratio), Math.atan(t.tobottom), Math.atan(t.longest_own_text_len), Math.atan(t.child_height2width), Math.atan(t.tall_child_num), Math.atan((double)t.friend_link_area/(t.area+1)), Math.atan((double)t.full_list_area/(t.area+1)), Math.atan(t.text_len-t.full_list_len), Math.atan(t.text_len-t.anchor_len), boolean2Int(t.has_intro), boolean2Int(t.has_sub_title), boolean2Int(t.is_sub_title), boolean2Int(t.has_merged), boolean2Int(t.is_bold), boolean2Int(t.is_time), boolean2Int(t.list_of_subtitle), boolean2Int(t.list_of_keyword), boolean2Int(t.has_repeat_sb_structure), boolean2Int(t.has_other_block), boolean2Int(t.child_is_small), boolean2Int(t.has_one_child), 1};
		/*
		if (t.name.equals("div") && t.class_attr.equals("goodsList_index clearfix") && t.top == 188) {
			for (double d: att_vals) {
				System.out.print(d + "\t");
			}
			System.out.println();
		}
		*/
			//	System.out.println();
			//	System.out.println(att_vals.length);
			Instance inst = new Instance(1.0, att_vals);
			data_set.delete();
			data_set.add(inst);
		//	System.out.println(data_set.numInstances());
			try {
				double result = cls.classifyInstance(data_set.instance(0));
				/*
				if (t.name.equals("ul") && t.top == 1816 && t.width == 240 && t.left == 972) {
					System.out.println("asdf: " + result);
				}
				*/
				if (Math.abs(result-1.0)<0.01&&t.name.toLowerCase().compareTo("html")!=0) {
					t.is_list = true;
					t.pe.is_list = true;
					roots.is_list = true;
					
				} else {
					t.is_list = false;
					t.pe.is_list = false;
					roots.is_list = false;
				}
//				if(
//						||t.name.toLowerCase().compareTo("body")==0
//						||t.name.toLowerCase().compareTo("head")==0)
//				{
//					t.is_list = false;
//					t.pe.is_list = false;
//					roots.is_list = false;
//				}
				if(t.is_list)list_blocks.add(t);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (!e.is_list) {
			for (Element child: e.children) {
				traverse_domtree(child);
			}
		}
	}
	
	public void endDocument() throws SAXException {
		if (red != null) {
			red.tobottom = page.height-(red.top+red.height);
		}
		if (green != null) {
			green.tobottom = page.height - (green.top+green.height);
		}
		traverse_domtree(dom_root); 
		isListPage();
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
				e.id_attr = id;
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
		String href_attr = atts.getValue("href");
		if (href_attr != null) {
			//style_attr = style_attr.replaceAll("[ \t\r\n]?(&nbsp;)?(&gt;)?", "");
			if (href_attr.length() > 0)
				e.src = href_attr;
		}
		else
		{
			e.src = atts.getValue("url");
		}
		String style = atts.getValue("webkit_style");
		//String style = atts.getValue("style");
		if (style != null && style.length() != 0) {
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
				} else if (sub_segs[0].equalsIgnoreCase("display")) {
					if (sub_segs[1].equals("none")) {
						e.is_displaynone = true;
					}
				} else if (sub_segs[0].equalsIgnoreCase("visibility")) {
					if (sub_segs[1].equals("hidden")) {
						e.is_displaynone = true;
					}
				} else if (sub_segs[0].equalsIgnoreCase("font-weight")) {
					if (sub_segs[1].equals("bold")) {
						e.is_bold = true;
					}
				} 
				else {
					e.atts.put(sub_segs[0], sub_segs[1]);
				}
			}
		}
		
		if (e.name.equals("a")) {
			style = atts.getValue("style");
			if (style != null && style.indexOf("background-image:")>=0) {
				e.img_num = 1;
			}
		}
		//System.out.println("set feaure internal: " +  feature.width + " " + feature.height);
	}
	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		if (xpath.length() > 2000) {
			is_abnormal_page = true;
		}
		if (full_xpath.length() > 4000) {
			is_abnormal_page = true;
			return;
		}
		Element e = path.peekLast();
		
		Element new_e = new Element();
		new_e.name = qName;
		
		setFeatureFromAtts(atts, new_e);
		if (new_e.name.equals("strong") || new_e.name.equals("h1")|| new_e.name.equals("h2")|| new_e.name.equals("h3")) {
			new_e.is_bold = true;
		}
		if (new_e.name.equals("html")) {
			page.width = new_e.width;
			page.height = new_e.height;
		}
		if (page.height < new_e.top + new_e.height) {
			page.height = new_e.top + new_e.height;
		}

        xpath.append("/" + qName);
		String full_name = HtmlContentHandler.getFulltag(qName, new_e.id_attr, new_e.class_attr);
		full_xpath.append("/" + full_name);
		new_e.full_name = full_name;
		new_e.styles = atts;
		
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
		
		path.add(new_e);
		
		String original_style = atts.getValue("style");
		if (original_style != null && original_style.indexOf("border:8px solid green;")>=0) {
			green = new_e;
		} else if (original_style != null && original_style.indexOf("border:8px solid red;")>=0) {
			red = new_e;
		}
		if (dom_root == null) {
			dom_root = new_e;
		}
		
		
		PrintElement pe = new PrintElement();
		pe.name = qName;
		pe.styles = atts;
		if (root == null) {
			root = pe;
			//System.out.println("here.");
		}
		new_e.pe = pe;
		PrintElement parent_pe = print_path.peekLast();
		if (parent_pe != null) {
			parent_pe.children.add(pe);
		}
		print_path.add(pe);

	}

	//文字节点或者不可见节点或者很小的节点
	private boolean isIndiffentBlock(Element e) {
		if (e.name.length() == 0) {
			return true;
		}
		if (e.is_displaynone) {
			return true;
		}
		if (e.child_num <= 2 && (e.area<5000 || e.height <= 20) && e.img_num == 0) {
			if (e.text_len == 0) {
				return true;
			} else if (e.text_len == 1 && e.text.toString().equals("|")) {
				return true;
			} else {
				return false;
			}
		} else if (e.child_num <= 2 && (e.area<5000 || e.height <= 20) && e.img_num == 1 && e.text_len == 0 && e.anchor_num == 0) { 
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isTimeString(String s) {
		s = s.replaceAll("[\\(\\)\\[\\]]", "");
		if (s.matches("[12][0-9]{3}年[0-9]{1,2}月[0-9]{1,2}日")) {
			return true;
		}
		if (s.matches("([01][0-9]年)?[0-9]{1,2}月[0-9]{1,2}日")) {
			return true;
		}
		if (s.matches("[12][0-9]{3}[-/][0-9]{1,2}[-/][0-9]{1,2}")) {
			return true;
		}
		 return false;
	}
	
	
	private void isList(Element e) {
		int real_child_num = 0;
		Element first_child = null;
		for (Element child: e.children) {
			if (child.area > 0) {
				if (real_child_num == 0) {
					first_child = child;
				}
				real_child_num++;
			}
		}
		if (first_child != null) {
			if (first_child.height <= 60 && e.height > 200 && first_child.width*2 > e.width && first_child.text_len < 10 && first_child.text_len > 0) {
				e.has_sub_title = true;
			} else if (e.top > 50 && first_child.height < 40 && first_child.text_len < 20 && e.height > 200 && first_child.width*2 > e.width && first_child.text_len > 0) {
				e.has_sub_title = true;
			}
		}
		if (real_child_num == 1 && first_child.has_sub_title) {
			e.has_sub_title = true;
		}
		
		if (real_child_num > 1) {
			e.fake_child_num = real_child_num;
		} else if (real_child_num == 1) {
			e.fake_child_num = first_child.fake_child_num;
		} else {
			e.fake_child_num = 0;
		}
		
		if (real_child_num > 1) {
			int area1 = 0, area2 = 0;
			for (Element child: e.children) {
				if (child.area > area1) {
					area2 = area1;area1 = child.area;
				} else if (child.area > area2) {
					area2 = child.area;
				}
			}
			if (area2 > 0) {
				e.first_second_child_ratio = ((double)area1)/area2;
			}
		} else if (real_child_num == 1) {
			e.first_second_child_ratio = first_child.first_second_child_ratio;
		}
		/*
		if (e.name.equals("tr") && e.top == 1178 && e.left == 218 && e.width == 987 && e.height == 612) {
			System.out.println("ddd " + real_child_num);
		}
		*/
		
		if (real_child_num > 1) {
			for (Element child: e.children) {
				if (e.biggest_child_area < child.area) {
					e.biggest_child_area = child.area;
				}
				if (e.most_img_num < child.img_num) {
					e.most_img_num = child.img_num;
				}
			}
		} else if (real_child_num == 1) {
			e.biggest_child_area = first_child.biggest_child_area;
			e.most_img_num = first_child.most_img_num;
		} else {
			e.biggest_child_area = e.area;
		}
		
		if (e.name.equals("img")) {
			e.biggest_img_area = e.area;
			e.biggest_img_width = e.width;
		} else {
			if (e.child_num >= 1) {
				for (Element child: e.children) {
					if (e.biggest_img_area < child.biggest_img_area) {
						e.biggest_img_area = child.biggest_img_area;
					}
					if (e.biggest_img_width < child.biggest_img_width) {
						e.biggest_img_width = child.biggest_img_width;
					}
				}
			}
		}
		
		if (real_child_num > 1) {
			int left = -10;
			int top = -10;
			boolean left2right = true;
			boolean top2bottom = true;
			for (Element child: e.children) {
				if (child.left >= left) {
					left = child.left+child.width;
				} else {
					left2right = false;
				}
				if (child.top >= top) {
					top = child.top+child.height;
				} else {
					top2bottom = false;
				}
			}
			if (left2right) {
				e.child_arrange_style = 1;
			} else if (top2bottom) {
				e.child_arrange_style = 2;
			} else {
				e.child_arrange_style = 3;
			}
		} else if (real_child_num == 1) {
			e.child_arrange_style = first_child.child_arrange_style;
		}
		
		if (real_child_num == 1) {
			e.longest_own_text_len = first_child.longest_own_text_len;
		}
		
		if (real_child_num == 1) {
			e.child_height2width = first_child.child_height2width;
			e.tall_child_num = first_child.tall_child_num;
		} else if (real_child_num > 1) {
			int cnt = 0;
			for (Element child: e.children) {
				if (!this.isIndiffentBlock(child) && child.width > 50) {
					++cnt;
					double tmp = ((double)child.height)/child.width;
					e.child_height2width += tmp;
					if (tmp > 0.6) {
						e.tall_child_num++;
					}
				}
			}
			if (cnt > 0)
				e.child_height2width = e.child_height2width/cnt;
		}
		
		if (e.text_len <= 8 && e.height < 60 && e.area > 0 && xpath.indexOf("li") < 0
				&& (e.text.toString().indexOf("简介")>=0 || e.text.toString().indexOf("介绍")>=0)) {
			e.has_intro_title = true;
		}
		for (Element child: e.children) {
			if (child.has_intro_title) {
				e.has_intro_title = true;
			}
			if (child.longest_text_len > e.longest_text_len) {
				e.longest_text_len = child.longest_text_len;
			}
			if (e.name.equals("a")) {
				e.longest_text_len = 0;
			}
		}
		if (e.longest_text_len > 20 && e.has_intro_title) {
			e.has_intro = true;
		}
		
		for (Element child: e.children) {
			if (child.friend_link_area != 0) {
				e.friend_link_area = child.friend_link_area;
				break;
			}
		}
		if (e.friend_link_area == 0 && e.anchor_len >= 12 && e.anchor_num >= 4 && e.area > 10000) {
			if (e.text.toString().startsWith("友情链接")) {
				e.friend_link_area = e.area;
			}
		}
		for (Element child: e.children) {
			if (child.full_list_area != 0) {
				e.full_list_area = child.full_list_area;
				e.full_list_len = child.full_list_len;
				break;
			}
		}
		for (Element child: e.children) {
				e.time_num += child.time_num;
		}
		for (Element child: e.children) {
			if (child.has_list) {
				e.has_list = true;
				break;
			}
		}
		
		int sub_list_index = -1;
		int indiffent_child_num = 0;
		boolean is_sub_title = false;
		boolean is_detail_item = false;
		int index = 0;
		first_child = null;
		for (Element child: e.children) {
			if (isIndiffentBlock(child) || (!child.is_list && (child.text_len==child.anchor_len))) {
				++indiffent_child_num;
				continue;
			}
			if (index == 0) {
				if (child.text_len < 20 && child.height <60) {
					is_sub_title = true;
				}
				if (!child.is_list) {
					if ((child.anchor_len >= 2 || (child.anchor_num>0 && child.img_num>0)) && child.text_len < 150 && child.height < 200) {
						is_detail_item = true;
					}
				}
				if (child.is_list) {
					sub_list_index = 0;
					first_child = child;
				}
			} else if (index == 1) {
				if (child.is_list && !child.has_merged) {
					sub_list_index = 1;
					first_child = child;
				}
			}
			index++;
		}

		if (sub_list_index == 0 && index == 1 && e.own_text_len < 10) {
			e.has_one_child = true;
			e.is_list = true;
			{
				e.area_list = first_child.area_list;
				e.area_other = first_child.area_other;
				e.list_num1 = first_child.list_num1;
				e.strict_list_num1 = first_child.strict_list_num1;
				e.indiffent_child_num1 = first_child.indiffent_child_num1;
				e.list_num2 = first_child.list_num2;
				e.strict_list_num2 = first_child.strict_list_num2;
				e.indiffent_child_num2 = first_child.indiffent_child_num2;
				e.list_num3= first_child.list_num3;
				e.indiffent_child_num3 = first_child.indiffent_child_num3;
				e.big_list_num = first_child.big_list_num;
				e.is_sub_title = first_child.is_sub_title;
				e.is_bold = first_child.is_bold;
				e.list_of_subtitle = first_child.list_of_subtitle;
				e.list_of_keyword = first_child.list_of_keyword;
				e.has_repeat_sb_structure = first_child.has_repeat_sb_structure;
				e.has_other_block = first_child.has_other_block;
				e.child_is_small = first_child.child_is_small;
				e.has_merged = first_child.has_merged;

			}
			/*
			first_child.is_list = false;
			if (first_child.pe != null) {
				first_child.pe.is_list = false;
			}
			*/
		//	list_blocks.remove(first_child);
			first_child = null;
	//		return;
		}
		
		if (sub_list_index == 1 && is_detail_item && index == 2) {
			{
				e.area_list = first_child.area_list;
				e.area_other = first_child.area_other;
				e.list_num1 = first_child.list_num1;
				e.strict_list_num1 = first_child.strict_list_num1;
				e.indiffent_child_num1 = first_child.indiffent_child_num1;
				e.list_num2 = first_child.list_num2;
				e.strict_list_num2 = first_child.strict_list_num2;
				e.indiffent_child_num2 = first_child.indiffent_child_num2;
				e.list_num3= first_child.list_num3;
				e.indiffent_child_num3 = first_child.indiffent_child_num3;
				e.big_list_num = first_child.big_list_num;
				e.is_sub_title = first_child.is_sub_title;
				e.is_bold = first_child.is_bold;
				e.list_of_subtitle = first_child.list_of_subtitle;
				e.list_of_keyword = first_child.list_of_keyword;
				e.has_repeat_sb_structure = first_child.has_repeat_sb_structure;
				e.has_other_block = first_child.has_other_block;
				e.child_is_small = first_child.child_is_small;
				e.has_merged = first_child.has_merged;
			}
			e.is_list = true;
			e.has_merged = true;
	//		return;
		}

		if (sub_list_index == 1 && is_sub_title && index == 2) {
			{
				e.area_list = first_child.area_list;
				e.area_other = first_child.area_other;
				e.list_num1 = first_child.list_num1;
				e.strict_list_num1 = first_child.strict_list_num1;
				e.indiffent_child_num1 = first_child.indiffent_child_num1;
				e.list_num2 = first_child.list_num2;
				e.strict_list_num2 = first_child.strict_list_num2;
				e.indiffent_child_num2 = first_child.indiffent_child_num2;
				e.list_num3= first_child.list_num3;
				e.indiffent_child_num3 = first_child.indiffent_child_num3;
				e.big_list_num = first_child.big_list_num;
				e.is_sub_title = first_child.is_sub_title;
				e.is_bold = first_child.is_bold;
				e.list_of_subtitle = first_child.list_of_subtitle;
				e.list_of_keyword = first_child.list_of_keyword;
				e.has_repeat_sb_structure = first_child.has_repeat_sb_structure;
				e.has_other_block = first_child.has_other_block;
				e.child_is_small = first_child.child_is_small;
				e.has_merged = first_child.has_merged;
			}
			e.is_list = true;
			e.has_merged = true;
	//		return;
		}
		
		if (e.child_num == 3) {
			Element child1 = e.children.get(0);
			if (child1.text_len < 20 && child1.height <60) {
				Element child2 = e.children.get(1);
				if ((child2.anchor_len > 5 && child2.text_len < 150 && child2.height < 200)
						|| (child2.img_num >0 && child2.text_len == 0 && child2.height < 200)) {
					Element child3 = e.children.get(2);
					if (child3.is_list && !child3.has_merged) {
						{
							first_child = child3;
							e.area_list = first_child.area_list;
							e.area_other = first_child.area_other;
							e.list_num1 = first_child.list_num1;
							e.strict_list_num1 = first_child.strict_list_num1;
							e.indiffent_child_num1 = first_child.indiffent_child_num1;
							e.list_num2 = first_child.list_num2;
							e.strict_list_num2 = first_child.strict_list_num2;
							e.indiffent_child_num2 = first_child.indiffent_child_num2;
							e.list_num3= first_child.list_num3;
							e.indiffent_child_num3 = first_child.indiffent_child_num3;
							e.big_list_num = first_child.big_list_num;
							e.is_sub_title = first_child.is_sub_title;
							e.is_bold = first_child.is_bold;
							e.list_of_subtitle = first_child.list_of_subtitle;
							e.list_of_keyword = first_child.list_of_keyword;
							e.has_repeat_sb_structure = first_child.has_repeat_sb_structure;
							e.has_other_block = first_child.has_other_block;
							e.child_is_small = first_child.child_is_small;
							e.has_merged = first_child.has_merged;
						}
						e.is_list = true;
						e.has_merged = true;
	//					return;
					}
				}
			}
		}

		int area_list = 0;
		int area_other = 0;
		int list_num = 0;
		boolean has_long_text = false;
		for (Element child: e.children) {
			if (isIndiffentBlock(child)) {
				++indiffent_child_num;
				continue;
			}
			if (child.is_list) {
				area_list += child.area;
				list_num++;

			} else {
				area_other += child.area;
				if (child.longest_text_len > 40 && child.anchor_len < 5 && child.width > 250) {
					has_long_text = true;
					break;
				}
			}
		}
		e.area_list = area_list;
		e.area_other = area_other;
		if (list_num == 1 &&  area_other < 100000 && area_other > 0 && area_list>area_other*4 && !has_long_text) {
			e.is_list = true;
	//		e.has_merged = true;
	//		return;
		}
		list_num = 0; 
		int strict_list_num = 0;
		indiffent_child_num = 0;
		int text_num = 0;
		int pool_text_num = 0;
		int iter_num = 0;
		for (Element child: e.children) {
			iter_num++;
			if (isIndiffentBlock(child)) {
				++indiffent_child_num;
				continue;
			}
			if (child.name.equals("li")
					|| child.name.equals("dd") 
					|| child.name.equals("dt")
					|| child.name.equals("tr")
					|| child.name.equals("th")
					|| child.name.equals("td")) {
				if (child.anchor_len > 0) {
					++list_num;
				} else if (child.anchor_num > 0 && child.img_num > 0) {
					++list_num;
				} else if (child.anchor_num > 0) {
					++list_num;
				}
				if (child.anchor_len*2>child.text_len) {
					++strict_list_num;
				} else if (child.text_len == 0 && child.anchor_num > 0 && child.img_num > 0) {
					++strict_list_num;
				}

				if (list_num > 0 && child.anchor_num == 0 && iter_num < e.child_num-1) {
					text_num++;
				} else if (child.text_len >child.anchor_len*2) {
					pool_text_num++;
				}
			}
		}

		e.list_num1 = list_num;
		e.strict_list_num1 = strict_list_num;
		e.indiffent_child_num1 = indiffent_child_num;
		if (list_num >= 4 && list_num +indiffent_child_num+ 2 >= e.children.size() && !(text_num>=2 && text_num+pool_text_num>=e.child_num)) {
			if (e.full_list_area == 0 && ((e.area > 60000 && e.width < 500) || e.time_num >= 4)) {
				e.full_list_area = e.area;
				e.full_list_len = e.text_len;
			}
			e.has_list = true;
			e.is_list = true;
	//		return;
		}
		/*
		if (e.name.equals("ul") && e.id_attr.equals("dropmenu")) {
			System.out.println("asdf " + list_num + "\t" + e.is_list);
		}
		*/
		if (strict_list_num >= 2 && e.children.size() == strict_list_num+indiffent_child_num) {
			e.has_list = true;
			e.is_list = true;
	//		return;
		}
		if (list_num >= 2 && e.children.size() == list_num+indiffent_child_num) {
			e.has_list = true;
			e.is_list = true;
	//		return;
		}

		if (e.own_text_len < 15) {
			//	int null_child_num = 0;
			indiffent_child_num = 0;
			list_num = 0; 
			strict_list_num = 0;
			Element base = null;
			int big_list_num = 0;
			int cnt = 0;
			int sub_title = 0;
			for (Element child: e.children) {
				if (isIndiffentBlock(child)) {
					++indiffent_child_num;
					continue;
				}
				cnt++;
				if (base == null) {
					base = child;
				}
				if (cnt == 2 && list_num == 0 && base.is_sub_title) {
					sub_title = 1;
					base = child;
				}
				if (base.name.equals(child.name) && list_tag_set.contains(base.name)) {

					int area1 = base.area;
					int area2 = child.area;

					if (child.anchor_len > 0 && child.anchor_len*2>=child.text_len && child.height < 500 && ((area1*2>area2 && area2*2>area1) || (area1<30000 && area2<30000) || (base.text_len < 20 && child.text_len < 20)) && !(child.anchor_len>50 && child.height > 150 && child.width < 500)) {
						++list_num;
					} else if (child.text_len == 0 && child.anchor_num > 0 && child.img_num > 0) {
						++list_num;
					}

					if (child.anchor_len>2*(child.text_len-child.anchor_len) && area2<50000 && !(child.anchor_len>50 && child.height > 150 && child.width < 500)) {
						++strict_list_num;
					}
					if (child.anchor_len>=4 && child.text_len > 30
							&& child.width == base.width && ((area1*1.5>area2 && area2*1.5>area1) || (area1*2>area2 && area2*2>area1 && child.text_len < 100 && base.text_len < 100)) && child.height < 300) {
						++big_list_num;
					}
				}
				base = child;
			}
			e.list_num2 = list_num;
			e.strict_list_num2 = strict_list_num;
			e.indiffent_child_num2 = indiffent_child_num;
			e.big_list_num = big_list_num;
			if (list_num >= 4 && (list_num +indiffent_child_num+ 2 >= e.children.size() || list_num >= (e.child_num-indiffent_child_num-list_num)*3)) {
				if (e.full_list_area == 0 && ((e.area > 60000 && e.width < 500) || e.time_num >= 4)) {
					e.full_list_area = e.area;
					e.full_list_len = e.text_len;
				}
				e.is_list = true;
				e.has_list = true;
	//			return;
			}

			if (strict_list_num >= 2 && e.children.size()== strict_list_num + indiffent_child_num ) {
				e.is_list = true;
				e.has_list = true;
	//			return;
			}

			//	String attr = e.id_attr + "\t" + e.class_attr;
			if (big_list_num >= 4 && big_list_num +indiffent_child_num+ 2 >= e.children.size() ) {
				e.has_list = true;
				e.is_list = true;
	//			return;
			}

			if ((list_num >= 3 || big_list_num >= 3) && big_list_num +indiffent_child_num + sub_title == e.children.size() ) {
				e.has_list = true;
				e.is_list = true;
	//			return;
			}
			

			indiffent_child_num = 0;
			list_num = 0; 
			strict_list_num = 0;
			base = null;
			cnt = 0;
			for (Element child: e.children) {
				if (isIndiffentBlock(child)) {
					++indiffent_child_num;
					continue;
				}
				cnt++;
				if (base == null) {
					base = child;
				}
				if (cnt == 2 && list_num == 0 && base.is_sub_title) {
					base = child;
				}
				if (base.name.equals(child.name) && list_tag_set.contains(base.name)) {
					if (child.text_len == 0 && child.anchor_num > 0 && child.img_num > 0) {
						++list_num;
					} else if (child.anchor_len*4>child.text_len && child.width == base.width && child.height == base.height && !(child.anchor_len>50 && child.height > 150 && child.width < 500)) {
						//	System.out.println(child.class_attr + "\t" + base.class_attr);
						++list_num;
					}
					base = child;
				}

			}
			e.list_num3 = list_num;
			e.indiffent_child_num3 = indiffent_child_num;
			if (list_num >= 4 && list_num +indiffent_child_num+ 2 >= e.children.size()) {
				if (e.full_list_area == 0 && ((e.area > 60000 && e.width < 500) || e.time_num >= 4)) {
					e.full_list_area = e.area;
					e.full_list_len = e.text_len;
				}
				e.has_list = true;
				e.is_list = true;
				//	System.out.println(full_xpath + "\t" + e.full_name + "\t" + e.class_attr);
	//			return;
			}
			
			indiffent_child_num = 0;
			list_num = 0; 
			strict_list_num = 0;
			base = null;
			cnt = 0;
			for (Element child: e.children) {
				if (isIndiffentBlock(child)) {
					++indiffent_child_num;
					continue;
				}
				cnt++;
				if (base == null) {
					base = child;
				}
				if (cnt == 2 && list_num == 0 && base.is_sub_title) {
					base = child;
				}
				if (base.name.equals(child.name) && list_tag_set.contains(base.name)) {
					 if (child.text_len < 70 && child.anchor_num > 0 && child.img_num > 0 && child.area < 100000) {
							++list_num;
						}
					base = child;
				}

			}
			
			if (list_num >= 4 && list_num +indiffent_child_num+ 1 >= e.children.size()) {
				if (e.full_list_area == 0 && ((e.area > 60000 && e.width < 500) || e.time_num >= 4)) {
					e.full_list_area = e.area;
					e.full_list_len = e.text_len;
				}
				e.has_list = true;
				e.is_list = true;
				//	System.out.println(full_xpath + "\t" + e.full_name + "\t" + e.class_attr);
	//			return;
			}
		}
		if (e.anchor_len*3>e.text_len && e.child_num >=6) {
			boolean flag = false;
			for (Element child: e.children) {
				if (child.is_list && child.text_len > 100) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				for (int k = 2; k <=5; ++k) {
					int cnt = 0;
					HashMap<String, Integer> repeat_cnt = new HashMap<String, Integer>();
					StringBuffer sb = new StringBuffer();
					int max_repeat_num = 0;
					String max_repeat = "";
					Element base = null;
					int same_tag_cnt = 0;
					for (Element child: e.children) {
						if (base == null) {
							base = child;
						}
						if (child.name.equals(base.name)) {
							same_tag_cnt++;
						}
					//	System.out.println("hh  " + child.name);
						++cnt;
						if (cnt%k!=0) {
							sb.append(child.name);
						} else {
							sb.append(child.name);
						//	System.out.println("asdf  " + sb);
							Integer num = repeat_cnt.get(sb.toString());
							if (num == null) {
								repeat_cnt.put(sb.toString(), 1);
							} else {
								repeat_cnt.put(sb.toString(), num+1);
								if (max_repeat_num < num+1) {
									max_repeat_num = num+1;
									max_repeat = sb.toString();
								}
							}
							sb.setLength(0);
							sb.trimToSize();
						}
						base = child;
					}
					if (same_tag_cnt*3>e.child_num*2) {
						break;
					}
			//		System.out.println(max_repeat_num + "\t" + e.child_num);
					if (max_repeat_num*k>=(e.child_num-max_repeat_num*k)*3 && e.top > 0) {
						e.has_repeat_sb_structure = true;
						e.is_list = true;
						e.has_list = true;
						//System.out.println(max_repeat);
		//				return;
					}
				}
			}
		}
		//重看一次
		if (e.child_num < 5) {
			int state = 0;
			int chip_num = 0;
			for (Element child: e.children) {
				if (isIndiffentBlock(child)) {
					
				} else if (child.is_sub_title) {
					if (state == 0) {
						state = 1;
					}
				} else if (child.is_list) {
					if (state == 1) {
						state = 2;
					} else {
						state = 0;
						break;
					}
				} else if ((child.text_len == 0 && child.img_num > 0 && child.height < 200) || (child.anchor_len > 0 && child.height < 60 && child.text_len < 10)) {
					if (state == 1 && child.anchor_len>(child.text_len-child.anchor_len)*2) {
						state = 2;
					} else {
						chip_num++;
						if (chip_num>1) {
							state = 0;
							break;
						}
					}
				} else {
					state = 0;
					break;
				}
			}
			if (state == 2) {
				e.list_of_subtitle = true;
				e.is_list = true;
//				return;
			}
		}
		
		list_num = 0;
		boolean has_other_block = false;
		if (e.child_num < 5) {
			
			for (Element child: e.children) {
				if (isIndiffentBlock(child)) {
					
				} else if (child.is_list) {
					list_num++;
				} else if ((child.text_len-child.anchor_len) == 0) {
					
				} else {
					has_other_block = true;
					break;
				}
			}
			e.has_other_block = has_other_block;
			if (list_num == 1 && !has_other_block) {
				e.is_list = true;
	//			return;
			}
		}

//		System.out.println(e.child_num);
		if (e.child_num > 1) {
			int t = 0;
			for (; t < e.child_num;++t) {
				if (isIndiffentBlock(e.children.get(t))) {
					
				} else {
					break;
				}
			}
			if (t < e.child_num) {
				first_child = e.children.get(t);
				String[] recommend_keys = {"相关", "其他相关", "类似", "猜你喜欢", "热门", "最新", "附近的", "友情链接", "推荐"};
				if (first_child.text_len < 15 && first_child.height < 65) {
					for (String text: recommend_keys) {
						if (first_child.text.toString().startsWith(text) || ((first_child.is_sub_title) && first_child.text.toString().indexOf(text)>=0)) {
							e.list_of_keyword = true;
							e.is_recommand_title = true;
							e.is_list = true;
	//						return;
						}
					}
				}
				if (first_child.text_len < 15 && first_child.height < 65 && first_child.anchor_len >= 4
						&& first_child.text.toString().endsWith("更多>>") && e.anchor_len*6>e.text_len) {
					e.list_of_keyword = true;
					e.is_list = true;
	//				return;
				}
				if (first_child.text_len <= 10 && first_child.height < 65 && first_child.anchor_len >= 2
						&& first_child.text.toString().endsWith("更多") && first_child.is_sub_title) {
					e.list_of_keyword = true;
					e.is_list = true;
	//				return;
				}
				if (first_child.text_len <= 10 && first_child.height < 65 && first_child.anchor_len >= 1
						&& first_child.text.toString().endsWith("MORE") && e.anchor_len*2>e.text_len) {
					e.list_of_keyword = true;
					e.is_list = true;
	//				return;
				}
			}
		}


		
		if (e.child_num > 6 && e.anchor_len>(e.text_len-e.anchor_len)*5 && e.area < 400000) {
			boolean child_is_small = true;
			for (Element child: e.children) {
				if (child.area < 20000) {
					
				} else {
					child_is_small = false;
					break;
				}
			}
			e.child_is_small = child_is_small;
			if (child_is_small) {
				e.is_list = true;
	//			return;
			}
			
		}
		
		if (e.has_intro) {
			e.is_list = false;
		}
		if (e.longest_own_text_len > 50) {
			e.is_list = false;
		}
		isListExtend(e);
	}

	public void isListExtend(Element e)
	{
		int list_num=0;
		int list_area=0;
		int other_num=0;
		int other_area=0;
		if (e.is_list && e.child_num > 2) {
			for (int i = 1; i < e.child_num; i++) {
				Element child = e.children.get(i);
				if (!child.is_list||child.is_text) {
					other_num++;
					list_area+=child.area;
				}
				else
				{
					list_num++;
					other_area+=child.area;
				}
			}
		}
		if(list_area*3<other_area&&other_area>e.area/2)
		{
			e.is_list = false;
		}
		if(list_num==0&&other_area>0)
		{
			e.is_list = false;
		}
		if(e.name.compareTo("table")==0)
		{
			list_num=getListNum(e);
			if(list_num>3)e.is_list = true;
			if(list_num==1)e.is_list = false;
		}
		
	}
	
	public int getListNum(Element e)
	{
		int list_num=0;
		for(Element c:e.children)
		{
			if(c.src!=null)
			{
				list_num++;
			}
			else list_num+=getListNum(c);
		}
		return list_num;
	}
	private void isFooter(Element e) {
		if ((e.width >= 800 || e.width > page.width)
				&& e.top + e.height + 30 > page.height
				&& e.height < 200
				&& e.height > 0 && e.text_len > 10
				&& e.top > 100) {
			e.is_footer = true;
			return;
		}
		if ((e.text.toString().indexOf("©")>=0 || e.text.toString().indexOf("ICP")>=0 )&& e.text.length() > 10
				&& path.size()>=2 && e.top > 100 && e.text_len < 200) {
			e.is_footer = true;
		}
		String attr = e.class_attr + " " + e.id_attr;
		if (attr.toLowerCase().indexOf("foot")>=0
				&& (e.width >= 800 || e.width > page.width)
				&& e.top + e.height + 200 > page.height
				&& e.height < 500 && e.height > 0 && e.text_len > 10) {
			e.is_footer = true;
		}
		if ((e.text.toString().indexOf("©")>=0 || e.text.toString().indexOf("ICP")>=0 )&& e.text.length() > 10
				&& path.size()>=2 && attr.toLowerCase().indexOf("foot")>=0 && e.text_len < 200) {
			e.is_footer = true;
		}
	}
	
	private boolean isLogin(Element	e) {
		/*
		int cnt = 0;
		if (s.length() < 20) {
			String[] keys = {"登录","注册","密码", "用户名"};
			for (String key: keys) {
				if (s.indexOf(key)>=0) {
					cnt++;
				}
			}
			if (cnt >=2) {
				return true;
			}
		}
		return false;
		*/
		if (e.input_area > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	private void isSingleBlock(Element e) {
		if (e.child_num > 1) {

			if (e.name.equals("li") && e.height < 200 && e.text_len < 100) {
				e.is_single_block = true;
				e.pe.is_single_block = true;
				return;
			}
			boolean is_single_block = true;
			Element first_child = null;
			boolean flag = false;
//			System.out.println("isIndiffentBlock"+e.name);
			if(e.name.toLowerCase().equals("tbody"))flag = true;
			for (int i = 0; i < e.child_num; i++) {
				Element child = e.children.get(i);
				if (isIndiffentBlock(child)) {
//					if(flag)System.out.println("isIndiffentBlock"+e.text);
				} else {
					if (first_child == null) {
						first_child = child;
						if ((first_child.area > 40000 && first_child.width < 1424) 
								|| (first_child.area > 40000 && first_child.height > 300) 
								|| first_child.text_len > 40) {
							is_single_block = false;
//							if(flag)System.out.println("first_child1"+e.text);
							break;
						}
					} else {
						if (first_child.name.equals(child.name) 
								&& child.height == first_child.height 
								&& child.width == first_child.width 
								&& child.text_len < 40) {
//							if(flag)System.out.println("first_child2"+e.text);
						} else if (first_child.name.equals(child.name) 
								&& child.height <= 110 
								&& child.width <= 300 
								&& child.text_len < 50) {
//							if(flag)System.out.println("first_child3"+e.text);
						} else {
							is_single_block = false;
//							if(flag)System.out.println("first_child4"+e.text);
							break;
						}
					}
				}
			}
			e.is_single_block = is_single_block;
			e.pe.is_single_block = is_single_block;
			if (e.is_single_block) {
//				if(flag)System.out.println("first_child5"+e.text);
				return;
			}
			boolean has_big_child = false;
			for (Element child: e.children) {
				if (child.area < 20000 && child.text_len < 50) {
//					if(flag)System.out.println("first_child6"+e.text);
				} else {
					has_big_child = true;
//					if(flag)System.out.println("first_child7"+e.text);
					break;
				}
			}
			if (!has_big_child) {
				e.is_single_block = true;
				e.pe.is_single_block = true;
//				if(flag)System.out.println("first_child8"+e.text);
				return;
			}
		}
	}
	
	
	private void traverseToFixAttr(Element e) {
		for (Element child: e.children) {
			if (child.is_list) {
				child.is_list = false;
				child.pe.is_list = false;
			}
			traverseToFixAttr(child);
		}
	}
	
	private void isInfoBlock(Element e) {
		if (e.child_num >= 2) {
			int real_child_num = 0;
			int info_child_num = 0;
			for (Element child: e.children) {
				if (this.isIndiffentBlock(child) && child.name.length() != 0) {
					continue;
				}
				real_child_num++;
				if (child.height < 50 && child.text_len < 50 && (e.text_len-e.anchor_len)*2>e.anchor_len) {
					int pos1 = child.text.toString().indexOf("：");
					int pos2 = child.text.toString().indexOf(":");
					if ((pos1> 0 && pos1 <= 5)|| (pos2> 0 && pos2 <= 5)) {
						info_child_num++;
					}
				}
			}
			if ((info_child_num>=3 && info_child_num+2>= real_child_num)
					|| (info_child_num==2 && info_child_num>= real_child_num && e.text.indexOf("用户名") < 0 && !this.isLogin(e))) {
				e.is_info = true;
				e.is_text = true;
				if (e.is_list) {
					e.is_list = false;
					e.pe.is_list = false;
				}
				traverseToFixAttr(e);
			}
		}
	}
	
	private void isText(Element e) {
		isSingleBlock(e);
		isInfoBlock(e);
		if (e.is_text) {
			return;
		}
		/*
		if (e.id_attr.equals("detail_left_partno") && e.top == 213) {
			System.out.println("asdf " + e.tag_num + "\t" + (e.text_len-e.anchor_len));
		}
*/
		
		if ((e.text_len-e.anchor_len<e.anchor_len && e.own_text_len < 10) || e.name.equals("option") || e.name.equals("textarea") || e.is_footer) {
			e.is_text = false;
			return;
		}
		if ((e.top == 0 && e.left == 0 && page.width != 0 && page.height != 0) || (e.width == 0 && e.height == 0 && page.width != 0 && page.height != 0) || e.is_displaynone) {
			e.is_text = false;
			return;
		}
		
		if (e.name.equals("a")) {
			e.is_text = false;
			return;
		}
		if (e.width < 300 && e.height > 800 && e.child_num < 5) {
			boolean has_small_child = false;
			for (Element child: e.children) {
				if (this.isIndiffentBlock(child)) {
					
				} else if (child.height < 50 && child.text_len < 20) {
					has_small_child = true;
					break;
				}
			}
			if (!has_small_child) {
				e.is_text = false;
				return;
			}
		}


/*
		if ((e.text_len-e.anchor_len)<e.visible_tag_num*3) {
			e.is_text = false;
			return;
		}
		*/

		if (e.is_sub_title) {
			e.is_text = false;
			return;
		}
		
		String attr = e.class_attr + e.id_attr;
		if (attr.indexOf("list") >= 0 && e.anchor_num >= 5 && e.child_num >=5 && e.anchor_num*2>=e.child_num) {
			e.is_text = false;
			return;
		}

		int text_length = 0;
		int other_length = 0;
		int other_area = 0;
		int big_child_num = 0;
		int big_child_area = 0;
		int text_num = 0;
		int other_num = 0;
		int side_other_length = 0;
		int first_text = -1;
		int end_text = -1;
		int index = 0;
		int own_text_len = 0;
		int own_text_num = 0;
		int a_text_len = 0;
		int big_text_area = 0;
		int big_text_num = 0;
		int not_big_text_area = 0;
		int real_text_num = 0;
		Element first_big_text = null;
		int real_child_num = 0;
		for (Element child: e.children) {
			if (!this.isIndiffentBlock(child)) {
				real_child_num++;
			}
			if (child.area > 300000 && child.text_len > 50) {
				big_child_num++;
				big_child_area += child.area;
			}
			if (child.is_text && child.text_len > 100 && child.text_len > child.anchor_len*4) {
				big_text_area += child.area;
				big_text_num++;
				if (first_big_text == null)
					first_big_text = child;
			} else if (child.text_len < 20 || !child.is_text) {
				not_big_text_area += child.area;
			}
			if (child.is_text || child.is_text_can) {
				text_length += child.text_len;
				text_num++;
				if (child.is_text) {
					real_text_num++;
				}
				if (first_text == -1) {
					first_text = index;
				}
				if (index > end_text) {
					end_text = index;
				}
			} else if (child.name.length() == 0 && child.text_len-child.anchor_len>child.anchor_len) {
				text_length += child.text_len;
				own_text_len += child.text_len;
				own_text_num++;
				text_num++;
				if (child.text_len > 20) {
					real_text_num++;
				}
				if (first_text == -1) {
					first_text = index;
				}
				if (index > end_text) {
					end_text = index;
				}
			}
			else if (!child.is_sub_title || child.is_recommand_title) {
				other_area += child.area;
				other_length += child.text_len;
				other_num++;
			}
			index++;
		}
		if (first_text >= 0) {
			for (int i = 0; i < first_text; i++) {
				side_other_length += e.children.get(i).text_len;
			}
		}
		if (end_text >= 0) {
			for (int i = end_text+1; i < e.child_num; i++) {
				side_other_length += e.children.get(i).text_len;
			}
		}
		if (own_text_len > 10 && own_text_num > 3 && own_text_num > other_num && e.height < 100
				&& text_length*2 > other_length) {
			if (e.text_len >= 20 || (e.text_len > 10 && e.top < 50 && e.width > 300)) {
				e.is_text = true;
			} else {
				e.is_text_can = true;
			}
			return;
		}
		if ((text_length >= other_length*2 || (e.longest_own_text_len > 50 && text_length>other_length)) && !(big_child_num > 1 && big_child_area*2 > e.area) && (e.height < page.height || page.height == 0) ) {
			if (isLogin(e)) {
				
			} else if (text_num <= 5 && text_length < other_length*10 && side_other_length > 15) {

			} else if (text_num <= 5 && other_area > 100000 && other_length > 10) {
				
			} else if (e.height > 600 && big_text_area > 60000 && big_text_area*2<not_big_text_area) {
				
			} else if (big_text_area > 1000000 && big_text_num == 1 && big_text_area*2>e.area && real_child_num > 2) {
				
			} else if (real_text_num == 1 && big_text_num == 1 && first_big_text.width >= 400 && e.height > 250 && first_big_text.height*3>e.height && real_child_num > 2) {
				
			} else if (e.text_len >= 20 || (e.text_len > 10 && e.top < 50 && e.width > 300)) {
				e.is_text = true;
			} else {
				e.is_text_can = true;
			}
		//	e.is_text = true;
		} else {
			e.is_text = false;
		}
		
		if (!e.is_text && !e.is_text_can && e.child_num > 2) {
			for (int i = 1; i < e.child_num; i++) {
				Element child = e.children.get(i);
				if (child.is_text) {
					Element pre = e.children.get(i-1);
					if (pre.is_sub_title && !pre.is_recommand_title) {
						pre.is_text = true;
						pre.pe.is_text = true;
					}
//					System.out.println("aaaaa ");
				}
			}
		}
		if (!e.is_text && !e.is_text_can && e.child_num > 2) {
			for (int i = 1; i < e.child_num; i++) {
				Element child = e.children.get(i);
				if (child.is_text||child.name.contains("img")) {
					Element pre = e.children.get(i-1);
					if (pre.is_sub_title && !pre.is_recommand_title) {
						pre.is_text = true;
						pre.pe.is_text = true;
					}
//					System.out.println("aaaaa ");
				}
			}
		}
		/*
		if (e.class_attr.equals("Pannel") && e.top == 21616 && e.height == 278) {
			System.out.println("asdf " + e.tag_num + "\t" + (e.text_len-e.anchor_len));
		}
		*/
		
	}
	
	private void isSubTitle(Element e) {
		String attr = e.class_attr + e.id_attr;
		if ((e.is_bold || (e.name.length() == 2 && e.name.startsWith("h")) || attr.indexOf("title")>=0) && e.text_len < 20 && e.height < 60) {
			e.is_sub_title = true;
		}
		if (e.text_len < 20 && e.height < 60 && e.child_num <= 3 && e.child_num >=1) {
			Element first_child = null;
			for (Element child: e.children) {
				if (isIndiffentBlock(child) && child.name.length() > 0) {
					
				} else if (child.text_len< 5 && (child.text.toString().toLowerCase().startsWith("more") || child.text.toString().startsWith("更多"))) {
					
				} else if (child.img_num == 1 && child.text_len == 0) {
					
				} else {
					first_child = child;
					break;
				}
			}
			if (first_child != null && first_child.is_sub_title && first_child.text_len + 5 > e.text_len) {
				e.is_sub_title = true;
			}
		}
		String[] recommend_keys = {"相关", "其他相关", "类似", "猜你喜欢", "热门", "最新", "附近的", "友情链接", "推荐"};
		if (e.is_sub_title) {
			for (String text: recommend_keys) {
				if (e.text.toString().indexOf(text)>=0) {
					e.is_recommand_title = true;
//						return;
				}
			}
		}		
	}
	

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		Element e = path.peekLast();

		if (qName.equalsIgnoreCase(e.name)) {
		//	System.out.println(e.name + "\t" + e.top + "\t" + e.left + "\t" + e.height + "\t" + e.width + "\t" + full_xpath);
			//System.out.println(full_xpath);
			path.pollLast();
			PrintElement pe = print_path.peekLast();
			print_path.pollLast();
			Element parent = path.peekLast();
			e.child_num = e.children.size();

			if (qName.equalsIgnoreCase("script") || qName.equalsIgnoreCase("style")) {
				xpath.delete(xpath.lastIndexOf("/"), xpath.length());
				full_xpath.delete(full_xpath.lastIndexOf("/"), full_xpath.length());
				return;
			}
			
			e.area = e.width*e.height;
			for (Element child: e.children) {
				if (child.name.length() == 0) {
					e.text_len += child.text_len;
					if (e.name.equals("a")||e.src!=null) {
						e.anchor_len += child.text_len;
						e.anchor_num = 1;
					}
					e.text.append(child.text);
					
				} else if (!child.is_displaynone) {
					e.text_len += child.text_len;
					if (e.name.equals("a")||e.src!=null) {
						e.anchor_len += child.text_len;
						e.anchor_num = 1;
					} else {
						e.anchor_len += child.anchor_len;
						e.anchor_num += child.anchor_num;
					}
					if (e.name.equals("img")) {
						e.img_num = 1;
						e.img_area = e.area;
					} else {
						e.img_num += child.img_num;
						e.img_area += child.img_area;
					}
					e.text.append(child.text);
					e.input_area += child.input_area;
					e.tag_num += child.tag_num;
					e.visible_tag_num += child.visible_tag_num;
					e.anchor_area += child.anchor_area;
				}
			}
			e.tag_num += 1;
			if (e.area > 0 || e.text_len > 0 || e.img_num > 0) {
				e.visible_tag_num += 1;
			}
			if (e.name.equals("img")) {
				e.img_num = 1;
				e.img_area = e.area;
			}
			if (e.name.equals("a")) {
				e.anchor_num = 1;
				e.anchor_area = e.area;
				e.anchor_len = e.text_len;
			}
			if (e.name.equals("input")) {
				e.input_area = e.area;
			}
	//		System.out.println(e.img_num + "\t" + full_xpath);
			isSubTitle(e);
			isList(e);
			
			if (e.is_list) {
				pe.is_list = true;
			//	list_blocks.add(e);
		//		System.out.println(e.text_len + "\t" + e.anchor_len + "\t" + e.anchor_num + "\t" + e.text);
			}
			isFooter(e);
			if (e.is_footer) {
				pe.is_footer = true;
			}
			if(!e.is_list)
			{
				isText(e);
				if (e.is_text) {
					pe.is_text = true;
					text_blocks.add(e);
				}
			}
		//	e.children.clear();
			if (parent != null) {
				parent.children.add(e);
			}
			xpath.delete(xpath.lastIndexOf("/"), xpath.length());
			full_xpath.delete(full_xpath.lastIndexOf("/"), full_xpath.length());

		}
	}

	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (is_abnormal_page) {
			return;
		}

		Element element = path.peekLast();
		
		String s = String.valueOf(ch, start, length);
		PrintElement pe = new PrintElement();
		
		pe.text = s;
		PrintElement p = print_path.peekLast();
		if (p != null) {
			p.children.add(pe);
		}
		if (element.name.equalsIgnoreCase("script") || element.name.equalsIgnoreCase("style")) {
			return;
		}
		StringBuffer out = new StringBuffer();
		int len = PageSegmentation.cleanString(s, out);
		if (out.length() > 0) {
			Element child = new Element();
			child.text.append(out);
			if (isTimeString(child.text.toString())) {
				child.text_len = 1;
				child.is_time = true;
				child.time_num = 1;
			} else {
				child.text_len = len;
			}
			if (element != null) {
				element.children.add(child);
				element.own_text_len += len;
				if (element.longest_own_text_len < len) {
					element.longest_own_text_len = len;
				}
				if (element.longest_text_len < len) {
					element.longest_text_len = len;
				}
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
	public Element IsHasText(Element e)
	{
		int Max_area=0;
		Element res=null;
		for(Element c:e.children)
		{
			if(c.is_text){
				if(Max_area<c.area){
					Max_area = c.area;
					res = c;
				}
			}
			else{
				Element d = IsHasText(c);
				if(d!=null&&Max_area<d.area){
					res = d;
					Max_area = d.area;
				}
			}
		}
		return res;
	}
	public int  OtherContentLen(Element e){
		int area=0;
		for(Element c:e.children)
		{
			if(c.is_text||c.name.compareTo("img")==0){
				area += c.area;
			}
			else{
				area += OtherContentLen(c);
			}
		}
		return area;
	}
	public void isListPage(){
		if(is_abnormal_page)
		{
			return;
		}
		if(text_blocks.size()==0)
		{
			isListPage = true;
			System.out.println("list_num");
//			return;
		}
		long Big_Text_area=0;
		long Big_Text_left=0;
		long Big_Text_top=0;
		long frist_Text_area=0;
		long frist_Text_top=0;
		long frist_Text_left=0;
		long Big_List_area=0;
		long Big_List_left=0;
		long Big_List_top=0;
		long frist_List_area=0;
		long frist_List_top=0;
		long frist_List_left=0;
		Element Big_list = null;
		Element Big_Text = null;
		long list_num=0;
		long text_num=0;
		for(Element e:text_blocks)
		{
			if(e.top>(page.height/2)||e.left>700||e.area<10000)
			{
//				System.out.println("1e"+e.top+"|"+page.height);
				continue;
			}
			else
			{
				text_num++;
				if(frist_Text_area==0)
				{
					frist_Text_area = e.area;
					frist_Text_left = e.left;
					frist_Text_top = e.top;
				}
				if(e.area>Big_Text_area)
				{
					Big_Text_area = e.area;
					Big_Text_left = e.left;
					Big_Text_top = e.top;
					Big_Text = e;
				}
			}
		}
		for(Element e:list_blocks)
		{
			if(e.name.contains("html"))
			{
				System.out.println(e.name);
			}
			if(e.top>page.height/2||e.left>700||e.area<5000)continue;
			list_num++;
			if(frist_List_area==0)
			{
				frist_List_area = e.area;
				frist_List_left = e.left;
				frist_List_top = e.top;
			}
			if(e.area>Big_List_area)
			{
				Big_List_area = e.area;
				Big_List_left = e.left;
				Big_List_top = e.top;
				Big_list = e;
			}
		}
		if(Big_list!=null&&Big_Text!=null&&Big_List_area>0)
		{
				if(Big_List_area>Big_Text_area
						&&(Math.abs(Big_Text.left-Big_list.left)<100&&Big_List_left<500)
						&&((Math.abs(Big_Text.top-Big_list.top)<100
								||Big_Text_top-Big_List_top>500)
								&&Big_List_top<page.height/2)
						)
				{
//					System.out.println("ok");
					isListPage = true;
					if(text_num>list_num*2)isListPage = false;
				}
				if(!isListPage&&Big_List_area>Big_Text_area
						&&list_num>=5&&text_num<=2
						&&((Math.abs(Big_Text.top-Big_list.top)<100
								||Big_Text_top-Big_List_top>500)
								&&Big_List_top<page.height/2))
				{
					isListPage = true;
				}
				if(!isListPage&&Big_List_area>3*Big_Text_area
						&&list_num>=3
						&&((Math.abs(Big_Text.top-Big_list.top)<400
								||Math.abs(Big_Text_top-Big_List_top)<400)
								&&Big_List_top<page.height/2))
				{
					isListPage = true;
				}
				if(!isListPage&&Big_List_area>7*Big_Text_area
						&&list_num>=2
						&&((Math.abs(Big_Text.top-Big_list.top)<400
								||Math.abs(Big_Text_top-Big_List_top)<400)
								&&Big_List_top<page.height/2))
				{
					isListPage = true;
				}
				if(!isListPage&&Big_List_area>10*Big_Text_area
						&&list_num>=2
						&&((Math.abs(Big_Text.top-Big_list.top)<700
								||Math.abs(Big_Text_top-Big_List_top)<700)
								&&Big_List_top<page.height/2))
				{
					isListPage = true;
				}
				if(Big_List_area>Big_Text_area&&Big_Text_area>10000)
				{
					if(Math.abs(Big_Text.left-Big_list.left)<100
							&&Big_Text.width+Big_Text.left<=Big_list.width
							&&Math.abs(Big_Text.top-Big_list.top)<100
							&&Big_Text.height+Big_Text.top<=Big_list.height)
					{
						if(Big_Text.area*2>Big_List_area
							&&Big_Text.left<page.width/2&&Big_Text.top<page.height/2)
						{
//							System.out.println("Listcontaintext1"+Big_Text.area);
							isListPage = false;
						}
						if(Big_Text!=null&&Big_Text.area*3>Big_List_area
								&&(Big_Text.top-Big_list.top<100)&&(Big_Text.left-Big_list.left<200)
								&&Big_Text.left<page.width/2&&Big_Text.top<page.height/2)
						{
//							System.out.println("Listcontaintext2"+Big_Text.area);
							isListPage = false;
						}
					}
				}
		}
		if(Big_list!=null&&Big_Text==null&&Big_List_area>0)
		{
			System.out.println("ListPage");
			if(Big_list.left<page.width/2||(page.width==0&&Big_list.left<600))isListPage = true;
		}

		if(Big_List_area>Big_Text_area&&Big_Text_area>10000)
		{
			Element e = IsHasText(Big_list);
//			if(e!=null)System.out.println("Listcontaintext"+e.area);
			if(e!=null&&e.area>Big_List_area)isListPage = false;
			if(e!=null&&e.area*2>Big_List_area
					&&e.left<page.width/2&&e.top<page.height/2)
			{
//				System.out.println("Listcontaintext1"+e.area);
				isListPage = false;
			}
			
			if(e!=null&&e.area*3>Big_List_area
					&&(e.top-Big_list.top<100)&&(e.left-Big_list.left<200)
					&&e.left<page.width/2&&e.top<page.height/2)
			{
//				System.out.println("Listcontaintext2"+e.area);
				isListPage = false;
			}
			
//			if(e!=null)
//			{
//				System.out.println("Text Height:"+e.height+"|"+Big_Text.top);
//				System.out.println("List Height:"+Big_list.height+"|"+Big_list.top);
//				System.out.println("page:"+page.width+"|"+page.height);
//				System.out.println("e:"+e.left+"|"+e.top);
//				System.out.println("area:"+e.area*4+"|"+Big_List_area);
//			}
			//横向
			if(e!=null&&e.area*4>Big_List_area
					&&e.left<page.width/2
					&&e.top<page.height/2
					&&e.left>=Big_list.left
					&&(e.width+Big_Text.left+100>=Big_list.width+Big_list.left))
			{
				System.out.println("横向:"+e.area);
				isListPage = false;
			}
			//纵向
			if(e!=null&&e.area*4>Big_List_area
					&&e.left<page.width/2
					&&e.top<page.height/2
					&&e.top>=Big_list.top
					&&(e.height+Big_Text.top+100>=Big_list.height+Big_list.top))
			{
				System.out.println("纵向:"+e.area);
				isListPage = false;
			}
			int otherarea = OtherContentLen(Big_list);
			System.out.println("otherarea:"+otherarea);
//			if(otherarea*2>Big_List_area)isListPage = false;
		}
//		if(Big_List_area==0&&Big_Text_area==0)isListPage=false;
		System.out.println("text_num"+text_num);
		System.out.println("list_num"+list_num);
		System.out.println("Big_List_area"+Big_List_area);
		System.out.println("Big_Text_area"+Big_Text_area);
		System.out.println("Big_List_left"+Big_List_left);
		System.out.println("Big_Text_left"+Big_Text_left);
		System.out.println("Big_List_top"+Big_List_top);
		System.out.println("Big_Text_top"+Big_Text_top);
		return;
	}
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException, SAXException {
		int index = 1913;
		
		ListBlockExtractor		htmlContentHandler		= new ListBlockExtractor();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		FileInputStream f_stream = null;
		int i=73;
//		int i=1;
//		while(i<=100)
		{
			String name="t"+i;
//			String name="t1026";
			File file = new File("D:\\JavaWorkplace\\sogou\\601Pages\\"+name+".html");
//			file = new File("D:\\JavaWorkplace\\sogou\\data\\LastNewPage\\t497.html");
//			file = new File("D:\\JavaWorkplace\\sogou\\data\\pages\\t"+num+".html");
//			file = new File("D:\\JavaWorkplace\\sogou\\data\\pages\\t1408.html");
//			file = new File("D:\\JavaWorkplace\\sogou\\data\\NewPage\\t798.html");
			if(!file.exists())
			{
				i++;
//				continue;
			}
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String tempString = null,s=null,url=null;
			int line=1;
			while ((tempString = reader.readLine()) != null) {
				if(line==1)url = tempString;
				else s+=tempString;
				line++;
				tempString=null;
			}
			try {
		//		htmlContentHandler.setUrl("http://www.iqiyi.com/yinyue/20121214/10453bddc18b141b.html");
				parser.parse(new InputSource(new StringReader(s)));
				if (htmlContentHandler.red != null) {
					System.out.println(htmlContentHandler.red.has_list);
				}
				System.out.println(i+"|"+htmlContentHandler.isListPage);
				Counter cnt = new Counter();
				String out = htmlContentHandler.root.traverse_debug(cnt);
			//	System.out.println(out);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("ListPages\\"+name+".html")), "utf8"));
			//	writer.write("<base href=\"" + opw.url.toString() + "\">\n");
			//	writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf8\">\n");
				writer.write(out);
				writer.flush();
				writer.close();
			}  catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			i++;
		}

	}
} 

