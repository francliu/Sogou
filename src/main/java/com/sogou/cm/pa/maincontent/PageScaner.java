package com.sogou.cm.pa.maincontent;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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

public class PageScaner {
	public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException {

		OriginPageInputFormatV3 inputFormat = new OriginPageInputFormatV3();
		
		RecordReader reader = inputFormat.createRecordReader(null, null);


		FileSystem fs = FileSystem.getLocal(new Configuration());
		File file = new File("C:\\Users\\sunjian\\Downloads\\pages0");
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
		
		int progress = 0;
		ArrayList<HtmlPage> pages = new ArrayList<HtmlPage>();
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

					String htmlPage = new String(output, 0, resultLength, "UTF-8");
					/*
					if (opw.url.toString().equals("http://henan.163.com/15/0906/11/B2QUCTJA02270HI2.html")) {
						System.out.println(htmlPage);
						
					} else {
						
						continue;
					}
					*/
					try {
						//System.out.println(opw.url.toString());
					//	if (!opw.url.toString().equalsIgnoreCase("http://ask.ci123.com/questions/show/584606/"))
						//	continue;
					//	System.out.println(htmlPage);
				//		System.out.println(opw.url.toString());
						List<Element> blocks = main_content.extractAllBlock(opw.url.toString(), htmlPage);
						//if (main_content.html_page.is_hub2 )
						for (Element e: blocks) {
						}
						//	System.out.println(opw.url.toString() );

						
						/*
						InputSource is =  new InputSource(new StringReader(htmlPage));
						
						XMLReader xmlreader = new Parser();
						xmlreader.setFeature(Parser.namespacesFeature, false);
						Transformer transformer = TransformerFactory.newInstance().newTransformer();
						
						DOMResult result = new DOMResult();
						transformer.transform(new SAXSource(xmlreader, is), result);
						
						Node htmlNode = result.getNode();
				         
				         
						for (Element e: blocks) {
							//System.out.println(e.full_xpath + "\t" + e.text);

							NodeList nodes = (NodeList) xpath.evaluate(e.full_xpath, htmlNode, XPathConstants.NODESET);
							//System.out.println(nodes.getLength());
							if (nodes.getLength() != 1) {
								int len1 = e.text.toString().length();
								String s2 = "";
								for (int i = 0; i < nodes.getLength(); ++i) {
									Node node = nodes.item(i);
									s2 += node.getTextContent();
								}
								s2 = s2.replaceAll("[ \t\r\n]?(&nbsp;)?(&gt;)?", "");
								if (s2.length() > len1 + 10) {
									System.out.println(nodes.getLength() + "\t" + (s2.length() - len1) + "\t" + e.full_xpath + "\t" + e.text);
								}
							}

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
	}
}
