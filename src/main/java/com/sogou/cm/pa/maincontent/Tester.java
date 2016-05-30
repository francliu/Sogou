package com.sogou.cm.pa.maincontent;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;






import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

public class Tester {
	public static void main(String[] args) throws XPathExpressionException, IOException,
    SAXNotRecognizedException, SAXNotSupportedException,
    TransformerFactoryConfigurationError, TransformerException {

XPathFactory xpathFac = XPathFactory.newInstance();
XPath xpath = xpathFac.newXPath();

InputStream input = new FileInputStream("t.html");
InputSource is = new InputSource(input);
is.setEncoding("UTF-8");

XMLReader reader = new Parser();
reader.setFeature(Parser.namespacesFeature, false);
Transformer transformer = TransformerFactory.newInstance().newTransformer();

DOMResult result = new DOMResult();
transformer.transform(new SAXSource(reader, is), result);

Node htmlNode = result.getNode();
NodeList nodes = (NodeList) xpath.evaluate("/html/body/div[starts-with(@class, 'main') and @class='main clearfix']/div[@class='line_l clearfix']/div/div[@class='tabcontent pd']", htmlNode, XPathConstants.NODESET);
System.out.println(nodes.getLength());
for (int i = 0; i < nodes.getLength(); ++i) {
	Node node = nodes.item(i);
	System.out.println(node.getTextContent());
}
}
}
