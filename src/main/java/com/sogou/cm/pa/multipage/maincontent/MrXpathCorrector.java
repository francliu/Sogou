package com.sogou.cm.pa.multipage.maincontent;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sogou.web.selector.offsum.OriginPageInputFormatV3;
import com.sogou.web.selector.offsum.OriginPageOutputFormat;
import com.sogou.web.selector.offsum.OriginPageWritable;
import com.sogou.web.selector.offsum.OriginPageWritable.Attribute;
import com.sogou.web.selector.urllib.URLUtils;
import com.sogou.web.selector.urllib.UrlInfo;

public class MrXpathCorrector extends Configured implements Tool {
	
	public static String getMemStat() {
		 SimpleDateFormat STANDARD_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 
	  Runtime runtime = Runtime.getRuntime();
	Date date = new Date(System.currentTimeMillis());
				StringBuilder sb = new StringBuilder(1024);
				sb.append("[").append(STANDARD_FORMAT.format(date)).append("]");
				sb.append("[").append(runtime.totalMemory() >> 20).append(":").append(runtime.freeMemory() >> 20)
						.append("] ");
				sb.append("");
				return (sb.toString());
	}

	private static class ScanMapper extends Mapper<BytesWritable, OriginPageWritable, Text, Text> {
		private PageParser content_handler;
		Parser					parser;
		StringBuffer out_key = new StringBuffer();
		byte[] output = new byte[4096 * 1024 + 1];
		Inflater decompresser = new Inflater();
		CodepageDetectorProxy	codepageDetectorProxy	= CodepageDetectorProxy.getInstance();
		int cnt = 0;
		HashMap<String, String> url2cluster = new HashMap<String, String>();
		HashMap<String, ArrayList<HtmlXpath>> cluster2mc = new HashMap<String, ArrayList<HtmlXpath>>();
		
		
		protected void setup(Context context) throws IOException, InterruptedException {
			System.out.println(((FileSplit) context.getInputSplit()).getPath());
				content_handler=new PageParser();
				parser = new Parser();
				parser.setContentHandler(content_handler);
				codepageDetectorProxy.add(new ByteOrderMarkDetector());
				// this.codepageDetectorProxy.add(new ParsingDetector(false));
				codepageDetectorProxy.add(JChardetFacade.getInstance());
				BufferedReader reader2 = new BufferedReader(new FileReader(new File("sample_url.txt")));
				String line;
				
				while ((line = reader2.readLine()) != null) {
					String[] segs = line.split("\t");
					url2cluster.put(segs[0], segs[1]);
				}
				reader2.close();
				
				reader2 = new BufferedReader(new FileReader(new File("cluster_maincontent.txt")));
				
				while ((line = reader2.readLine()) != null) {
					String[] segs = line.split("\t");
					ArrayList<HtmlXpath> mcs = new ArrayList<HtmlXpath>();
					for (int i = 1; i < segs.length; ++i) {
						HtmlXpath hx = new HtmlXpath();
						hx.fromString(segs[i]);
						mcs.add(hx);
					}
					if (mcs.size() > 0)
						cluster2mc.put(segs[0], mcs);
				}
				reader2.close();
		}

		
		protected void map(BytesWritable key, OriginPageWritable value, Context context) throws IOException, InterruptedException {
			InputSplit split = context.getInputSplit();
			FileSplit fs = (FileSplit) split;
			String fname = fs.getPath().getParent().getName();
			boolean is_webkit = false;
			if (fname.indexOf("webkit") >= 0) {
				is_webkit = true;
			}
			if (key == null)
				return;

			if (value == null) {
				return;
			}
			
			if (value.url.toString().length() > 256) {
				return;
			}
			
			String clusterid = url2cluster.get(value.url.toString());
			if (clusterid == null) {
				return;
			}
			
			ArrayList<HtmlXpath> mcs = cluster2mc.get(clusterid);
			if (mcs == null) {
				return;
			}
			

			
			Attribute err_reason = value.getAttribute("Error-Reason");
			if (err_reason != null) {
				return;
			}
			if (value.body.getLength() > 0) {
				int rc = 0;
				try {
					Attribute originSiteAttr = value.getAttribute("Original-Size");
					if (originSiteAttr == null)
						throw new IOException("NULL Original-Size");
					int originalSize = Integer.parseInt(originSiteAttr.val.toString());
					if (originalSize > 4096 * 1024) {
						rc = 1;
						throw new Exception("error");
					}
					Inflater decompresser = new Inflater();
decompresser.setInput(value.body.getBytes(),0, value.body.getLength());
					int resultLength=0;
					try {
						resultLength = decompresser.inflate(output);
					} catch (DataFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (resultLength != originalSize) {
						rc = 2;
						throw new Exception("error");
					}
					decompresser.end();
					String htmlPage = "";
					if (!is_webkit) {
						int len = resultLength < 50000 ? resultLength : 50000;
						ByteArrayInputStream in = new ByteArrayInputStream(output);
						Charset charset = codepageDetectorProxy.detectCodepage(in, len);
						 htmlPage = new String(output, 0, resultLength, charset);
					} else {
						 htmlPage = new String(output, 0, resultLength, "UTF-8");
					}
					try {
						content_handler.setXpath(mcs);
						content_handler.setIsWebkit(is_webkit);
						parser.parse(new InputSource(new StringReader(htmlPage)));
						if (is_webkit) {
							Iterator iter = content_handler.own_xpathes.entrySet().iterator();
							while (iter.hasNext()) {
								Entry entry = (Entry) iter.next();
								String xpath = (String) entry.getKey();
								StringBuffer text = (StringBuffer) entry.getValue();
								context.write(new Text(clusterid + "\t" + xpath), new Text(value.url.toString() + "\t" + text.toString() + "\t" + "0"));
								context.getCounter("debug", "webkit_xpath").increment(1);
							}
						} else {
							
							Iterator iter = content_handler.own_xpathes.entrySet().iterator();
							while (iter.hasNext()) {
								Entry entry = (Entry) iter.next();
								String xpath = (String) entry.getKey();
								StringBuffer text = (StringBuffer) entry.getValue();
								context.write(new Text(clusterid + "\t" + xpath), new Text(value.url.toString() + "\t" + text.toString() + "\t" + "1"));
							//	context.getCounter("debug", "webkit_xpath").increment(1);
							}
							
							context.getCounter("debug", "normal_xpath").increment(content_handler.own_xpathes.size());
							 iter = content_handler.candidate_xpathes.entrySet().iterator();
							while (iter.hasNext()) {
								Entry entry = (Entry) iter.next();
								String xpath = (String) entry.getKey();
								HashMap<String, StringBuffer> val = (HashMap<String, StringBuffer>) entry.getValue();
								Iterator iter2 = val.entrySet().iterator();
								while (iter2.hasNext()) {
									Entry entry2 = (Entry) iter2.next();
									String xpath2 = (String) entry2.getKey();
									StringBuffer text2 = (StringBuffer) entry2.getValue();
								//	System.out.println(clusterid + "\t" + xpath + "\t" + value.url.toString() + "\t" + xpath2 + "\t" + text2.toString() + "\t" + "1");
									context.write(new Text(clusterid + "\t" + xpath), new Text(value.url.toString() + "\t" + xpath2 + "\t" + text2.toString() + "\t" + "1"));
									context.getCounter("debug", "candidate_xpath").increment(1);
								}
							}
						}
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						System.out.println(value.url);
						e.printStackTrace();
						context.getCounter("SCANNER", "Exception").increment(1);
					}
				} catch (Throwable e) {
					System.out.println(value.url);
					e.printStackTrace();
					context.getCounter("SCANNER", "ErrorContent:" + rc).increment(1);
				}
				
			}
		}
	}
	
	private static class ScanReducer extends
	Reducer<Text, Text, Text, Text> {

		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			HashMap<String, String> url2basetext = new HashMap<String, String>();
			HashMap<String, String> url2text = new HashMap<String, String>();
			HashMap<String, HashMap<String, String> > url2candidate = new HashMap<String, HashMap<String, String> >();
			String[] segs = key.toString().split("\t");
			if (segs.length < 2) {
				return;
			}
			String clusterid = segs[0];
			String xpath = segs[1];
			for (Text val: values) {
				String[] segs2 = val.toString().split("\t");
				if (segs2.length == 3) {
					if (segs2[2].equalsIgnoreCase("0")) {
						url2basetext.put(segs2[0], segs2[1]);
					} else {
						url2text.put(segs2[0], segs2[1]);
					}
					
				} else if (segs2.length == 4) {
					HashMap<String, String> temp = url2candidate.get(segs2[0]);
					if (temp != null) {
						temp.put(segs2[1], segs2[2]);
					} else {
						temp = new HashMap<String, String>();
						temp.put(segs2[1], segs2[2]);
						url2candidate.put(segs2[0], temp);
					}
				}
			}
			
			HashMap<String, String> url2mc = new HashMap<String, String>();
			Iterator iter = url2candidate.entrySet().iterator();
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				String url = (String) entry.getKey();
				String base = url2basetext.get(url);
				if (base == null) {
					continue;
				}
				double max_similar = 0.0;
				String match_xpath = "";
				String match_text = "";
				HashMap<String, String> v = (HashMap<String, String>) entry.getValue();
				Iterator iter2 = v.entrySet().iterator();
				while (iter2.hasNext()) {
					Entry entry2 = (Entry) iter2.next();
					String xpath2 = (String) entry2.getKey();
					String text2 = (String) entry2.getValue();
					double t = PageSimilarityCalculator.minEditDistance(base, text2);
					if (t > max_similar) {
						max_similar = t;
						match_xpath = xpath2;
						match_text = text2;
					}
				}
				if (max_similar > 0.5) {
					url2mc.put(url, match_xpath);
			//		context.write(new Text("0" + "\t" + clusterid + "\t" + url + "\t" + max_similar + "\t" + xpath + "\t" + match_xpath + "\t" + base + "\t" + match_text), null);
				} else {
			//		context.write(new Text("1" + "\t" + clusterid + "\t" + url + "\t" + max_similar + "\t" + xpath + "\t" + match_xpath + "\t" + base + "\t" + match_text), null);
				}
			}
			for (String u: url2mc.keySet()) {
		//		context.write(new Text(url2mc.get(u) + "\t" + u + "\t" + xpath), null);			
			}
			
			
			for (String k: url2basetext.keySet()) {
				if (!url2text.containsKey(k) && !url2mc.containsKey(k)) {
					context.write(new Text(xpath + "\t" + k + "\t" + url2basetext.get(k)), null);
				}
			}
			
			

			
		}
		

	}


	
	public int run(String[] args) throws Exception {
		Job job = new Job(this.getConf(), "AdTrainDataExtractor");

		job.setJarByClass(this.getClass());

		job.setMapperClass(ScanMapper.class);
		job.setReducerClass(ScanReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);

	//	job.setPartitionerClass(ScanPartitioner.class);

		job.setInputFormatClass(OriginPageInputFormatV3.class);
	//	job.setOutputFormatClass(OriginPageOutputFormat.class);
	//	job.setOutputFormatClass(GBKOutputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
	//	LazyOutputFormat.setOutputFormatClass(job, GBKOutputFormat.class);

		job.waitForCompletion(true);

		return 0;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Tool pageScanner = new MrXpathCorrector();
		ToolRunner.run(pageScanner, args);
	}

}
