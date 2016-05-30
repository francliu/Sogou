package com.sogou.pa.ListPage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ModelTester2 {
	public static void main(String[] args) throws IOException, SAXException {
		int index = 1739;
		FileInputStream f_stream = new FileInputStream(new File("./page_out/" +index+".html"));
		byte[] bs = new byte[1024*1024*4];
		int len = f_stream.read(bs);
		String s = new String(bs, 0, len, "UTF-8");
		ListBlockExtractor		htmlContentHandler		= new ListBlockExtractor();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		
		try {
	//		htmlContentHandler.setUrl("http://www.iqiyi.com/yinyue/20121214/10453bddc18b141b.html");
			parser.parse(new InputSource(new StringReader(s)));
			if (htmlContentHandler.red != null) {
				System.out.println(htmlContentHandler.red.has_list);
			}
			Counter cnt = new Counter();
			String out = htmlContentHandler.root.traverse_debug(cnt);
		//	System.out.println(out);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("tt.html")), "utf8"));
		//	writer.write("<base href=\"" + opw.url.toString() + "\">\n");
		//	writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf8\">\n");
			writer.write(out);
			writer.flush();
			writer.close();
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
}
