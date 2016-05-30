package com.sogou.cm.pa.multipage.maincontent;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.sogou.web.selector.urllib.URLUtils;


public class MRDebug  extends Configured implements Tool {

	/**
	 * @param args
	 */
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {
	
		protected void setup(Context context) throws IOException,
		InterruptedException {

	}
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			MessageDigest md;
			byte[] dig_id;

				try {
					md = MessageDigest.getInstance("md5");	
					dig_id = new byte[16];
					Long docid = new Long(0);
					md.reset();
				//	String text = "0(0)0��0��0��0��0��0��������0������0������0������0������0����һ0���ڶ�0������0������0������0������0������0����һ0���ڶ�0������0������0������0������0������0����һ0���ڶ�0������0������0������0������0������0����һ0���ڶ�0������0������0������0";
				//	String text = "0(0)0��0��0��0��0��0��������0������0����һ0���ڶ�0������0������0������0������0������0����һ0���ڶ�0������0������0������0������0������0����һ0���ڶ�0������0������0������0������0������0����һ0���ڶ�0������0������0������0������0������0����һ0";
					String text = value.toString();
					md.update(text.getBytes("utf8"));
					try {
						md.digest(dig_id, 0, 16);
						for (int j = 0; j < dig_id.length; ++j) {
							docid=docid<<8;
							docid += dig_id[j]&0xff;
						}
					} catch (DigestException ee) {
						docid = Long.valueOf(text.toString().length());
						// TODO Auto-generated catch block
						ee.printStackTrace();
					}
					System.out.println(docid);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}




		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}

	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

			context.write(key, null);
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}

	
	public int run(String[] args) throws Exception {
		Job job = new Job(this.getConf(), "SampleIndexUrl");

		job.setJarByClass(this.getClass());

		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.waitForCompletion(true);

		return 0;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Tool tool = new MRDebug();
		ToolRunner.run(tool, args);
	}

}
