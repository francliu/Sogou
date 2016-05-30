package com.sogou.pa.ListPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.xml.sax.Attributes;


public class Element {
	String name;
	String full_name;
	boolean is_displaynone;
	String id_attr;
	String class_attr;
	StringBuffer ids_classes;
	String src;
	Attributes styles;
	HashMap<String, String> atts;
	int top;
	int left;
	int height;
	int width;
	int area;
	int text_len;
	int own_text_len;
	int longest_own_text_len;
	int anchor_len;
	int anchor_num;
	int img_num;
	int img_area;
	int child_num;
	int fake_child_num;
	int input_area;
	int area_list;
	int area_other;
	int list_num1;
	int strict_list_num1;
	int indiffent_child_num1;
	int list_num2;
	int strict_list_num2;
	int indiffent_child_num2;
	int list_num3;
	int indiffent_child_num3;
	int big_list_num;
	int tag_num;
	int visible_tag_num;
	int anchor_area;
	
	int biggest_child_area;
	int biggest_img_area;
	int biggest_img_width;
	int most_img_num;
	int child_arrange_style;
	double first_second_child_ratio;
	int tobottom;
	double child_height2width;
	int tall_child_num;
	int longest_text_len;
	int friend_link_area;
	int full_list_area;
	int full_list_len;
	int time_num;
	boolean has_intro_title;
	boolean has_intro;
	boolean has_sub_title;
	boolean has_list;
	boolean is_recommand_title;
	boolean is_info;
	
	
	boolean is_single_block;
	boolean is_list;
	boolean is_text;
	boolean is_text_can;
	boolean is_footer;
	boolean is_sub_title;
	boolean has_merged;
	boolean is_bold;
	boolean is_time;
	boolean list_of_subtitle;
	boolean list_of_keyword;
	boolean has_repeat_sb_structure;
	boolean has_other_block;
	boolean child_is_small;
	boolean has_one_child;
//	boolean has_background_img;
	StringBuffer text;
	ArrayList<Element> children;
	PrintElement pe;
	
	Element() {
		name = "";
		full_name = "";
		is_displaynone = false;
		id_attr = "";
		class_attr = "";
		ids_classes = new StringBuffer();
		src = "";
		atts = new HashMap<String, String>();
		top = left = height = width = 0;
		area = 0;
		text_len = anchor_len = anchor_num = 0;
		own_text_len = 0;
		longest_own_text_len = 0;
		area_list = area_other = 0;
		tag_num = 0;
		visible_tag_num = 0;
		anchor_area = 0;
		
		biggest_child_area = 0;
		biggest_img_area = 0;
		biggest_img_width = 0;
		most_img_num = 0;
		child_arrange_style = 0;
		first_second_child_ratio = 1.0;
		tobottom = 0;
		child_height2width = 0;
		tall_child_num = 0;
		longest_text_len = 0;
		friend_link_area = 0;
		full_list_area = 0;
		full_list_len = 0;
		time_num = 0;
		has_intro_title = false;
		has_sub_title = false;
		has_list = false;
		is_single_block = false;
		is_recommand_title = false;
		is_info = false;
		
		img_num = 0;
		img_area = 0;
		text = new StringBuffer();
		children = new ArrayList<Element>();
		has_intro = false;
		is_list = false;
		is_text = false;
		is_text_can = false;
		is_footer = false;
		has_merged = false;
		is_bold = false;
		is_sub_title = false;
		is_time = false;
		list_of_subtitle = false;
		list_of_keyword = false;
		has_repeat_sb_structure = false;
		has_other_block = false;
		child_is_small = false;
		has_one_child = false;
		fake_child_num = 0;
		list_num1 = 0;
		strict_list_num1 = 0;
		indiffent_child_num1 = 0;
		list_num2 = 0;
		strict_list_num2 = 0;
		indiffent_child_num2 = 0;
		list_num3 = 0;
		indiffent_child_num3 = 0;
		big_list_num = 0;
		input_area = 0;
//		has_background_img = false;
		styles = null;
		child_num = 0;
		pe = null;
	}
	
}