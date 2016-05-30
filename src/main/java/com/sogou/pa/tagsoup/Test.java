package com.sogou.pa.tagsoup;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class ElementTest{
	int child;
}
public class Test {
	@SuppressWarnings("resource")
	public static void map(String a)
	{
		a+="sjdsm";
	}
	public static void main(String[] args) throws ParseException
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//设置日期格式
		Date date1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
		        .parse(df.format(new Date()));
		long start_time = date1.getTime();
        String c = "哈哈scc的发收		2/3页s23到sdd的时时时时";
        map(c);
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");      
        Matcher m = p.matcher(c);      
        c = m.replaceAll("");  
        System.out.println("c:"+c);
//        Pattern digit = Pattern.compile("([\u4e00-\u9fa5]+)");
//        Matcher m = digit.matcher(c);
        String  s="";
        while(m.find())
        {
        	s+=m.group(1);
        	 System.out.println(m.group(1));
        }
        c="http://www.sushangwang.net/photo/show.php?itemid=120&page=8";
        TagContentTool.IsTurnByUrl(c);
        System.out.println(TagContentTool.IsTurnByUrl(c));
        p = Pattern.compile("[\\]第]*?([\\d]+)/[\\d]+[\\]页]*?");  
        m = p.matcher(c);
//        if(m.find())
//        {
//        	System.out.println(m.group(1)+"-----"+m.group(0).length());
//        }
        while(m.find())
        {
        	System.out.println(m.group(1)+"-----"+m.group(0).length());
        }
        if(s.length()>4)System.out.println(s+""+s.length());
        System.out.println(c.length()+c);
        String url="http://www.to3d.com/yp/web/index.php?15905/categroy-product/page-2/page-1/page-2/page-1/page-3/page-2/page-1/page-3/page-2/page-3/page-1/page-2/page-3/page-1/page-3/page-1/page-2/page-3.html0";
        String[] urls = url.split("\\.");
		for(int i=0;i<urls.length;i++)
		{
			System.out.println("第"+i+"个匹配到的urls内容:"+urls[i]);
		}
		System.out.println(38*0.85);
		Date date2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
		        .parse(df.format(new Date()));
		long end_time = date2.getTime();
        long interval_time = end_time - start_time > 0 ? end_time - start_time:0;
        System.out.println("运行时间："+interval_time+"ms");
	}
}

