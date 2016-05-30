package com.sogou.pa.tagsoup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class HtmlContentTurnPageRecognize {
	
	@SuppressWarnings("resource")
	public static void getHtmlContent(String url,String html) throws IOException, SAXException, ParseException
	{

		int GetTurnPageNum=0;
		int TurnPageNum=0;
		//加载url读取url翻页标志
//		File urlfile = new File("/home/liujianfei/python/huhu/src/haha/tagsoup/TurnPage1000.txt");
//		File urlfile = new File("D:\\JavaWorkplace\\TurnPage\\src\\haha\\tagsoup\\1000NewTurnPage_05_08.txt");
		File urlfile = new File("D:\\JavaWorkplace\\TurnPage\\src\\haha\\tagsoup\\NewTurnPage1000.txt");
		
		BufferedReader urlreader = null;
		
		urlreader = new BufferedReader(new FileReader(urlfile));
		String tempString = null;
		int originTurnPageNum=0;
		int CorrectPage=0;
		HashMap<String,Long> urls = new HashMap<String,Long>();
		while ((tempString = urlreader.readLine()) != null) {
			String[] strs = tempString.split("\\s");
			//System.out.println(strs[0]);
			if(strs[4].compareTo("1")==0)
			{
				originTurnPageNum++;
				if(strs[2].compareTo("1")==0)CorrectPage++;
			}
			if(strs[2].compareTo("1")==0)
			{
				TurnPageNum++;
				urls.put(strs[0], (long) 1);
			}
			else urls.put(strs[0], (long) 0);
		}
		
		//读取数据识别是否为翻页
		HtmlContentDomTree		htmlContentHandler		= new HtmlContentDomTree();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);	
		File file;
		BufferedReader reader = null;
		int num=1;
		Vector<String> urlsResult = new Vector<String>();
//		File RandomFile = new File("D:\\JavaWorkplace\\TurnPage\\src\\haha\\IsTurnPage4.txt");
		File RandomFile = new File("D:\\JavaWorkplace\\TurnPage\\src\\haha\\IsTurnPage3.txt");
//		File RandomFile = new File("D:\\JavaWorkplace\\TurnPage\\src\\haha\\IsTurnPageold.txt");
		BufferedWriter writer = null;
		
		writer = new BufferedWriter(new FileWriter(RandomFile));
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//设置日期格式
		Date date1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
		        .parse(df.format(new Date()));
		System.out.println("运行开始时间："+df.format(new Date()));
		long start_time = date1.getTime();
		long total_read_time=0;
		long total_parse_time=0;
		while(num<=992)
		{
//			file = new File("D:\\JavaWorkplace\\TurnPage\\src\\pages\\t"+num+".html");
			file = new File("D:\\JavaWorkplace\\TurnPage\\src\\NewPage\\t"+num+".html");
//			file = new File("D:\\JavaWorkplace\\TurnPage\\src\\pages\\t636.html");
//			file = new File("D:\\JavaWorkplace\\TurnPage\\src\\NewPage\\t680.html");
			if(!file.exists())
			{
				num++;
				continue;
			}
			reader = new BufferedReader(new FileReader(file));
			tempString = null;
			int line=1;
//			System.out.println("翻d页:"+url+"---"+urls.get(url));
			Date date3 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
			        .parse(df.format(new Date()));
			long start_read_time = date3.getTime();
			while ((tempString = reader.readLine()) != null) {
				//System.out.println(tempString);
				if(line==1)url = tempString;
				else html+=tempString;
				line++;
				tempString=null;
				//break;
			}
			Date date4 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
			        .parse(df.format(new Date()));
			long end_read_time = date4.getTime();
			total_read_time+=end_read_time-start_read_time;
			htmlContentHandler.url=url;
			Date date5 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
			        .parse(df.format(new Date()));
			long start_parse_time = date5.getTime();
			parser.parse(new InputSource(new StringReader(html)));
			boolean trunPage = htmlContentHandler.IsTurnPage();
//			boolean trunPage = true;
//			System.out.println("IsFristPage:"+htmlContentHandler.IsFristPage);
			if(htmlContentHandler.IsFristPage!=1&&!trunPage)trunPage= TagContentTool.IsTurnByUrl(url);
			Date date6 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
			        .parse(df.format(new Date()));
			long end_parse_time = date6.getTime();
			total_parse_time+=end_parse_time-start_parse_time;
//			System.out.println("num:"+num+"old:"+urls.get(url)+"new:"+trunPage+htmlContentHandler.IsFirstPage+"-------------"+url);
			if(urls.get(url)!=null&&urls.get(url)>0)
			{
				writer.write("num:"+num+" "+url+"  1  "+trunPage+"\n");
//				TurnPageNum++;
			}
			if(trunPage)
			{
				urlsResult.add(tempString);
				if(urls.get(url)!=null&&urls.get(url)>0)
				{
					GetTurnPageNum++;
				}
				else
				{
					writer.write("num:"+num+" "+url+"  0  1\n");
				}
			}
			html="";
			url="";
			htmlContentHandler.clear();
			num++;
			System.out.println("翻页的召回率:"+GetTurnPageNum+"|"+TurnPageNum);
			System.out.println("翻页的准确率:"+GetTurnPageNum+"|"+urlsResult.size());
		}
		Date date2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
		        .parse(df.format(new Date()));
		System.out.println("运行结束时间："+df.format(new Date()));
		long end_time = date2.getTime();
        long interval_time = end_time - start_time > 0 ? end_time - start_time:0;
        System.out.println("运行读文件所花时间："+total_read_time+"ms");
        System.out.println("运行分析所有页面所花总时间："+total_parse_time+"ms");
        System.out.println("运行分析单个页面所花时间："+total_parse_time/(double)992+"ms");
        System.out.println("运行时间："+interval_time+"ms");
		writer.flush();
		writer.close();
		//System.out.println("原始翻页的准确率:"+OriginIsTurn);
		//正确翻页识别的召回率
		
		System.out.println("原始翻页的召回率:"+CorrectPage+"|"+TurnPageNum+"|"+CorrectPage/(double)TurnPageNum);
		System.out.println("原始翻页的准确率:"+CorrectPage+"|"+originTurnPageNum+"|"+CorrectPage/(double)originTurnPageNum);
	
		System.out.println("翻页的召回率:"+GetTurnPageNum+"|"+TurnPageNum+"|"+GetTurnPageNum/(double)TurnPageNum);
		System.out.println("翻页的准确率:"+GetTurnPageNum+"|"+urlsResult.size()+"|"+GetTurnPageNum/(double)urlsResult.size());
	}
	public static void main(String[] args) throws IOException, SAXException, ParseException {
		
	    String html="";
	    String url="";
		getHtmlContent(url,html);
		System.out.println(url);
		System.out.println(html);
	}
}
//reader = new BufferedReader(new FileReader(file));
//tempString = null;
//Vector<String> urlsResult1 = new Vector<String>();
//Pattern digit = Pattern.compile("^http.*");
//Pattern start = Pattern.compile("<!DOCTYPE HTML>");
//Pattern end = Pattern.compile("</html>");
//boolean starthtml = false;
//boolean endhtml = false;
//Matcher m;
//Vector<String> urlsResult = new Vector<String>();
//long num=0;
//while ((tempString = reader.readLine()) != null) {
//	m = digit.matcher(tempString);
//	if(m.matches())url = m.group();
//	String[] strs = tempString.split("\\s");
//	m = start.matcher(tempString);
//	if(m.matches())starthtml=true;
//	m = end.matcher(tempString);
//	if(m.matches())endhtml=true;
//	if(starthtml)
//	{
//		html+=tempString;
//	}
//	if(endhtml)
//	{
//		starthtml=false;
//		endhtml=false;
//		num++;
//		System.out.println(num+"-------------"+num);
//		parser.parse(new InputSource(new StringReader(html)));
//		boolean trunPage = htmlContentHandler.IsTurnPage();
//		if(htmlContentHandler.IsFirstPage)trunPage=false;
//		System.out.println(trunPage+"-------------"+url);
//		if(urls.get(urlsdoc.get(num))>0)
//		{
//			TurnPageNum++;
//		}
//		if(trunPage)
//		{
//			
//			urlsResult.add(tempString);
//			System.out.println("翻页:"+num+"---"+urlsdoc.get(num));
//			if(urls.get(urlsdoc.get(num)).toString().length()>0)
//			{
//				//System.out.println("翻页:"+GetTurnPageNum);
//				GetTurnPageNum++;
//			}
//		}
//		html="";
//	}
//	htmlContentHandler.clear();
//}
//}
////System.out.println("原始翻页的准确率:"+OriginIsTurn);
////正确翻页识别的召回率
//System.out.println("翻页的召回率:"+GetTurnPageNum+"|"+TurnPageNum);
//System.out.println("翻页的准确率:"+GetTurnPageNum+"|"+urlsResult.size());