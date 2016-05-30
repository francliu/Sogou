package com.sogou.cm.pa.pagecluster;

import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sogou.cm.pa.multipage.maincontent.MainContentExtractorFromXpath;
import com.sogou.web.selector.offsum.OffsumPageWritableV2;
import com.sogou.web.selector.offsum.OriginPageInputFormatV3;
import com.sogou.web.selector.offsum.OffsumPageInputFormatV2.OffsumPageRecordReader;
import com.sogou.web.selector.offsum.OffsumPageWritableV2.ContentItem;

public class PageScanerOffsumPage_debug5 {
	public static void main(String[] args) throws IOException, InterruptedException, SAXException {
		OriginPageInputFormatV3 inputFormat = new OriginPageInputFormatV3();
		
		RecordReader reader = inputFormat.createRecordReader(null, null);


		FileSystem fs = FileSystem.getLocal(new Configuration());
		FSDataInputStream fdis = fs.open(new Path("C:\\Users\\sunjian\\Downloads\\pages0"));
		File file = new File("C:\\Users\\sunjian\\Downloads\\pages1");
		FileSplit file_split = new FileSplit(new Path("C:\\Users\\sunjian\\Downloads\\pages0"), 0, file.length(), null);
		reader.initialize(file_split, new TaskAttemptContextImpl(new Configuration(), new TaskAttemptID()));

		
		String test_url = "http://task.renwuyi.com/676848.html";				

		
		FileOutputStream f_stream = new FileOutputStream(new File("tt.html"));
		
		MainContentExtractorFromXpath		htmlContentHandler		= new MainContentExtractorFromXpath();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		
		HashMap<String, String> url2mc = new HashMap<String, String>();
		BufferedReader reader2 = new BufferedReader(new FileReader(new File("C:\\Users\\sunjian\\Downloads\\tt.txt")));
		
		String line;
		while ((line = reader2.readLine()) != null) {
			int i = line.indexOf("\t");
			url2mc.put(line.substring(0, i), line.substring(i+1));
		}
		reader2.close();
		
		CodepageDetectorProxy	codepageDetectorProxy	= CodepageDetectorProxy.getInstance();
		byte[] output = new byte[4096 * 1024 + 1];
		codepageDetectorProxy.add(new ByteOrderMarkDetector());
		// this.codepageDetectorProxy.add(new ParsingDetector(false));
		codepageDetectorProxy.add(JChardetFacade.getInstance());
		
		int progress = 0;
		long elapse = 0;
		int cnt = 0;
		while(reader.nextKeyValue()){
			progress++;
			//System.err.println("Progress: " + progress);
			OffsumPageWritableV2 opw = (OffsumPageWritableV2) reader.getCurrentValue();
			if(opw == null)
				continue;
		//	System.out.println(opw.url);

			if (!url2mc.containsKey(opw.url)) {
				continue;
			}
			int originalSize = 0;
			for(ContentItem ci : opw.contentItems){
				if (ci.type.equals("snapshot") == false)
					continue;
				originalSize = ci.originalSize;
				if (ci.content.length > 0) {
					int rc = 0;

						if (originalSize > 4096 * 1024) {
							rc = 1;
							continue;
						}
						Inflater decompresser = new Inflater();
						decompresser.setInput(ci.content);
						int resultLength=0;
						try {
							resultLength = decompresser.inflate(output);
						} catch (DataFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (resultLength != originalSize) {
							rc = 2;
							continue;
						}
						decompresser.end();
						int len = resultLength < 50000 ? resultLength : 50000;
						ByteArrayInputStream in = new ByteArrayInputStream(output);
						Charset charset = codepageDetectorProxy.detectCodepage(in, len);
						String htmlPage = new String(output, 0, resultLength, charset);
						int i = 0;
						while (true) {
							int j = htmlPage.indexOf("\n", i);
							if (j < 0 || j + 2 >= htmlPage.length()) {
								i = -1;
								break;
							}
							if (htmlPage.charAt(j+1) == '\n') {
								i = j+2;
								break;
							} else if (htmlPage.charAt(j+1) == '\r' && htmlPage.charAt(j+2) == '\n') {
								i = j+3;
								break;
							} else {
								i = j+1;
							}
						}
						if ( i < 0) {
							continue;
						}
					//	System.out.println(htmlPage.substring(i));
						htmlPage = htmlPage.substring(i);
						String mc = url2mc.get(opw.url);
						if (mc == null || mc.length() == 0) {
							continue;
						}
						HashMap<String, Integer> xpathes = new HashMap<String, Integer>();
						HashSet<String> mcs = new HashSet<String>();
						String[] segs = mc.split("\t");
						for (int j = 1; j < segs.length; ++j) {
							int type = Integer.valueOf(segs[j+1]);
							if (type != 1) {
								xpathes.put(segs[j], type);
							} else {
					//			System.out.println(segs[i]);
								mcs.add(segs[j]);	
							}
							++j;
						}
						long start = System.currentTimeMillis();
						htmlContentHandler.setXpathes(xpathes, mcs);
						parser.parse(new InputSource(new StringReader(htmlPage)));
						htmlContentHandler.getMainContent();
						long end = System.currentTimeMillis();
						elapse += end - start;
						++cnt;
						//System.out.println(htmlContentHandler.getMainContent());
				}
			}
			//opw.getOffsumPageBytes();
			//break;
		}
		System.out.println("time elapse: " + elapse + "\t" + cnt);
	}
}
