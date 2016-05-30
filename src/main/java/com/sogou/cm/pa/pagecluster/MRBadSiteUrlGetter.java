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
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.sogou.web.selector.urllib.URLUtils;


public class MRBadSiteUrlGetter  extends Configured implements Tool {

	/**
	 * @param args
	 */
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {
		HashSet<String> domains = new HashSet<String>();
		HashSet<String> sites = new HashSet<String>();
		HashMap<String, Long> domain2info = new HashMap<String, Long>();
		HashMap<String, Long> site2info	 = new HashMap<String, Long>();
		Long limit = 1000000l;
		protected void setup(Context context) throws IOException,
		InterruptedException {
			BufferedReader reader = new BufferedReader(new FileReader(new File("bad_clusters2.txt")));
			
			String line;
			while ((line = reader.readLine()) != null) {
				String[] segs = line.split(" ");
				if (segs.length != 3) {
					continue;
				}
				if (segs[0].equals("site")) {
					sites.add(segs[1]);
				} else if (segs[0].equals("domain")) {
					domains.add(segs[1]);
				}
			}
			reader.close();
			
			 reader = new BufferedReader(new FileReader(new File("domain_info.txt")));
			

			while ((line = reader.readLine()) != null) {
				String[] segs = line.split("\t");
				if (segs.length == 2) {
					Long num = new Long(segs[1]);
				//	if (num < limit)
						domain2info.put(segs[0], num);
				}
			}
			reader.close();
			
			BufferedReader reader2 = new BufferedReader(new FileReader(new File("site_info.txt")));
			
			while ((line = reader2.readLine()) != null) {
				String[] segs = line.split("\t");
				if (segs.length == 2) {
					Long num = new Long(segs[1]);
				//	if (num < limit)
						site2info.put(segs[0], num);
				}
			}
			reader2.close();
	}
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			String[] segs = value.toString().split(" ");
			
			String url = segs[0];
			String domain = URLUtils.getMainDomain(url);
			String site = URLUtils.getDomainWithoutPort(url);
			if (domain == null || site == null || domain.length() == 0 || site.length() == 0) {
				return;
			}
			if (site2info.containsKey(site)) {
				long num = site2info.get(site);
				if (num < limit && sites.contains(site)) {
					context.write(new Text(url), new Text(""));
				}
			} else if (domain2info.containsKey(domain)) {
				long num = domain2info.get(domain);
				if (num < limit && domains.contains(domain)) {
					context.write(new Text(url), new Text(""));
				}
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
		Tool tool = new MRBadSiteUrlGetter();
		ToolRunner.run(tool, args);
	}

}
