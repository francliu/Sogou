package com.sogou.pa.ListPage;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;

public class PrintElement {
	String name;
	Attributes styles;
	boolean is_list;
	boolean is_text;
	boolean in_text;
	boolean in_list;
	boolean is_footer;
	boolean in_footer;
	boolean is_single_block;
	boolean in_single_block;
	String text;
	ArrayList<PrintElement> children;
	int id;
	
	PrintElement() {
		name = "";
		text = "";
		children = new ArrayList<PrintElement>();
		is_list = false;
		is_text = false;
		in_list = false;
		is_footer = false;
		in_footer = false;
		is_single_block = false;
		in_single_block = false;
		styles = null;
		id = 0;
	}
	
	public String traverse_debug(Counter cnt) {
		StringBuffer sb = new StringBuffer();
		if (this.name.equals("")) {
			return text.toString();
		}
		sb.append("<" + name + " ");
		String style = "";
		if (this.styles != null) {
			for (int i = 0; i < styles.getLength(); ++i) {
				String name = styles.getQName(i);
				String value = styles.getValue(i);
				if (!name.equalsIgnoreCase("style")) 
					sb.append(name + "=\"" + value + "\" ");
				else 
					style = value;
			}
		}
		String added_border = "border:8px solid yellow;";
		boolean is_add_border = false;
    	if (this.is_list && !this.in_list) {
	//	if (this.is_list) {
		
	//	if (this.is_text && !this.in_text && !this.in_list && !this.is_list && !this.in_footer && !this.is_footer && !this.in_single_block) {
				
	//	if (this.is_footer) {
			is_add_border = true;
			/*
			if (this.id != 0) {
				added_border = "border:8px solid green;";
			}
			*/
		}
		if (is_add_border) {
		//	System.out.println("hrer.");
			cnt.cnt++;
			sb.append("style=\"" + added_border + style + "\" ");
		} else if (style.length() > 0) {
			sb.append("style=\"" + style + "\" ");
		}
		sb.append(">\n");
		for (PrintElement tn: children) {
			if (this.is_list || this.in_list) {
				tn.in_list = true;
			}
			if (this.is_footer || this.in_footer) {
				tn.in_footer = true;
			}
			if (this.is_text || this.in_text) {
				tn.in_text = true;
			}
			if (this.is_single_block || this.in_single_block) {
				tn.in_single_block = true;
			}
			sb.append(tn.traverse_debug(cnt));
		}

		sb.append("</" + name + ">\n");
		return sb.toString();
	}
	
	public void sample_list_block(ArrayList<PrintElement> list_blocks) {
    	if (this.is_list) {
    		list_blocks.add(this);
			for (PrintElement tn: children) {
				tn.in_list = true;
			}
		} else {

			for (PrintElement tn: children) {
				tn.sample_list_block(list_blocks);
			}
		}
	}
}
