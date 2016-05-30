package com.sogou.cm.pa.multipage.maincontent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

import org.xml.sax.SAXException;

import com.sogou.cm.pa.maincontent.HtmlContentHandler;
import com.sogou.cm.pa.pagecluster.ClusterInfo;
import com.sogou.cm.pa.pagecluster.HtmlPage;
import com.sogou.cm.pa.pagecluster.PageSegmentation;
import com.sogou.web.selector.urllib.URLUtils;

public class Tester {
	public static String getMemStat() {
		 SimpleDateFormat STANDARD_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 
	  Runtime runtime = Runtime.getRuntime();
	Date date = new Date(System.currentTimeMillis());
				StringBuilder sb = new StringBuilder(1024);
				sb.append("[").append(STANDARD_FORMAT.format(date)).append("]");
				sb.append("[").append(runtime.totalMemory() >> 20).append(":").append(runtime.freeMemory() >> 20)
						.append("] ");
				sb.append("");
				return (sb.toString());
	}
	

	
	public static void main(String[] args) throws IOException, SAXException {
		String s = "企业客服直线： 010-84450630 84450639 84450561 84450633 84926436 84926406 84450591 84450627";
//				+ "Ne\n\t\r   365.com All Rights Reserved 广州新观点信息科技有限公司 版权所有\n  ";
		System.out.println(HtmlContentHandler.containContract(s));
		

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (c == '。' || c == '，' || c == ','||c == '!'||c == '！' || c == '\u00a0' || c == '\u3000') {
				sb.append(' ');
			} else {
				sb.append(c);
			}
			if (c == ':' || c == '：' || c == ' ' || c == '\u00a0' || c == '\u3000') {
				while (i+1<s.length()) {
					c = s.charAt(i+1);
					if (c == ' ' || c == '\u00a0' || c == '\u3000') {
						++i;
					} else {
						break;
					}
				}
				if (i+1<s.length() && s.charAt(i+1)=='(' && sb.charAt(sb.length()-1)==' ') {
					sb.deleteCharAt(sb.length()-1);
				}
			}
		}
		String[] segs = sb.toString().split(" ");
		StringBuffer contact = new StringBuffer();
		for (String seg: segs) {
	//		System.out.println(seg);
			if(HtmlContentHandler.containContract(seg)) {
				if (contact.length() > 0) {
					contact.append(' ');
				}
				contact.append(seg);
			}
		}
		System.out.println(contact);
		
		byte[] begin_flag = new byte[]{(byte) 0x0c, (byte) 0xe5};
		String begin_flag_s = "\ue50c";  //new String(begin_flag, "UTF-16LE");
		byte[] bs = begin_flag_s.getBytes("utf8");
		for (byte b: bs) {
			System.out.format("%x  ", b);
		}
		
		
	}
}
