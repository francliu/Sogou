package com.sogou.cm.pa.pagecluster;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.SAXException;

import com.sogou.cm.pa.multipage.maincontent.HtmlXpath;
import com.sogou.cm.pa.multipage.maincontent.PageParser;
import com.sogou.web.selector.urllib.URLUtils;


public class MRSimpleClusterGetter  extends Configured implements Tool {

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
			String s = value.toString();
			try {
				ClusterInfo ci = new ClusterInfo();
				ci.fromString(s);
				String cluster_key = ci.level + "\t" + ci.site;
				for (HtmlPage hp: ci.pages) {
					HashMap<String, XpathInfo> tmp = new HashMap<String, XpathInfo>();
					for (String xpath: hp.xpath2info.keySet()) {
						tmp.put(String.valueOf(xpath.hashCode()), hp.xpath2info.get(xpath));
					}
					hp.xpath2info = tmp;
				}
					context.write(new Text(ci.toSimpleString()), new Text(""));
			} catch (Exception e) {
				
			}
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}
	


	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {
		
		protected void setup(Context context) throws IOException, InterruptedException {


		}
		
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
		Tool tool = new MRSimpleClusterGetter();
		ToolRunner.run(tool, args);
	}

}
