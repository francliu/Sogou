package com.sogou.pa.turnpage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

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
		File urlfile = new File("D:\\JavaWorkplace\\sogou\\Before518Page");
//		File urlfile = new File("D:\\JavaWorkplace\\sogou\\data\\Before518Page");
//		File urlfile = new File("D:\\JavaWorkplace\\sogou\\data\\NewTurnPage1000.txt");
//		File urlfile = new File("D:\\JavaWorkplace\\sogou\\data\\TurnPage1000.txt");
//		File urlfile = new File("D:\\JavaWorkplace\\sogou\\data\\Third1000Url");
		BufferedReader urlreader = null;
		
		urlreader = new BufferedReader(new FileReader(urlfile));
		String tempString = null;
		int originTurnPageNum=0;
		int CorrectPage=0;
		HashMap<String,Long> urls = new HashMap<String,Long>();
//		HashMap<String,Long> PageNums = new HashMap<String,Long>();
		while ((tempString = urlreader.readLine()) != null) {
//			String[] strs = tempString.split("\\s");
//			//System.out.println(strs[0]);
////			long value=Long.parseLong(strs[5]);
////			if(value>1)PageNums.put(strs[0],value);
//			if(strs[4].compareTo("1")==0)
//			{
//				originTurnPageNum++;
//				if(strs[2].compareTo("1")==0)CorrectPage++;
//			}
//			if(strs[2].compareTo("1")==0)
//			{
////				TurnPageNum++;
//				urls.put(strs[0], (long) 1);
//			}
//			else urls.put(strs[0], (long) 0);
			urls.put(tempString, (long) 1);
		}
		
		
		//读取数据识别是否为翻页
		HtmlContentDomTree		htmlContentHandler		= new HtmlContentDomTree();
		Parser					parser					= new Parser();
		parser.setContentHandler(htmlContentHandler);	
		File file;
		BufferedReader reader = null;
		int num=1;
		Vector<String> urlsResult = new Vector<String>();
//		File RandomFile = new File("D:\\JavaWorkplace\\sogou\\src\\haha\\ThirdTurnPage.txt");
//		File RandomFile = new File("D:\\JavaWorkplace\\sogou\\src\\haha\\ThirdTurnPageold.txt");
//		File RandomFile = new File("D:\\JavaWorkplace\\sogou\\src\\haha\\FristTurnPage.txt");
//		File RandomFile = new File("D:\\JavaWorkplace\\sogou\\src\\haha\\SecondTurnPage.txt");
		File RandomFile = new File("D:\\JavaWorkplace\\sogou\\519IsTurnPageLast.txt");
//		File RandomFile = new File("D:\\JavaWorkplace\\sogou\\src\\haha\\519IsTurnPage.txt");
//		File RandomFile = new File("D:\\JavaWorkplace\\sogou\\data\\Feathers\\OldFeathers.csv");
//		File RandomFile = new File("D:\\JavaWorkplace\\sogou\\data\\Feathers\\NewFeathers.csv");
		BufferedWriter writer = null;
		
		writer = new BufferedWriter(new FileWriter(RandomFile));
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//设置日期格式
		Date date1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
		        .parse(df.format(new Date()));
		System.out.println("运行开始时间："+df.format(new Date()));
		long start_time = date1.getTime();
		long total_read_time=0;
		long total_parse_time=0;
		long PageCorrectNum=0;
		num=1;
//		while(num<=6500)
		{
//			file = new File("D:\\JavaWorkplace\\sogou\\data\\ThirdPages\\t"+num+".html");
//			file = new File("D:\\JavaWorkplace\\sogou\\data\\ThirdPages\\t741.html");
//			file = new File("D:\\JavaWorkplace\\sogou\\data\\FristPages\\t"+num+".html");
			file = new File("D:\\JavaWorkplace\\Sogou\\t.html");
//			file = new File("D:\\JavaWorkplace\\sogou\\data\\LastNewPage\\t497.html");
//			file = new File("D:\\JavaWorkplace\\sogou\\data\\pages\\t"+num+".html");
//			file = new File("D:\\JavaWorkplace\\sogou\\data\\pages\\t1408.html");
//			file = new File("D:\\JavaWorkplace\\sogou\\data\\NewPage\\t798.html");
			if(!file.exists()||urls.get(url)!=null)
			{
				num++;
//				continue;
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
			System.out.println(tempString);
			Date date4 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
			        .parse(df.format(new Date()));
			long end_read_time = date4.getTime();
			total_read_time+=end_read_time-start_read_time;
			HtmlContentDomTree.url=url;
			Date date5 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
			        .parse(df.format(new Date()));
			long start_parse_time = date5.getTime();
			parser.parse(new InputSource(new StringReader(html)));
			boolean trunPage = htmlContentHandler.IsTurnPage();
//			boolean trunPage = true;
//			System.out.println("IsFristPage:"+htmlContentHandler.IsFristPage);
			if(HtmlContentDomTree.IsFristPage!=1&&!trunPage)
			{
				trunPage= TagContentTool.IsTurnByUrl(url);
			}
//			else if(trunPage)TagContentTool.GetPageNumByUrl(url);
			Date date6 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
			        .parse(df.format(new Date()));
			long end_parse_time = date6.getTime();
			total_parse_time+=end_parse_time-start_parse_time;
//			System.out.println("num:"+num+"old:"+urls.get(url)+"new:"+trunPage+htmlContentHandler.IsFirstPage+"-------------"+url);
//			int IsCurrentPage=0;
//			if(PageNums.get(url)!=null)
			{
				System.out.println("HtmlContentDomTree.pageNum:"+HtmlContentDomTree.pageNum);
				System.out.println("HtmlContentDomTree.CurrentPage:"+HtmlContentDomTree.CurrentPage);
				
//				if(HtmlContentDomTree.CurrentPage==-1)
//				{
////					System.out.println(num+"个Page:"+PageNums.get(url)+HtmlContentDomTree.pageNum);
//					HtmlContentDomTree.CurrentPage = HtmlContentDomTree.pageNum;
//				}
				if(HtmlContentDomTree.CurrentPage==-1)
				{
					TagContentTool.GetPageNumByUrl(url);
				}
				if(HtmlContentDomTree.CurrentPage==-1)
				{
					HtmlContentDomTree.CurrentPage=2;
				}
				System.out.println("HtmlContentDomTree.CurrentPage:"+HtmlContentDomTree.CurrentPage);
				
			}
//			if(urls.get(url)!=null&&urls.get(url)>0)
//			{
//				writer.write(url+"\n");
////				writer.write("num:"+num+" "+url+"  1  "+trunPage+"  "+HtmlContentDomTree.CurrentPage+" "+IsCurrentPage+"\n");
//				TurnPageNum++;
//			}
			if(trunPage)
			{
				urlsResult.add(tempString);
//				if(urls.get(url)!=null&&urls.get(url)>0)
//				{
//					GetTurnPageNum++;
//					if(HtmlContentDomTree.CurrentPage!=-1&&PageNums.get(url)!=null&&PageNums.get(url)==HtmlContentDomTree.CurrentPage)
//					{
//						System.out.println("PageNums.get(url):"+PageNums.get(url));
//						IsCurrentPage=1;
//						PageCorrectNum++;
//					}
//				}
//				else
				{
//					writer.write(url+"\n");
//					writer.write("num:"+num+" "+HtmlContentDomTree.CurrentPage+"\n");
					writer.write("num:"+num+" "+url+"  1  "+HtmlContentDomTree.CurrentPage+"\n");
//					writer.write("num:"+num+" "+url+"  0  1  "+HtmlContentDomTree.CurrentPage+" "+IsCurrentPage+"\n");
				}
			}
			html="";
			
			htmlContentHandler.clear();
			
//			StoreFeathers.storeFeathers(writer);
//			if(urls.get(url)!=null&&urls.get(url)>0)writer.write(",1\n");
//			else writer.write(",0\n");
			url="";
			num++;
			System.out.println("翻页的正确率:"+GetTurnPageNum+"|"+TurnPageNum);
			System.out.println("翻页的准确率:"+GetTurnPageNum+"|"+urlsResult.size());
		}
//		System.out.println("HtmlContentDomTree.CurrentPage:"+HtmlContentDomTree.CurrentPage);
		Date date2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
		        .parse(df.format(new Date()));
		System.out.println("运行结束时间："+df.format(new Date()));
		long end_time = date2.getTime();
        long interval_time = end_time - start_time > 0 ? end_time - start_time:0;
        System.out.println("运行读文件所花时间："+total_read_time+"ms");
        System.out.println("运行分析所有页面所花总时间："+total_parse_time+"ms");
        System.out.println("运行分析单个页面所花时间："+total_parse_time/(double)num+"ms");
        System.out.println("运行时间："+interval_time+"ms");
		writer.flush();
		writer.close();
		//System.out.println("原始翻页的准确率:"+OriginIsTurn);
		//正确翻页识别的召回率
		System.out.println("页码识别的正确率:"+PageCorrectNum+"|"+GetTurnPageNum+"|"+PageCorrectNum/(double)GetTurnPageNum);
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