package com.sogou.cm.pa.maincontent;

public class HtmlPage {
	boolean multi_column_page;
	int column_num;
	int height;
	int width;
	boolean is_hub;
	boolean is_hub2; //
	boolean has_single_big_mainblock;
	boolean has_main_colomn;
	Element	content_title;
	public String title;
	String url;
	int type; //1 video detail page; 2 novel page； 3 video list page
	HtmlPage() {
		multi_column_page = false;
		column_num = 1;
		height = 0;
		width = 0;
		is_hub = false;
		is_hub2 = false;
		has_single_big_mainblock = false;
		content_title = null;
		title = "";
		has_main_colomn = false;
		url = "";
		type = 0;
	}
}