package com.sogou.pa.turnpage;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TagContentTool {

	public static void printPath(LinkedList<Element> Allpath){
//		for(int i=0;i<Allpath.size();i++)
//		{
//			System.out.println("<"+Allpath.get(i).tag+"  href="+Allpath.get(i).href+">"+Allpath.get(i).content+"</"+Allpath.get(i).tag+">");
//		}
	}
	public static boolean IsHrefNull(String href)
	{
		if(href==null||href.length()==0
				||href.compareTo("javascript:;")==0
				||href.compareTo("javascript://")==0
				||href.compareTo("#")==0
				||href.compareTo("#@")==0)
		{
			return true;
		}
		return false;
	}
	public static boolean IshasClassOrId(Element tree)
	{
		if(tree.tag.toLowerCase().compareTo("strong")==0
				||(tree.classes!=null&&tree.classes.length()>0)
				||(tree.ids!=null&&tree.ids.length()>0)
				||(tree.color!=null&&tree.color.length()>0))
		{
			return true;
		}
		return false;
	}
	public static boolean IshasClass(Element tree)
	{
		if((tree.classes!=null&&tree.classes.length()>0)
				||(tree.ids!=null&&tree.ids.length()>0))
		{
			return true;
		}
		return false;
	}
	public static String getContentFromChild(Element tree)
	{
		String s=tree.content;
		String content = s;
		if(s.length()>0)
		{
			if(IshasClass(tree)&&tree.tag.compareTo("a")==0)
			{
				if(s.matches("[\\[]*?1[\\]]*?"))
				{
					HtmlContentDomTree.IsFristPage=1;
				}
			}
			return s;
		}
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
					if(HtmlContentDomTree.IsFristPage!=1)
					{
						HtmlContentDomTree.IsFristPage=0;
					}
				}
			}
			else if(content.matches("[\\[]*?2[\\]]*?"))
			{
				
				if(IsHrefNull(tree.href))
				{
					HtmlContentDomTree.CurrentPage=2;
				}
			}
			else if(content.matches("[\\[]*?[\\d]+[\\]]*?"))
			{
				if(HtmlContentDomTree.IsFristPage!=1)
				{
					Pattern p = Pattern.compile("[\\[第]*?([\\d]+)[\\]页]*?");  
					Matcher m = p.matcher(tree.content);
	    	        if(m.find())
	    	        {
	    	        	if(m.group(1).length()<=6)
	    	        	{
		     	        	int value = Integer.parseInt(m.group(1));
		     	        	if(value<=100000&&value>1)
		    	        	{
		    	        		HtmlContentDomTree.CurrentPage=value;
		    	        	}
	    	        	}
	        		}
					HtmlContentDomTree.IsFristPage=0;
				}
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
			if(Allpath.get(i).content.length()==0)s+=getContentFromChild(Allpath.get(i));
			else s+=Allpath.get(i).content;
		}
		return s;
	}
	public static void ExtractDomTree(Element tree){
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");      
        Matcher m = p.matcher(tree.content);     
        tree.content= m.replaceAll("");
        if(tree.tag.compareTo("a")==0)
        {
        	tree.content=getContentFromChild(tree);
        	p = Pattern.compile("^[(&nbsp;)\\[\\s]*?([\\d]+)[(&nbsp;)\\]\\s]*?$");      
            m = p.matcher(tree.content); 
        	if(m.find())
            {
            	tree.content=m.group(1);
            }
        	if(IsCorrectContent(tree.content))
        	{
    			p = Pattern.compile("[\\[第]*?([\\d]+)[\\]页]*?");  
    			m = p.matcher(tree.content);
    	        if(m.find())
    	        {
    	        	if(m.group(1).length()<=6)
    	        	{
	     	        	int value = Integer.parseInt(m.group(1));
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
        	p = Pattern.compile("^[\\[\\s]*?([\\d]+)[\\]\\s]*?$");
            if(m.find())
            {
            	tree.content=m.group(1);
            }
        	if(IsCorrectContent(tree.content))
        	{
     			p = Pattern.compile("^[\\[第]*?([\\d]+)[\\]页]*?$");  
     			m = p.matcher(tree.content);
     	        if(m.find())
     	        {
     	        	if(m.group(1).length()<7)
     	        	{
	     	        	int value = Integer.parseInt(m.group(1));
	     	        	if(value<100000&&value>=1)
	     	        	{
	     	        		HtmlContentDomTree.dom.add(tree);
	     	        	}
     	        	}
         		}
     	        p = Pattern.compile("[\\[]*?([\\d]+)/[\\d]+[\\]]*?");      
		        m = p.matcher(tree.content); 
		        if(m.matches())
		        {
		        	if(m.group(1).length()<7)
     	        	{
	     	        	int value = Integer.parseInt(m.group(1));
	     	        	if(value<100000&&value>=1)
	     	        	{
	     	        		HtmlContentDomTree.dom.add(tree);
	     	        	}
     	        	}
		        }
        	}
        }
		for(int i=0;i<tree.children.size();i++)
		{
			ExtractDomTree(tree.children.get(i));
		}
	}
	
	public static Integer gettagContent(Element tree){
		if(tree.content.length()>7)return -1;
		Pattern p = Pattern.compile("[\\[]*?([\\d]+)[\\[]*?");  
		Matcher m = p.matcher(tree.content);
        if(m.find())
        {
        	if(m.group(1).length()<7)
        	{
 	        	int value = Integer.parseInt(m.group(1));
 	        	if(value<100000&&value>=1)
	        	{
	        		return value;
	        	}
        	}
		}
        return -1;
	}
	public static void printDomTree(Element tree){
//        Pattern p = Pattern.compile("\\s*|\t|\r|\n");      
//        Matcher m = p.matcher(tree.content);     
//        tree.content= m.replaceAll("");
////		System.out.println("<"+tree.tag+"  href="+tree.href+">"+tree.content+"</"+tree.tag+">");
//		for(int i=0;i<tree.children.size();i++)
//		{
////			System.out.println("<"+tree.children.get(i).tag+"></"+tree.children.get(i).tag+">");
//			printDomTree(tree.children.get(i));
//		}
	}
	public static void printDomTree(LinkedList<Element> tree){
//		for(int i=0;i<tree.size();i++)
//		{
//			System.out.println("<"+tree.get(i).tag+"href="+tree.get(i).href+">"+tree.get(i).content+"</"+tree.get(i).tag+">");
//		}
	}
	public static boolean IsCorrectContent(String s)
	{
		if(s.contains("第一页")
				||s.toLowerCase().contains("page")
				||s.contains("前页")
				||s.contains("上页")
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
				||s.matches("(\\&nbsp;)*?[\\[\\s]*?[\\d]+[\\s\\]]*?(\\&nbsp;)*?")
				||s.matches("[\\[第跳转到\\s]*?[\\d]+/[\\d][\\]页\\s]*?")){
			return true;
		}
		return false;
	}
	public static boolean IsHasChildDomTree(String s)
	{
		if(s.toLowerCase().compareTo("ul")==0
				||s.toLowerCase().compareTo("div")==0
				||s.toLowerCase().compareTo("table")==0
				||s.toLowerCase().compareTo("p")==0
				||s.toLowerCase().compareTo("span")==0)
		{
			return true;
		}
		return false;
	}
	public static boolean HasPrevTurnArea(String s)
	{
		if((s.length()<30&&s.toLowerCase().contains("page"))
				||s.contains("分页")
				||s.contains("第一页")
				||s.contains("前页")
				||s.contains("上页")
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
	public static boolean HasPrevHref(String s)
	{
		if(s.contains("第一页")
				||s.contains("前页")
				||s.contains("上页")
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
			||s.contains("尾页")
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
	public static Integer IsPageDigit(Element e)
	{
		if(e.content.length()>0)
		{
			Pattern p = Pattern.compile("^[\\[第]*?([\\d]+)[\\]页]*?$");  
			Matcher m = p.matcher(e.content);
			int value=0;
			if(m.matches())
			{
				value = Integer.parseInt(m.group(1));
				return value;
			}
			p = Pattern.compile("^[\\[第]*?([\\d]+)/[\\d]+[\\]页]*?$");  
			m = p.matcher(e.content);
	        if(m.find())
	        {
	        	value = Integer.parseInt(m.group(1));
	        	if(value>1)
	        	{
	        		HtmlContentDomTree.IsSlowdpage=1;
	        	}
	        	return value;
	        }
		}
		return 0;
	}
	public static boolean IsPageDigit(Element e,String match)
	{
		if(e.content.length()>0)
		{
			if(e.content.matches("^[\\[第]*?1[\\]页]*?$"))
			{
				return true;
			}
			if(e.content.matches("^[\\[第]*?1/[\\d]+[\\]页]*?$"))
			{
				return true;
			}
		}
		return false;
	}
	public static boolean IsSecondPageDigit(Element e)
	{
		if(e.content.length()>0)
		{
			if(e.content.matches("[\\[第]*?2[\\]页]*?"))
			{
				if(TagContentTool.IsHrefNull(e.href))HtmlContentDomTree.CurrentPage=2;
				return true;
			}
			if(e.content.matches("[\\[第]*?2/[\\d]+[\\]页]*?"))
			{
				HtmlContentDomTree.CurrentPage=2;
				return true;
			}
		}
		return false;
	}
	public static String getOneHref(Element e)
	{
		if(e.tag.compareTo("a")==0)
		{
			return e.href;
		}
		for(int i=0;i<e.children.size();i++)
		{
			String href = getOneHref(e.children.get(i));
			if(href.length()>0)
			{
				return href;
			}
		}
		return "";
	}
	
	public static boolean GetPageNumByUrl(String url)
	{
		Pattern p = Pattern.compile("page{1}[a-z]*?[=-]{1}([-\\+]{0,1}[\\d]+)"); 
		Matcher m = p.matcher(url.trim().toLowerCase());
		int value = 0;
		while(m.find())
		{
			String tmp = m.group(1);
			if(tmp.length()<5&&tmp.length()>0&&tmp!=null)value = Integer.parseInt(tmp);
		}
		if(value>1)
		{
			HtmlContentDomTree.CurrentPage=value;
			return true;
		}
		p = Pattern.compile("p{1}[=-]{0,1}([-\\+]{0,1}[\\d]+)[/]*?"); 
		m = p.matcher(url.trim().toLowerCase());
		value = 0;
		String[] urls = url.split("\\.");
		for(int i=0;i<urls.length-1;i++)
		{
			url+=urls[i];
		}
		m = p.matcher(url.trim().toLowerCase());
		value = 0;
		if(m.find())
		{
			String tmp = m.group(1);
			if(tmp.length()<5&&tmp.length()>0&&tmp!=null)value = Integer.parseInt(tmp);
		}
		if(value>1)
		{
			HtmlContentDomTree.CurrentPage=value;
			return true;
		}
		return false;
	}
	public static boolean IsTurnByUrl(String url)
	{
		Pattern p = Pattern.compile("page{1}[=-]{1}([-\\+]{0,1}[\\d]+)$"); 
		Matcher m = p.matcher(url.trim().toLowerCase());
		int value = 0;
		if(m.find())
		{
			String tmp = m.group(1);
			if(tmp.length()<5&&tmp.length()>0&&tmp!=null)value = Integer.parseInt(tmp);
		}
		if(value>1)
		{
			HtmlContentDomTree.CurrentPage=value;
			return true;
		}
		m = p.matcher(url.trim().toLowerCase());
		value = 0;
		if(m.find())
		{
			String tmp = m.group(1);
			if(tmp.length()<5&&tmp.length()>0&&tmp!=null)value = Integer.parseInt(tmp);
		}
		if(value>1)return true;
		return false;
	}
	public static int getPagination(String url)
	{
		if(HtmlContentDomTree.CurrentPage==-1)
		{
			TagContentTool.GetPageNumByUrl(url);
		}
		if(HtmlContentDomTree.CurrentPage==-1)
		{
			HtmlContentDomTree.CurrentPage=2;
		}
		return HtmlContentDomTree.CurrentPage;
	}
}
