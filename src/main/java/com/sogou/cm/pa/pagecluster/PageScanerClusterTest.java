package com.sogou.cm.pa.pagecluster;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;

import com.sogou.web.selector.offsum.OriginPageInputFormatV3;
import com.sogou.web.selector.offsum.OriginPageWritable;
import com.sogou.web.selector.offsum.OriginPageWritable.Attribute;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
//import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.HTMLCodepageDetector;   
import info.monitorenter.cpdetector.io.ParsingDetector;
//import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnicodeDetector;

public class PageScanerClusterTest {
	public static void main(String[] args) throws IOException, InterruptedException {
		OriginPageInputFormatV3 inputFormat = new OriginPageInputFormatV3();
		
		RecordReader reader = inputFormat.createRecordReader(null, null);


		FileSystem fs = FileSystem.getLocal(new Configuration());
		FSDataInputStream fdis = fs.open(new Path("C:\\Users\\sunjian\\Downloads\\pages0"));
		File file = new File("C:\\Users\\sunjian\\Downloads\\pages1");
		FileSplit file_split = new FileSplit(new Path("C:\\Users\\sunjian\\Downloads\\pages0"), 0, file.length(), null);
		reader.initialize(file_split, new TaskAttemptContextImpl(new Configuration(), new TaskAttemptID()));

		
		
		CodepageDetectorProxy	codepageDetectorProxy	= CodepageDetectorProxy.getInstance();
	//	CodepageDetectorProxy	codepageDetectorProxy2	= CodepageDetectorProxy.getInstance();
		byte[] output = new byte[4096 * 1024 + 1];
	//	codepageDetectorProxy.add(new ByteOrderMarkDetector());
	//	codepageDetectorProxy.add(new ParsingDetector(false));
		codepageDetectorProxy.add(JChardetFacade.getInstance());
	//	codepageDetectorProxy.add(new ParsingDetector(false));
	//	codepageDetectorProxy.add(JChardetFacade.getInstance());
	//	codepageDetectorProxy.add(new ParsingDetector(false));
		//codepageDetectorProxy.add(new HTMLCodepageDetector(false));
		
		//codepageDetectorProxy.add(ASCIIDetector.getInstance());
		//codepageDetectorProxy.add(UnicodeDetector.getInstance());
		//codepageDetectorProxy.
		
		ClusterPredictor predictor = new ClusterPredictor("t.txt");
		HashMap<String, ArrayList<String>> cluster_result = new HashMap<String, ArrayList<String>>();
		
		PageSegmentation		htmlContentHandler		= new PageSegmentation();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		
		int progress = 0;
		ArrayList<HtmlPage> pages = new ArrayList<HtmlPage>();
		long begin = System.currentTimeMillis();
		long ela = 0;
	//	int cnt = 0;
		while (reader.nextKeyValue()) {
			progress++;
			if (progress % 100 == 0)
				System.err.println("Progress: " + progress);
			BytesWritable docId = (BytesWritable) reader.getCurrentKey();
			OriginPageWritable opw = (OriginPageWritable) reader.getCurrentValue();
			if (opw == null)
				continue;
			
			//System.out.println(opw.url);
			if (opw.url.toString().length() > 256) {
				continue;
			}
			
			Attribute err_reason = opw.getAttribute("Error-Reason");
			if (err_reason != null) {
				continue;
			}
			if (opw.body.getLength() > 0) {
				int rc = 0;
				try {

					Attribute originSiteAttr = opw.getAttribute("Original-Size");
					if (originSiteAttr == null)
						throw new IOException("NULL Original-Size");
					int originalSize = Integer.parseInt(originSiteAttr.val.toString());
					if (originalSize > 4096 * 1024) {
						rc = 1;
						throw new Exception("error");
					}
					Inflater decompresser = new Inflater();
					decompresser.setInput(opw.body.getBytes(), 0, opw.body.getLength());
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

					ByteArrayInputStream in = new ByteArrayInputStream(output);
					int len = resultLength < 50000 ? resultLength : 50000;
					len = resultLength;					Charset charset = codepageDetectorProxy.detectCodepage(in, len);

					if (charset.equals(Charset.forName("GB2312"))||charset.equals(Charset.forName("Big5"))) {
						charset = Charset.forName("GBK");
					}
				//	if (charset.equals(Charset.forName("Big5")))
					System.out.println(opw.url.toString() + "\t" + charset);
					String htmlPage = new String(output, 0, resultLength, "GBK");
			//	if (opw.url.toString().equals("http://www.19lou.com/forum-1637-thread-38435851-21217-1.html"))
				//	System.out.println(htmlPage);
					try {
						for (int i = 0; i < 1; ++i) {
							/*
							long b = System.currentTimeMillis();
							htmlContentHandler.setHtmlCode(htmlPage);
							parser.parse(new InputSource(new StringReader(htmlPage)));
							long a = System.currentTimeMillis() ;
							ela += a- b;
							System.out.println(opw.url + "\t" + (a-b));
							*/
							/*
							long b = System.currentTimeMillis();
							String re = predictor.predict(opw.url.toString(), htmlPage);
						//	System.out.println("result:  " + re);
							long a = System.currentTimeMillis() ;
							ela += a- b;
							System.out.println(opw.url + "\t" + (a-b));
							if (re == null) {
								System.out.println("null result: " + opw.url.toString());
							} else {
								if (cluster_result.containsKey(re)) {
									cluster_result.get(re).add(opw.url.toString());
								} else {
									ArrayList<String> temp = new ArrayList<String>();
									temp.add(opw.url.toString());
									cluster_result.put(re, temp);
								}
							}
							*/
						}
					} catch (Exception e) {
						
					}
				} catch (Exception e) {
					
				}
			}
			/*

			*/
			//System.out.println(opw.url.toString() + "\t" + opw.body.getLength());
		}
		long end = System.currentTimeMillis();
		
		for (String key: cluster_result.keySet()) {
			ArrayList<String> t = cluster_result.get(key);
			System.out.println(key + "\t" + t.size());
			for (String url: t) {
				System.out.println(url);
			}
		}
	//	System.out.println("time elapse: " + predictor.page_parse_time + "\t" + predictor.predict_time);
	//	System.out.println("time elapse: " + (end-begin));
	//	System.out.println("time elapse:  " + ela);
	}
}
