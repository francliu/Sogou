package com.sogou.cm.pa.maincontent;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;






import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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

public class Test2 {
	public static void main(String[] args) throws XPathExpressionException, IOException,
    SAXNotRecognizedException, SAXNotSupportedException,
    TransformerFactoryConfigurationError, TransformerException {
		String s = "2010年03月16日09:54";
		System.out.println(s.matches("(.*[^0-9]|^)[12][0-9]{3}年[0-9]{1,2}月[0-9]{1,2}日.*"));
		System.out.println(s.matches("(.*[^0-9]|^)[12][0-9]{3}[-/][0-9]{1,2}[-/][0-9]{1,2}.*"));
	}
}
