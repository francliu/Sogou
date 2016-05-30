package com.sogou.cm.pa.pagecluster;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
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
import info.monitorenter.cpdetector.io.UnicodeDetector;



public class PageScaner {
	
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
	
	public static void main(String[] args) throws IOException, InterruptedException {
		OriginPageInputFormatV3 inputFormat = new OriginPageInputFormatV3();
		
		RecordReader reader = inputFormat.createRecordReader(null, null);


		FileSystem fs = FileSystem.getLocal(new Configuration());
		FSDataInputStream fdis = fs.open(new Path("C:\\Users\\sunjian\\Downloads\\pages0"));
		File file = new File("C:\\Users\\sunjian\\Downloads\\pages1");
		FileSplit file_split = new FileSplit(new Path("C:\\Users\\sunjian\\Downloads\\pages0"), 0, file.length(), null);
		reader.initialize(file_split, new TaskAttemptContextImpl(new Configuration(), new TaskAttemptID()));

		
		CodepageDetectorProxy	codepageDetectorProxy	= CodepageDetectorProxy.getInstance();
		byte[] output = new byte[4096 * 1024 + 1];
		codepageDetectorProxy.add(new ByteOrderMarkDetector());
		// this.codepageDetectorProxy.add(new ParsingDetector(false));
		codepageDetectorProxy.add(JChardetFacade.getInstance());
		
		PageSegmentationV2		htmlContentHandler		= new PageSegmentationV2();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		
		
		int progress = 0;
		ArrayList<HtmlPage> pages = new ArrayList<HtmlPage>();
		while (reader.nextKeyValue()) {
			progress++;
			if (progress % 1000 == 0) {
				
				System.err.println("Progress: " + progress);
				System.err.println(getMemStat());
			}
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
					Charset charset = codepageDetectorProxy.detectCodepage(in, len);
					String htmlPage = new String(output, 0, resultLength, charset);
					try {
						
//System.out.println(opw.url.toString());
						htmlContentHandler.setHtmlCode(htmlPage);
						parser.parse(new InputSource(new StringReader(htmlPage)));
						HtmlPage html_page = htmlContentHandler.html_page;
						
						html_page.url = opw.url.toString();
						
					//	HtmlPage h2 = new HtmlPage();
					//	h2.fromString(html_page.toString());
					//	h2.url = opw.url.toString();
						pages.add(html_page);
						/*
						if (opw.url.toString().equalsIgnoreCase("http://www.yesky.com/imagesnew/software/img/2006-2-14/news/3597.html")) {
							System.out.println(htmlPage);
						//	System.out.println(html_page.tags);
						}
						*/
						
						
						
						
						
						
					//	System.out.println(opw.url);
					} catch (Exception e) {
					//	System.out.println(e.getCause());
					}
				} catch (Exception e) {
					
				}
			}

			//System.out.println(opw.url.toString() + "\t" + opw.body.getLength());
		}
		HierarchicalClusterV2 cluster = new HierarchicalClusterV2();
		System.out.println(pages.size());
		
		int t1 = 0, t2 = 1;
		for (int i = 0; i < pages.size(); ++i) {
			if (pages.get(i).url.equalsIgnoreCase("http://www.fxdzw.com/dzsj/MCU_ps2.htm")) {
				t1 = i;
			}
			
//			if (pages.get(i).url.equalsIgnoreCacat tse("http://demon1decadent.blog.sohu.com/entry/")) {
			if (pages.get(i).url.equalsIgnoreCase("http://www.fxdzw.com/dpj/ls_aj.htm")) {
				//System.out.println("asdf: " + pages.get(i).url);
				t2 = i;
			}
		//	System.out.println(pages.get(i).url);
		}
	//	System.out.println(pages.get(t1).url + "\t" + pages.get(t2).url + "\t" + pages.get(t1).getDistance_debug(pages.get(t2)));
	//	System.out.println(pages.get(t1).url + "\t" + pages.get(t2).url + "\t" + pages.get(t2).getDistance_debug(pages.get(t1)));
		/*
		HtmlPage p1 = pages.get(0);
		HtmlPage p2 = pages.get(1);
		System.out.println(p1.getDistance(p2) + " " + p1.getDistance(p2));
		long start = System.currentTimeMillis();
		for (int t = 0; t < 1000000; ++t) {
			double d = p1.getDistance(p2);
		}
		long end = System.currentTimeMillis();
		System.out.println("elapse time: " + (end - start));
		*/
		
		int cluster_num = cluster.cluster(pages);
		
		ArrayList<ArrayList<HtmlPage>> result = cluster.data_t;
		
		System.out.println("cluster_num: " + cluster_num);
		int big_clu = 0;
		int big_clu_url = 0;
		int all_url = 0;
		for (int i = 0; i < cluster_num; ++i) {
			ArrayList<HtmlPage> cl = result.get(i);
			all_url += cl.size();
			if (cl.size() >= 1) {
				++big_clu;
				big_clu_url += cl.size();
			} else {
				continue;
			}
			System.out.println(cl.size());
			for (int j = 0; j < cl.size(); ++j) {
				System.out.println(cl.get(j).url);
			}
		}
		System.out.println(big_clu + "\t" + big_clu_url + "\t" + all_url);
		System.out.println(pages.get(t1).url + "\t" + pages.get(t2).url + "\t" + pages.get(t1).getDistance_debug(pages.get(t2)));
		System.out.println(pages.get(t1).url + "\t" + pages.get(t2).url + "\t" + pages.get(t2).getDistance_debug(pages.get(t1)));
		System.out.println(big_clu + "\t" + big_clu_url + "\t" + all_url);
		
		/*
		Random random = new Random();
		int sample_num = 10;
		int index = 0;
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("site_clusters.txt")));
		for (int i = 0; i < cluster_num; ++i) {
			ArrayList<HtmlPage> temp = result.get(i);
			ClusterInfo ci = new ClusterInfo();
			int k = 0;
			if (temp.size() < 5) {
				continue;
			}
			for (HtmlPage hp: temp) {
				ci.urls.add(hp.url);
				++k;
				if (k <= sample_num) {
					ci.pages.add(hp);
				} else {
					int r = random.nextInt(k);
					if (r < sample_num) {
						ci.pages.set(r, hp);
					}
				}
			}
			ci.limit = 0.0;
			for (int j = 0; j < ci.pages.size(); ++j) {
				HtmlPage hp1 = ci.pages.get(j);
				for (int t = 0; t < j; ++t) {
					double dist = hp1.getDistance(ci.pages.get(t));
					if (dist > ci.limit) {
						ci.limit = dist;
					}
				}
			}
			ci.level = "site";
			ci.site = "www.tudou.com";
			ci.key = ci.level + " " + ci.site + " " + index;
			++index;
			writer.write(ci.toString_debug());
			writer.newLine();
		}
		
		writer.flush();
		writer.close();
		*/
	}
}
