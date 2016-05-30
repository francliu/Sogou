package com.sogou.pa.ListPage;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
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
import info.monitorenter.cpdetector.io.UnicodeDetector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

class Counter {
	int cnt = 0;
}

public class PageScaner {

	public static void main(String[] args) throws Exception {

		OriginPageInputFormatV3 inputFormat = new OriginPageInputFormatV3();
		
		RecordReader reader = inputFormat.createRecordReader(null, null);


		String fn = "C:\\Users\\sunjian\\Downloads\\page.0530";
		File file = new File(fn);
		FileSplit file_split = new FileSplit(new Path(fn), 0, file.length(), null);
		reader.initialize(file_split, new TaskAttemptContextImpl(new Configuration(), new TaskAttemptID()));
		String test_url = "http://mxd.duowan.com/";
	//	FileOutputStream f_stream = new FileOutputStream(new File("tt.html"));
		
		CodepageDetectorProxy	codepageDetectorProxy	= CodepageDetectorProxy.getInstance();
		byte[] output = new byte[4096 * 1024 + 1];
		codepageDetectorProxy.add(new ByteOrderMarkDetector());
		// this.codepageDetectorProxy.add(new ParsingDetector(false));
		codepageDetectorProxy.add(JChardetFacade.getInstance());

		ListBlockExtractor extractor = new ListBlockExtractor();
		Parser					parser					= new Parser();
		parser.setContentHandler(extractor);
		Random random = new Random();
		int page_cnt = 0;
		int progress = 0;
		while (reader.nextKeyValue()) {
			progress++;
			if (progress % 1000 == 0)
				System.err.println("Progress: " + progress);
			BytesWritable docId = (BytesWritable) reader.getCurrentKey();
			OriginPageWritable opw = (OriginPageWritable) reader.getCurrentValue();
			if (opw == null)
				continue;
			
			
/*
			if (Math.abs(ran.nextInt())%1000 <100) {
				System.out.println(opw.url);
			}
			if (!opw.url.toString().equals("asdf")) {
				continue;
			}
	*/		
			
			if (opw.url.toString().length() > 256) {
				continue;
			}
			if (!opw.url.toString().equals(test_url)) {
				continue;
			}

			
			Attribute err_reason = opw.getAttribute("Error-Reason");
			if (err_reason != null) {
				continue;
			}
			if (opw.body.getLength() > 0) {
				int rc = 0;

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
				//	System.out.println(opw.body.getBytes().length + "\t" + opw.body.getLength());
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

					String htmlPage = new String(output, 0, resultLength, "UTF-8");
					/*
					if (opw.url.toString().equals("http://www.etao.com/buy/cp_x8e1pM7A0sK808jevNO68Q.html")) {
						System.out.println(htmlPage);
						
					} else {
						continue;
					}
					*/
					
					//	System.out.println(htmlPage);
						BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("t.html")), "utf8"));
						writer2.write("<base href=\"" + opw.url.toString() + "\">\n");
						writer2.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf8\">\n");
						writer2.write(htmlPage);
						writer2.close();
						parser.parse(new InputSource(new StringReader(htmlPage)));

						Counter cnt = new Counter();
						String out = extractor.root.traverse_debug(cnt);
						for (Element e: extractor.text_blocks) {
					//		System.out.println(e.text_len + "\t" + e.anchor_len + "\t" + e.anchor_num + "\t" + e.text);	
						}
						System.out.println(cnt.cnt);
						System.out.println("success.");
						
				//		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("page_out/" + page_cnt + ".html")), "utf8"));
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("tt.html")), "utf8"));
						writer.write("<base href=\"" + opw.url.toString() + "\">\n");
						writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf8\">\n");
						writer.write(out);
						writer.flush();
						writer.close();
						page_cnt++;
			}

			//System.out.println(opw.url.toString() + "\t" + opw.body.getLength());
		}
	}
}
