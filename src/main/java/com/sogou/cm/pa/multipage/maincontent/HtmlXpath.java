package com.sogou.cm.pa.multipage.maincontent;

public class HtmlXpath {
	boolean use_num;
	String class_xpath;
	String num_xpath;
	int type; //Element block_type
	String separator = "8u7y5";
	public HtmlXpath() {
		use_num = false;
		class_xpath = "";
		num_xpath = "";
		type = 0;
	}
	
	public void CopyOther(HtmlXpath hx) {
		use_num = hx.use_num;
		class_xpath = hx.class_xpath;
		num_xpath = hx.num_xpath;
		type = hx.type;
	}
	
	public String toString() {
		if (use_num) {
			return type + separator + class_xpath + separator + num_xpath;
		} else {
			return type + separator + class_xpath;
		}
	}
	
	public void fromString(String s) {
		String[] segs = s.split(separator);
		if (segs.length != 3 && segs.length != 2) {
			return;
		}
		type = Integer.valueOf(segs[0]);
		if (segs.length == 2) {
			use_num = false;
			class_xpath = segs[1];
		} else {
			use_num = true;
			class_xpath = segs[1];
			num_xpath = segs[2];
		}
	}
}
