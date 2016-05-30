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


public class MrSampleCrawlUrl2  extends Configured implements Tool {

	/**
	 * @param args
	 */
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, Text> {
		HashMap<String, Long> domain2info = new HashMap<String, Long>();
		HashMap<String, Long> site2info	 = new HashMap<String, Long>();
		protected void setup(Context context) throws IOException,
		InterruptedException {
			BufferedReader reader = new BufferedReader(new FileReader(new File("domain_info.txt")));
			
			String line;
			while ((line = reader.readLine()) != null) {
				String[] segs = line.split("\t");
				if (segs.length == 2) {
					domain2info.put(segs[0], new Long(segs[1]));
				}
			}
			reader.close();
			
			BufferedReader reader2 = new BufferedReader(new FileReader(new File("site_info.txt")));
			
			while ((line = reader2.readLine()) != null) {
				String[] segs = line.split("\t");
				if (segs.length == 2) {
					site2info.put(segs[0], new Long(segs[1]));
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
				context.write(new Text("site\t" + site), new Text(url));
				return;
			}
			if (domain2info.containsKey(domain)) {
				context.write(new Text("domain\t" + domain), new Text(url));
			}

		}
			
			protected void cleanup(Context context) throws IOException, InterruptedException {

			}
	}

	public static class IntSumReducer extends
	
	Reducer<Text, Text, Text, Text> {
		int cnt = 0;
		int max_int = 0x7fffffff;
		ArrayList<String> urls_arr = new ArrayList<String>();
		Random random = new Random();
		HashMap<String, Long> domain2info = new HashMap<String, Long>();
		HashMap<String, Long> site2info	 = new HashMap<String, Long>();
		protected void setup(Context context) throws IOException,
		InterruptedException {
			BufferedReader reader = new BufferedReader(new FileReader(new File("domain_info.txt")));
			
			String line;
			while ((line = reader.readLine()) != null) {
				String[] segs = line.split("\t");
				if (segs.length == 2) {
					domain2info.put(segs[0], new Long(segs[1]));
				}
			}
			reader.close();
			
			BufferedReader reader2 = new BufferedReader(new FileReader(new File("site_info.txt")));
			
			while ((line = reader2.readLine()) != null) {
				String[] segs = line.split("\t");
				if (segs.length == 2) {
					site2info.put(segs[0], new Long(segs[1]));
				}
			}
			reader2.close();
	}
		int getSampleNum(long num) {
			if (num > 1000000) {
				return 10000;
			} else if (num > 100000) {
				return 5000;
			} else if (num > 10000) {
				return 2000;
			} else {
				return 1000;
			}
		}
		
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			cnt = 0;
			urls_arr.clear();
			String[] segs = key.toString().split("\t");
			if (segs.length < 2) {
				return;
			}
			String site = segs[1];
			long num = 0;
			if (segs[0].equals("site")) {
				num = site2info.get(site);
			} else if (segs[0].equals("domain")) {
				num = domain2info.get(site);
			}
			if (num == 0) {
				return;
			}
			int sample_num = getSampleNum(num);
			for (Text val: values) {
				String url = val.toString();
				++cnt;
				if (cnt == max_int) {
					break;
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
				context.write(key, new Text(url));
			}

		}
	}


	
	public int run(String[] args) throws Exception {
		Job job = new Job(this.getConf(), "StatCrawlUrl");

		job.setJarByClass(this.getClass());

		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
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
		Tool tool = new MrSampleCrawlUrl2();
		ToolRunner.run(tool, args);
	}

}
