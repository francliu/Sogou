package com.sogou.cm.pa.maincontent;

import java.util.HashMap;


class ConstantField {
	public static final int init_top = 1000000;
	public static final int init_left = 1000000;
	public static final int init_bottom = 0;
	public static final int init_right = 0;
}

public class Element {
	public String tag;
	public String xpath;
	public String full_xpath;
	public String num_xpath;
	String id;
	String class_attr;
	String style;
	String src;
	StringBuffer ids_classes;
	String block_title;
	int text_len;
	int text_num;
	int max_text_len;
	int anchor_len;
	int anchor_num;
	int outdomain_anchor_num;
	int outdomain_anchor_len;
	int hidden_text_len;
	public StringBuffer text;
	StringBuffer text_anchor_flag;
	StringBuffer child_tags;
	String own_text;
	int tag_num;
	int visible_dom_num;
	public int left;
	public int top;
	public int width;
	public int height;
	int child_left;
	int child_top;
	int child_right;
	int child_bottom;
	boolean xy_modified;
	public int img_iframe_area;
	int img_area;
	int img_num;
	int text_area;
	int form_area;
	int type;//0 text;  1 link;  2 other;
	boolean is_single_block;
	boolean is_out_domain;
	boolean is_list;
	public boolean is_video;
	public boolean contain_contenttitle;
	boolean is_sidebar;
	boolean is_tag;
	boolean is_userinfo;
	boolean is_userinfo2;
	boolean is_recommend_tips;
	boolean contain_big_pic;
	boolean contain_big_iframe;
	boolean contain_contract;
	boolean has_media_child;
	boolean has_last_and_next;
	boolean is_float;
	boolean has_disclaimer;
	boolean contain_publictime;
	int breadcrumb_flag; //0 not contain ">",  1 all ">" is valid,  "2" some ">" is invalid.
	int reply_tips_flag;  //0 not contain "回复", 1 contain "回复" and valid, 2 contain "回复" but invalid
	public boolean has_download;
	public boolean is_maincontent;
	int pagelist_flag;
	int struct_hash;
	public int block_type;  //0:unknown 1:main_content(deprecated) 2:tag  3:breadcrumb  4:content_title  5:reply_tips  6:user_info  7:recommend  8:video play area  9:list of hub page  10:bottom contact  11:disclaimer  12:share  13:last and next  14:hidden block  15:comment  16:public time
	HashMap<String, Integer> tag2num;
	public static final int VIDEO_PLAYER_BLOCK = 8;
	Element() {
		tag = "";
		xpath = "";
		full_xpath = "";
		num_xpath = "";
		id = "";
		class_attr = "";
		style = "";
		src = "";
		ids_classes = new StringBuffer();
		block_title = "";
		text_len = anchor_len = anchor_num = 0;
		outdomain_anchor_num = outdomain_anchor_len = 0;
		text_num = max_text_len = 0;
		hidden_text_len = 0;
		tag_num = visible_dom_num = 0;
		left = top = width = height = 0;
		child_left = ConstantField.init_left;
		child_top = ConstantField.init_top;
		child_right = ConstantField.init_right;
		child_bottom = ConstantField.init_bottom;
		xy_modified = false;
		img_iframe_area = 0;
		img_area = 0;
		img_num = 0;
		text_area = 0;
		form_area = 0;
		type = 2;
		is_single_block = false;
		is_out_domain = false;
		is_list = false;
		is_video = false;
		is_sidebar = false;
		is_tag = false;
		is_userinfo = false;
		is_userinfo2 = false;
		is_recommend_tips = false;
		contain_big_pic = false;
		contain_big_iframe = false;
		contain_contract = false;
		has_media_child = false;
		has_last_and_next = false;
		has_disclaimer = false;
		contain_publictime = false;
		breadcrumb_flag = 0;
		reply_tips_flag = 0;
		pagelist_flag = 0;
		contain_contenttitle = false;
		has_download = false;
		is_float = false;
		text = new StringBuffer();
		text_anchor_flag = new StringBuffer();
		child_tags = new StringBuffer();
		own_text = "";
		block_type = 0;
		is_maincontent = false;
		struct_hash = 0;
		tag2num = new HashMap<String, Integer>();
	}
	public String toPrintString() {
		StringBuffer s = new StringBuffer();
		s.append(xpath + " " + left + " " + top + " " + width + " " + height + "\n");
		s.append("id: " + id.toString() + "\n");
		s.append("class: " + class_attr.toString() + "\n");
		s.append("style: " + style.toString() + "\n");
		return s.toString();
	}
}

