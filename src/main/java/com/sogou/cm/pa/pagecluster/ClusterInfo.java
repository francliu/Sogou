package com.sogou.cm.pa.pagecluster;

import java.util.ArrayList;

public class ClusterInfo {
	public String level;
	public String site;
	public String key;
	public double limit;
	public ArrayList<HtmlPage> pages;
	public ArrayList<String> urls;
	public String seperator1 = "n3k8y";
	public String simple_seperator1 = "=";
	public ClusterInfo() {
		level = "site";
		site = "";
		key = "";
		limit = 0.0;
		pages = new ArrayList<HtmlPage>();
		urls = new ArrayList<String>();
	}
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(level);
		s.append(seperator1);
		s.append(site);
		s.append(seperator1);
		s.append(key);
		s.append(seperator1);
		s.append(limit);
		for (HtmlPage hp: pages) {
			s.append(seperator1);
			s.append(hp.toString());
		}
		return s.toString();
	}
	
	public String toString_debug() {
		StringBuffer s = new StringBuffer();
		s.append(level);
		s.append(seperator1);
		s.append(site);
		s.append(seperator1);
		s.append(key);
		s.append(seperator1);
		s.append(limit);
		for (HtmlPage hp: pages) {
			s.append(seperator1);
			s.append(hp.toString());
		}
		s.append(seperator1);
		s.append("For:Debug");
		for (String url: urls) {
			s.append(seperator1);
			s.append(url);
		}
		return s.toString();
	}
	
	public void fromString(String s) {
		
		String[] segs = StringUtil.split2(s, seperator1);//s.split(seperator1);
		
		if (segs.length < 5) {
			return;
		}
		
		level = segs[0];
		
		site = segs[1];
		key = segs[2];
		limit = new Double(segs[3]);
		
		pages.clear();
		
		int i = 4;
		for (; i < segs.length; ++i) {
			if (segs[i].equalsIgnoreCase("For:Debug")) {
				break;
			}
			HtmlPage hp = new HtmlPage();
			hp.fromString(segs[i]);
			pages.add(hp);
		}
		++i;
		for (; i < segs.length; ++i) {
			urls.add(segs[i]);
		}
		
	}
	
	public String toSimpleString() {
		StringBuffer s = new StringBuffer();
		s.append(level);
		s.append(simple_seperator1);
		s.append(site);
		s.append(simple_seperator1);
		s.append(key);
		s.append(simple_seperator1);
		s.append(limit);
		for (HtmlPage hp: pages) {
			s.append(simple_seperator1);
			s.append(hp.toSimpleString());
		}
		return s.toString();
	}
	
	public void fromSimpleString(String s) {
		
		String[] segs = StringUtil.split2(s, simple_seperator1);//s.split(seperator1);
		
		if (segs.length < 5) {
			return;
		}
		
		level = segs[0];
		
		site = segs[1];
		key = segs[2];
		limit = new Double(segs[3]);
		
		pages.clear();
		
		int i = 4;
		for (; i < segs.length; ++i) {
			if (segs[i].equalsIgnoreCase("For:Debug")) {
				break;
			}
			HtmlPage hp = new HtmlPage();
			hp.fromSimpleString(segs[i]);
			pages.add(hp);
		}
		++i;
		for (; i < segs.length; ++i) {
			urls.add(segs[i]);
		}
		
	}
	
	public void fromString2(String s) {
		
		String[] segs = s.split(seperator1);
		
		if (segs.length < 5) {
			return;
		}
		
		level = segs[0];
		
		site = segs[1];
		key = segs[2];
		limit = new Double(segs[3]);
		
		pages.clear();
		
		int i = 4;
		for (; i < segs.length; ++i) {
			if (segs[i].equalsIgnoreCase("For:Debug")) {
				break;
			}
			HtmlPage hp = new HtmlPage();
			hp.fromString(segs[i]);
			pages.add(hp);
		}
		++i;
		for (; i < segs.length; ++i) {
			urls.add(segs[i]);
		}
		
	}
	
	public void toString2() {
		System.out.println(urls.size());
		for (String url: urls) {
			System.out.println(url);
		}
	}

	public double getDist(HtmlPage hp) {
		double dist = 0.0;
		/*
		for (String xpath: hp.xpath2info.keySet()) {
			System.out.println(xpath + "\t" + hp.xpath2info.get(xpath).num);
		}
		System.out.println("-------------------------------------------");
		*/
		for (HtmlPage t: this.pages) {
			dist += hp.getDistance(t);
			/*
			for (String xpath: t.xpath2info.keySet()) {
				System.out.println(xpath + "\t" + t.xpath2info.get(xpath).num);
			}
			System.out.println(t.url + "\t" + hp.getDistance(t));
			*/
		}
		if (this.pages.size() == 0) {
			return 1.0;
		}
		return dist/this.pages.size();
	}
}
