package com.sogou.pa.ListPage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Random;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class ModelTester {
	public static void main(String[] args) throws IOException, SAXException {
		int[] red = new int[3000];
		int[] green = new int[3000];
		BufferedReader reader = new BufferedReader(new FileReader(new File("marked_result.txt")));
		int cnt = 0;
		String line;
		while ((line = reader.readLine()) != null) {
			String[] segs = line.split("\t");
			if (segs.length == 3) {
				int pos1 = segs[0].lastIndexOf('/');
				int pos2 = segs[0].lastIndexOf('.');
				if (pos2 < 0 || pos1 < 0 || pos2 < pos1) {
					System.out.println(line);
				} else {
					int index = Integer.valueOf(segs[0].substring(pos1+1, pos2));
					if (segs[1].equals("0")) {
						cnt++;
						red[index] = 0;
					} else if (segs[1].equals("1")) {
				//		cnt++;
						red[index] = 1;
					} else {
						red[index] = 2;
					}
					if (segs[2].equals("0")) {
						cnt++;
						green[index] = 0;
					} else if (segs[2].equals("1")) {
						green[index] = 1;
				//		cnt++;
					} else {
						green[index] = 2;
					}
				}
			}
		}
		reader.close();
		
		reader = new BufferedReader(new FileReader(new File("correct_data.txt")));
		while ((line = reader.readLine()) != null) {
			String[] segs = line.split("\t");
			if (segs.length == 5) {
				if (segs[4].equals("1")) {
					if (segs[1].equals("red")) {
						int t = Integer.valueOf(segs[0]);
						if (red[t] == 0) {
							red[t] = 1;
						} else if (red[t] == 1) {
							red[t] = 0;
						}
					} else if (segs[1].equals("green")) {
						int t = Integer.valueOf(segs[0]);
						if (green[t] == 0) {
							green[t] = 1;
						} else if (green[t] == 1) {
							green[t] = 0;
						}
					}
				} else if (segs[4].equals("2")) {
					if (segs[1].equals("red")) {
						int t = Integer.valueOf(segs[0]);
						red[t] = 2;
					} else if (segs[1].equals("green")) {
						int t = Integer.valueOf(segs[0]);
						green[t] = 2;
					}
				}
			}
		}
		reader.close();

		int num = 0;
		Random random = new Random();
		int cnt1 = 0, cnt2 = 0;
		int err = 0;
		int all = 0;
		for (int index = 0; index <= 2724; index++) {
			
			if (index%200==0) {
				System.out.println("process: " + all + "\t" + err);
			}

			
			try {
				FileInputStream f_stream = new FileInputStream(new File("./page_out/" +index+".html"));
				byte[] bs = new byte[1024*1024*4];
				int len = f_stream.read(bs);
				String s = new String(bs, 0, len, "UTF-8");
				int pos1 = s.indexOf('"');
				int pos2 = s.indexOf('"', pos1+1);
				String url = s.substring(pos1+1, pos2);
		//	System.out.println(url);
				ListBlockExtractor		htmlContentHandler		= new ListBlockExtractor();
				Parser					parser					= new Parser();
				parser.setContentHandler(htmlContentHandler);
		//		htmlContentHandler.setUrl("http://www.iqiyi.com/yinyue/20121214/10453bddc18b141b.html");
				parser.parse(new InputSource(new StringReader(s)));

				if (red[index] == 0 || red[index] == 1) {
					if (htmlContentHandler.red != null) {
						num++;
						Element t = htmlContentHandler.red;
						if ((t.is_list && red[index] == 0) || (!t.is_list && red[index] == 1)) {
							err++;
							System.out.println(index + "\tred\t" + url + "\t" + red[index]);
						}
						all++;
					} else {
						System.out.println("index: " + index);
					}
				}
				if (green[index] == 0 || green[index] == 1) {
					if (htmlContentHandler.green != null) {
						num++;
						all++;
						Element t = htmlContentHandler.green;
						if ((t.is_list && green[index] == 0) || (!t.is_list && green[index] == 1)) {
							err++;
							System.out.println(index + "\tgreen\t" + url + "\t" + green[index]);
						}

					} else {
						System.out.println("index2: " + index);
					}
				}
		//		String out = htmlContentHandler.root.traverse_debug();
		//		System.out.println(out);
			}  catch (IOException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			}
		}

		System.out.println("data split: " + all + "\t" + err);
	}
}
