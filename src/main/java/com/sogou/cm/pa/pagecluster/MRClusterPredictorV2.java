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


public class MRClusterPredictorV2  extends Configured implements Tool {

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
			InputSplit split = context.getInputSplit();
			FileSplit fs = (FileSplit) split;
			String fname = fs.getPath().getParent().getName();
			try {
			if (fname.indexOf("htmlinfo") >= 0) {
				String s = value.toString();
				int pos = s.indexOf("\t");
				if (pos < 0) {
					return;
				}
				String url = s.substring(0, pos);
				String domain = URLUtils.getMainDomain(url);
				String site = URLUtils.getDomainWithoutPort(url);
				if (domain == null || site == null || domain.length() == 0 || site.length() == 0) {
					return;
				}
			//	HtmlPage hp = new HtmlPage();
				String hp_s = s.substring(pos+1);
				//hp.fromString(hp_s);
				if (site2info.containsKey(site)) {
					context.getCounter("debug", "site level page").increment(1);
					context.write(new Text("site\t" + site + "\t1"), new Text(hp_s));
					return;
				}
				else if (domain2info.containsKey(domain)) {
					context.getCounter("debug", "domain level page").increment(1);
					context.write(new Text("domain\t" + domain + "\t1"), new Text(hp_s));
				} else {
					context.getCounter("debug", "not in site and domain").increment(1);
				}
			} else {
				String s = value.toString();
				ClusterInfo ci = new ClusterInfo();
				ci.fromSimpleString(s);
				context.write(new Text(ci.level + "\t" + ci.site + "\t0"), new Text(s));
			}
			} catch (Exception e) {
				context.getCounter("debug", "invalid input").increment(1);
			}
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}
	
	private static class ScanPartitioner extends Partitioner<Text, Text> {

		
		public int getPartition(Text key, Text value, int numPartitions) {
			String s = key.toString();
			String key2 = s.substring(0, s.length()-2);
			return (key2.hashCode() & Integer.MAX_VALUE) % numPartitions;
		}

	}

	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {
		ClusterPredictor cp = null;
		String last_key = "";
		ArrayList<ClusterInfo> cis = new ArrayList<ClusterInfo>();
		HashMap<String, String> cluster2mc = new HashMap<String, String>();
		
		protected void setup(Context context) throws IOException, InterruptedException {

			BufferedReader reader2 = new BufferedReader(new FileReader(new File("cluster_qdbmaincontent.txt")));
			String line;
			
			while ((line = reader2.readLine()) != null) {
				int pos = line.indexOf('\t');
				if (pos > 0) {
					String clusterid = line.substring(0, pos);
					String mc = line.substring(pos+1);
					cluster2mc.put(clusterid, mc);
				} else {
					cluster2mc.put(line, "");
				}
			}
			reader2.close();
		}
		
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			String s = key.toString();
			String cur_key = s.substring(0, s.length()-2);
			char c = s.charAt(s.length()-1);
			if (!cur_key.equalsIgnoreCase(last_key)) {
				cis.clear();
				cp = null;
			}
			for (Text val: values) {
	//			if (cur_key.equalsIgnoreCase(last_key)) {
					if (c == '0') {
						context.getCounter("debug", "cluster info").increment(1);
						ClusterInfo ci = new ClusterInfo();
						ci.fromSimpleString(val.toString());
						cis.add(ci);
					} else {
						context.getCounter("debug", "input page").increment(1);
						if (cp == null) {
							cp = new ClusterPredictor(cis);
						}
						HtmlPage hp = new HtmlPage();
						hp.fromString(val.toString());
						try {
							String result = cp.predict(hp);
							if (result != null) {
								if (cluster2mc.containsKey(result)) {
									String mc = cluster2mc.get(result);
									context.write(new Text(hp.url), new Text(result + "\t" + mc));
								} else {
									context.write(new Text(hp.url), new Text(result + "\t" + "nocluster2mc"));
								}
							} else {
								context.write(new Text(hp.url), new Text("nocluster"));
							}
						} catch (SAXException e) {
							context.getCounter("debug", "reduce exception").increment(1);
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
			}
			last_key = cur_key;
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}

	
	public int run(String[] args) throws Exception {
		Job job = new Job(this.getConf(), "SampleIndexUrl");

		job.setJarByClass(this.getClass());

		job.setMapperClass(TokenizerMapper.class);
		job.setPartitionerClass(ScanPartitioner.class);
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
		Tool tool = new MRClusterPredictorV2();
		ToolRunner.run(tool, args);
	}

}
