package com.sogou.pa.turnpage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Handler;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.ccil.cowan.tagsoup.PYXScanner;
import org.ccil.cowan.tagsoup.Parser;
import org.ccil.cowan.tagsoup.Scanner;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ccil.cowan.tagsoup.XMLWriter; 
class ElementTest{
	int child;
}
public class Test {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception
	{
//		File file = new File("D:\\JavaWorkplace\\onlineHadoop\\src\\Tagsoup\\htmlTest");
//		BufferedReader reader = null;
//		//System.out.println("以行为单位读取文件内容，�?��读一整行�?);
//		reader = new BufferedReader(new FileReader(file));
//		String str="",html="";
//		while ((str = reader.readLine()) != null) {
//			html+=str;
//		}
//		reader.close();
		ElementTest tmp = new ElementTest();
		tmp.child = 1;
		ArrayList<ElementTest> res = new ArrayList<ElementTest>();
		res.add(tmp);
		System.out.println("-------b-----"+res.get(res.size()-1).child);
		tmp.child = 13;
		System.out.println("-------a-----"+res.get(res.size()-1).child);
		HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet("http://www.goupuzi.com/simple/t411394_33.html");
        HttpResponse response = client.execute(request);
        String c = "哈哈";
        System.out.println(c.length());
        // Check if server response is valid
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() != 200) {
            throw new IOException("Invalid response from server: " + status.toString());
        }

        // Pull content stream from response
        HttpEntity entity = response.getEntity();
        InputStream inputStream = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)); 
        String html = "",str="";
        while((str=reader.readLine())!=null)
        {
        	html+=str;
        	//System.out.println(str);
        }
		StringReader xmlReader = new StringReader("");  
		StringReader sr = new StringReader(html);  
		InputSource src = new InputSource(sr);//构建InputSource实例  
		Parser parser = new Parser();//实例化Parse  
		XMLWriter writer = new XMLWriter();//实例化XMLWriter，即SAX内容处理�? 

		Scanner scan = new PYXScanner();  
		//Handler h = new Handler();
		parser.setContentHandler(writer);//设置内容处理�? 
		parser.parse(src);//解析  
		scan.scan(xmlReader, parser);//通过xmlReader读取解析后的结果  
		char[] buff = new char[1024]; 
		File result = new File("D:\\JavaWorkplace\\onlineHadoop\\src\\Tagsoup\\htmlTest.xml");
		BufferedWriter writerXml = new BufferedWriter(new FileWriter(result));
		System.out.println("------------");
		System.out.println(xmlReader.read());
		while(xmlReader.read(buff) != -1) { 
			writerXml.write(new String(buff));
			System.out.println("------------");
		    System.out.println(new String(buff));//打印解析后的结构良好的HTML文档  
		} 
		writerXml.flush();
		writerXml.close();
	}
}

