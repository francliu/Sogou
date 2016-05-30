package com.sogou.cm.pa.pagecluster;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.sogou.web.selector.urllib.URLUtils;


public class MRClusterMainContent  extends Configured implements Tool {

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
			InputSplit split = context.getInputSplit();
			FileSplit fs = (FileSplit) split;
			String fname = fs.getPath().getParent().getName();
			if (fname.indexOf("test3")>=0) {
				String[] segs = value.toString().split("\t");
				if (segs == null || segs.length == 0) {
					return;
				}
				String url = segs[0];
				String domain = URLUtils.getMainDomain(url);
				if (domain != null) {
					context.write(new Text(domain), new Text("0\t" + value));
				}
			} else {
				String[] segs = value.toString().split("\t");
				if (segs == null || segs.length == 0) {
					return;
				}
				String domain = segs[0];
				context.write(new Text(domain), new Text("1\t" + value));
			}
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}

	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			HashMap<String, String> url2content = new HashMap<String, String>();
			String cluster_result = "";
			Integer cnt  = 0;
			for (Text val: values) {
				String[] segs = val.toString().split("\t");
				if (segs[0].equals("1")) {
					cluster_result = val.toString();
				} else if (segs[0].equals("0")) {
					StringBuffer content = new StringBuffer();
					if (segs.length >= 3) {
						for (int i = 2; i < segs.length; ++i) {
							content.append(segs[i] + "\t");
						}
					}
					url2content.put(segs[1], content.toString());
				}
			}
			if (cluster_result.length() > 0) {
				String[] segs = cluster_result.toString().split("\t");
				if (segs.length < 4) {
					return;
				}
				context.write(new Text(segs[1]), new Text(segs[2]));
				for (int i = 3; i < segs.length; ++i) {
					String[] segs2 = segs[i].split(" ");
					context.write(new Text(segs2[0]), null);
					for (int j = 1; j < segs2.length; ++j) {
						String content = "";
						if (url2content.containsKey(segs2[j])) {
							content = url2content.get(segs2[j]);
						}
						context.write(new Text(segs2[j]), new Text(content));
					}
				}
			}


		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {
		}
	}

	
	public int run(String[] args) throws Exception {
		Job job = new Job(this.getConf(), "StatCrawlUrl");

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
		Tool tool = new MRClusterMainContent();
		ToolRunner.run(tool, args);
	}

}
