package com.sogou.cm.pa.maincontent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

public class MainContentTester {
	public static void main(String[] args) throws IOException {
		RuleBasedMainContentExtractor main_content = new RuleBasedMainContentExtractor();
		FileInputStream f_stream = new FileInputStream(new File(args[0]));
		byte[] bs = new byte[1024*1024*4];
		int len = f_stream.read(bs);
		String s = new String(bs, 0, len, "UTF-8");
		String out = "";
		String out2 = "";
		String url = args[1];
		List<Element> blocks = main_content.extractMainContent(url, s);
		Tool.quickSortY(blocks, 0, blocks.size()-1);
		Writer writer = new OutputStreamWriter(new FileOutputStream("url_hash.txt"), "utf-8"); 
		writer.write(Integer.toHexString(url.hashCode()));
		writer.close();
		Writer writer1 = new OutputStreamWriter(new FileOutputStream("maincontent1.txt"), "utf-8"); ;
		Writer writer2 = new OutputStreamWriter(new FileOutputStream("maincontent2.txt"), "utf-8"); 
		for (int i = 0; i < blocks.size(); ++i) {
			Element f = blocks.get(i);
		//	System.out.println(f.is_list + "\t" + f.text);
		//	System.out.println(f.text_len - f.anchor_len + "\t" + f.text);
		//	System.out.println(f.anchor_len + "\t" + (f.text_len-f.anchor_len) + "\t" + f.max_text_len + "\t" + f.text_num + "\t" + f.text);
	        out += String.format("<div style=\"position: absolute; left: %dpx; top: %dpx; width: %dpx; height: %dpx;  border:3px solid red;\">%s</div>\n",f.left, f.top, f.width, f.height, f.xpath + "\t" + f.text.toString());
	        out2 += String.format("<div style=\"position: absolute; left: %dpx; top: %dpx; width: %dpx; height: %dpx;  border:3px solid red;\"></div>\n",f.left, f.top, f.width, f.height);
		//	System.out.println(f.toPrintString());
		}
		writer1.write("<meta charset=\"utf8\">\n");
		writer1.write(out);
		writer2.write(out2);
		writer1.close();
		writer2.close();
		System.out.println(out);
	}
}
