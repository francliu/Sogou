package com.sogou.cm.pa.maincontent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MainContentTester2 {
	public static void main(String[] args) throws IOException {
		RuleBasedMainContentExtractor main_content = new RuleBasedMainContentExtractor();
		FileInputStream f_stream = new FileInputStream(new File("t2.html"));
		byte[] bs = new byte[1024*1024*4];
		int len = f_stream.read(bs);
		String s = new String(bs, 0, len, "UTF-8");
		String out = "";
		String out2 = "";
		String url = "http://item.jd.com/1325076964.html";
		List<Element> blocks = new ArrayList<Element>();
		long start = System.currentTimeMillis() ;
		for (int i = 0; i < 1000; ++i) {
			blocks = main_content.extractMainContent(url, s);
		}
		long end = System.currentTimeMillis();
		System.out.println("elapse time: " + (end-start));
		
		f_stream = new FileInputStream(new File("t2.html"));
		len = f_stream.read(bs);
		s = new String(bs, 0, len, "UTF-8");

		url = "http://www.jobui.com/company/7570053/";

			blocks = main_content.extractMainContent(url, s);
		
		
		Tool.quickSortY(blocks, 0, blocks.size()-1);

		for (int i = 0; i < blocks.size(); ++i) {
			Element f = blocks.get(i);
		//	System.out.println(f.is_list + "\t" + f.text);
		//	System.out.println(f.text_len - f.anchor_len + "\t" + f.text);
		//	System.out.println(f.anchor_len + "\t" + (f.text_len-f.anchor_len) + "\t" + f.max_text_len + "\t" + f.text_num + "\t" + f.text);
	        out += String.format("<div style=\"position: absolute; left: %dpx; top: %dpx; width: %dpx; height: %dpx;  border:3px solid red;\">%s</div>\n",f.left, f.top, f.width, f.height, f.xpath + "\t" + f.text.toString());
	//        out2 += String.format("<div style=\"position: absolute; left: %dpx; top: %dpx; width: %dpx; height: %dpx;  border:3px solid red;\"></div>\n",f.left, f.top, f.width, f.height);
		//	System.out.println(f.toPrintString());
		}
		System.out.println(out);
	}
}
