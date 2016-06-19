package com.sogou.pa.ListPage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class ListFeatherExtractor {

	public static void InitListFeathers() throws IOException{

		@SuppressWarnings("resource")
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("list_blockTrain.arff")), "utf8"));
//		System.out.println("train set num: 	" + cnt);
		writer.write("@relation listblock\n");
		writer.write("@attribute is_stop {true, false}\n");
		writer.write("@attribute author {true, false}\n");
		writer.write("@attribute latestArictle {true, false}\n");
		writer.write("@attribute showAllfloors {true, false}\n");
		writer.write("@attribute registers {true, false}\n");
		writer.write("@attribute theTimeReport {true, false}\n");
		writer.write("@attribute TecentBlog {true, false}\n");
		writer.write("@attribute share {true, false}\n");
		writer.write("@attribute haveSolvedquestion {true, false}\n");
		writer.write("@attribute answers {true, false}\n");

		writer.write("@attribute check {true, false}\n");
		writer.write("@attribute reply {true, false}\n");
		writer.write("@attribute publish {true, false}\n");
		writer.write("@attribute origin {true, false}\n");
		writer.write("@attribute readtimes {true, false}\n");

		writer.write("@attribute novel {true, false}\n");
		writer.write("@attribute nextPage {true, false}\n");
		writer.write("@attribute album {true, false}\n");
		writer.write("@attribute position {true, false}\n");
		writer.write("@attribute experience {true, false}\n");

		writer.write("@attribute purchase {true, false}\n");
		writer.write("@attribute introduction {true, false}\n");
		writer.write("@attribute steps {true, false}\n");
		writer.write("@attribute position_intro {true, false}\n");
		writer.write("@attribute position_name {true, false}\n");
		writer.write("@attribute position_numbers {true, false}\n");
		
		//--------------------
		writer.write("@attribute Big_Text_area numeric\n");
		writer.write("@attribute Big_Text_left numeric\n");
		writer.write("@attribute Big_Text_top numeric\n");
		writer.write("@attribute frist_Text_area numeric\n");
		writer.write("@attribute frist_Text_top numeric\n");
		writer.write("@attribute frist_Text_left numeric\n");

		writer.write("@attribute Big_List_area numeric\n");
		writer.write("@attribute Big_List_left numeric\n");
		writer.write("@attribute Big_List_top numeric\n");
		writer.write("@attribute frist_List_area numeric\n");
		writer.write("@attribute frist_List_top numeric\n");
		writer.write("@attribute frist_List_left numeric\n");

		
		writer.write("@attribute list_num numeric\n");
		writer.write("@attribute text_num numeric\n");
		writer.write("@attribute special_situation_area numeric\n");
		writer.write("@attribute list_area numeric\n");
		writer.write("@attribute text_area numeric\n");
		writer.write("@attribute other_area numeric\n");

		writer.write("@attribute src_num numeric\n");
		writer.write("@attribute strict_src_num numeric\n");
		writer.write("@attribute mid_text_num numeric\n");
		writer.write("@attribute mid_list_area numeric\n");
		writer.write("@attribute mid_text_area numeric\n");
		writer.write("@attribute mid_other_area numeric\n");
		writer.write("@attribute area_equal_max_num numeric\n");
		writer.write("@attribute area_equal_max_area numeric\n");

		writer.write("@attribute is_sub_title {true, false}\n");
		
		writer.write("@attribute is_list {0, 1}\n");
		writer.write("@data\n");
		writer.flush();
		writer.close();
		
		BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("list_blockTest.arff")), "utf8"));
		writer2.write("@relation listblock\n");
		writer2.write("@attribute is_stop {true, false}\n");
		writer2.write("@attribute author {true, false}\n");
		writer2.write("@attribute latestArictle {true, false}\n");
		writer2.write("@attribute showAllfloors {true, false}\n");
		writer2.write("@attribute registers {true, false}\n");
		writer2.write("@attribute theTimeReport {true, false}\n");
		writer2.write("@attribute TecentBlog {true, false}\n");
		writer2.write("@attribute share {true, false}\n");
		writer2.write("@attribute haveSolvedquestion {true, false}\n");
		writer2.write("@attribute answers {true, false}\n");

		writer2.write("@attribute check {true, false}\n");
		writer2.write("@attribute reply {true, false}\n");
		writer2.write("@attribute publish {true, false}\n");
		writer2.write("@attribute origin {true, false}\n");
		writer2.write("@attribute readtimes {true, false}\n");

		writer2.write("@attribute novel {true, false}\n");
		writer2.write("@attribute nextPage {true, false}\n");
		writer2.write("@attribute album {true, false}\n");
		writer2.write("@attribute position {true, false}\n");
		writer2.write("@attribute experience {true, false}\n");

		writer2.write("@attribute purchase {true, false}\n");
		writer2.write("@attribute introduction {true, false}\n");
		writer2.write("@attribute steps {true, false}\n");
		writer2.write("@attribute position_intro {true, false}\n");
		writer2.write("@attribute position_name {true, false}\n");
		writer2.write("@attribute position_numbers {true, false}\n");
		
		//--------------------
		writer2.write("@attribute Big_Text_area numeric\n");
		writer2.write("@attribute Big_Text_left numeric\n");
		writer2.write("@attribute Big_Text_top numeric\n");
		writer2.write("@attribute frist_Text_area numeric\n");
		writer2.write("@attribute frist_Text_top numeric\n");
		writer2.write("@attribute frist_Text_left numeric\n");

		writer2.write("@attribute Big_List_area numeric\n");
		writer2.write("@attribute Big_List_left numeric\n");
		writer2.write("@attribute Big_List_top numeric\n");
		writer2.write("@attribute frist_List_area numeric\n");
		writer2.write("@attribute frist_List_top numeric\n");
		writer2.write("@attribute frist_List_left numeric\n");

		
		writer2.write("@attribute list_num numeric\n");
		writer2.write("@attribute text_num numeric\n");
		writer2.write("@attribute special_situation_area numeric\n");
		writer2.write("@attribute list_area numeric\n");
		writer2.write("@attribute text_area numeric\n");
		writer2.write("@attribute other_area numeric\n");

		writer2.write("@attribute src_num numeric\n");
		writer2.write("@attribute strict_src_num numeric\n");
		writer2.write("@attribute mid_text_num numeric\n");
		writer2.write("@attribute mid_list_area numeric\n");
		writer2.write("@attribute mid_text_area numeric\n");
		writer2.write("@attribute mid_other_area numeric\n");
		writer2.write("@attribute area_equal_max_num numeric\n");
		writer2.write("@attribute area_equal_max_area numeric\n");

		writer2.write("@attribute is_sub_title {true, false}\n");
		
		writer2.write("@attribute is_list {0, 1}\n");
		writer2.write("@data\n");
		writer2.flush();
		writer2.close();
		
	}
}
