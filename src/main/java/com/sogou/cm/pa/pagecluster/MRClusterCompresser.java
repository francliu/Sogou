package com.sogou.cm.pa.pagecluster;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

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


public class MRClusterCompresser  extends Configured implements Tool {

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
				ci.fromSimpleString(s);
				String cluster_key = ci.level + "\t" + ci.site;
				context.write(new Text(cluster_key), new Text(ci.toSimpleString()));
				//String out = ci.toSimpleString();
				
				//context.write(new Text(ZipTools.compress(out.getBytes())), new Text("") );
			} catch (Exception e) {
				
			}
		}
		
		protected void cleanup(Context context) throws IOException, InterruptedException {

		}
	}
	


	public static class IntSumReducer extends
			Reducer<Text, Text, Text, Text> {
		byte[] output = new byte[4096 * 1024 + 1];
		protected void setup(Context context) throws IOException, InterruptedException {


		}
		
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			
			StringBuffer sb = new StringBuffer();
			int i = 1;
			for (Text val: values) {
				sb.append(val.toString());
				if (i%800==0) {
					byte[] bs = ZipTools.compress(sb.toString().getBytes());
					sb.setLength(0);
					sb.trimToSize();
					context.write(new Text(bs), null);
				}
				++i;
				
			//	context.write(key, val);
			}
			if (sb.length() > 0) {
				long start = System.currentTimeMillis();
				byte[] bs = ZipTools.compress(sb.toString().getBytes());
				long end = System.currentTimeMillis();
				context.getCounter("debug", "compress_time").increment(end-start);
				context.getCounter("debug", "compress_num").increment(1);
				context.write(new Text(bs), null);
				
				long a = System.currentTimeMillis();
				Inflater decompresser = new Inflater();
				decompresser.setInput(bs, 0, bs.length);
				int resultLength=0;
				context.getCounter("debug", "all").increment(1);
				try {
					resultLength = decompresser.inflate(output);
					//System.out.println("asdf");
				} catch (DataFormatException e) {
					context.getCounter("debug", "exception").increment(1);
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				}
				decompresser.end();
				long b = System.currentTimeMillis();
				context.getCounter("debug", "decompress_num").increment(1);
				context.getCounter("debug", "decompress_time").increment(b -a);
			}
			
		//	context.write(key, new Text(bs));
			
			/*
			ClusterInfo ci = new ClusterInfo();
			long a = System.currentTimeMillis();
			Inflater decompresser = new Inflater();
			byte[] bs = key.toString().getBytes();
			decompresser.setInput(bs, 0, bs.length);
			int resultLength=0;
			context.getCounter("debug", "all").increment(1);
			try {
				resultLength = decompresser.inflate(output);
				//System.out.println("asdf");
			} catch (DataFormatException e) {
				context.getCounter("debug", "exception").increment(1);
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			}
			decompresser.end();
			long b = System.currentTimeMillis();
			ci.fromSimpleString(new String(output, 0, resultLength));
			context.getCounter("debug", "all_time").increment(System.currentTimeMillis() -a);
			context.getCounter("debug", "decompress_time").increment(b -a);
			context.write(key, null);
			*/
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
		Tool tool = new MRClusterCompresser();
		ToolRunner.run(tool, args);
	}

}
