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
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.sogou.web.selector.urllib.URLUtils;


public class MrSampleCrawlUrl  extends Configured implements Tool {

	/**
	 * @param args
	 */
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {
		int cnt = 0;
		int sample_num = 1000;
		int max_int = 0x7fffffff;
		HashMap<String, ArrayList<String>> data = new HashMap<String, ArrayList<String>>();
		HashMap<String, Integer> domain_cnt = new HashMap<String, Integer>();
		Random random = new Random();
		protected void setup(Context context) throws IOException,
		InterruptedException {
	}
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			
			String val = value.toString();
			/*
			if (val.indexOf("SUCCESS") < 0) {
				return;
			}
			String[] segs = value.toString().split(" ");
			String url = segs[4];
			if (!url.startsWith("http:")) {
				return;
			}
			*/
			String url = val;
			String site = URLUtils.getMainDomain(url);
			if (site == null) {
				return;
			}
			
			if (cnt == max_int) {
				return;
			}
			Integer num = domain_cnt.get(site);
			if (num == null) {
				num = 1;
			} else {
				num++;
			}
			domain_cnt.put(site, num);
			if (num-1 < sample_num) {
				ArrayList<String> temp = data.get(site);
				if (temp == null) {
					temp = new ArrayList<String>();
				}
				temp.add(url);
				data.put(site, temp);
			} else {
				int index = random.nextInt(num);
				ArrayList<String> temp = data.get(site);
				if (index < sample_num) {
					temp.set(index, url);
					data.put(site, temp);
				}
			}
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {
			Collection<ArrayList<String> > urls = data.values();
			Iterator<ArrayList<String> > iter = urls.iterator();
			while (iter.hasNext()) {
				ArrayList<String> temp = iter.next();
				for (int i = 0; i < temp.size(); ++i) {
					context.write(new Text(URLUtils.getMainDomain(temp.get(i))), new Text(temp.get(i)));
				}
			}
		}
	}

	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {
		int cnt = 0;
		int sample_num = 1000;
		int max_int = 0x7fffffff;
		ArrayList<String> urls_arr = new ArrayList<String>();
		Random random = new Random();
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			
			cnt = 0;
			urls_arr.clear();
			for (Text val: values) {
				String url = val.toString();
				++cnt;
				if (cnt == max_int) {
					return;
				}
				
				if (cnt-1 < sample_num) {
					urls_arr.add(url);
				} else {
					int index = random.nextInt(cnt);
					if (index < sample_num) {
						urls_arr.set(index, url);
					}
				}
			}
			for (String url: urls_arr) {
				context.write(new Text(url), null);
			}

		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {
		}
	}

	
	public int run(String[] args) throws Exception {
		Job job = new Job(this.getConf(), "SampleCrawlUrl");

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
		Tool tool = new MrSampleCrawlUrl();
		ToolRunner.run(tool, args);
	}

}
