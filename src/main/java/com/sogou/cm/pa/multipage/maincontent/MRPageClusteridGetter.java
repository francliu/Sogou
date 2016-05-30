package com.sogou.cm.pa.multipage.maincontent;


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
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.sogou.web.selector.urllib.URLUtils;


public class MRPageClusteridGetter  extends Configured implements Tool {

	/**
	 * @param args
	 */
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {
		HashSet<String> domains = new HashSet<String>();
		protected void setup(Context context) throws IOException,
		InterruptedException {

		}
		
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			InputSplit split = context.getInputSplit();
			FileSplit fs = (FileSplit) split;
			String fname = fs.getPath().getParent().getName();
			boolean is_webkit = false;
			if (fname.indexOf("webkit_pageinfo") < 0) {
				String[] segs = value.toString().split("\t");
				if (segs.length != 2) {
					return;
				}
				context.write(new Text(segs[0]), new Text("0\t" + segs[1]));
			} else {
				String s = value.toString();
				int pos = s.indexOf('\t');
				if (pos < 0) {
					return;
				}
				String url = s.substring(0, pos);
				String pageinfo = s.substring(pos+1);
				context.write(new Text(url), new Text("1\t" + pageinfo));
				
			}
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}

	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			String pageinfo = null;
			String clusterid = null;
			String url = key.toString();
			for (Text val: values) {
				String s = val.toString();
				if (s.startsWith("0")) {
					clusterid = s.substring(2);
				} else {
					pageinfo = s.substring(2);
				}
			}
			if (pageinfo != null && clusterid != null) {
				context.write(new Text(clusterid), new Text(pageinfo));
			}
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
		Tool tool = new MRPageClusteridGetter();
		ToolRunner.run(tool, args);
	}

}
