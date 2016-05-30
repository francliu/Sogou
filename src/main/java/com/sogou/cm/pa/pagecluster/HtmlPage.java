package com.sogou.cm.pa.pagecluster;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

class XpathInfo {
	long hash;
	boolean is_sidebar;
	int num;
	int anchor_len;
	int before_text_len;
	int before_max_node_len;
	String seperator1 = "u8m2y";
	String simple_seperator1 = "c";
	XpathInfo() {
		hash = 0;
		num = 0;
		is_sidebar = false;
		anchor_len = 0;
		before_text_len = 0;
		before_max_node_len = 0;
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(hash);
		s.append(seperator1);
		s.append(is_sidebar);
		s.append(seperator1);
		s.append(num);
		s.append(seperator1);
		s.append(anchor_len);
		s.append(seperator1);
		s.append(before_text_len);
		s.append(seperator1);
		s.append(before_max_node_len);
		return s.toString();
	}
	
	public void fromString(String s) {
		String[] segs = StringUtil.split2(s, seperator1);//s.split(seperator1);
		if (segs.length != 6) {
			return;
		}
		hash = Long.valueOf(segs[0]);
		is_sidebar = Boolean.valueOf(segs[1]);
		num = Integer.valueOf(segs[2]);
		anchor_len = Integer.valueOf(segs[3]);
		before_text_len = Integer.valueOf(segs[4]);
		before_max_node_len = Integer.valueOf(segs[5]);
	}
	
	public String toSimpleString() {
		StringBuffer s = new StringBuffer();
		s.append((int)(hash));
		s.append(simple_seperator1);
		if (is_sidebar) {
			s.append(1);
		} else {
			s.append(0);
		}
		
		s.append(simple_seperator1);
		s.append(num);
		return s.toString();
	}
	
	public void fromSimpleString(String s) {
		String[] segs = StringUtil.split2(s, simple_seperator1);//s.split(seperator1);
		if (segs.length != 3) {
			return;
		}
		hash = Long.valueOf(segs[0]);
		if (segs[1].equals("1"))
			is_sidebar = true;
		else
			is_sidebar = false;
		num = Integer.valueOf(segs[2]);
	}
	
	public void fromString2(String s) {
		String[] segs = s.split(seperator1);
		if (segs.length != 6) {
			return;
		}
		hash = Long.valueOf(segs[0]);
		is_sidebar = Boolean.valueOf(segs[1]);
		num = Integer.valueOf(segs[2]);
		anchor_len = Integer.valueOf(segs[3]);
		before_text_len = Integer.valueOf(segs[4]);
		before_max_node_len = Integer.valueOf(segs[5]);
	}
}


public class HtmlPage {
	public String url;
	public HashMap<String, String> shingles;
	//HashMap<String, Integer> xpath2num;
	public HashMap<String, StringBuffer> xpath2text;
	public HashMap<String, XpathInfo> xpath2info;
	public String tags;
	String seperator1 = "e3f5m";
	String seperator2 = "b7o5d";
	String simple_seperator1 = "a";
	String simple_seperator2 = "b";
	public int index;
	
	public HtmlPage() {
		index = -1;
		shingles = new HashMap<String, String>();
		//xpath2num = new HashMap<String, Integer>();
		tags = "";
		url = "";
		xpath2text = new HashMap<String, StringBuffer>();
		xpath2info = new HashMap<String, XpathInfo>();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(url);
		for (String xpath: xpath2info.keySet()) {
			sb.append(seperator1);
			sb.append(xpath);
	//		sb.append(seperator2);
	//		sb.append(xpath2num.get(xpath));
			sb.append(seperator2);
			sb.append(xpath2info.get(xpath).toString());
			
		}
		return sb.toString();
	}
	
	public void fromString(String s) {
		if (s != null) {
		//	this.xpath2num.clear();
			this.xpath2info.clear();
			String[] segs = StringUtil.split2(s, seperator1);//s.split(seperator1);
			this.url = segs[0];
			for (int i = 1; i < segs.length; ++i) {
				String val = segs[i];
			//	System.out.println(val);
				String[] segs2 = StringUtil.split2(val, seperator2);//val.split(seperator2);
				if (segs2.length == 2) {
					XpathInfo xi = new XpathInfo();
					xi.fromString(segs2[1]);
		//			System.out.println(segs2[0] + "\t" + num);
					this.xpath2info.put(segs2[0], xi);
				}
			}
		}
	}
	
	public String toSimpleString() {
		StringBuffer sb = new StringBuffer();
	//	sb.append(url);
		int i = 0;
		for (String xpath: xpath2info.keySet()) {
			if (i > 0) {
				sb.append(simple_seperator1);
			}
			++i;
			sb.append(xpath);
	//		sb.append(seperator2);
	//		sb.append(xpath2num.get(xpath));
			sb.append(simple_seperator2);
			sb.append(xpath2info.get(xpath).toSimpleString());
			
		}
		return sb.toString();
	}
	
	public void fromSimpleString(String s) {
		if (s != null) {
		//	this.xpath2num.clear();
			this.xpath2info.clear();
			String[] segs = StringUtil.split2(s, simple_seperator1);//s.split(seperator1);
		//	this.url = segs[0];
			for (int i = 0; i < segs.length; ++i) {
				String val = segs[i];
			//	System.out.println(val);
				String[] segs2 = StringUtil.split2(val, simple_seperator2);//val.split(seperator2);
				if (segs2.length == 2) {
					XpathInfo xi = new XpathInfo();
					xi.fromSimpleString(segs2[1]);
		//			System.out.println(segs2[0] + "\t" + num);
					this.xpath2info.put(segs2[0], xi);
				}
			}
		}
	}
	
	public String toString2() {
		StringBuffer sb = new StringBuffer();
		for (String xpath: xpath2info.keySet()) {
			sb.append(xpath.hashCode());
			sb.append(seperator2);
	//		sb.append(xpath2num.get(xpath));
	//		sb.append(seperator2);
			sb.append(xpath2info.get(xpath).toString());
			sb.append(seperator1);
		}
		if (sb.length() > 0) {
			sb.delete(sb.length()-seperator1.length(), sb.length());
		}
		return sb.toString();
	}
	

	
	public void fromString2(String s) {
		if (s != null) {
		//	this.xpath2num.clear();
			this.xpath2info.clear();
			String[] segs = s.split(seperator1);
			this.url = segs[0];
			for (int i = 1; i < segs.length; ++i) {
				String val = segs[i];
			//	System.out.println(val);
				String[] segs2 = val.split(seperator2);
				if (segs2.length == 2) {
			//		Integer num = Integer.valueOf(segs2[1]);
		//			System.out.println(segs2[0] + "\t" + num);
			//		this.xpath2num.put(segs2[0], num);
				//	Long num2 = Long.valueOf(segs2[2]);
					XpathInfo xi = new XpathInfo();
					xi.fromString(segs2[1]);
		//			System.out.println(segs2[0] + "\t" + num);
					this.xpath2info.put(segs2[0], xi);
				}
			}
		}
	}
	
	void tags2Shingles() {
		int window_length = 10;
		int left = 0; 
		int right = 0;
		int n = 0;
		for (int i = 0; i < tags.length(); ++i) {
			if (tags.charAt(i) == ' ') {
				++n;
				if (n >= window_length) {
					--n;
					right = i;
					shingles.put(tags.substring(left, right), tags.substring(left, right));
					while (left < i && tags.charAt(left) != ' ') {
						++left;
					}
					++left;
				}
			}
		}
	}
	/*
	double getDistance(HtmlPage hp) {
		Iterator<String> iter = hp.shingles.keySet().iterator();
		int comm = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			if (this.shingles.containsKey(temp)) {
				++comm;
			}
		}
	//	System.out.println(comm);
		int base = hp.shingles.size();
		if (base < this.shingles.size()) {
			base = this.shingles.size();
		}
		return 1.0 - (double)comm/(double)base;
	}
	*/

	/*
	double getDistance(HtmlPage hp) {
		Iterator<String> iter = hp.xpath2num.keySet().iterator();
		int comm = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			if (this.xpath2num.containsKey(temp)) {
				int t1 = hp.xpath2num.get(temp);
			//	int t2 = this.xpath2num.get(temp);
				comm += t1;
				//++comm;
			}
		}
		
		int all = 0;
		Collection<Integer> b1 = hp.xpath2num.values();
		for (Integer i: b1) {
			all += i;
		}
		double d1 = 1.0 - (double)comm/(double)all;
		
		iter = this.xpath2num.keySet().iterator();
		comm = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			if (hp.xpath2num.containsKey(temp)) {
				int t1 = this.xpath2num.get(temp);
			//	int t2 = this.xpath2num.get(temp);
				comm += t1;
				//++comm;
			}
		}
		
		all = 0;
		b1 = this.xpath2num.values();
		for (Integer i: b1) {
			all += i;
		}
		double d2 = 1.0 - (double)comm/(double)all;
		if (d2 > d1) {
			return d2;
		} else {
			return d1;
		}
	}
	*/
	
	double getDistance(HtmlPage hp) {
		Iterator iter = hp.xpath2info.entrySet().iterator();
		int comm = 0;
		int comm2 = 0;
		int all = 0;
		int comm_init2 = 0;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			String key = (String) entry.getKey();
			XpathInfo info1 = (XpathInfo) entry.getValue();
			int t1 = info1.num;
			XpathInfo info2 = this.xpath2info.get(key);
			if (info2 != null ) {
				int t2 = info2.num;
				if (info1.hash == info2.hash  || (info1.is_sidebar || info2.is_sidebar)) {
					comm_init2 += t2;
					t1 = 0;
					t2 = 0;
				}
			//	int t2 = this.xpath2num.get(temp);
				comm += t1;
				comm2 += t2;
				all += t1;
				//++comm;
			} else {
				all += t1;
			}
		}
		if (all == 0) {
			comm = 1;
			all = 1;
		}
		double d1 = 1.0 - (double)comm/(double)all;
		
		all = 0;
		for (XpathInfo xi: this.xpath2info.values()) {
			all += xi.num;
		}
		all -= comm_init2;
		if (all == 0) {
			comm2 = 1;
			all = 1;
		}
		double d2 = 1.0 - (double)comm2/(double)all;
		//return (d1+d2)/2;
		double min = d1, max = d2;
		if (min > max) {
			min = d2;
			max = d1;
		}
		return max;
	}
	
	double getDistance_debug(HtmlPage hp) {
		Iterator iter = hp.xpath2info.entrySet().iterator();
		int comm = 0;
		int comm2 = 0;
		int all = 0;
		int comm_init2 = 0;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
			String key = (String) entry.getKey();
			XpathInfo info1 = (XpathInfo) entry.getValue();
			int t1 = info1.num;
			XpathInfo info2 = this.xpath2info.get(key);
			if (info2 != null ) {
				int t2 = info2.num;
				if (info1.hash == info2.hash  || (info1.is_sidebar || info2.is_sidebar)) {
					comm_init2 += t2;
					t1 = 0;
					t2 = 0;
				}
			//	int t2 = this.xpath2num.get(temp);
				comm += t1;
				comm2 += t2;
				all += t1;
				System.out.println("0\t" + t1 + "\t" + key);
				//++comm;
			} else {
				all += t1;
				System.out.println("1\t" + t1 + "\t" + key);
			}
		}
		if (all == 0) {
			comm = 1;
			all = 1;
		}
		double d1 = 1.0 - (double)comm/(double)all;
		System.out.println("d  " + d1);
		
		all = 0;
		for (XpathInfo xi: this.xpath2info.values()) {
			all += xi.num;
		}
		all -= comm_init2;
		if (all == 0) {
			comm2 = 1;
			all = 1;
		}
		double d2 = 1.0 - (double)comm2/(double)all;
		//return (d1+d2)/2;
		double min = d1, max = d2;
		if (min > max) {
			min = d2;
			max = d1;
		}
		return max;
	}
	
	/*
	double getDistance_max(HtmlPage hp) {
		Iterator<String> iter = hp.xpath2num.keySet().iterator();
		int comm = 0;
		int all = 0;
		int original_length = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			int t1 = hp.xpath2num.get(temp);
			original_length += t1;
			if (this.xpath2num.containsKey(temp)) {
				
				XpathInfo info1 = hp.xpath2info.get(temp);
				XpathInfo info2 = this.xpath2info.get(temp);
				if (info1.hash == info2.hash || (info1.is_sidebar && info2.is_sidebar)) {
					t1 = t1/3000;
				}
			//	int t2 = this.xpath2num.get(temp);
				comm += t1;
				all += t1;
				//++comm;
			} else {
				all += t1;
			}
		}
		if (all == 0) {
			comm = 1;
			all = 1;
		}
		double d1 = 1.0 - (double)comm/(double)all;
		
		int original_length2 = 0;
		iter = this.xpath2num.keySet().iterator();
		comm = 0;
		all = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			int t1 = this.xpath2num.get(temp);
			original_length2 += t1;
			if (hp.xpath2num.containsKey(temp)) {
				
			//	int t2 = this.xpath2num.get(temp);
				XpathInfo info1 = hp.xpath2info.get(temp);
				XpathInfo info2 = this.xpath2info.get(temp);
				if (info1.hash == info2.hash || (info1.is_sidebar && info2.is_sidebar)) {
					t1 = t1/3000;
				}
				comm += t1;
				all += t1;
				//++comm;
			} else {
				all += t1;
			}
		}
		if (all == 0) {
			comm = 1;
			all = 1;
		}
		double d2 = 1.0 - (double)comm/(double)all;
		//return (d1+d2)/2;
		double min = d1, max = d2;
		if (min > max) {
			min = d2;
			max = d1;
		}
		return max;
	}
	
	double getDistance(HtmlPage hp) {
		Iterator<String> iter = hp.xpath2num.keySet().iterator();
		int comm = 0;
		int all = 0;
		boolean text_is_diff = false;
		int original_length = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			int t1 = hp.xpath2num.get(temp);
			original_length += t1;
			if (this.xpath2num.containsKey(temp)) {
				
				XpathInfo info1 = hp.xpath2info.get(temp);
				XpathInfo info2 = this.xpath2info.get(temp);
				if (info1.hash == info2.hash || (info1.is_sidebar && info2.is_sidebar)) {
					t1 = t1/3000;
				}
				int t2 = this.xpath2num.get(temp);
				comm += t1;
				all += t1;
				//++comm;
			} else {
				all += t1;
			}
		}
		if (all == 0) {
			comm = 1;
			all = 1;
		}
		double d1 = 1.0 - (double)comm/(double)all;
		
		int original_length2 = 0;
		iter = this.xpath2num.keySet().iterator();
		int comm_backup = comm;
		comm = 0;
		all = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			int t1 = this.xpath2num.get(temp);
			original_length2 += t1;
			if (hp.xpath2num.containsKey(temp)) {
				
			//	int t2 = this.xpath2num.get(temp);
				XpathInfo info1 = hp.xpath2info.get(temp);
				XpathInfo info2 = this.xpath2info.get(temp);
				if (info1.hash == info2.hash || (info1.is_sidebar && info2.is_sidebar)) {
					t1 = t1/3000;
				}
				comm += t1;
				all += t1;
				//++comm;
			} else {
				all += t1;
			}
		}
		if (all == 0) {
			comm = 1;
			all = 1;
		}
		double d2 = 1.0 - (double)comm/(double)all;
		
		if (d1 < 0.1) {
			if (comm_backup-comm*2 > 200) {
				text_is_diff = true;
			}
		}
		if (d2 < 0.1) {
			if (comm-comm_backup*2 > 200) {
				text_is_diff = true;
			}
		}
		//return (d1+d2)/2;
		
		double min = d1, max = d2;
		if (min > max) {
			min = d2;
			max = d1;
		}

		//System.out.println(min + "\t" + this.url + "\t" + hp.url);
		return max;

	}
	*/
	/*
	double getDistance_debug(HtmlPage hp) {
		Iterator<String> iter = hp.xpath2num.keySet().iterator();
		int comm = 0;
		int all = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			int t1 = hp.xpath2num.get(temp);
			if (this.xpath2num.containsKey(temp)) {
				
				XpathInfo info1 = hp.xpath2info.get(temp);
				XpathInfo info2 = this.xpath2info.get(temp);
				if (info1.hash == info2.hash || (info1.is_sidebar && info2.is_sidebar)) {
					t1 = t1/3000;
				}
			//	int t2 = this.xpath2num.get(temp);
				comm += t1;
				all += t1;
				System.out.println("0  " +t1 + "\t" +  temp);
				//++comm;
			} else {
				all += t1;
				System.out.println("1  " + t1+ "\t" +  temp);
			}
		}
		if (all == 0) {
			comm = 1;
			all = 1;
		}
		double d1 = 1.0 - (double)comm/(double)all;
		
		iter = this.xpath2num.keySet().iterator();
		int comm_backup = comm;
		comm = 0;
		all = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			int t1 = this.xpath2num.get(temp);
			if (hp.xpath2num.containsKey(temp)) {
				
			//	int t2 = this.xpath2num.get(temp);
				XpathInfo info1 = hp.xpath2info.get(temp);
				XpathInfo info2 = this.xpath2info.get(temp);
				if (info1.hash == info2.hash || (info1.is_sidebar && info2.is_sidebar)) {
					t1 = t1/3000;
				}
				comm += t1;
				all += t1;
				
				//++comm;
			} else {
				all += t1;
				
			}
		}
		if (all == 0) {
			comm = 1;
			all = 1;
		}
		double d2 = 1.0 - (double)comm/(double)all;
		
		System.out.println("sub d:  " + d1 + "\t" + d2);
		boolean text_is_diff = false;
		if (d1 < 0.1) {
			if (comm_backup-comm*2 > 200) {
				text_is_diff = true;
			}
		}
		if (d2 < 0.1) {
			if (comm-comm_backup*2 > 200) {
				text_is_diff = true;
			}
		}
		//return (d1+d2)/2;
		double min = d1, max = d2;
		if (min > max) {
			min = d2;
			max = d1;
		}
		if (min < 0.1 && max > 0.8 && text_is_diff) {
			return 0.6;
		}
		return min;
	}
	*/
	/*
	double getDistance_debug(HtmlPage hp) {
		Iterator<String> iter = hp.xpath2num.keySet().iterator();
		int comm = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			if (this.xpath2num.containsKey(temp)) {
				int t1 = hp.xpath2num.get(temp);
				System.out.println("0  " + hp.xpath2num.get(temp) + "\t" +  temp);
			//	int t2 = this.xpath2num.get(temp);
				comm += t1;
				//++comm;
			} else {
				System.out.println("1  " + hp.xpath2num.get(temp) + "\t" +  temp);
			}
		}
		
		int all = 0;
		Collection<Integer> b1 = hp.xpath2num.values();
		for (Integer i: b1) {
			all += i;
		}
		double d1 = 1.0 - (double)comm/(double)all;
		
		iter = this.xpath2num.keySet().iterator();
		comm = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			if (hp.xpath2num.containsKey(temp)) {
				int t1 = this.xpath2num.get(temp);
			//	int t2 = this.xpath2num.get(temp);
				comm += t1;
				//++comm;
			}
		}
		
		all = 0;
		b1 = this.xpath2num.values();
		for (Integer i: b1) {
			all += i;
		}
		double d2 = 1.0 - (double)comm/(double)all;
		System.out.println("sub d:  " + d1 + "\t" + d2);
		if (d2 > d1) {
			return d2;
		} else {
			return d1;
		}
	}
	*/
	/*
	double getDistance_debug(HtmlPage hp) {
		Iterator<String> iter = hp.xpath2num.keySet().iterator();
		int comm = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			if (this.xpath2num.containsKey(temp)) {
				System.out.println("0  " + hp.xpath2num.get(temp) + "\t" +  temp);
				int t1 = hp.xpath2num.get(temp);
			//	int t2 = this.xpath2num.get(temp);
				comm += t1;
				//++comm;
			} else {
				System.out.println("1  " + hp.xpath2num.get(temp) + "\t" +  temp);
			}
		}
		
		int all = 0;
		Collection<Integer> b1 = hp.xpath2num.values();
		for (Integer i: b1) {
			all += i;
		}
		Collection<Integer> b2 = this.xpath2num.values();
		for (Integer i: b2) {
	//		all += i;
		}
	//	System.out.println("comm: " + comm);
		int base = hp.xpath2num.size();
		if (base < this.xpath2num.size()) {
			base = this.xpath2num.size();
		}
		System.out.println("debug: " + comm + "\t" + all);
		return 1.0 - (double)comm/(double)all;
	}
	*/
	/*
	double getDistance_debug(HtmlPage hp) throws InterruptedException {
		
		int window_length = 10;
		int left = 0; 
		int right = 0;
		int n = 0;
		int same = 0;
		int diff = 0;
		for (int i = 0; i < tags.length(); ++i) {
			if (tags.charAt(i) == ' ') {
				++n;
				if (n >= window_length) {
					--n;
					right = i;
					String temp = tags.substring(left, right);
					right--;
					while (right > left && tags.charAt(right) != ' ') {
						--right;
					}
					String end_tag = tags.substring(right+1, i);

					if (hp.shingles.containsKey(temp)) {
						++same;
						System.out.print(end_tag + " ");
						System.out.flush();
						Thread.sleep(1);
					} else {
						++diff;
						System.err.print(end_tag + " ");
						System.err.flush();
						Thread.sleep(1);
					}
					while (left < i && tags.charAt(left) != ' ') {
						++left;
					}
					++left;
				}
			}
		}
		System.out.print("\n");
		System.out.println("same and diff tag num: " + same + "\t" + diff);
		Iterator<String> iter = hp.shingles.keySet().iterator();
		int comm = 0;
		while (iter.hasNext()) {
			String temp = iter.next();
			if (this.shingles.containsKey(temp)) {
				++comm;
			}
		}
	//	System.out.println(comm);
		int base = hp.shingles.size();
		if (base < this.shingles.size()) {
			base = this.shingles.size();
		}
		return 1.0 - (double)comm/(double)base;
	}
	*/
}
