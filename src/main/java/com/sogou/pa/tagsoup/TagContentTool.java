package com.sogou.pa.tagsoup;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagContentTool {
	public static void printPath(LinkedList<Element> Allpath){
		for(int i=0;i<Allpath.size();i++)
		{
			System.out.println("<"+Allpath.get(i).tag+"  href="+Allpath.get(i).href+">"+Allpath.get(i).content+"</"+Allpath.get(i).tag+">");
		}
	}
	public static boolean IsHrefNull(String href)
	{
		if(href==null||href.length()==0
				||href.compareTo("javascript:;")==0
				||href.compareTo("#")==0)
		{
			return true;
		}
		return false;
	}
	public static boolean IshasClassOrId(Element tree)
	{
		if(tree.tag.toLowerCase().compareTo("strong")==0
				||(tree.classes.size()>0&&tree.classes.get(0)!=null&&tree.classes.get(0).length()>0)
				||(tree.ids.size()>0&&tree.ids.get(0)!=null&&tree.ids.get(0).length()>0)
				||(tree.color!=null&&tree.color.length()>0))
		{
			return true;
		}
		return false;
	}
	public static String getContentFromChild(Element tree)
	{
		String s=tree.content;
		String content = s;
		if(s.length()>0)return s;
		LinkedList<Element> Allpath = tree.children;
		if(IshasClassOrId(tree))
		{
			if(content.length()==0)content += getContentFromChild(tree.children);
			if(content.matches("[\\[]*?1[\\]]*?"))
			{
				if(IsHrefNull(tree.href))
				{
					HtmlContentDomTree.IsFristPage=1;
				}
				else
				{
					if(HtmlContentDomTree.IsFristPage!=1)HtmlContentDomTree.IsFristPage=0;
				}
			}
			else if(content.matches("[\\[]*?[\\d]+[\\]]*?"))
			{
				if(HtmlContentDomTree.IsFristPage!=1)HtmlContentDomTree.IsFristPage=0;
			}
			return content;
		}
		for(int i=0;i<Allpath.size();i++)
		{
			if(Allpath.get(i).content.length()==0)s+=getContentFromChild(Allpath.get(i));
			else s+=Allpath.get(i).content;
		}
		return s;
	}
	public static String getContentFromChild(LinkedList<Element> tree)
	{
		String s="";
		LinkedList<Element> Allpath = tree;
		for(int i=0;i<Allpath.size();i++)
		{
//			System.out.println("children---------"+Allpath.get(i).tag+Allpath.get(i).content);
			if(Allpath.get(i).content.length()==0)s+=getContentFromChild(Allpath.get(i));
			else s+=Allpath.get(i).content;
//			System.out.println("<"+Allpath.get(i).tag+"  href="+Allpath.get(i).href+">"+Allpath.get(i).content+"</"+Allpath.get(i).tag+">");
		}
		return s;
	}
	public static void ExtractDomTree(Element tree){
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");      
        Matcher m = p.matcher(tree.content);     
        tree.content= m.replaceAll("");
//        System.out.println("<"+tree.tag+"  href="+tree.href+">"+tree.content+"</"+tree.tag+">");
        if(tree.tag.compareTo("a")==0)
        {
//        	System.out.println("inter aaaaa");
//        	System.out.println("<"+tree.tag+"  href="+tree.href+">"+tree.content+"</"+tree.tag+">");
//        	System.out.println("a before content:---------"+tree.content);
        	tree.content=getContentFromChild(tree);
//        	System.out.println("a after content from children:-----------"+tree.content);
        	if(IsCorrectContent(tree.content))
        	{
    			p = Pattern.compile("[\\[第]*?([\\d]+)[\\]页]*?");  
    			m = p.matcher(tree.content);
    	        if(m.find())
    	        {
    	        	if(m.group(1).length()<=6)
    	        	{
	     	        	int value = Integer.parseInt(m.group(1));
//	     	        	System.out.println("a tag包含数字:"+value+"---长度:"+m.group(0).length());
	     	        	if(value<=100000&&value>=1)
	    	        	{
	    	        		HtmlContentDomTree.dom.add(tree);
	    	        	}
    	        	}
        		}
        	}
        	return;
        	
        }
        else if(tree.tag.compareTo("script")!=0
        		&&tree.tag.compareTo("option")!=0
        		&&tree.tag.compareTo("style")!=0)
        {
        	if(IsCorrectContent(tree.content))
        	{
//        		System.out.println("非 a tag的内容"+tree.content);
//        		System.out.println("<"+tree.tag+"  href="+tree.href+">"+tree.content+"</"+tree.tag+">");
     			p = Pattern.compile("[\\[第]*?([\\d]+)[\\]页]*?");  
     			m = p.matcher(tree.content);
     	        if(m.find())
     	        {
     	        	if(m.group(1).length()<7)
     	        	{
	     	        	int value = Integer.parseInt(m.group(1));
//	     	        	System.out.println("非 a tag的匹配的数值"+value+"长度："+m.group(1).length());
	     	        	if(value<100000&&value>=1)
	     	        	{
	     	        		HtmlContentDomTree.dom.add(tree);
	     	        	}
     	        	}
         		}
        	}
        }
//        System.out.println("<"+tree.tag+"  href="+tree.href+">"+tree.content+"</"+tree.tag+">");
		for(int i=0;i<tree.children.size();i++)
		{
//			System.out.println("<"+tree.children.get(i).tag+"></"+tree.children.get(i).tag+">");
			ExtractDomTree(tree.children.get(i));
		}
	}
	public static boolean IsTurnByUrl(String url)
	{
		Pattern p = Pattern.compile("page{1}[=-]{1}([-\\+]{0,1}[\\d]+)$"); 
		Matcher m = p.matcher(url.trim().toLowerCase());
		int value = 0;
		if(m.find())
		{
			String tmp = m.group(1);
//			System.out.println("匹配到的内容:"+tmp+url);
			if(tmp.length()<5&&tmp.length()>0&&tmp!=null)value = Integer.parseInt(tmp);
		}

		if(value>1)return true;
		String[] urls = url.split("\\.");
		for(int i=0;i<urls.length-1;i++)
		{
			url+=urls[i];
//			System.out.println("第"+i+"个匹配到的urls内容:"+urls[i]);
		}
		m = p.matcher(url.trim().toLowerCase());
//		System.out.println("匹配到的urls内容:"+url+urls.length);
		value = 0;
		if(m.find())
		{
			String tmp = m.group(1);
//			System.out.println("匹配到的内容:"+tmp+url);
			if(tmp.length()<5&&tmp.length()>0&&tmp!=null)value = Integer.parseInt(tmp);
		}
		if(value>1)return true;
		return false;
	}
	public static Integer gettagContent(Element tree){
		if(tree.content.length()>7)return -1;
		Pattern p = Pattern.compile("[\\[]*?([\\d]+)[\\[]*?");  
		Matcher m = p.matcher(tree.content);
//		System.out.println("1IsFristPage:"+tree.content+"---------"+tree.tag);

        if(m.find())
        {
        	if(m.group(1).length()<7)
        	{
 	        	int value = Integer.parseInt(m.group(1));
// 	        	System.out.println(tree.content+"after:"+value+"-----"+m.group(0).length());
 	        	if(value<100000&&value>=1)
	        	{
	        		return value;
	        	}
        	}
		}
        return -1;
	}
	public static void printDomTree(Element tree){
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");      
        Matcher m = p.matcher(tree.content);     
        tree.content= m.replaceAll("");
//		System.out.println("<"+tree.tag+"  href="+tree.href+">"+tree.content+"</"+tree.tag+">");
		for(int i=0;i<tree.children.size();i++)
		{
//			System.out.println("<"+tree.children.get(i).tag+"></"+tree.children.get(i).tag+">");
			printDomTree(tree.children.get(i));
		}
	}
	public static void printDomTree(LinkedList<Element> tree){
		for(int i=0;i<tree.size();i++)
		{
			System.out.println("<"+tree.get(i).tag+"href="+tree.get(i)+">"+tree.get(i).content+"</"+tree.get(i).tag+">");
		}
	}
	public static boolean IsCorrectContent(String s)
	{
		if(s.contains("第一页")
				||s.toLowerCase().contains("page")
				||s.contains("前页")
				||s.contains("上一页")
				||s.contains("上一頁")
				||s.contains("\u4e0b\u4e00\u9875")
				||s.contains("最前页")
				||s.contains("分页")
				||s.contains("返回列表")
				||s.toLowerCase().contains("first")
				||s.toLowerCase().contains("previous")
				||s.contains("后页")
				||s.contains("下一页")
				||s.contains("下一頁")
				||s.contains("后一页")
				||s.contains("\u4e0a\u4e00\u9875")
				||s.contains("最后页")
				||s.contains("返回列表")
				||s.toLowerCase().contains("next")
				||s.toLowerCase().contains("last")||
				s.toLowerCase().contains("pages")
				||s.matches("[\\[]*?[\\d]+[\\]]*?")
				||s.matches("[\\[第跳转到]*?[\\d]+/[\\d][\\]页]*?")){
			return true;
		}
		return false;
	}
	public static boolean IsHasChildDomTree(String s)
	{
		if(s.toLowerCase().compareTo("ul")==0
				||s.toLowerCase().compareTo("div")==0
				||s.toLowerCase().compareTo("table")==0
				||s.toLowerCase().compareTo("p")==0)
		{
			return true;
		}
		return false;
	}
	public static boolean HasPrevTurnArea(String s)
	{
		
		if((s.length()<30&&s.toLowerCase().contains("page"))
				||s.contains("分页")
				){
			return true;
		}
		return false;
	}
	public static boolean HasPrevHref(String s)
	{
		if(s.contains("第一页")
				||s.contains("前页")
				||s.contains("上一页")
				||s.contains("上一頁")
				||s.contains("\u4e0b\u4e00\u9875")
				||s.contains("最前页")
				||s.contains("first")
				||s.contains("previous")
				){
			return true;
		}
		return false;
	}
	public static boolean hasEndTurnPage(String s){
		if(s.contains("后页")
			||s.contains("下一页")
			||s.contains("下一頁")
			||s.contains("后一页")
			||s.contains("\u4e0a\u4e00\u9875")
			||s.contains("最后页")
			||s.contains("返回列表")
			||s.contains("next")
			||s.contains("last")
			||s.contains("pages"))
		{
			return true;
		}
		return false;
	}
	public static boolean IsPageDigit(Element e)
	{
		if(e.content.length()>0)
		{
//			System.out.println(e.content);
			if(e.content.matches("[\\[第]*?[\\d]+[\\]页]*?"))
			{
//				System.out.println("match"+e.content);
				return true;
			}
			Pattern p = Pattern.compile("[\\]第]*?([\\d]+)/[\\d]+[\\]页]*?");  
			Matcher m = p.matcher(e.content);
	        if(m.find())
	        {
//	        	System.out.println(m.group(1)+"-----"+m.group(0).length()+e.content);
	        	if(Integer.parseInt(m.group(1))>1)HtmlContentDomTree.IsSlowdpage=1;
	        	return true;
	        }
			if(e.content.matches("[\\[第]*?[\\d]+/[\\d]+[\\]页]*?"))return true;
		}
		return false;
	}
	public static boolean IsPageDigit(Element e,String match)
	{
		if(e.content.length()>0)
		{
//			System.out.println("匹配的第一页内容"+e.content);
			if(e.content.matches("[\\[第]*?1[\\]页]*?"))return true;
			if(e.content.matches("[\\[第]*?1/[\\d]+[\\]页]*?"))return true;
		}
		return false;
	}
	public static boolean IsSecondPageDigit(Element e)
	{
		if(e.content.length()>0)
		{
//			System.out.println("匹配的第一页内容"+e.content);
			if(e.content.matches("[\\[第]*?2[\\]页]*?"))return true;
			if(e.content.matches("[\\[第]*?2/[\\d]+[\\]页]*?"))return true;
		}
		return false;
	}
	public static String getOneHref(Element e)
	{
		if(e.tag.compareTo("a")==0)
		{
//			System.out.println("href:"+e.href+"pagehref------");
			return e.href;
		}
		for(int i=0;i<e.children.size();i++)
		{
			String href = getOneHref(e.children.get(i));
			if(href.length()>0)
			{
//				System.out.println("href:"+href+"pagehref------");
				return href;
			}
		}
		return "";
	}
	public static int ChildContentLength(Element e)
	{
		if(e.content!=null)
		{
			printDomTree(e);
	        Pattern p = Pattern.compile("\\s*|\t|\r|\n");      
	        Matcher m = p.matcher(e.content);     
//	        System.out.println("tag 空格过滤前内容:"+e.content);
	        e.content = m.replaceAll("");
			Pattern digit = Pattern.compile("([\u4e00-\u9fa5\\W]+)");
	        m = digit.matcher(e.content);
	        String  s="";
	        while(m.find())
	        {
	        	s+=m.group(1);
	        }
//	        System.out.println("tag 空格过滤后内容:"+e.content);
//	        System.out.println("匹配的内容:"+s);
			return s.length();
		}
		for(int i=0;i<e.children.size();i++)
		{
			int length = ChildContentLength(e.children.get(i));
			return length;
		}
		return 0;
	}
}
