package com.sogou.cm.pa.pagecluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

public class StringUtil {
	public static List<String> split(String ss, String sep) {
		ArrayList<String> segs = new ArrayList<String>();
		StringBuilder s = new StringBuilder(ss);
		int beginindex = 0;
		for (int i = 0; i <= s.length() - sep.length(); ++i) {
			if (s.substring(i, i+sep.length()).equals(sep)) {
				//segs.add(s.substring(beginindex, i));
				beginindex = i + sep.length();
				i = i + sep.length() - 1;
			}
		}
		if (beginindex < s.length()) {
			segs.add(s.substring(beginindex));
		}
		return segs;
	}
	
	public static String[] split2(String s, String sep) {
		ArrayList<String> segs = new ArrayList<String>();
		int pos , prev = 0;
		while ((pos = s.indexOf(sep, prev)) >= 0) {
			segs.add(s.substring(prev, pos));
			prev = pos + sep.length();
		}
		if (prev < s.length()) {
			segs.add(s.substring(prev));
		}
		String[] re = new String[segs.size()];
		segs.toArray(re);
		return re;
	}
	
	
	public static void main(String[] args) throws IOException {
		FileInputStream f_stream = new FileInputStream(new File("C:\\Users\\sunjian\\Downloads\\t.txt"));
		byte[] bs = new byte[1024*1024*4];
		int len = f_stream.read(bs);
		String s = new String(bs, 0, len, "UTF-8");
		KMP k = new KMP();
	//	k.kmp(s, "abcd");
	//	String s = "234eryasdf234fsadasdfgsadfasdfsadfsdf234asdfcghsfdfgasdfasdgxdvaw45qwdzfbgadft34123asdfasdf12345hsdfghsdfgsd1234qwefasdfasdf";
		long a = System.currentTimeMillis();
		for (int i = 0; i < 10000; ++i) {
		//	k.kmp(s, "n3k8y");
			String[] segs = StringUtil.split2(s, "n3k8y");
		}
		System.out.println(System.currentTimeMillis()-a);
		a = System.currentTimeMillis();
		for (int i = 0; i < 10000; ++i) {
			String[] segs = s.split("n3k8y");
		}
		System.out.println(System.currentTimeMillis()-a);
		a = System.currentTimeMillis();
		for (int i = 0; i < 10000; ++i) {
			StringTokenizer st = new StringTokenizer(s, "n3k8y");

		}
		System.out.println(System.currentTimeMillis()-a);
		
		a = System.currentTimeMillis();
		for (int i = 0; i < 10000; ++i) {
			String[] segs = StringUtils.splitByWholeSeparator(s, "n3k8y");

		}
		System.out.println(System.currentTimeMillis()-a);
		
		
		/*
		for (String ss: segs) {
			System.out.println(ss);
		}
		*/
	}
}
