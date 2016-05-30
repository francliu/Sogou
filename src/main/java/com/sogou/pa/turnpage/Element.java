package com.sogou.pa.turnpage;

import java.util.LinkedList;


class Element{
	String classes;
	String ids;
	LinkedList<Element> children;
	String content;
	String tag;
	String color;
	String href;
	Element(){
		classes = "";
		ids = "";
		children = new LinkedList<Element>();
		content="";
		tag="";
		color="";
		href="";
	}
}