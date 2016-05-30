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



class Element {
	String name;
	String full_name;
	StringBuffer text;
	int left;
	int top;
	int width;
	int height;
	String xpath;
	HashMap<String, Integer> tag2num;
	boolean child_is_big;
	boolean child_is_merageable;
	boolean child_is_merageable2;
	boolean is_displaynone;
	int text_len;
	int own_text_len;
	int anchor_len;
	int p_tag_num;
	int text_child_num;
	int child_num;
	Element() {
		left = top = width = height = 0;
		xpath = "";
		name = "";
		full_name = "";
		text = new StringBuffer();
		tag2num = new HashMap<String, Integer>();
		child_is_big = false;
		child_is_merageable = true;
		child_is_merageable2 = false;
		is_displaynone = false;
		text_len = 0;
		own_text_len = 0;
		anchor_len = 0;
		p_tag_num = 0;
		text_child_num = 0;
		child_num = 0;
	}
}

class XpathInfo {
	int deepth;
	String last_node;
	ArrayList<String> num_xpathes;
	XpathInfo() {
		deepth = 0;
		last_node = "";
		num_xpathes = new ArrayList<String>();
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

class NoMatchXpath {
	HtmlXpath hx;
	String text;
	double similarity;
	double xpath_similarity;
	HtmlXpath match_hx;
	String match_text;
	public NoMatchXpath() {
		hx = new HtmlXpath();
		text = "";
		match_hx = new HtmlXpath();
		match_text = "";
		similarity = 0.0;
		xpath_similarity = 0.0;
	}
}


public class PageParser implements ContentHandler {
	

	HashMap<String, XpathInfo> attr_xpathes;
	HashMap<String, XpathInfo> num_xpathes;
	HashSet<String> num_xpathes_set;
	HashMap<String, StringBuffer> own_xpathes;
	HashMap<String, HashMap<String, StringBuffer>> candidate_xpathes;
	LinkedList<Element> path;
	StringBuffer full_xpath;
	LinkedList<PathNode> new_xpath;
	boolean is_webkit_page;
	ArrayList<NoMatchXpath> no_match_xpathes;
	boolean in_head;
	Context context;
	String[] mergeable_tag = {"li", "dt", "dd", "p", "span", "strong", "font", "a"};
	boolean is_abnormal_page;
	String[] used_tag = {"div", "table", "tr", "td", "ul"};
	HashSet<String> used_tag_set;
	HashMap<String, StringBuffer> xpath2text;
	HashMap<String, String> xpath2tbodyxpath;
	HashMap<String, String> tbodyxpath2xpath;
	int text_len_limit = 20000;
	static  String MatchTbodyXpath="match tbody xpath";
	
	PageParser() {
		attr_xpathes = new HashMap<String, XpathInfo>();
		num_xpathes = new HashMap<String, XpathInfo>();
		own_xpathes = new HashMap<String, StringBuffer>();
		candidate_xpathes = new HashMap<String, HashMap<String, StringBuffer> >();
		path = new LinkedList<Element>();
		full_xpath = new StringBuffer();
		new_xpath = new LinkedList<PathNode>();
		num_xpathes_set = new HashSet<String>();
		is_webkit_page = false;
		no_match_xpathes = null;
		in_head = false;
		is_abnormal_page = false;
		used_tag_set = new HashSet<String>();
		for (String tag: used_tag) {
			used_tag_set.add(tag);
		}
		xpath2text = new HashMap<String, StringBuffer>();
		xpath2tbodyxpath = new HashMap<String, String>();
		tbodyxpath2xpath = new HashMap<String, String>();
	}
	
	public void clear() {
	//	xpathes.clear();
		own_xpathes.clear();
		candidate_xpathes.clear();
		path.clear();
		full_xpath.setLength(0);
		full_xpath.trimToSize();
		new_xpath.clear();
		in_head = false;
		is_abnormal_page = false;
		xpath2text.clear();
	}
	
	public void setNoMatchXpath(ArrayList<NoMatchXpath> input) {
		no_match_xpathes = input;
	}
	
	public void setXpath(ArrayList<HtmlXpath> input_xpathes) {
		attr_xpathes.clear();
		num_xpathes.clear();
		num_xpathes_set.clear();
		no_match_xpathes = null;
		xpath2tbodyxpath.clear();
		tbodyxpath2xpath.clear();
		for (HtmlXpath hx: input_xpathes) {

			if (hx.use_num) {
				num_xpathes_set.add(hx.num_xpath);
				XpathInfo xi = num_xpathes.get(hx.class_xpath);
				if (xi != null) {
					xi.num_xpathes.add(hx.num_xpath);
				} else {
					xi = new XpathInfo();
					String[] segs = hx.class_xpath.split("/");
					xi.deepth = segs.length-1;
					xi.last_node = segs[segs.length-1];
					xi.num_xpathes.add(hx.num_xpath);
					num_xpathes.put(hx.class_xpath, xi);
				}
				
			} else {
				XpathInfo xi = new XpathInfo();
				String[] segs = hx.class_xpath.split("/");
				xi.deepth = segs.length-1;
				xi.last_node = segs[segs.length-1];
				attr_xpathes.put(hx.class_xpath, xi);
				
				String xpath = hx.class_xpath;
				if (xpath.indexOf("/tbody")>=0) {
					StringBuffer nx = new StringBuffer();
					int start = 0;
					int pos;
					while ((pos=xpath.indexOf("/tbody/", start))>=0) {
						nx.append(xpath.substring(start, pos));
						start = pos+"/tbody".length();
					}
					
					if (xpath.endsWith("/tbody")) {
						nx.append(xpath.substring(start, xpath.length()-"/tbody".length()));
					} else {
						nx.append(xpath.substring(start));
					}
				//	System.out.println(nx.toString());
					xpath2tbodyxpath.put(nx.toString(), xpath);
				}
			}
		}
	//	System.out.println(num_xpathes.containsKey("/html/body/center/table/tbody/tr/td[@class='bk']/table"));
	}
	
	public void setIsWebkit(boolean is_webkit) {
		this.is_webkit_page = is_webkit;
	}
	

	
	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
	}

	
	public void startDocument() throws SAXException {
		clear();
	}

	
	public void endDocument() throws SAXException {
		for (String tbodyxpath: tbodyxpath2xpath.keySet()) {
		//	System.out.println("hhh");
			HashMap<String, StringBuffer> temp2 = new HashMap<String, StringBuffer>();
			StringBuffer sb = new StringBuffer();
			sb.append(PageParser.MatchTbodyXpath);
			temp2.put(tbodyxpath2xpath.get(tbodyxpath), sb);
			candidate_xpathes.put(tbodyxpath, temp2);
		}
		HashMap<String, HashMap<String, StringBuffer> > temp = new HashMap<String, HashMap<String, StringBuffer> >();
		for (String xpath: candidate_xpathes.keySet()) {
			if (!own_xpathes.containsKey(xpath)) {
				temp.put(xpath, candidate_xpathes.get(xpath));
			}
		}
		candidate_xpathes.clear();
		candidate_xpathes = temp;
		/*
		if (no_match_xpathes != null && no_match_xpathes.size() > 0) {
			Iterator iter = xpath2text.entrySet().iterator();
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				String xpath_s = (String) entry.getKey();
				StringBuffer text = (StringBuffer) entry.getValue();
				for (NoMatchXpath nmx: no_match_xpathes) {
					if (nmx.text.length() > 0) {
						String nxpath = "";
						if (nmx.hx.use_num) {
							nxpath = nmx.hx.num_xpath;
						} else {
							nxpath = nmx.hx.class_xpath;
						}
						if (Math.abs(nmx.text.length()-text.length()) < 5000) {
							double temp1 = 0.0;
							if (nmx.text.length() < text_len_limit) {
								temp1 = PageSimilarityCalculator.minEditDistance2(nmx.text, text.toString());
							} else {
								temp1 = 1-(double)Math.abs(nmx.text.length()-text.length())/(double)nmx.text.length();
							}
							double temp2 = 0.0;
							
							temp2 =PageSimilarityCalculator.minEditDistance3(nxpath, xpath_s);
							if (xpath_s.indexOf("bordB")>=0) {
								System.out.println(xpath_s + "\t" + temp2 + "\t" + text);
							}
							if (temp1 > 0.1) {
								if (temp1 > nmx.similarity || (temp1 == nmx.similarity && temp2 > nmx.xpath_similarity)) {
									nmx.similarity = temp1;
									nmx.xpath_similarity = temp2;
									nmx.match_text = text.toString();
									nmx.match_hx.class_xpath = xpath_s;
								}
							}
						}
					} else if (text.length() == 0 && !nmx.hx.use_num){
						double temp1 = 0.0;
						temp1 =PageSimilarityCalculator.minEditDistance3(nmx.hx.class_xpath, xpath_s);
	
						if (temp1 > nmx.similarity && temp1 > 0.7) {
							nmx.similarity = temp1;
							nmx.match_hx.class_xpath = xpath_s;
						}
					}
				}
			}

		}
		*/
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
		if (full_xpath.length() > 2000 || path.size() > 100) {
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
		path.add(new_e);
		
		if (qName.equalsIgnoreCase("head")) {
			in_head = true;
		}
	}

	
	private void setCandidate(String used_xpath, String candidate_xpath, Element e) {
		HashMap<String, StringBuffer> temp =  candidate_xpathes.get(used_xpath);
		if (temp != null) {
			StringBuffer sb = temp.get(candidate_xpath);
			if (sb != null) {
				sb.append(e.text);
			} else {
				sb = new StringBuffer();
				sb.append(e.text);
				temp.put(candidate_xpath, sb);
			}
		} else {
			temp = new HashMap<String, StringBuffer>();
			StringBuffer sb = new StringBuffer();
			sb.append(e.text);
			temp.put(candidate_xpath, sb);
			candidate_xpathes.put(used_xpath, temp);
		}
	}
	
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		
		Element e = path.peekLast();
	//	System.out.println(qName);
		if (qName.equalsIgnoreCase(e.name)) {
			path.pollLast();

			if (!qName.equalsIgnoreCase("script") && !qName.equalsIgnoreCase("style")) {
				Element parent = path.peekLast();
				if (parent != null) {
					parent.text.append(e.text);
					if (e.text.length() > text_len_limit) {
						parent.child_is_big = true;
					}
				}
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
			//	System.out.println(num_xpath_s);
			//	if (xpath_s.endsWith("class='pct']"))
			//		System.out.println(xpath_s + "\t" + e.text);
				if (is_webkit_page) {
					String used_xpath = "";
					if (attr_xpathes.containsKey(xpath_s)) {
						//System.out.println("here");
						used_xpath = xpath_s;
					} else if (num_xpathes_set.contains(num_xpath_s)) {
						//System.out.println(num_xpath_s);
						used_xpath = num_xpath_s;
					}
					if (used_xpath.length() > 0) {
						StringBuffer sb = own_xpathes.get(used_xpath);
						if (sb != null) {
							sb.append(e.text);
						} else {
							sb = new StringBuffer();
							sb.append(e.text);
							own_xpathes.put(used_xpath, sb);
						}
					}
				} else {
					if (no_match_xpathes == null || no_match_xpathes.size() == 0) {
				//	System.out.println(xpath_s);
						
						if (xpath2tbodyxpath.containsKey(xpath_s)) {
							tbodyxpath2xpath.put(xpath2tbodyxpath.get(xpath_s), xpath_s);
						}
						
						if (attr_xpathes.containsKey(xpath_s)) {
							StringBuffer sb = own_xpathes.get(xpath_s);
							if (sb != null) {
								sb.append(e.text);
							} else {
								sb = new StringBuffer();
								sb.append(e.text);
								own_xpathes.put(xpath_s, sb);
							}
						//	System.out.println("hhh "+xpath_s);
						} else {
					//		System.out.println(xpath_s);
					//		String used_xpath = "";
							String candidate_xpath = "";
							if (num_xpathes.containsKey(xpath_s)) {
								
							//	used_xpath = num_xpathes.get(xpath_s).html_xpath.num_xpath;
								candidate_xpath = num_xpath_s;
								for (String used_xpath: num_xpathes.get(xpath_s).num_xpathes) {
									setCandidate(used_xpath, candidate_xpath, e);
								}
							//	System.out.println("0:  " + xpath_s + "\t" + used_xpath);
							} else {
								Iterator iter = num_xpathes.entrySet().iterator();
								while (iter.hasNext()) {
									Entry entry = (Entry) iter.next();
									String xpath = (String) entry.getKey();
									XpathInfo xi = (XpathInfo) entry.getValue();

									if (e.full_name.equalsIgnoreCase(xi.last_node) ) {
										String[] t_segs = xpath.split("table");
										int depth_diff = 3;
										if (t_segs.length-1 > depth_diff) {
											depth_diff = t_segs.length-1;
										}
								//		System.out.println(full_xpath + "\t" + path.size() + "\t" + xi.deepth);
										if (Math.abs(path.size()+1-xi.deepth) <= depth_diff) {
										//	System.out.println(num_xpath_s);
										//	used_xpath = xi.html_xpath.num_xpath;
											candidate_xpath = num_xpath_s;
											for (String used_xpath: xi.num_xpathes) {
												setCandidate(used_xpath, candidate_xpath, e);
											}
										}
									}
									
								}
							}
							
							String used_xpath = "";
							candidate_xpath = "";
							Iterator iter = attr_xpathes.entrySet().iterator();
							while (iter.hasNext()) {
								Entry entry = (Entry) iter.next();
								String xpath = (String) entry.getKey();
								if (!own_xpathes.containsKey(xpath)) {
									
									XpathInfo xi = (XpathInfo) entry.getValue();
									//System.out.println(xi.last_node);
									String[] t_segs = xpath.split("table");
									int depth_diff = 3;
									if (t_segs.length-1 > depth_diff) {
										depth_diff = t_segs.length-1;
									}
									if (e.full_name.equalsIgnoreCase(xi.last_node) ) {
								//		System.out.println(full_xpath + "\t" + path.size() + "\t" + xi.deepth);
										if (Math.abs(path.size()+1-xi.deepth) <= depth_diff) {
											used_xpath = xpath;
											candidate_xpath = xpath_s;
											setCandidate(used_xpath, candidate_xpath, e);
										}
									}
								}
							}
						}

					} else if(!xpath_s.startsWith("/html/head")) {
				//		System.out.println("here.");
						if (!e.child_is_big && used_tag_set.contains(qName.toLowerCase())) {
							StringBuffer sb = xpath2text.get(xpath_s);
							if (sb != null) {
								sb.append(e.text);
							} else {
								sb = new StringBuffer();
								sb.append(e.text);
								xpath2text.put(xpath_s, sb);
							}
						}
						/*
						boolean flag = false;
						for (String mt: mergeable_tag) {
							if (qName.equalsIgnoreCase(mt)) {
								flag = true;
								break;
							}
						}
						if (!flag) {
							for (NoMatchXpath nmx: no_match_xpathes) {
								if (nmx.text.length() > 0) {
									
									if (Math.abs(nmx.text.length()-e.text.length()) < 2000) {
										double temp = 0.0;
										if (nmx.text.length() < text_len_limit) {
											temp = PageSimilarityCalculator.minEditDistance3(nmx.text, e.text.toString());
										} else {
											temp = 1-(double)Math.abs(nmx.text.length()-e.text.length())/(double)nmx.text.length();
										}
										double temp2 = 0.0;
										if (!nmx.hx.use_num) {
											temp2 =PageSimilarityCalculator.minEditDistance3(nmx.hx.class_xpath, xpath_s);
										} else {
											temp2 =PageSimilarityCalculator.minEditDistance3(nmx.hx.num_xpath, num_xpath_s);
										}
									//	System.out.println(xpath_s + "\t" + e.text);
									//	if (xpath_s.equalsIgnoreCase("/html/body/div[@id='doc']/div[@class='content-wrap']/div[@id='content' and @class='page-offerdetail page-content-sub']/div[@class='segment-box']/div[@id='site_content' and @class='segment layout layout-s5m0']/div[@class='grid-main']/div[@class='main-wrap region region-type-big']/div[@class='mod mod-offerDetailContext1 app-offerDetailContext1 mod-ui-not-show-title']/div[@class='m-body']/div[@class='m-content']/div[@id='J_DetailInside' and @class='detail-inside area-detail-property']/div[@id='mod-detail' and @class='mod-detail']/div[@id='mod-detail-bd' and @class='mod-detail-bd']/div[@class='region-custom region-detail-property region-blank ui-sortable region-vertical']")) {
									//		System.out.println(temp + "\t" + e.text);
									//	}
									//	System.out.println(xpath_s + "\t" +temp + "\t" +  e.text);
										if (temp > 0.5) {
											System.out.println(xpath_s + "\t" +temp + "\t" +  e.text);
										//	System.out.println(nmx.hx.class_xpath + "\t" + xpath_s + "\t" + temp + "\t" +  nmx.text + "\t" + e.text.toString());
											if (context != null) {
												try {
													context.write(new Text(nmx.hx.class_xpath + "\t" + xpath_s + "\t" + temp + "\t" +  nmx.text + "\t" + e.text.toString()), null);
												} catch (IOException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												} catch (InterruptedException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
											}
											if (temp > nmx.similarity || (temp == nmx.similarity && temp2 > nmx.xpath_similarity)) {
												nmx.similarity = temp;
												nmx.xpath_similarity = temp2;
												//if (e.text.length() < 20000)
												nmx.match_text = e.text.toString();
												nmx.match_hx.class_xpath = xpath_s;
												if (nmx.hx.use_num) {
													nmx.match_hx.use_num = true;
													nmx.match_hx.num_xpath = num_xpath_s;
												}
											}
										}
									}
								} else {
									double temp = 0.0;
									if (!nmx.hx.use_num) {
										temp =PageSimilarityCalculator.minEditDistance3(nmx.hx.class_xpath, xpath_s);
									} else {
										temp =PageSimilarityCalculator.minEditDistance3(nmx.hx.num_xpath, num_xpath_s);
									}
									if (temp > nmx.similarity && temp > 0.7) {
										nmx.similarity = temp;
										nmx.match_hx.class_xpath = xpath_s;
										if (nmx.hx.use_num) {
											nmx.match_hx.use_num = true;
											nmx.match_hx.num_xpath = num_xpath_s;
										}
									}
								}
							}
						}
						*/
					}
				}

			}
			full_xpath.delete(full_xpath.lastIndexOf("/"), full_xpath.length());
			new_xpath.pollLast();
		}
		if (qName.equalsIgnoreCase("head")) {
			in_head = false;
		}
	}

	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		Element element = path.peekLast();
		if (element.name.equalsIgnoreCase("script") || element.name.equalsIgnoreCase("style")) {
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
		String test_xpath = "78u7y5/html/body/div[@class='wenzhang']/div[@class='cc']/div[@class='content_con_z']/ul[@class='tent_con']/li[starts-with(@class, 'tent_con_l')]/div[starts-with(@class, 'div')]/table[starts-with(@class, 'tb')]/tbody/tr/td/a";
		FileInputStream f_stream = new FileInputStream(new File("t.html"));
		byte[] bs = new byte[1024*1024*4];
		int len = f_stream.read(bs);
		String s = new String(bs, 0, len, "UTF-8");
		
		f_stream = new FileInputStream(new File("t2.html"));
		bs = new byte[1024*1024*4];
		len = f_stream.read(bs);
		String s2 = new String(bs, 0, len, "UTF-8");
		
		f_stream = new FileInputStream(new File("t.txt"));
		bs = new byte[1024*1024*4];
		len = f_stream.read(bs);
		String s3 = new String(bs, 0, len, "UTF-8");
		
		PageParser		htmlContentHandler		= new PageParser();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		//try {
			ArrayList<HtmlXpath> xpathes = new ArrayList<HtmlXpath>();
			HtmlXpath hx = new HtmlXpath();
			hx.fromString(test_xpath);
			xpathes.add(hx);
			htmlContentHandler.setXpath(xpathes);
			htmlContentHandler.setIsWebkit(true);
			parser.parse(new InputSource(new StringReader(s)));
		//	System.out.println(htmlContentHandler.own_xpathes.size());
			StringBuffer temp = (htmlContentHandler.own_xpathes.get(hx.class_xpath));
			String text1 = "";
			if (temp != null) {
				text1 = temp.toString();
			}
			System.out.println(text1);
			htmlContentHandler.setXpath(xpathes);
			
			ArrayList<NoMatchXpath> no_match_xpathes = new ArrayList<NoMatchXpath>();
			NoMatchXpath nmx = new NoMatchXpath();
			HtmlXpath nhx = new HtmlXpath();
			nhx.class_xpath = "/html/body/div[@class='luntan']/div[@class='huiFu area']/div[@class='right']";//"/html/body/div[@id='doc']/div[@class='content-wrap']/div[@id='content' and @class='page-offerdetail page-content-sub']/div[@class='segment-box segment-box-fluid']/div[@class='segment layout layout-fluid']/div[@class='region region-type-fluid']/div[@class='mod mod-offerDetailContext1 app-offerDetailContext1 mod-ui-not-show-title']/div[@class='m-body']/div[@class='m-content']/div[@id='J_DetailInside' and @class='detail-inside detail-full-column area-detail-property']/div[@id='mod-detail' and @class='mod-detail']/div[@class='grid-full grid-full-bd']/div[@id='mod-detail-bd' and @class='mod-detail-bd']/div[@class='region-custom region-detail-property region-takla ui-sortable region-vertical']";			
		//	nhx.num_xpath = "/html/body/center/table/tbody/tr/td/table[1]/tbody[2]";
			nhx.use_num = false;
			nmx.hx = nhx;
			nmx.text = "发表于2012-12-0909:42:49|只看该作者2楼吼吼，看到你已经开始灭杀运动了，希望你家的小强统统被小屋子收剿。非常高兴看到你的第一篇文字，还有两个团友在之前就报名预约，不知道灭杀效果如何。 我的体验是，几年前在老房子住的时候，我们在药剂附近要放一点黄瓜，最好是粘了香油的黄瓜，这也是我们同事间交流的心得。 还有，特恶心的说一句，你家的小强会飞吗？如果是，那这个小屋子肯定管用，有一种说法，会飞的小强的是（学名）德国蠊，是藏匿在包装箱中。如何识别德国镰，要看你家的厨房瓷砖墙缝中有没有小黑点，就是像小黑芝麻那种的点点。 继续关注你的灭杀第二章，已经向部分团友报告您回北京的消息，口碑老好了。    ①：英国产《宝汀顿》24听500ml装，每箱400元。②：英国产《纽卡索》24听500ml装，每箱380元。③：德国产《艾丁格》24瓶500ml装，两箱400元。以下货品，诚意出让，言无二价，谢谢！特别推出：原装木箱moutoncadet《小绵羊》2008年份，3支装500元。引用回复发表于2012-12-0910:04:40|只看该作者3楼灭杀必须是全楼一起行动这个必须说明一下。 几年前我家居住那个宿舍大院，n多n多的家有蟑螂活动迹象，楼上楼下草木皆兵几经排查挖出了根源，源于一位老妈妈贪小便宜从非洲带回的某、某食品，她家的蟑螂毫不夸张的说，冰箱一开门密匝匝的像撒了一瓶酱油，黑乎乎的一团，她历经考验满不在乎，还大肆说见过的如何多、如何大，不当回事，我们怕怕。 有一次吃饭聚会，听协和的大夫讲，老楼中地下室蟑螂如何活跃，绘声绘色的吓人，饭桌上吃一口想吐一口，因为这个楼的历史一追溯就到了解放前。再后来听说蟑螂的恶效，可以带来n多疾病，噗通，让我们准备活到120岁的信念大大受挫，于是家家开始找秘方、找灭蟑大法。 我们使用过的，效果最好的，当然是溴氰菊酯喷剂，三层楼几个单元集体行动，大院里空前的齐心，就是为了赶尽杀绝，因为那个时候听说那个最最吓人的病，就是小强转播的。 那几天家家都不开伙做饭，不回家睡觉，都院里吃食堂、睡机关。因为农药的味把家里熏的难以入食、入眠，最后老同志站出来说话了，又是科学、又是团结、又是。。。。，一句话，味道太重了，肯定对身体不好，好几天熏的脑仁疼。为了邻里的大局观，为了尊重老领导，那年月，算了，忍了，大家四下找货源。如果坚定使用溴氰菊酯的，在杀蟑的同时，注意院里四周的管道口边，一定有死去歪倒的老鼠，那个农药太牛掰了。。。。难忘~~~ 后来我们开始了排查寻找，当年新源里有一家小店竟然有进口的小蟑螂屋，售价80多元，说实话当时真不舍得花这个钱，到一想到生命攸关的，四处咨询，问了协和医院的几个人，说友谊商店曾卖过，我们又杀到友商，说早就不进货了，最后还得拐着弯求助驻外的大哥老姐。 我的体会是，小屋子排杀德国蠊还好，排杀其他的效果一般，还有那种白色的超级怪异不知道名字的。 灭杀要多家一起行动，可以肯定的说，你家现在有蟑螂，邻居家绝对也有，不是一个平层，是一串式的，楼上楼下串着，小强行动的路线是上下搭、上下楼窜，记住：第一，厨房的管道一定要严防死守；第二，夜里一点多之后要拉网式排查、排杀，轻手轻脚，猛然开灯，绝对是目瞪口呆的一景。 私下可以问问周围的邻居家发现没有，一旦有，即使几天没有活跃的迹象，潜伏期非常可怕，一出现蟑螂，后果就是碎了瓶的酱油，黑乎乎的一片。          本帖于2012年12月09日11时30分被o洋巴掌o修改!①：英国产《宝汀顿》24听500ml装，每箱400元。②：英国产《纽卡索》24听500ml装，每箱380元。③：德国产《艾丁格》24瓶500ml装，两箱400元。以下货品，诚意出让，言无二价，谢谢！特别推出：原装木箱moutoncadet《小绵羊》2008年份，3支装500元。引用回复发表于2012-12-0910:13:27|只看该作者4楼德国蠊还是日本蟑？话说当年俺来京城，认识了两个物种，一是白萝卜，另一就是蟑螂。看到白萝卜，俺太惊讶了，竟然还有长得这么硕大粗笨的萝卜，和那心里美或青萝卜怎能相提并论？至于蟑螂，学长介绍，大个儿会飞的来自德国，小个儿的来自日本。说句实话，昨天我还在想，你的蟑螂屋貌似汉高产品，是否对症我家疑似的日本蟑？不过转念一想，中国人也有爱吃奶酪的，老外也有爱吃豆腐的，所以，拭目以待！引用回复发表于2012-12-0910:17:43|只看该作者5楼你说的粘虫纸我也用过，学名不是这个哈我们昵称【召虫纸】。 还有那声波的召唤，竟然能召来吓死人的老鼠，勾起我难忘的一幕。 不多说了，你看看家里的蟑螂会飞吗？这个据说是最简易的排查，非常不中听的说一句，会飞的才好杀除，不飞的还得加药。 再补充一句，不飞的只有冷冻-40，对这个学说我一直深表怀疑。 非常同情你的蟑螂遭遇战，我们一同科普吧，如果真的管用，继续为你家免费提供药剂药饵。 另，倒时差是一门技术，体验太多太深，困的不行也不睡，坚持坚持，喝酒喝咖啡赌钱打牌，强撑硬挺就是不睡，还有困倦中的浸入式（窗你，文明用语，以免围观者误解）入睡最管用。 预祝灭杀第一战役告捷，顺祝xv老师好，wanwan小朋友好。            ①：英国产《宝汀顿》24听500ml装，每箱400元。②：英国产《纽卡索》24听500ml装，每箱380元。③：德国产《艾丁格》24瓶500ml装，两箱400元。以下货品，诚意出让，言无二价，谢谢！特别推出：原装木箱moutoncadet《小绵羊》2008年份，3支装500元。引用回复发表于2012-12-0910:20:50|只看该作者6楼文字真俏皮，我们还是注重实效吧上班我再给你找找另一个。 社区发的那种药面似的，不管用。 追补：杀就杀绝，深有体会，一旦反扑，药还得上量！！    ①：英国产《宝汀顿》24听500ml装，每箱400元。②：英国产《纽卡索》24听500ml装，每箱380元。③：德国产《艾丁格》24瓶500ml装，两箱400元。以下货品，诚意出让，言无二价，谢谢！特别推出：原装木箱moutoncadet《小绵羊》2008年份，3支装500元。引用回复发表于2012-12-0911:16:40|只看该作者7楼回来了？一回来就有动静啦？一家有蟑螂，全楼都有的。所以单灭自家是不够的。一般隔几个月就要查杀一次，以免卷土重来。我习惯有灭虫公司。学校、家长，究竟在教育中做什么为什么要学国学——突然的一点感悟字里乾坤大——建议慎重对待识字班有感于女儿的坚持为她作周年书法练习记再谈孩子的动手能力与家长的作用幼儿阅读从绘本开始母女俩都不容易的抉择女儿是这样“被上”幼儿园的让我们家长来给自己的孩子寻找合适的活动吧关于女儿的富养从女儿对换老师的不同态度看女儿自主判断意识的加强由这没完没了的做足球想到的：不过，让孩子做个丑足球又何妨显示全部签名引用回复发表于2012-12-0918:31:20|只看该作者8楼我在晴雪的小超市里买蟑螂屋，很好使。粘住蟑螂，一般都逃不掉的……(无正文)引用回复发表于2012-12-0918:41:58|只看该作者9楼你回来了？团过好多次的小泰监据说不错，城里有很多拥趸者(无正文)莺莺燕燕春春,花花柳柳真真,事事风风韵韵,娇娇嫩嫩,停停当当人人。引用回复发表于2012-12-0919:29:04|只看该作者10楼记下来了。谢谢！引用回复";
			
			no_match_xpathes.add(nmx);
			
		//	htmlContentHandler.setNoMatchXpath(no_match_xpathes);
			htmlContentHandler.setIsWebkit(false);
			parser.parse(new InputSource(new StringReader(s2)));
			/*
			for (NoMatchXpath t: no_match_xpathes) {
				System.out.println(t.similarity + "\t" +t.match_hx.class_xpath+ "\t" + t.match_text);

			}
			*/
			
			HashMap<String, StringBuffer> can = htmlContentHandler.candidate_xpathes.get(hx.class_xpath);
		//	String textt = (htmlContentHandler.own_xpathes.get(test_xpath)).toString();
		//	System.out.println(textt);
			double max_similar = 0.0;
			String key = "";
			int depth = 0;
			String val = "";
			Iterator iter = can.entrySet().iterator();
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				String xpath = (String) entry.getKey();
				
				StringBuffer text2 = (StringBuffer) entry.getValue();
			
			//	System.out.println(text1);
			//	System.out.println(text2);
			//	System.out.println("here1: " + text2);
				double t = PageSimilarityCalculator.minEditDistance(text1, text2.toString());
				System.out.println(xpath + "\t" + t + "\t" +  text2);
			//	System.out.println("here2: " + text2);
				if (t > max_similar) {
					max_similar = t;
					key = xpath;
					val = text2.toString();
					String[] segs = key.split("/");
					depth = segs.length;
				} else if (Math.abs(t-max_similar)<0.000001) {
					
					String[] segs = xpath.split("/");
					//System.out.println(key + "\t" + depth + "\t" + segs.length);
					if (segs.length < depth) {
						key = xpath;
						val = text2.toString();
						depth = segs.length;
					}
					
				}
			}
			System.out.println(max_similar + "\t" + key + "\n" + val);
			


	}
} 

