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
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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

import com.sogou.cm.pa.maincontent.Element;
import com.sogou.cm.pa.maincontent.RuleBasedMainContentExtractor;
import com.sogou.web.selector.offsum.OriginPageInputFormatV3;
import com.sogou.web.selector.offsum.OriginPageOutputFormat;
import com.sogou.web.selector.offsum.OriginPageWritable;
import com.sogou.web.selector.offsum.OriginPageWritable.Attribute;
import com.sogou.web.selector.urllib.URLUtils;
import com.sogou.web.selector.urllib.UrlInfo;

public class MRWebkitMaincontentGetterDebug extends Configured implements Tool {
	
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
		byte[] output = new byte[4096 * 1024 + 1];
		Inflater decompresser = new Inflater();
		CodepageDetectorProxy	codepageDetectorProxy	= CodepageDetectorProxy.getInstance();
		RuleBasedMainContentExtractor main_content = new RuleBasedMainContentExtractor();
	//	HashMap<Long, String> url2cluster = new HashMap<Long, String>();
		MessageDigest md;
		byte[] dig_id;
		
		XpathNodeCounter		htmlContentHandler		= new XpathNodeCounter();
		Parser					parser					= new Parser();
		Random ran = new Random();
		HashSet<String> debug_urls = new HashSet<String>();
		
		
		protected void setup(Context context) throws IOException, InterruptedException {
			System.out.println(((FileSplit) context.getInputSplit()).getPath());
			parser.setContentHandler(htmlContentHandler);
			try {
				md = MessageDigest.getInstance("md5");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dig_id = new byte[16];
			
			BufferedReader reader2 = new BufferedReader(new FileReader(new File("debug_urls.txt")));
			String line;
			int cnt = 0;
			while ((line = reader2.readLine()) != null) {
				debug_urls.add(line);
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
			if (!debug_urls.contains(value.url.toString())) {
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
					String htmlPage = new String(output, 0, resultLength, "utf8");
			
					List<Element> blocks = main_content.extractMainContent(value.url.toString(), htmlPage);
					String title = main_content.html_page.title;
					if (title == null || title.length() == 0) {
						title = String.valueOf(ran.nextLong());
					}
					//StringBuffer s = new StringBuffer(value.url.toString());
					//HashMap<String, XpathStat> xpathes = new HashMap<String, XpathStat>();
					HashMap<Integer, HashMap<String, XpathStat>> type_xpathes = new HashMap<Integer, HashMap<String, XpathStat>>();
					HashSet<String> xpath_set = new HashSet<String>();
					for (Element e: blocks) {
						HashMap<String, XpathStat> xpathes = type_xpathes.get(e.block_type);
						if (xpathes == null) {
							xpathes = new HashMap<String, XpathStat>();
							type_xpathes.put(e.block_type, xpathes);
						}
						md.reset();
						String text = e.text.toString();
						Long docid= new Long(0);
						xpath_set.add(e.full_xpath);
						/*
						if (e.full_xpath.equals("/html/body/div[@id='blog-163-com-main' and @class='nb-wrap wsy']/div[@class='nb-are nb-cnt']/div[@class='wkg']/div[@id='blog-163-com-container' and @class='c wc h clearfix']/div[@id='-3' and @class='nb-mdl lcr m-3']/div[@class='nb-mc lcr']/div[@class='c cc lcr nb-jsc']/div[@class='nbw-ryt ztag clearfix']/div[@class='left']/div[@class='lcnt bdwr bds0 bdc0']/div[@class='mcnt ztag']/div[@class='snl']")) {
							System.out.println("h: " + text + "\t" + e.width + "\t" + e.height);
						}
						*/
						if (text.length() > 0 && e.width*e.height>e.img_iframe_area*2 && e.width*e.height<800000 && !e.is_video && !e.has_download) {
							/*
							if (text.length() < 200) {
								String lcs = Utils.getLCS(text, title);
								if (lcs.length() >= 5) {
									CharSequence lcs2 = lcs;
									CharSequence cs = "";

									text = text.replace(lcs2, cs);


								}
							}
							*/
							text = text.replaceAll("[0-9.]+", "0");
							/*
							if (e.full_xpath.equalsIgnoreCase("/html/body/div[@class='wrap']/div[@class='main clearfix']/div[@class='mainBoxL']/div[@id='hsLoan-pos' and @class='lefBox']/div[@class='gfdk_con']/table")) {
								System.out.println(text + "\t" + opw.url.toString());
							}
							*/
							
							
							try {
								md.update(text.getBytes("utf8"));
								md.digest(dig_id, 0, 16);
								for (int j = 0; j < dig_id.length; ++j) {
									docid=docid<<8;
									docid += dig_id[j]&0xff;
								}
							} catch (Exception ee) {
								docid = Long.valueOf(e.text.toString().length());
								// TODO Auto-generated catch block
								ee.printStackTrace();
							}
						} else {
							docid = ran.nextLong();
						}

						XpathStat xs = xpathes.get(e.full_xpath);
						if (xs != null) {
							xs.mc_num++;
							xs.area += e.width*e.height;
							xs.img_iframe_area += e.img_iframe_area;
							xs.numXpath2code.put(e.num_xpath, docid);
							if (e.is_video) {
								xs.is_video = true;
							}
							if (e.has_download) {
								xs.has_download = true;
							}
							xs.text.append(text);
						//	xs.num_xpathes.add(e.num_xpath);
						} else {
							xs = new XpathStat();
							xs.xpath = e.full_xpath;
							xs.area = e.width*e.height;
							xs.img_iframe_area = e.img_iframe_area;
							xs.mc_num = 1;
							if (e.is_video) {
								xs.is_video = true;
							}
							if (e.has_download) {
								xs.has_download = true;
							}
							//xs.num_xpathes.add(e.num_xpath);
							xs.numXpath2code.put(e.num_xpath, docid);
							xs.text.append(text);
							xpathes.put(e.full_xpath, xs);
						}
					}
					
					htmlContentHandler.setXpath(xpath_set);
					parser.parse(new InputSource(new StringReader(htmlPage)));
					PageStat ps = new PageStat();
					ps.url = value.url.toString();
					ps.title = title;
					for (Integer block_type: type_xpathes.keySet()) {
						HashMap<String, XpathStat> xpathes = type_xpathes.get(block_type);
						ArrayList<XpathStat> xses = new ArrayList<XpathStat>();
						for (String xp: xpathes.keySet()) {
							int num = htmlContentHandler.xpath2num.get(xp);
							XpathStat xs = xpathes.get(xp);
							xs.num = num;
							md.reset();
							String text = xs.text.toString();
							
							Long docid= new Long(0);
							if (text.length() > 0 && xs.area>xs.img_iframe_area*2  && xs.area<800000 && !xs.is_video && !xs.has_download) {
								
								try {
									md.update(text.getBytes("utf8"));
									md.digest(dig_id, 0, 16);
									for (int j = 0; j < dig_id.length; ++j) {
										docid=docid<<8;
										docid += dig_id[j]&0xff;
									}
								} catch (Exception ee) {
									docid = Long.valueOf(xs.text.toString().length());
									// TODO Auto-generated catch block
									ee.printStackTrace();
								}
							} else {
								docid = ran.nextLong();
							}
							xs.hash_code = docid;
							xs.text.setLength(0);
							xs.text.trimToSize();
							xses.add(xs);
					//		context.write(new Text(value.url + "\t" + text + "\t" + docid), new Text(""));
						}
						ps.xses.put(block_type, xses);
					}

					
					context.write(value.url, new Text(ps.toString()));

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
		
		protected void setup(Context context) throws IOException, InterruptedException {

		}
		
		public void reduce(Text key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			for (Text val: values) {
				context.write(key, val);
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
		Tool pageScanner = new MRWebkitMaincontentGetterDebug();
		ToolRunner.run(pageScanner, args);
	}

}
