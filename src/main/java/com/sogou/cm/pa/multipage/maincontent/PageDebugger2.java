package com.sogou.cm.pa.multipage.maincontent;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
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

import com.sogou.cm.pa.maincontent.RuleBasedMainContentExtractor;
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

public class PageDebugger2 {
	public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException {

		String test_url = "http://detail.china.alibaba.com/offer/1218300859.html";
	//	String test_xpath = "/html/body/div[@class='w']/div[@class='right']/div[@id='product-detail' and @class='m m1']/div[@class='ui-switchable-panel']/div[@id='product-detail-1' and @class='mc']/div[@class='p-parameter']";
	//	       test_xpath = "/html/body";
		int common_cnt = 0;
		
		FileOutputStream f_stream = new FileOutputStream(new File("tt2.html"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(f_stream, "utf8"));
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File("ttt2.html")));
		
		HashMap<String, String> xpath2text = new HashMap<String, String>();
		
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

		RuleBasedMainContentExtractor main_content = new RuleBasedMainContentExtractor();
		XPathFactory xpathFac = XPathFactory.newInstance();
		XPath xpath = xpathFac.newXPath();
		
		HashMap<String, String> url2cluster = new HashMap<String, String>();
		HashMap<String, ArrayList<HtmlXpath>> cluster2mc = new HashMap<String, ArrayList<HtmlXpath>>();
		BufferedReader reader2 = new BufferedReader(new FileReader(new File("C:\\Users\\sunjian\\Downloads\\sample_url.txt")));
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
		String clusterid = url2cluster.get(test_url);
		if (clusterid == null) {
			return;
		}
		
		ArrayList<HtmlXpath> mcs = cluster2mc.get(clusterid);
		if (mcs == null) {
	//		return;
		}
		//System.out.println("here.");
		int progress = 0;
		while (reader.nextKeyValue()) {
			progress++;
			if (progress % 1000 == 0)
				System.err.println("Progress: " + progress);
			BytesWritable docId = (BytesWritable) reader.getCurrentKey();
			OriginPageWritable opw = (OriginPageWritable) reader.getCurrentValue();
			if (opw == null)
				continue;
			
		//	System.out.println(opw.url);

			if (opw.url.toString().length() > 256) {
				continue;
			}
			 if (!opw.url.toString().equalsIgnoreCase(test_url)) {
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
				//	String htmlPage = new String(output, 0, resultLength, "UTF-8");
					writer.write(htmlPage);
					try {
					//	System.out.println("here.");
						MaincontentExtractor		htmlContentHandler		= new MaincontentExtractor();
						Parser					parser					= new Parser();
						parser.setContentHandler(htmlContentHandler);
						//try {
							ArrayList<HtmlXpath> xpathes = new ArrayList<HtmlXpath>();
							HtmlXpath hx = new HtmlXpath();
							htmlContentHandler.setXpath(mcs);

							parser.parse(new InputSource(new StringReader(htmlPage)));
							ArrayList<Element> blocks = htmlContentHandler.mces;
						String out = "";
						for (int i = 0; i < blocks.size(); ++i) {
							Element f = blocks.get(i);
						//	System.out.println(f.xpath + "\t" + f.full_xpath + "\t" +  f.text);
						//	System.out.println(f.text_len  + "\t" +  f.anchor_len + "\t" + f.text);
						//	System.out.println(f.anchor_len + "\t" + (f.text_len-f.anchor_len) + "\t" + f.max_text_len + "\t" + f.text_num + "\t" + f.text);
					        out += String.format("<div style=\"position: absolute; left: %dpx; top: %dpx; width: %dpx; height: %dpx;  border:3px solid red;\">%s</div>\n",f.left, f.top, f.width, f.height, f.xpath +"\t" +  f.text);

						//	System.out.println(f.toPrintString());
						}
						System.out.println(out);
						writer2.write(out);
						/*
						InputSource is =  new InputSource(new StringReader(htmlPage));
						
						XMLReader xmlreader = new Parser();
						xmlreader.setFeature(Parser.namespacesFeature, false);
						Transformer transformer = TransformerFactory.newInstance().newTransformer();
						
						DOMResult result = new DOMResult();
						transformer.transform(new SAXSource(xmlreader, is), result);
						
						Node htmlNode = result.getNode();

							NodeList nodes = (NodeList) xpath.evaluate(test_xpath, htmlNode, XPathConstants.NODESET);
							System.out.println(nodes.getLength());
							String s2 = "";
							if (nodes.getLength() != 0) {
								
								for (int i = 0; i < nodes.getLength(); ++i) {
									Node node = nodes.item(i);
									s2 += node.getTextContent();
								}
								s2 = s2.replaceAll("[ \t\r\n]?(&nbsp;)?(&gt;)?", "");
								System.out.println(s2);

								
							}
*/
					//	System.out.println(opw.url.toString());
					//	System.out.println(opw.url);
					} catch (Exception e) {
						
					}
					
				} catch (Exception e) {
					
				}
			}

			//System.out.println(opw.url.toString() + "\t" + opw.body.getLength());
		}
		/*
		Iterator iter = xpath2text.entrySet().iterator();

		while (iter.hasNext()) {
			Entry entry = (Entry)iter.next();
			String xp = (String)entry.getKey();
			String text = (String)entry.getValue();
			System.out.println(xp + "\t" + text);

		}
		
*/
		writer.close();
		writer2.close();
	}
}
