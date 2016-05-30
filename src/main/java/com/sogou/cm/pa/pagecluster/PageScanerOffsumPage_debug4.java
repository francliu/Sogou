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
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sogou.cm.pa.multipage.maincontent.MainContentExtractorFromFlagPage;
import com.sogou.web.selector.offsum.OffsumPageWritableV2;
import com.sogou.web.selector.offsum.OriginPageInputFormatV3;
import com.sogou.web.selector.offsum.OffsumPageInputFormatV2.OffsumPageRecordReader;
import com.sogou.web.selector.offsum.OffsumPageWritableV2.ContentItem;

public class PageScanerOffsumPage_debug4 {
	public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, SAXException, XPathExpressionException {
		OriginPageInputFormatV3 inputFormat = new OriginPageInputFormatV3();
		
		RecordReader reader = inputFormat.createRecordReader(null, null);


		FileSystem fs = FileSystem.getLocal(new Configuration());
		FSDataInputStream fdis = fs.open(new Path("C:\\Users\\sunjian\\Downloads\\pages0"));
		File file = new File("C:\\Users\\sunjian\\Downloads\\pages1");
		FileSplit file_split = new FileSplit(new Path("C:\\Users\\sunjian\\Downloads\\pages0"), 0, file.length(), null);
		reader.initialize(file_split, new TaskAttemptContextImpl(new Configuration(), new TaskAttemptID()));

		
		String test_url = "http://v.youku.com/v_show/id_XMTA4NDgxMDQw.html";
		
		BufferedReader freader = new BufferedReader(new FileReader(new File("url_mc.txt")));
		String line;
		HashMap<String, String> url2mc = new HashMap<String, String>();
		while ((line = freader.readLine()) != null) {
			String[] segs = line.split("\t");
			if (segs.length == 1) {
				//System.out.println(line);
				url2mc.put(segs[0], "");
			} else {
				url2mc.put(segs[0], segs[1]);
			}
		}
		freader.close();
		
		
		DocumentBuilder db;
		XPath xpath;
		XPathExpression expr;
		
		
		CodepageDetectorProxy	codepageDetectorProxy	= CodepageDetectorProxy.getInstance();
		byte[] output = new byte[4096 * 1024 + 1];
		codepageDetectorProxy.add(new ByteOrderMarkDetector());
		// this.codepageDetectorProxy.add(new ParsingDetector(false));
		codepageDetectorProxy.add(JChardetFacade.getInstance());
		
		MainContentExtractorFromFlagPage		htmlContentHandler		= new MainContentExtractorFromFlagPage();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);
		int progress = 0;
		while(reader.nextKeyValue()){
			progress++;
			//System.err.println("Progress: " + progress);
			OffsumPageWritableV2 opw = (OffsumPageWritableV2) reader.getCurrentValue();
			if(opw == null)
				continue;
		//	System.out.println(opw.url);
			if (!opw.url.equalsIgnoreCase(test_url)) {
		//		continue;
			}
			int originalSize = 0;
			for(ContentItem ci : opw.contentItems){
				if (ci.type.equals("xmlpage") == false)
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
						String xpage = new String(output, 0, resultLength, "UTF-16LE");
					//	System.out.println(xpage);
					//	System.out.println(htmlPage);
						
				        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				        db = dbf.newDocumentBuilder();
				        
				        XPathFactory factory = XPathFactory.newInstance();
				        xpath = factory.newXPath();
			         Document doc = db.parse(new InputSource(new StringReader(xpage)));
			         expr = xpath.compile("//content/major");
			         Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
			         if (node != null) {
			         	String content = node.getTextContent();
			         	if (url2mc.containsKey(opw.url))
			         		System.out.println(opw.url + "\t" + "http://task.www.sogou.com/web_sac/maincontent_"+opw.url.hashCode()+ ".html"+"\t" +  url2mc.get(opw.url) + "\t" + content);
			         }
						
				}
			}
			//opw.getOffsumPageBytes();
			//break;
		}
	}
}
