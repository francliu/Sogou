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

public class MRCrawledUrlGetter extends Configured implements Tool {
	
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

		
		
		protected void setup(Context context) throws IOException, InterruptedException {

		}

		
		protected void map(BytesWritable key, OriginPageWritable value, Context context) throws IOException, InterruptedException {

			if (key == null)
				return;

			if (value == null) {
				return;
			}
			
			if (value.url.toString().length() > 256) {
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
					/*
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
*/
					context.write(value.url, new Text(""));

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
			context.write(key, null);
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
		Tool pageScanner = new MRCrawledUrlGetter();
		ToolRunner.run(pageScanner, args);
	}

}
