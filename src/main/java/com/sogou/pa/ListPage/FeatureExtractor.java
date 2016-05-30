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


public class FeatureExtractor {
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
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("list_block1.arff")), "utf8"));
		System.out.println("train set num: 	" + cnt);
		writer.write("@relation listblock\n");
		writer.write("@attribute index numeric\n");
		writer.write("@attribute index2 numeric\n");
		writer.write("@attribute index3 {red, green}\n");
	//	writer.write("@attribute url string\n");
		writer.write("@attribute is_displaynone {true, false}\n");
		writer.write("@attribute top numeric\n");
		writer.write("@attribute left numeric\n");
		writer.write("@attribute height numeric\n");
		writer.write("@attribute width numeric\n");
		writer.write("@attribute area numeric\n");
		writer.write("@attribute text_len numeric\n");
		writer.write("@attribute own_text_len numeric\n");
		writer.write("@attribute anchor_len numeric\n");
		writer.write("@attribute anchor_num numeric\n");
		writer.write("@attribute anchor2text numeric\n");
		writer.write("@attribute img_num numeric\n");
		writer.write("@attribute child_num numeric\n");
		writer.write("@attribute fake_child_num numeric\n");
		writer.write("@attribute input_area numeric\n");
		writer.write("@attribute area_list numeric\n");
		writer.write("@attribute area_other numeric\n");
		writer.write("@attribute list_num1 numeric\n");
		writer.write("@attribute strict_list_num1 numeric\n");
		writer.write("@attribute indiffent_child_num1 numeric\n");
		writer.write("@attribute list_num2 numeric\n");
		writer.write("@attribute strict_list_num2 numeric\n");
		writer.write("@attribute indiffent_child_num2 numeric\n");
		writer.write("@attribute list_num3 numeric\n");
		writer.write("@attribute indiffent_child_num3 numeric\n");
		writer.write("@attribute big_list_num numeric\n");
		writer.write("@attribute biggest_child_area numeric\n");
		writer.write("@attribute biggest_img_area numeric\n");
		writer.write("@attribute biggest_img_width numeric\n");
		writer.write("@attribute most_img_num numeric\n");
		writer.write("@attribute child_arrange_style numeric\n");
		writer.write("@attribute first_second_child_ratio numeric\n");
		writer.write("@attribute tobottom numeric\n");
		writer.write("@attribute longest_own_text_len numeric\n");
		writer.write("@attribute child_height2width numeric\n");
		writer.write("@attribute tall_child_num numeric\n");
		writer.write("@attribute friend_link_ratio numeric\n");
		writer.write("@attribute full_list_ratio numeric\n");
		writer.write("@attribute full_list_len numeric\n");
		writer.write("@attribute pure_text_len numeric\n");
		writer.write("@attribute has_intro {true, false}\n");
		writer.write("@attribute has_sub_title {true, false}\n");
		
		
		writer.write("@attribute is_sub_title {true, false}\n");
		writer.write("@attribute has_merged {true, false}\n");
		writer.write("@attribute is_bold {true, false}\n");
		writer.write("@attribute is_time {true, false}\n");
		writer.write("@attribute list_of_subtitle {true, false}\n");
		writer.write("@attribute list_of_keyword {true, false}\n");
		writer.write("@attribute has_repeat_sb_structure {true, false}\n");
		writer.write("@attribute has_other_block {true, false}\n");
		writer.write("@attribute child_is_small {true, false}\n");
		writer.write("@attribute has_one_child {true, false}\n");
		writer.write("@attribute is_list {0, 1}\n");
		writer.write("@data\n");
		
		
		BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("list_block2.arff")), "utf8"));
		writer2.write("@relation listblock\n");
		writer2.write("@attribute index numeric\n");
		writer2.write("@attribute index2 numeric\n");
		writer2.write("@attribute index3 {red, green}\n");
	//	writer2.write("@attribute url string\n");
		writer2.write("@attribute is_displaynone {true, false}\n");
		writer2.write("@attribute top numeric\n");
		writer2.write("@attribute left numeric\n");
		writer2.write("@attribute height numeric\n");
		writer2.write("@attribute width numeric\n");
		writer2.write("@attribute area numeric\n");
		writer2.write("@attribute text_len numeric\n");
		writer2.write("@attribute own_text_len numeric\n");
		writer2.write("@attribute anchor_len numeric\n");
		writer2.write("@attribute anchor_num numeric\n");
		writer2.write("@attribute anchor2text numeric\n");
		writer2.write("@attribute img_num numeric\n");
		writer2.write("@attribute child_num numeric\n");
		writer2.write("@attribute fake_child_num numeric\n");
		writer2.write("@attribute input_area numeric\n");
		writer2.write("@attribute area_list numeric\n");
		writer2.write("@attribute area_other numeric\n");
		writer2.write("@attribute list_num1 numeric\n");
		writer2.write("@attribute strict_list_num1 numeric\n");
		writer2.write("@attribute indiffent_child_num1 numeric\n");
		writer2.write("@attribute list_num2 numeric\n");
		writer2.write("@attribute strict_list_num2 numeric\n");
		writer2.write("@attribute indiffent_child_num2 numeric\n");
		writer2.write("@attribute list_num3 numeric\n");
		writer2.write("@attribute indiffent_child_num3 numeric\n");
		writer2.write("@attribute big_list_num numeric\n");
		writer2.write("@attribute biggest_child_area numeric\n");
		writer2.write("@attribute biggest_img_area numeric\n");
		writer2.write("@attribute biggest_img_width numeric\n");
		writer2.write("@attribute most_img_num numeric\n");
		writer2.write("@attribute child_arrange_style numeric\n");
		writer2.write("@attribute first_second_child_ratio numeric\n");
		writer2.write("@attribute tobottom numeric\n");
		writer2.write("@attribute longest_own_text_len numeric\n");
		writer2.write("@attribute child_height2width numeric\n");
		writer2.write("@attribute tall_child_num numeric\n");
		writer2.write("@attribute friend_link_ratio numeric\n");
		writer2.write("@attribute full_list_ratio numeric\n");
		writer2.write("@attribute full_list_len numeric\n");
		writer2.write("@attribute pure_text_len numeric\n");
		writer2.write("@attribute has_intro {true, false}\n");
		writer2.write("@attribute has_sub_title {true, false}\n");
		
		
		writer2.write("@attribute is_sub_title {true, false}\n");
		writer2.write("@attribute has_merged {true, false}\n");
		writer2.write("@attribute is_bold {true, false}\n");
		writer2.write("@attribute is_time {true, false}\n");
		writer2.write("@attribute list_of_subtitle {true, false}\n");
		writer2.write("@attribute list_of_keyword {true, false}\n");
		writer2.write("@attribute has_repeat_sb_structure {true, false}\n");
		writer2.write("@attribute has_other_block {true, false}\n");
		writer2.write("@attribute child_is_small {true, false}\n");
		writer2.write("@attribute has_one_child {true, false}\n");
		writer2.write("@attribute is_list {0, 1}\n");
		writer2.write("@data\n");
		int num = 0;
		Random random = new Random();
		int cnt1 = 0, cnt2 = 0;
		for (int index = 0; index <= 2724; index++) {
			
			if (index%200==0) {
				System.out.println("process: " + index + "\t" + num);
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
				int n = random.nextInt(2);
				n = 0;
				if (red[index] == 0 || red[index] == 1) {
					if (htmlContentHandler.red != null) {
						num++;
						Element t = htmlContentHandler.red;
			//			System.out.println(t.list_num2);
					//	writer.write(num + "," + index + "," + "red" + "," +  url + "," + t.is_displaynone + "," + t.top + "," + t.left + "," + t.height + "," + t.width + "," + t.area + "," + t.text_len + "," + t.own_text_len + "," + t.anchor_len + "," + t.anchor_num + "," + t.img_num + "," + t.child_num + "," + t.fake_child_num + "," + t.input_area + "," + t.area_list + "," + t.area_other + "," + t.list_num1 + "," + t.strict_list_num1 + "," + t.indiffent_child_num1 + "," + t.list_num2 + "," + t.strict_list_num2 + "," + t.indiffent_child_num2 + "," + t.list_num3 + "," + t.indiffent_child_num3 + "," + t.big_list_num + "," + t.is_sub_title + "," + t.has_merged + "," + t.is_bold + "," + t.is_time + "," + t.list_of_subtitle + "," + t.list_of_keyword + "," + t.has_repeat_sb_structure + "," + t.has_other_block + "," + t.child_is_small + "," + t.has_one_child + "," + red[index] + "\n");
						
						String out = num + "," + index + "," + "red"+ "," + t.is_displaynone + "," + Math.atan(t.top) + "," + Math.atan(t.left) + "," + Math.atan(t.height) + "," + Math.atan(t.width) + "," + Math.atan(t.area) + "," + Math.atan(t.text_len) + "," + Math.atan(t.own_text_len) + "," + Math.atan(t.anchor_len) + "," + Math.atan(t.anchor_num) + "," + Math.atan((double)t.anchor_len/(t.text_len-t.anchor_len)) + "," + Math.atan(t.img_num) + "," + Math.atan(t.child_num) + "," + Math.atan(t.fake_child_num) + "," + Math.atan(t.input_area) + "," + Math.atan(t.area_list) + "," + Math.atan(t.area_other) + "," + Math.atan(t.list_num1) + "," + Math.atan(t.strict_list_num1) + "," + Math.atan(t.indiffent_child_num1) + "," + Math.atan(t.list_num2) + "," + Math.atan(t.strict_list_num2) + "," + Math.atan(t.indiffent_child_num2) + "," + Math.atan(t.list_num3) + "," + Math.atan(t.indiffent_child_num3) + "," + Math.atan(t.big_list_num) + "," + Math.atan(t.biggest_child_area) + "," + Math.atan(t.biggest_img_area) + "," + Math.atan(t.biggest_img_width) + "," + Math.atan(t.most_img_num) + "," + Math.atan(t.child_arrange_style) + "," + Math.atan(t.first_second_child_ratio) + "," + Math.atan(t.tobottom) + "," + Math.atan(t.longest_own_text_len) + "," + Math.atan(t.child_height2width) + "," + Math.atan(t.tall_child_num) + "," + Math.atan((double)t.friend_link_area/(t.area+1)) + "," + Math.atan((double)t.full_list_area/(t.area+1)) + "," + Math.atan(t.text_len-t.full_list_len) + "," + Math.atan(t.text_len-t.anchor_len) + "," + t.has_intro + "," + t.has_sub_title + "," + t.is_sub_title + "," + t.has_merged + "," + t.is_bold + "," + t.is_time + "," + t.list_of_subtitle + "," + t.list_of_keyword + "," + t.has_repeat_sb_structure + "," + t.has_other_block + "," + t.child_is_small + "," + t.has_one_child + "," + red[index] + "\n";

						if (n == 0) {
							cnt1++;
							writer.write(out);
						} else if (n == 1) {
							cnt2++;
							writer2.write(out);
						} else {
							System.out.println("errorrrrrrrrrrrrrrr.");
						}
				//		if (t.img_num > 0 && t.top < 50 && t.width > 800 && t.height < 200 && red[index] == 1) {
						if (t.has_intro) {
					//		if (t.has_sub_title) {
								System.out.println(index + "\t" + "red" + "\t" + url + "\t" +  red[index]);
							
						}
					} else {
						System.out.println("index: " + index);
					}
				}
				if (green[index] == 0 || green[index] == 1) {
					if (htmlContentHandler.green != null) {
						num++;
						Element t = htmlContentHandler.green;
				//		writer.write(num + "," + index + "," + "green" + "," +  url + "," + t.is_displaynone + "," + t.top + "," + t.left + "," + t.height + "," + t.width + "," + t.area + "," + t.text_len + "," + t.own_text_len + "," + t.anchor_len + "," + t.anchor_num + "," + t.img_num + "," + t.child_num + "," + t.fake_child_num + "," + t.input_area + "," + t.area_list + "," + t.area_other + "," + t.list_num1 + "," + t.strict_list_num1 + "," + t.indiffent_child_num1 + "," + t.list_num2 + "," + t.strict_list_num2 + "," + t.indiffent_child_num2 + "," + t.list_num3 + "," + t.indiffent_child_num3 + "," + t.big_list_num + "," + t.is_sub_title + "," + t.has_merged + "," + t.is_bold + "," + t.is_time + "," + t.list_of_subtitle + "," + t.list_of_keyword + "," + t.has_repeat_sb_structure + "," + t.has_other_block + "," + t.child_is_small + "," + t.has_one_child + "," + green[index] + "\n");
						String out = num + "," + index + "," + "green" + "," + t.is_displaynone + "," + Math.atan(t.top) + "," + Math.atan(t.left) + "," + Math.atan(t.height) + "," + Math.atan(t.width) + "," + Math.atan(t.area) + "," + Math.atan(t.text_len) + "," + Math.atan(t.own_text_len) + "," + Math.atan(t.anchor_len) + "," + Math.atan(t.anchor_num) + "," + Math.atan((double)t.anchor_len/(t.text_len-t.anchor_len)) + "," + Math.atan(t.img_num) + "," + Math.atan(t.child_num) + "," + Math.atan(t.fake_child_num) + "," + Math.atan(t.input_area) + "," + Math.atan(t.area_list) + "," + Math.atan(t.area_other) + "," + Math.atan(t.list_num1) + "," + Math.atan(t.strict_list_num1) + "," + Math.atan(t.indiffent_child_num1) + "," + Math.atan(t.list_num2) + "," + Math.atan(t.strict_list_num2) + "," + Math.atan(t.indiffent_child_num2) + "," + Math.atan(t.list_num3) + "," + Math.atan(t.indiffent_child_num3) + "," + Math.atan(t.big_list_num) + "," + Math.atan(t.biggest_child_area) + "," + Math.atan(t.biggest_img_area) + "," + Math.atan(t.biggest_img_width) + "," + Math.atan(t.most_img_num) + "," + Math.atan(t.child_arrange_style) + "," + Math.atan(t.first_second_child_ratio) + "," + Math.atan(t.tobottom) + "," + Math.atan(t.longest_own_text_len) + "," + Math.atan(t.child_height2width) + "," + Math.atan(t.tall_child_num) + "," + Math.atan((double)t.friend_link_area/(t.area+1)) + "," + Math.atan((double)t.full_list_area/(t.area+1)) + "," + Math.atan(t.text_len-t.full_list_len) + "," + Math.atan(t.text_len-t.anchor_len) + "," + t.has_intro + "," + t.has_sub_title + "," + t.is_sub_title + "," + t.has_merged + "," + t.is_bold + "," + t.is_time + "," + t.list_of_subtitle + "," + t.list_of_keyword + "," + t.has_repeat_sb_structure + "," + t.has_other_block + "," + t.child_is_small + "," + t.has_one_child + "," + green[index] + "\n";
				//		int n = random.nextInt(2);
						if (n == 0) {
							cnt1++;
							writer.write(out);
						} else if (n == 1) {
							cnt2++;
							writer2.write(out);
						} else {
							System.out.println("errorrrrrrrrrrrrrrr.");
						}
						if (t.has_intro) {
					//	if (t.has_sub_title) {
							System.out.println(index + "\t" + "green" + "\t" + url + "\t" +  green[index]);
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
		writer.flush();
		writer.close();
		writer2.flush();
		writer2.close();
		System.out.println("data split: " + cnt1 + "\t" + cnt2);
	}
}
