package com.sogou.cm.pa.maincontent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sogou.web.selector.urllib.URLUtils;

public class RuleBasedMainContentExtractor {
	
	HtmlContentHandler		htmlContentHandler;
	Parser					parser;
	List<Element>			blocks;
	List<Element>			main_blocks;
	Element sidebar_textblock;
	public HtmlPage html_page;
	String url;
	String domain;
	String[] bottom_block_keyword = {"copyright", "关于我们", "联系我们", "友情链接", "©", "免责声明", "网站地图", "风险提示"};
	String[] sidebar_attr = {"bottom"};
	String[] sideblock_keyword = {"更多", "热门", "相关", "其他"};
	HashSet<String> sidekeywords;
	HashSet<String> video_site;
	
	public RuleBasedMainContentExtractor() {
		htmlContentHandler		= new HtmlContentHandler();
		parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		
		blocks = new ArrayList<Element>();
		main_blocks = new ArrayList<Element>();
		url = "";
		sidekeywords = new HashSet<String>();
		for (String s: sideblock_keyword) {
			sidekeywords.add(s);
		}
		/*
		video_site = new HashSet<String>();
		video_site.add("iqiyi.com");
		video_site.add("pps.tv");
		video_site.add("sohu.com");
		video_site.add("17173.com");
		video_site.add("letv.com");
		video_site.add("youku.com");
		video_site.add("tudou.com");
		video_site.add("cntv.cn");
		*/
	}
	
	private void clear() {
		blocks.clear();
		main_blocks.clear();
		sidebar_textblock = null;
	}
	
	private void setBlockType() {
		HashSet<Element> element_set = new HashSet<Element>();
		for (ArrayList<Element> tes: htmlContentHandler.type_blocks.values()) {
			for (Element te: tes)
				element_set.add(te);
		}
		ArrayList<Element> temp = new ArrayList<Element>();
		for (Element e: blocks) {
			if (!element_set.contains(e)) {
				// e.block_type = 1;

				if (e.is_video) {
					e.block_type = 8;
				//	temp.add(e);
				} else if (html_page.is_hub2 && html_page.type != 2 && html_page.type != 3 && !(html_page.url.startsWith("http://t.qq.com/") || html_page.url.startsWith("http://weibo.com/") || html_page.url.startsWith("http://www.douban.com/group/topic/"))) {
					if (e.block_type != 10 && !(e.type == 0 && e.text_len > 100 && e.top < 400 && e.width > 400 && !e.is_list))
						e.block_type = 9;
				//	temp.add(e);
				} else if (UrlUtils.urlIsHomepage(url)) {
					if (e.type == 1 || e.is_list || e.anchor_len*2>e.text_len || e.text_len < 10) {
						continue;
					}
					String attr = e.class_attr + e.id;
					if (!e.is_sidebar && attr.toLowerCase().indexOf("sidebar") >= 0 && (e.left+e.width < 450 || e.left > 1000)) {
						e.is_sidebar = true;
						continue;
					}
				}
				e.is_maincontent = true;
				temp.add(e);
			}
		}
		blocks.clear();
		for (Element e: temp) {
			blocks.add(e);
		}
		
		for (Integer block_type: htmlContentHandler.type_blocks.keySet()) {
		//	System.out.println(htmlContentHandler.type_blocks.get(block_type).block_type + "\t" + htmlContentHandler.type_blocks.get(block_type).full_xpath);
			if (block_type == 14 || block_type == 15) {
				for (Element e: htmlContentHandler.type_blocks.get(block_type)) {
					for (Element e2: temp) {
						if (e.full_xpath.startsWith(e2.full_xpath)) {
							blocks.add(e);
							break;
						}
					}
				}
			} else {
				blocks.addAll(htmlContentHandler.type_blocks.get(block_type));
			}
		}
	}
	
	public List<Element> extractMainContent(String input_url, String html) {
		extractAllBlock(input_url, html);
		List<Element> mcs = new ArrayList<Element>();
		for (Element e: blocks) {
			if (e.is_maincontent) {
				mcs.add(e);
			}
		}
		return mcs;
	}
	
	public List<Element> extractAllBlock(String input_url, String html) {
		clear();
		html_page = new HtmlPage();
		url = input_url;
		domain = URLUtils.getDomain(url);
		html_page.url = input_url;
		try {
		//	System.out.println(html.length());
			htmlContentHandler.setUrl(url);
			parser.parse(new InputSource(new StringReader(html)));
			blocks = htmlContentHandler.blocks;
			html_page.content_title = htmlContentHandler.content_title;
			html_page.title = htmlContentHandler.title;
			getPageType();
			removeSideAd();
			
			removeTopAndBottomBlocks();
			
			if (UrlUtils.urlIsHomepage(url)) {
				setBlockType();
				return blocks;
			}
			
			removeMiddleAd();
			
			blockCluster();
			
			Tool.quickSortY(blocks, 0, blocks.size()-1);
		//	recoginzeSingleBigBlock();
			
			filterBlockByMcLimit();
			recognizeHubPage();
			Tool.quickSortY(blocks, 0, blocks.size()-1);
			
			
			if (!html_page.has_single_big_mainblock) {
				removeLinkBlock();
				removeCommentBlock();
				removeSidebar();
				removeOtherBlock();
				filterMore();
			}
			

			
			setBlockType();
			
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return blocks;
	//	return main_blocks;
	}
	
	private void filterBlockByMcLimit() {
		if (htmlContentHandler.mc_limit <= 0) {
			return;
		}
	//	System.out.println(htmlContentHandler.mc_limit);
		ArrayList<Element> temp = new ArrayList<Element>();
		for (Element e: blocks) {
			if (e.top >= htmlContentHandler.mc_limit-10) {
				break;
			}
			temp.add(e);
		}
		blocks = temp;
	}
	
	private void getPageType() {
		//boolean has_video_block = false;
		boolean no_text_block = false;
		for (Element e: blocks) {
		//	System.out.println(e.is_video);
			if (e.top < 1000 && (e.text_len-e.anchor_len)>200 && e.text_len>e.anchor_len*2 && e.max_text_len > 30) {
				int center = e.left + e.width/2;
				if(center > 500 && center < 900) {
				//	System.out.println(e.full_xpath + "\t" + e.text_len);
					no_text_block = true;
				}
			}
		}
	//	System.out.println( htmlContentHandler.has_video_block);
		if (!no_text_block && htmlContentHandler.has_video_block) {
			html_page.type = 1;
		//	System.out.println("hhh");
			return;
		}
		if (html_page.title.indexOf("最新章节")>=0 || html_page.title.indexOf("全文阅读")>=0) {
			//System.out.println(url);
			html_page.type = 2;
			return;
		}
		html_page.type = 0;
	}
	
	private void removeSideAd() {
		ArrayList<Element> temp = new ArrayList<Element>();
		int left = 10000;
		int right = 0;
		for (Element e : blocks) {
			int bottom = e.top + e.height;
			if (bottom > html_page.height) {
				html_page.height = bottom;
			}
			if (e.left > 1200) {
				continue;
			}
			if ((e.left < 50 || e.left+e.width > 1350) && e.width * e.height < 70000 && (e.anchor_len*2>e.text_len|| e.text_len < 5)) {
				continue;
			}
			if (e.left + e.width < 300 && e.width * e.height < 40000) {
				continue;
			}
			if (e.width * e.height < 10000 && !e.is_video) {
				continue;
			}
			if (e.width > 1440 && e.height < 100) {
				continue;
			}
			if (e.width < 1300) {
				if (left > e.left) {
					left = e.left;
				}
				if (right < e.left + e.width) {
					right = e.left + e.width;
				}
			}
			temp.add(e);
		}
		if (right > left) {
			html_page.width = right - left;
		}
		blocks = temp;
	}
	
	private void removeMiddleAd() {
		if (html_page.multi_column_page) {
			int ad_width = html_page.width - 150;
			if (ad_width < 880) {
				ad_width = 880;
			}
			List<Element> temp = new ArrayList<Element>();
			for (int i = 0; i < blocks.size(); ++i) {
				Element e = blocks.get(i);
				if (e.height <= 150 && e.width > ad_width && (e.text_len-e.anchor_len)<5) {
					;
				} else {
					temp.add(e);
				}
			}
			blocks.clear();
			blocks = temp;
		}
	}
	
	private void removeCommentBlock() {
		if (html_page.title.indexOf("评论") >= 0 || html_page.title.indexOf("点评") >= 0) {
			for (int i = 0; i < blocks.size(); ++i) {
				Element e = blocks.get(i);
			//	System.out.println(e.xpath + "\t" + e.text_area + "\t" + e.width * e.height + "\t" + e.text);
				if (i > 0 && e.text_area * 4 > e.width * e.height && e.text_len < 100
						&& e.width > e.height && e.height < 400 && e.top > 200
						&& e.text_area > 20000) {
					blocks.remove(i);
					return;
				}
			}
			return;
		}
		int comment_index = -1;
		for (int i = 0; i < blocks.size(); ++i) {
			Element e = blocks.get(i);
		//	System.out.println(e.xpath + "\t" + e.text_area + "\t" + e.width * e.height + "\t" + e.text);
			if (i > 0 && e.text_area * 4 > e.width * e.height && e.text_len < 100
					&& e.width > e.height && e.height < 400 && e.top > 200
					&& e.text_area > 20000) {
				comment_index = i;
				break;
			}
			
			int t = e.text.indexOf("评");
			int t2 = e.text.indexOf("论", t);
			if ( ((t2 >= 0 && t2 <= 3) || e.text.indexOf("点评") == 0) && e.ids_classes.toString().toLowerCase().indexOf("comm")>=0) {
				if (i>=2 && (e.top > 900 || (html_page.height - e.top) * 2 < html_page.height)) {
					comment_index = i;
					break;
				}
			}
		//	System.out.println(i + "\t" +e.ids_classes.toString().toLowerCase() + "\t" + e.text);
			t = e.text.indexOf("评论");
			if (t > 10 || t < 0) {
				t = e.text.indexOf("短评");
			}
			if (t >= 0 && t <= 10 && e.ids_classes.toString().toLowerCase().indexOf("comment")>=0) {
				if (i>=2 && e.top > 900 && e.text_len < 20) {
			//		System.out.println("here.");
					comment_index = i;
					break;
				}
			} 
			if (t >= 0 && t <= 10 && e.top > 2000 && i >= 2) {
				String temp = e.class_attr + e.id;
				if (temp.toLowerCase().indexOf("comment")>= 0) {
					comment_index = i;
					break;
				}
			}

			
			
			if ((e.text.indexOf("上一") >= 0 && e.text.indexOf("下一")>=0) || (e.text.indexOf("前一") >= 0 && e.text.indexOf("后一")>=0)) {
				if (i >= 2 && e.anchor_len*2 > e.text_len && e.text_len < 100 && e.height < 200 && e.top > 800) {
					comment_index = i;
					break;
				}
			}
		}
		if (comment_index < 0) {
			return;
		}
		Element comment = blocks.get(comment_index);
	//	System.out.println(comment.text + "\t" + comment.full_xpath);
		if (comment_index >= 0) {
			if (html_page.url.indexOf("bbs")<0 && html_page.url.indexOf("thread-")<0 && html_page.url.indexOf("viewthread")<0) {
				int i = comment_index-1;
				for (; i >= 0; --i) {
					Element e = blocks.get(i);
					if (e.top + e.height + 20 > comment.top) {
						if (e.height < 50) {
							continue;
						}
						if (e.top + e.height > comment.top && e.left >= comment.left + comment.width) {
							continue;
						}
						break;
					} else {
						break;
					}
				}
				List<Element> temp = blocks.subList(0, i+1);
				blocks = temp;
			} else {
				blocks.remove(comment_index);
			}
		}
	}
	
	private void filterMore() {
		ArrayList<Element> temp = new ArrayList<Element>();
		for (int i = blocks.size()-1; i >= 0; --i) {
			Element e = blocks.get(i);
		//	System.out.println(html_page.height + "\t" + e.top + "\t" + e.text);
			if (i > 0 && html_page.height - e.top < 350) {
				boolean flag = false;
				for (String s : bottom_block_keyword) {
					if (e.text.indexOf(s) >= 0) {
						flag = true;
						break;
					}
				}
				if (flag) {
					continue;
				}
			}
			if (i>=2 && e.top > 900 && e.height < 60 && e.text_len <= 10) {
				continue;
			}
			if (i>=2 && e.top > 600 && e.height < 60 && e.text_len <= 5) {
				continue;
			}
			String id_class = e.id + e.class_attr;
		//	System.out.println(e.top+e.height + 100  + "\t" + html_page.height);
			if (e.top+e.height + 100 > html_page.height && id_class.indexOf("foot") >= 0) {
				 continue;
			}
			temp.add(e);
		}
		blocks = temp;
	}
	
	private void removeSidebar() {
		
		ArrayList<Element> temp = new ArrayList<Element>();
		for (int k = 0; k < blocks.size(); ++k) {
			Element e = blocks.get(k);
		//	System.out.println(e.is_sidebar + "\t" + e.text);
		//	System.out.println(e.full_xpath + "\t" + e.text);

			if (e.text.length() > 2 && sidekeywords.contains(e.text.toString().substring(0, 2)) && e.text_len < 15 && e.height <= 60) {
			//	System.out.println("here: " + e.text);
				e.is_sidebar = true;
				for (int t = k+1; t < blocks.size(); ++t) {
					Element e_next = blocks.get(t);
			//		System.out.println(e_next.top + "\t" + e.text);
					if (e_next.top > e.top + e.height + 20) {
						break;
					}
					
					String prefix = e_next.full_xpath.substring(0, e_next.full_xpath.lastIndexOf("/"));
					if (e.full_xpath.startsWith(prefix)) {
				//		System.out.println("here: " + e_next.full_xpath);
						if (e_next.type == 1) {
							e_next.is_sidebar = true;
						}
					}
				}
			}
			
			
			if (e.block_title.length() > 2 && (sidekeywords.contains(e.block_title.toString().substring(0, 2)) || e.block_title.endsWith("推荐"))
					&& e.anchor_len*2>e.text_len && e.text_len < 200 && temp.size() > 0) {
				e.is_sidebar = true;
			}
			
			String attr = e.class_attr + e.id;
			if (attr.toLowerCase().indexOf("recommend") >= 0 && e.anchor_len > 0 && (e.top > 1000 || e.left > 800)) {
				e.is_sidebar = true;
			}
			
		//	System.out.println(e.full_xpath + "\t" + e.text);
			if (!e.is_sidebar && attr.toLowerCase().indexOf("sidebar") >= 0 && (e.left+e.width < 450 || e.left > 1000)) {
				e.is_sidebar = true;
			//	System.out.println("hhh");
			}

			
			if (!e.is_sidebar && e.left > 800 && e.width < 350) {
				String s = e.id + e.class_attr;
				if (s.toLowerCase().indexOf("side")>= 0 && e != sidebar_textblock) {
					e.is_sidebar = true;
				}
			}
			
			if (e.is_sidebar) {
				continue;
			}
			temp.add(e);
		}
		blocks.clear();
		blocks = temp;
	}
	
	private void removeOtherBlock() {
		ArrayList<Element> temp = new ArrayList<Element>();
		Element last_e = null;
		Element last_max_e = null;
		String[] sideblock_keyword = {"更多", "热门", "相关", "其他"};
		HashMap<String ,Integer> form2num = new HashMap<String, Integer>();
		
		int top_limit = -1;
		ArrayList<Element> disclaimers = htmlContentHandler.type_blocks.get(11);
		if (disclaimers != null) {
			for (Element e: disclaimers) {
				if (e.top > top_limit) {
					top_limit = e.top;
				}
			}
		}
		
		int normal_block_num = 0;
		int bottom_block_num = 0;
		for (int k = 0; k < blocks.size(); ++k) {
			Element e = blocks.get(k);
			Element above_e = null;
			if (k > 0)
				above_e = blocks.get(k-1);
			
			if (e.max_text_len < 8 && e.text_len < 50 && e.tag_num > 5 && e.height < 120 
					&& e.ids_classes.toString().toLowerCase().indexOf("content") < 0 && e.anchor_len > 0 && e.text.toString().indexOf("上传")<0 && e.text.toString().indexOf("标签")<0) {
				continue;
			} else if (e.outdomain_anchor_len >= 4 && e.outdomain_anchor_num >= e.anchor_num && e.outdomain_anchor_len * 6 > e.text_len 
					&& e.text_len < 200 && e.height < 400){
			//	System.out.println(e.outdomain_anchor_len + "\t" + e.outdomain_anchor_num + "\t" + e.text);
				continue;
			} else if (e.top * 10 > html_page.height * 9 && (last_e != null && last_e.top + 1000 < e.top) && e.text_len < 200) {
				continue;
			}
			
			if (html_page.content_title != null) {
				int base_top = html_page.content_title.top;
			//	System.out.println(base_top + "\t" + html_page.content_title.text);
				if (e.top + e.height <= base_top && e.width * e.height < 200000) {
					continue;
				}
			}
			
			// nav
			if (e.top < 400 && e.width >= 700 && e.anchor_len < e.anchor_num*4 && e.anchor_num > 20 && e.height < 350
					&& 2*(e.text_len-e.anchor_len)<e.anchor_len) {
				continue;
			}
			
			//side bar
			if (e.left >= 900 && e.width <= 250) {
				continue;
			}
			
			
			//banner
			if (e.top < 80 && e.height <= 600 && e.width > 800 && e.text_len < 20 && e.ids_classes.toString().indexOf("banner") >= 0) {
				continue;
			}
			
			if (e.anchor_len > 0 && e.anchor_len >= e.text_len && !e.is_video && html_page.type!=2) {
				if (((e.img_area*2<e.width*e.height && !html_page.is_hub) || (e.img_area < 100000 && e.top > 1000))) {
					if (!(html_page.type == 2 && e.width > 500 && e.text_len > 200)) {
					//	System.out.println(e.text);
						continue;
					}
				}
			}
			

			//if (e.text_len < 4 && (e.top > 900 || e.height*3 < e.width) && e.width * e.height < 200000) {
			if (e.text_len < 4&& !e.is_video && (e.img_area * 2 < e.width * e.height || e.width * e.height < 50000 || e.width > e.height * 2)) {
				if (e.img_area > 0 && e.img_area == e.img_iframe_area && (e.height*3>e.width || (above_e != null && above_e.img_area > 50000))&& e.width * e.height > 50000) {
					
				} else if (e.top < 400 && e.width * e.height >= 200000 && e.height >= 350) {
					
				}
				else {
			//		System.out.println("eee  " + e.text + "\t" +e.full_xpath);
					continue;
				}
			}
			
			//iframe
		//	System.out.println(e.img_iframe_area + "\t" + e.width*e.height + "\t" + e.full_xpath);
			if ((e.img_iframe_area-e.img_area)*2>e.width*e.height && e.img_iframe_area < 200000 && e.text_len < 50) {
				continue;
			}
			if ((e.img_iframe_area-e.img_area)*4>e.width*e.height && (e.img_iframe_area-e.img_area)<200000 && temp.size() >= 1 && e.text_len < 50 && e.anchor_len*2>e.text_len) {
				continue;
			}
			if (e.top > 1000 && (e.img_iframe_area-e.img_area)*3>e.width*e.height*2 && e.img_iframe_area < 300000 && e.text_len < 30) {
				continue;
			}
			
			//comment area
			if (e.xpath.indexOf("form")>=0) {
				String prefix = e.full_xpath.substring(0, e.full_xpath.indexOf("form"));
				int num = 0;
				if (form2num.containsKey(prefix)) {
					num = form2num.get(prefix)+1;
					form2num.put(prefix, num);
				} else {
					num = 1;
					form2num.put(prefix, num);
				}
				if (num < 3 && e.top > 1500 && e.text_len < 50 && e.width> 700 && e.height < 500) {
					continue;
				}
			}

			
			//text_area
			if (e.text_area > 20000 && e.text.length() < 200 && e.text_area*4 > e.width*e.height) {
			//	System.out.println(e.text_area + "\t" + e.width*e.height);
				continue;
			}
			//System.out.println("hhh  " + html_page.type);
			
			//list
			if (e.is_list && (e.top > 1800 || (last_max_e != null && last_max_e.type == 0 && (last_max_e.text_len-last_max_e.anchor_len)>500 && e.top > 500)) && temp.size() > 0) {
				if (!(html_page.type == 2 && e.width > 500 && e.text_len > 200))
					continue;
			}

			if ((e.text.indexOf("分享到") >= 0 || e.text.indexOf("分享")==0) && e.height < 400 && e.text_len < 200) {
				if (last_e != null && last_e.height > 200 && last_e.width > 350) {
					continue;
				}
			}
			
			//bbs
			if (e.left + e.width < 450 && e.width <= 200 &&  (e.height < 250 || e.height > e.width )&& e.img_area > 0 && e.text_len < 50) {
				continue;
			}
			
		//	System.out.println(e.top + "\t" + html_page.height);
			
			if (temp.size() >= 2 && (html_page.height-e.top)*2<html_page.height && e.height * 3 < e.width 
					&& e.height < 200 && e.text_len < 100) {
				if ((e.top - last_e.top - last_e.height) > 400 && e.ids_classes.indexOf("answer")<0 && html_page.url.indexOf("bbs")<0 && html_page.url.indexOf("thread-")<0) {
					continue;
				}
			}
			
			if (temp.size() > 0 && html_page.height - e.top < 100) {
				continue;
			}
			
			if (e.left > 900 && e.width < 300 && e.text_len < 20) {
				continue;
			}
			
			//login
			if (temp.size() > 0 && e.text_len < 30 && e.top > 700 && e.text.indexOf("登录")>=0 && e.text.indexOf("注册")>=0) {
				continue;
			}
			
			//video detail page multi img block
			if (html_page.type == 1 && e.img_num >=4 && e.img_area > 40000) {
				continue;
			}
			
			
			if (e.top > 1000 && e.height * 2 < e.width && e.text.length() >= 4 && e.text.length() < 200) {
				String s = e.text.substring(0, 4);
				if (s.equals("免责声明")) {
					continue;
				}
				int t = s.indexOf("推荐");
				if (t == 0 || t==2) {
					continue;
				}
			}
			
			
			if (e.top > 900 && temp.size() > 0) {
				boolean flag = false;
				for (String side_word : sideblock_keyword) {
					if (e.text.length() > side_word.length()
							&& e.text.substring(0, side_word.length()).toString().equals(side_word)) {
						flag = true;
						break;
					}
				}
				if (flag) {
					continue;
				}
			}
			
			
			if (temp.size() > 0 && e.height * 3 < e.width 
					&& e.height < 200 && e.text_len < 60) {
				if (e.text.indexOf("点评") >= 0 && e.text.indexOf("点评") <= 10 && html_page.title.indexOf("点评") < 0) {
					if (e.top > 200) {
						break;
					} else {
						continue;
					}
				}
				if (e.text.indexOf("评论") >= 0 && e.text.indexOf("评论") <= 3 && html_page.title.indexOf("评论") < 0) {					
					if (e.top > 200) {
						break;
					} else {
						continue;
					}
				}
			}
			
			//below disclaimer
			if (top_limit > 0 && e.top > top_limit) {
				break;
			}
			
			//sidebar			
		//	System.out.println(e.full_xpath + "\t" + normal_block_num + "\t" + e.class_attr + "\t" + e.top + "\t" + html_page.height);
			boolean flag = false;
			for (String sa: sidebar_attr) {
			//	System.out.println(sa);
				if (e.id.toLowerCase().indexOf(sa)>=0 || e.class_attr.toLowerCase().indexOf(sa)>=0) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				normal_block_num++;
			} else {
				if (html_page.height >= e.top*2) {
					bottom_block_num++;
				}
				
			//	System.out.println("here\t" + e.full_xpath);
				if (bottom_block_num == 0 && normal_block_num>0 && html_page.height < e.top*2) {
					continue;
				}
			}
			
			temp.add(e);
			last_e = e;
			if (last_max_e == null || last_max_e.text_len < last_e.text_len) {
				last_max_e = last_e;
			}
		}
		blocks.clear();
		blocks = temp;
	}
	
	private void removeLinkBlock() {
		if (domain.startsWith("www.cnki.com")) {
			return;
		}
		ArrayList<Element> temp = new ArrayList<Element>();
		int i = 0;
	//	System.out.println(html_page.is_hub);
		for (Element e: blocks) {

			if (e.type != 1) {
				++i;
				temp.add(e);
			} else {
			//	System.out.println(e.full_xpath + "\t" + e.text);
				if (e.top < 500 && e.height > e.width && e.width >= 300 
						&& ((e.left > 500 && e.left < 800) || (e.left + e.width > 500 && e.left + e.width < 800))) {
					temp.add(e);
				} else
				if (e.top < 300 && e.width > 600 && e.height > 400 && e.text_len < 80) {
					temp.add(e);
				}else 
				if (html_page.is_hub2 && e.height > 250
						&& e.left + e.width/2>500 && e.left + e.width/2<900
						&& e.top < 1000) {
					temp.add(e);
				} else if (html_page.type == 1 && (e.text.toString().indexOf("上传者")>=0)||e.text.toString().indexOf("标签")>=0) {
					temp.add(e);
				} else if (html_page.type == 2 && e.width > 500 && e.text_len > 200) {
					temp.add(e);
				}
				if ((e.text.indexOf("上一") >= 0 && e.text.indexOf("下一")>=0) || (e.text.indexOf("前一") >= 0 && e.text.indexOf("后一")>=0)) {
					if (i >= 2 && e.text_len < 100 && e.height < 200 && e.top > 900) {
						break;
					}
				}

			}
		}
	//	blocks.clear();
		blocks = temp;
	}
	
	private void recoginzeSingleBigBlock() {
		int area = 0;
		if (html_page.multi_column_page && blocks.size() > 3 &&  !html_page.has_main_colomn) {
			return;
		}
		String site = URLUtils.getDomainWithoutPort(html_page.url);
		if (site.equals("zhidao.baidu.com") || site.equals("wenwen.sogou.com") || site.equals("wenwen.soso.com")) {
			return;
		}
		for (Element e : blocks) {
			area += e.width * e.height;
		}
		int last_center_block_size = 0;
		for (Element e : blocks) {
			if ((double)e.width * e.height * 2 > area && e.top < 800 && e.left < 800
					&& e.width > 300 && e.height >= 500
					&& e.text_len > 30) {
				if (e.top > 550 && last_center_block_size > 40 && e.type != 1) {
					
				} else {
					//System.out.println(last_center_block_size);
					html_page.has_single_big_mainblock = true;
					blocks.clear();
					blocks.add(e);
					break;
				}
			}
			if (e.left + e.width/2 > 500 && e.left + e.width/2 < 950 && e.type != 1) {
				if (e.text_len - e.anchor_len >  last_center_block_size) {
					last_center_block_size = e.text_len - e.anchor_len;
				}
			}
		}
	}
	
	private void recognizeNovelListPage(StringBuffer sb) {
	//	System.out.println("hhh");
		if (html_page.is_hub2 && (html_page.title.indexOf("最新章节")>=0 || html_page.title.indexOf("全文阅读")>=0)) {
		//	System.out.println(url);
			html_page.type = 2;
		}
		/*
		if (html_page.is_hub2 && (html_page.title.indexOf("最新章节")>=0 || html_page.title.indexOf("全文阅读")>=0)) {
			int start = 0;
			int i = 0;
			for (; i < 10; ++i) {
				start=sb.toString().indexOf("章", start);
			//	System.out.println(start);
				if (start >=0) {
					start++;
				} else {
					break;
				}
			}
			if (i >= 10) {
				html_page.type = 2;
			//	System.out.println(url + "\t" + sb);
			}
		}
		*/
	}
	
	private void recognizeVideoListPage(StringBuffer sb) {
	//	System.out.println("hhh\t" + (sidebar_textblock != null));
		if (html_page.is_hub2 && sidebar_textblock != null && (html_page.title.indexOf("视频")>=0 || sb.toString().indexOf("视频")>=0)) {
			html_page.type = 3;
			blocks.add(sidebar_textblock);
		}
	}
	
	private void recognizeHubPage() {
		if (html_page.type == 1) {
			return;
		}
		int area1 = 0;
		int area2 = 0;
		int ad_height = 0;
		StringBuffer sb = new StringBuffer();
		int img_num = 0, img_area = 0;
		for (Element e: blocks) {
		//	System.out.println(e.full_xpath);
			if (e.top > 1000) {
				break;
			}
			if (e.type == 1 &&  e.height > 350 && (e.anchor_num*4 < e.anchor_len || e.height > 500 || e.img_area*2>e.width*e.height)) {
				if (((e.left+e.width+e.left)/2>300 || e.height < 600) && e.block_type != 7) {
					area1 += e.width * e.height;
					sb.append(e.text);
					img_num += e.img_num;
					img_area += e.img_area;
			//		System.out.println(e.full_xpath + "\t" + e.width + "\t" + e.height);
				}
			} else {
			//	System.out.println(e.full_xpath + "\t" + e.width*e.height + "\t" + e.text_len);
				area2 += e.width * e.height;
			}
			if (e.is_list && e.width>550 && e.height > 900 && !e.contain_big_pic) {
			//	System.out.println("asdf");
			//	String s = e.id.toLowerCase() + e.class_attr.toLowerCase();
			//	if (s.indexOf("list")>=0) {
					html_page.is_hub2 = true;
					sb.append(e.text);
					img_num += e.img_num;
					img_area += e.img_area;
					
			//	}
			}
			if (((e.img_iframe_area-e.img_area)*1.2>e.width*e.height || (e.img_num == 1 && e.img_area*1.2>e.width*e.height && e.width > 550))&& (e.left+e.width/2)<1000 && (e.left+e.width/2)>400) {
		//		System.out.println("ad  " + e.full_xpath + "\t" + e.text);
				ad_height += e.height;
			}
		//	System.out.println("ad height;  " + ad_height);
			if ((e.top < 410|| (e.top < 600 && e.top-ad_height < 300)) && e.type == 1 && e.height > 500 && e.width > 550 && e.height*e.width>350000) {
			//	System.out.println(e.full_xpath + "\t" +e.is_video + "\t" +  e.top + e.height);
				if (!(e.full_xpath.equals("/html/body") && blocks.size() > 1)) {
					html_page.is_hub2 = true;
					sb.append(e.text);
					img_num += e.img_num;
					img_area += e.img_area;
				}
			}
		}
	//	System.out.println("hhh");
	//	System.out.println("asdf: " + html_page.is_hub2);
	//	System.out.println(area1 + "\t" + area2);
		if (area1 > area2*3) {
			html_page.is_hub = true;
			html_page.is_hub2 = true;
			//return;
		}
		
		if (html_page.is_hub2) {
		//	recognizeNovelListPage(sb);
			//System.out.println(img_num + "\t" + img_area);
			if (img_num >= 8 && img_area >= 80000) {
				recognizeVideoListPage(sb);
			}
		}
		
		if (html_page.is_hub2) {
			if (url.indexOf("focus.cn/msgview/") >= 0) {
				html_page.is_hub2 = false;
				return;
			}
			for (Element e: blocks) {
				if ((e.type == 0 && e.text_len > 400 && e.top < 1500 && e.width > 600 && e.height > 100 && !e.is_list)
						|| e.contain_big_pic) {
				//	System.out.println(e.full_xpath + "\t" + e.text);
					html_page.is_hub2 = false;
					return;
				}
			}
		}
		
		if (html_page.is_hub) {
			return;
		}
		

		int hub_text_len = 0;
		int text_len = 0;
		for (Element e: blocks) {
			if (e.top < 700) {
				if (e.type == 1 && e.height > 250 && e.width > 600 && (e.anchor_num*4 < e.anchor_len || e.height > 500 || e.img_area*2>e.width*e.height)) {
					hub_text_len += e.text_len;
				}
			}
			text_len += e.text_len;
		}
	//	System.out.println(hub_text_len + "\t" + text_len);
		if (hub_text_len*2 > text_len) {
			//System.out.println("here.");
			html_page.is_hub = true;
		}
		
	}
	
	
	
	private void blockCluster() {
		//System.out.println(html_page.multi_column_page);
		if (html_page.multi_column_page) {
	//		System.out.println(blocks.size());
			Tool.quickSortX(blocks, 0, blocks.size()-1);
			List<ArrayList<Element>> cluster_blocks = new ArrayList<ArrayList<Element>>();
			ArrayList<Element> cur_blocks = new ArrayList<Element>();
			Element last_block = null;

			for (Element e : blocks) {
				if (last_block == null || e.left - last_block.left < 50) {
					cur_blocks.add(e);
					last_block = e;
				} else {
					last_block = e;
					cluster_blocks.add(cur_blocks);
					cur_blocks = new ArrayList<Element>();
					cur_blocks.add(e);
				}
			}
			if (cur_blocks.size() > 0) {
				cluster_blocks.add(cur_blocks);
			}
			ArrayList<Integer> cluster_top = new ArrayList<Integer>();
			int cur_top = 1000000;
			int min_top = 1000000;
			
			for (int i = 0; i < cluster_blocks.size(); ++i) {
				ArrayList<Element> cluster = cluster_blocks.get(i);
				for (int j = 0; j < cluster.size(); ++j) {
					Element e = cluster.get(j);
					if (e.top < cur_top) {
						cur_top = e.top;
					}
				}
				if (min_top > cur_top) {
					min_top = cur_top;
				}
				cluster_top.add(cur_top);
				cur_top = 1000000;
			}
			List<ArrayList<Element> > cluster_blocks_temp = new ArrayList<ArrayList<Element>>();
			for (int i = 0; i < cluster_top.size(); ++i) {
			//	System.out.println(cluster_top.get(i));
				if (cluster_top.get(i) - min_top > 1000) {
					continue;
				} else {
					ArrayList<Element> t = cluster_blocks.get(i);
					if (t.size() == 1) {
						Element te = t.get(0);
						if (te.top >= 700 && te.top < 900 && te.left > 1050 && te.left + te.width > 1400) {
							continue;
						}
					}
					cluster_blocks_temp.add(t);
				}
			}
			cluster_blocks.clear();
			cluster_blocks = cluster_blocks_temp;
			
			//System.out.println("cluster num:  " + cluster_blocks.size());
			if (cluster_blocks.size() == 1) {
				html_page.has_main_colomn = true;
			}
			if (cluster_blocks.size() >= 2) {
				ArrayList<Integer> cluster_width = new ArrayList<Integer>();
				ArrayList<Integer> cluster_area = new ArrayList<Integer>();
				int all_area = 0;
				for (int i = 0; i < cluster_blocks.size(); ++i) {
					ArrayList<Element> cluster = cluster_blocks.get(i);
					int width = 0;
					int area = 0;
					for (int j = 0; j < cluster.size(); ++j) {
						Element e = cluster.get(j);
						width += e.width;
						area += e.width * e.height;
						all_area += e.width * e.height;
					}
					cluster_width.add(width/cluster.size());
					cluster_area.add(area);
				}
				int main_cluster = -1;
				if (cluster_area.size() == 2) {
					int max = 0;
					int min = 1;
					if (cluster_width.get(max) < cluster_width.get(min)) {
						max = 1;
						min = 0;
					}
					if (cluster_width.get(max) > (double)cluster_width.get(min) * 1.5) {
						if (cluster_area.get(max)/(double)all_area > 0.5 && cluster_width.get(max) > 400) {
							main_cluster = max;
						}
					}
				} else {
					for (int i = 0; i < cluster_area.size(); ++i) {
						if (cluster_area.get(i)/(double)all_area > 0.5 && cluster_width.get(i) > 400) {
							main_cluster = i;
							break;
						}
					}
				}
				//System.out.println(main_cluster);
				if (main_cluster == -1) {
					if (cluster_width.size() == 2) {
						int max = 0;
						int min = 1;
						if (cluster_width.get(max) < cluster_width.get(min)) {
							max = 1;
							min = 0;
						}
						if (cluster_width.get(max) > cluster_width.get(min)*2) {
							main_cluster = max;
						}
					} else if (cluster_width.size() == 3) {
						int max = 0;
						for (int i = 1; i < cluster_width.size(); ++i) {
							if (cluster_width.get(i) > cluster_width.get(max)) {
								max = i;
							}
						}
						int temp = cluster_width.get(max);
						for (int i = 0; i < cluster_width.size(); ++i) {
					//		System.out.println(cluster_width.get(i) + "\t" + cluster_area.get(i));
							if (i == max) {
								continue;
							}
							temp -= cluster_width.get(i);
						}
						if (temp > 0) {
							main_cluster = max;
						}
					}
				}
			//	System.out.println(main_cluster);
				if (main_cluster >= 0) {
					html_page.has_main_colomn = true;
				//	System.out.println(main_cluster);
					blocks.clear();
					for (int i = 0; i < cluster_blocks.size(); ++i) {
						//System.out.println("asdf");
						ArrayList<Element> temp = cluster_blocks.get(i);
						if (main_cluster == i) {
							blocks.addAll(temp);
							continue;
						}
						if (temp.size() == 1) {
							Element e = temp.get(0);
							if (e.top < 100 || (double)e.top > (double)html_page.height * 0.8
									|| (e.left > 850 && e.width * 2 < e.height)) {
								continue;
							} else {
								blocks.add(e);
							}
						} else {
							int width = cluster_width.get(i);
							for (Element e : temp) {
								
								if (e.left > 400 && e.left < 700 && e.top < 800 && e.top > 100 && e.width >= 400) {
									blocks.add(e);
									continue;
								}
								if (e.width > width + 100) {
									blocks.add(e);
									continue;
								}

								if (e.max_text_len > 5 && e.type != 1 && !e.is_list && e.text_len - e.anchor_len > 20 && (e.text_len-e.anchor_len)*2>e.anchor_len && e.img_area < 30000) {
									
									if (html_page.type == 1) {
										blocks.add(e);
										continue;
									} else {
										if (sidebar_textblock == null || sidebar_textblock.text_len < e.text_len) {
											sidebar_textblock = e;
											//System.out.println("here  " + e.text);
										}
									}
								}
								if (e.width > 400 && e.text_len > 20 && e.left + e.width/2 > 500 && e.left + e.width/2<850) {
									blocks.add(e);
									continue;
								}
								if (e.width > 500 && e.text_len > 5 && e.left + e.width/2 > 500 && e.left + e.width/2<850 && e.top < 600 && e.anchor_len == 0) {
									blocks.add(e);
									continue;
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void removeTopAndBottomBlocks() {
		if (blocks.size() < 3) {
			return;
		}
		

	//	System.out.println(html_page.width);
		int wide_width_limit = 850;
		if (html_page.width >= 1200) {
			wide_width_limit = 980;
		} else if (html_page.width < 850) {
			wide_width_limit = html_page.width - 20;
		}
		int narrow_area = 0;
		int wide_area = 0;
		int cur_top = 100000;
		int cur_bottom = 0;
		int cur_type = 0; 
		Tool.quickSortY(blocks, 0, blocks.size()-1);
		for (Element e : blocks) {
			if (e.width > wide_width_limit) {
				if (cur_type != 2) {
					cur_type = 1;
					if (cur_top > e.top) {
						cur_top = e.top;
					}
					if (cur_bottom < e.top + e.height) {
						cur_bottom = e.top + e.height;
					}
				} else {
					narrow_area += cur_bottom - cur_top;
					cur_top = e.top;
					cur_bottom = e.top + e.height;
					cur_type = 1;
				}
				//wide_area += e.width * e.height;
			} else {
				if (cur_type != 1) {
					cur_type = 2;
					if (cur_top > e.top) {
						cur_top = e.top;
					}
					if (cur_bottom < e.top + e.height) {
						cur_bottom = e.top + e.height;
					}
				} else {
					wide_area += cur_bottom - cur_top;
					cur_top = e.top;
					cur_bottom = e.top + e.height;
					cur_type = 2;
				}
				//narrow_area += e.width * e.height;
			}
		}
		if (cur_type == 1) {
			wide_area += cur_bottom - cur_top;
		} else {
			narrow_area += cur_bottom - cur_top;
		}
	//	System.out.println(narrow_area + "\t" + wide_area);
		if (narrow_area > wide_area && blocks.size() >= 4) {
			html_page.multi_column_page = true;
		} else {
			html_page.multi_column_page = false;
		}
		
		if (html_page.multi_column_page) {
		//	System.out.println("here  " + wide_width_limit);
			ArrayList<Element> content_title = htmlContentHandler.type_blocks.get(4);
			int top_limit = 100000;
			if (content_title != null && content_title.size() > 0) {
				Element tmp = content_title.get(0);
				top_limit = tmp.top + tmp.height;
			}
			ArrayList<Element> temp = new ArrayList<Element>();
			for (int i = 0; i < blocks.size(); ++i) {
				
				Element e = blocks.get(i);
			//	System.out.println(e.width + "\t" + e.height + "\t" + e.text);
				if (e.contain_contenttitle || e.top >= top_limit) {
				//	System.out.println("hereee: " + e.text);
					temp.addAll(blocks.subList(i, blocks.size()));
					break;
				} else if (e.width > wide_width_limit && e.height < 400) {
					continue;
				} else if (e.width * e.height < 10000) {
					continue;
				} else if (e.left > 700) {
					temp.add(e);
					continue;
				}  else {
					temp.addAll(blocks.subList(i, blocks.size()));
					break;
				}
			}
			blocks.clear();
		//	System.out.println(temp.size());
			for (int i = temp.size() - 1; i >= 0; --i) {
				Element e = temp.get(i);
				if (e.width > wide_width_limit) {
					continue;
				} else if (e.width * e.height < 10000) {
					continue;
				} else if (e.left > 700) {
					blocks.add(e);
					continue;
				} else {
					blocks.addAll(temp.subList(0, i+1));
					break;
				}
			}
		} else {
		//	System.out.println("here.");
			ArrayList<Element> breadcrumbs = htmlContentHandler.type_blocks.get(3);
			int top_limit = 200;
			if (breadcrumbs != null && breadcrumbs.size() > 0) {
				Element tmp = breadcrumbs.get(0);
				top_limit = tmp.top + tmp.height;
			}
			ArrayList<Element> temp = new ArrayList<Element>();
			for (int i = 0; i < blocks.size(); ++i) {
				Element e = blocks.get(i);
				if ((e.width > wide_width_limit && e.height < 100 && e.text.length() < e.visible_dom_num * 3 && e.anchor_len * 1.5 > e.text_len)
						|| (e.width > wide_width_limit && e.height < 100 && e.top < top_limit)
						|| (e.top < 200 && e.width * e.height < 10000)
						|| (e.top < 120 && e.height < 150 && e.anchor_len*2 > e.text_len)
						|| (e.top < 100 && e.ids_classes.toString().toLowerCase().indexOf("header")>=0 && e.height < 220 && e.width >= 850)
						|| (e.top < 100 && (e.id.indexOf("header")>=0||e.class_attr.indexOf("header")>=0) && e.height < 300 && e.width >= 850)) {
					continue;
				} else if (e.width * e.height < 10000) {
					continue;
				} else {

					temp.addAll(blocks.subList(i, blocks.size()));
					break;
				}
			}
			blocks.clear();
			for (int i = temp.size() - 1; i >= 0; --i) {
				Element e = temp.get(i);
				boolean is_bottom = false;
				String text_s = e.text.toString().toLowerCase();
				for (String s : bottom_block_keyword) {
					if (text_s.indexOf(s) >= 0) {
						is_bottom = true;
						break;
					}
				}
				if (is_bottom && e.width > wide_width_limit && e.height < 250) {
					continue;
				} else if (e.width * e.height < 10000) {
					continue;
				} else {
					blocks.addAll(temp.subList(0, i+1));
					break;
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		RuleBasedMainContentExtractor main_content = new RuleBasedMainContentExtractor();
		FileInputStream f_stream = new FileInputStream(new File("D:/sunjian/workspace/hadoop_pa/t.html"));
		byte[] bs = new byte[1024*1024*4];
		int len = f_stream.read(bs);
		String s = new String(bs, 0, len, "UTF-8");
		String out = "";
		String url = "http://kaoshi.china.com/daoyou/peixun/2767167.htm";
		List<Element> blocks = main_content.extractAllBlock(url, s);
		System.out.println(main_content.html_page.is_hub2 + "\t" + main_content.html_page.type);
		Tool.quickSortY(blocks, 0, blocks.size()-1);
		for (int i = 0; i < blocks.size(); ++i) {
			Element f = blocks.get(i);
		//	System.out.println(f.full_xpath + "\t" + f.is_list);
		//	System.out.println(f.text_len  + "\t" +  f.anchor_len + "\t" + f.text);
		//	System.out.println(f.anchor_len + "\t" + (f.text_len-f.anchor_len) + "\t" + f.max_text_len + "\t" + f.text_num + "\t" + f.text);
	        out += String.format("<div style=\"position: absolute; left: %dpx; top: %dpx; width: %dpx; height: %dpx;  border:3px solid red;\">%s</div>\n",f.left, f.top, f.width, f.height, f.block_type + "\t" + f.full_xpath +"\t" +  f.text);

		//	System.out.println(f.toPrintString());
		}
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File("ttt2.html")));
		System.out.println(out);
		writer2.write(out);
		writer2.close();
	}

}
