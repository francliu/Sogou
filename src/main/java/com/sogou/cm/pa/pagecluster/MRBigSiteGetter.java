package com.sogou.cm.pa.pagecluster;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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


public class MRBigSiteGetter  extends Configured implements Tool {

	/**
	 * @param args
	 */
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {
		HashSet<String> domains = new HashSet<String>();
		HashSet<String> gen_sites = new HashSet<String>();
		protected void setup(Context context) throws IOException,
		InterruptedException {
			/*
			domains.add("focus.cn");
			domains.add("ganji.com");
			domains.add("baixing.com");
			domains.add("58.com");
			domains.add("fang.com");
			domains.add("anjuke.com");
			*/
			BufferedReader reader = new BufferedReader(new FileReader(new File("general_domain.txt")));
			
			String line;
			while ((line = reader.readLine()) != null) {
				String[] segs = line.split("\t");
				gen_sites.add(segs[0]);
				String domain = URLUtils.getMainDomain(segs[0]);
				domains.add(domain);
			}
			reader.close();
			
			
	}
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			String[] segs = value.toString().split(" ");
			
			String url = segs[0];
			String site = URLUtils.getDomain(url);
			String domain = URLUtils.getMainDomain(url);
			if (site == null || domain == null || site.length() == 0 || domain.length() == 0) {
				return;
			}
			if (!domains.contains(domain) || gen_sites.contains(site)) {
				context.write(new Text(site), new Text("1"));
			}

		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}
	
	public static class MyCombiner extends
	Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			Long num = new Long(0);
			for (Text val: values) {
				num += new Long(val.toString());
			}
			context.write(key, new Text(num.toString()));
		}

		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}

	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			Long num = new Long(0);
			for (Text val: values) {
				num += new Long(val.toString());
			}
			if (num < 10000) {
				return;
			}
			context.write(key, new Text(num.toString()));
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}

	
	public int run(String[] args) throws Exception {
		Job job = new Job(this.getConf(), "SampleIndexUrl");

		job.setJarByClass(this.getClass());

		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(MyCombiner.class);
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
		Tool tool = new MRBigSiteGetter();
		ToolRunner.run(tool, args);
	}

}
