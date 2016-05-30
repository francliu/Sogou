package com.sogou.pa.tagsoup;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

class Element{
	Vector<String> classes;
	Vector<String> ids;
	Vector<String> styles;
	LinkedList<Element> children;
	
	String content;
	String tag;
	boolean is_bold;
	String color;
	String href;
	String selected;
	Element(){
		classes = new Vector<String>();
		ids = new Vector<String>();
		styles = new Vector<String>();
		children = new LinkedList<Element>();
		content="";
		tag="";
		is_bold=false;
		color="";
		href="";
		selected="";
	}
	public void clear() {
		classes.clear();
		ids.clear();
		styles.clear();
		children.clear();
		content="";
		tag="";
		is_bold=false;
		color="";
		href="";
		selected="";
	}
}

public class HtmlContentDomTree implements ContentHandler {

//	public static double TurnAreaPageNum = 0;
//	public static double GetTurnAreaPageNum = 0;
	Locator locator;
	public static double TurnPageNum = 0;
	public static double GetTurnPageNum = 0;
	public static int preHref=-1;
	public static int IsSlowdpage=-1;
	public static int IsFristPage=-1;
	public static int IsHasPrevPage=-1;
	public static String url="";
	public static LinkedList<Element> dom;
	LinkedList<Element> Allpath;
	LinkedList<Element> domtrees;
	HashMap<Element,Double> IshasDomtree;
	Element ALLdomtree;
	HtmlContentDomTree() {
		dom = new LinkedList<Element>();
		Allpath = new LinkedList<Element>();
		domtrees = new LinkedList<Element>();
		IshasDomtree = new HashMap<Element,Double>();
		ALLdomtree = new Element();
	}	
	public void clear() {
		dom.clear();
		Allpath.clear();
		domtrees.clear();
		IshasDomtree.clear();
		preHref=-1;	
		IsSlowdpage=-1;
		IsFristPage=-1;
		IsHasPrevPage=-1;
		url="";
	}

	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
		this.locator = locator;
		//System.out.println("2.setDocumentLocator....");
	}

	public void startDocument() throws SAXException {
		clear();
		//System.out.println("1.startDocument....");
	}

	public void endDocument() throws SAXException {
		//System.out.println("endDocument....");
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// TODO Auto-generated method stub
		//System.out.println("3.startPrefixMapping....");
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub
		//System.out.println("endPrefixMapping....");
	}
	public void setFeatureFromAtts(Attributes atts, Element e) {
		String id = atts.getValue("id");
		if (id != null) {
		//	id = id.replaceAll("[ \t\r\n]?(&nbsp;)?(&gt;)?", "");
			if (id.length() > 0) {
				e.ids.add(id);
			}
		}
		String class_attr = atts.getValue("class");
		if (class_attr != null) {
		//	class_attr = class_attr.replaceAll("[ \t\r\n]?(&nbsp;)?(&gt;)?", "");
			if (class_attr.length() > 0) {
				e.classes.add(class_attr);
			}
		}
		String href = atts.getValue("href");
		if(href!=null)
		{
			if (href.length() > 0) {
				e.href = href;
			}
		}
		String selected = atts.getValue("selected");
		if(selected!=null)
		{
			e.selected = selected;
		}
		String title = atts.getValue("title");
		if(title!=null&&TagContentTool.HasPrevTurnArea(title)){
			    if(TagContentTool.HasPrevHref(title))
			    {
			    	if(TagContentTool.IsHrefNull(e.href))
			    	{
			    		IsHasPrevPage = 1;
			    		preHref=1;
			    	}
			    	else
			    	{
			    		preHref=0;
			    	}
			    }
			    findParentTag();
		}
		if(class_attr != null||id != null)
		{
			if(class_attr!=null&&class_attr.toLowerCase().contains("page")||id != null&&id.toLowerCase().contains("page"))
			{
				findParentTag();
			}
		}
		String style = atts.getValue("style");
		if (style != null && style.length() != 0) {
			String[] segs = style.split(";");
			for (int i = 0; i < segs.length; ++i) {
				String t = segs[i].replace(" ", "");
				String[] sub_segs = t.split(":");
				if (sub_segs.length < 2) {
					continue;
				}
				String num_str = sub_segs[1];
				num_str = num_str.replace("\"", "");
				int index = sub_segs[1].indexOf("px");
				if (index >= 0) {
					num_str = sub_segs[1].substring(0, index);
				}
				if (sub_segs[0].equalsIgnoreCase("font-weight")) {
					if (sub_segs[1].equals("bold")) {
						e.is_bold = true;
					}
				} 
				if (sub_segs[0].equalsIgnoreCase("color")) {
					e.color = sub_segs[1];
				} 
			}
		}
	}

	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		//System.out.println("startElement...."+qName);
		Element e = new Element();
		e.tag = qName;
		setFeatureFromAtts(atts, e);
		//建立整个html页面的Dom树
		Element last = Allpath.peekLast();
		if(Allpath.isEmpty())ALLdomtree = e; 
		else last.children.add(e);
		Allpath.add(e);
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		String s = String.valueOf(ch, start, length);
		Element a = Allpath.peekLast();
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");      
        Matcher m = p.matcher(s); 
        s = m.replaceAll("");
        s = s.trim();
        a.content+=s;
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		Element e = Allpath.peekLast();
		Allpath.pollLast();
		String content=e.content.toLowerCase();
		//判断当前标签是否为翻页区域
		if(content.length()<30&&!e.tag.equalsIgnoreCase("script") && !e.tag.equalsIgnoreCase("style") && !e.tag.equalsIgnoreCase("link"))
		{
			if(TagContentTool.HasPrevTurnArea(content))
			{
				findParentTag();
			}
			else if(content.length()<10&&TagContentTool.HasPrevHref(content))
			{
		    	if(TagContentTool.IsHrefNull(e.href))
				{
					IsHasPrevPage = 1;
					preHref=1;
				}
				else
				{
					IsHasPrevPage = 0;
					preHref=0;
				}
		    	findParentTag();
			}	
			else if(content.length()<13&&TagContentTool.hasEndTurnPage(content)){
				findParentTag();
			}
		}
	}
	public void findParentTag()
	{
		for(int i=Allpath.size()-1;i>=0;i--)
		{
			if(TagContentTool.IsHasChildDomTree(Allpath.get(i).tag))
			{
				if(IshasDomtree.get(Allpath.get(i))==null)
				{
					IshasDomtree.put(Allpath.get(i), (double)1);
					domtrees.add(Allpath.get(i));
				}
				return ;
			}
		}
	}
	public boolean IsTurnPage(){
		for(int i=0;i<domtrees.size();i++)
		{
//			System.out.println("dom"+i+"共:"+domtrees.size());
//			System.out.println("------------printDomTreeStart-------------");
//			TagContentTool.printDomTree(domtrees.get(i));
//			System.out.println("------------printDomTreeEnd-------------");
			dom.clear();
//			System.out.println("------------ExtractDomTreeStart-------------");
			TagContentTool.ExtractDomTree(domtrees.get(i));
//			System.out.println("------------ExtractDomTreeEnd-------------");
//			System.out.println("------------OkDomTreeStart-------------");
			TagContentTool.printDomTree(dom);
			boolean pagenum=IsTurnPage(dom);
//			System.out.println("dom"+i+"true");
			if(pagenum)
			{
//				System.out.println("dom"+i+"true");
				return true;
			}
		}
//		boolean turnpage = TagContentTool.IsTurnByUrl(url);
//		if(turnpage)return true;
		return false;
	}
	public boolean  IsTurnPage(LinkedList<Element> tree){
		if(tree.size()==0)return false;
		boolean HasHref=true;
		int pagenum=0;
		int firsthref=-1;
		int secondhref=-1;
		int pagehref=0;
		int AtagNum=0;
		Vector<Integer>  allTagA=new Vector<Integer>();
		int currentclassid=-1;
		for(int i=0;i<tree.size();i++)
		{
//			System.out.println(i+":tag:"+tree.get(i).tag+"  start");
//			System.out.println(i+":tag:"+tree.get(i).tag+":href:"+TagContentTool.getOneHref(tree.get(i),"1")+"共："+tree.size());
//			System.out.println(i+":<a>0</a>的length:"+TagContentTool.ChildContentLength(tree.get(i)));

			if(tree.get(i).tag.compareTo("a")==0)
			{
				Integer value = TagContentTool.gettagContent(tree.get(i));
				if(value!=-1)
				{
					AtagNum++;
					allTagA.add(value);
				}
				if(TagContentTool.IsPageDigit(tree.get(i),"1"))
				{
					if(TagContentTool.IsHrefNull(tree.get(i).href))IsFristPage=1;
				}
				else if(TagContentTool.IsPageDigit(tree.get(i)))
				{
					if(TagContentTool.IshasClassOrId(tree.get(i)))
					{
						if(currentclassid==-1)currentclassid=1;
						else currentclassid=0;
					}
				}
			}
			if(TagContentTool.IsPageDigit(tree.get(i),"1"))
			{
				String href = TagContentTool.getOneHref(tree.get(i));
//				System.out.println("第"+i+"个"+"<a>1</a>的href:"+href+"内容:"+tree.get(i).content);
				if(firsthref!=-1)return false;
				if(href.length()>0&&href.compareTo("#")!=0&&href.compareTo("javascript:;")!=0)
				{
//					System.out.println("第inter"+i+"个"+"<a>1</a>的href:"+href+"内容:"+tree.get(i).content);
					pagehref++;
					if(firsthref!=0)firsthref=0;//注意
					else firsthref=1;//注意
				}
				else
				{
					if(firsthref==-1)
					{
						IsFristPage=1;//注意
						firsthref=1;//注意
					}
					else
					{
						IsFristPage=0;//注意
						firsthref=0;//注意
					}
				}
				pagenum++;
//				System.out.println("<a>1</a>的href:"+href+"pagenum:"+pagenum);
			}
			else if(TagContentTool.IsPageDigit(tree.get(i)))
			{
				if(TagContentTool.IsSecondPageDigit(tree.get(i)))secondhref=1;
				String href = TagContentTool.getOneHref(tree.get(i));
				String tag = tree.get(i).tag;
				String content = tree.get(i).content;
//				System.out.println("<"+tag+"href="+href+">"+content+"</"+tag+">");
//					System.out.println(TagContentTool.getOneHref(tree.get(i)));
//				if(href.length()==0||href.compareTo("#")==0||href.compareTo("javascript:;")==0)
				if(TagContentTool.IsHrefNull(href))
				{
//					System.out.println("<"+tag+"href="+href+">"+content+"</"+tag+">"+"HasHref:"+HasHref);
					HasHref = false;
				}
				else 
				{
//					System.out.println("pagehref:"+pagehref);
					pagehref++;
				}
//				System.out.println("href:"+href+"pagehref:"+pagehref);
				pagenum++;
			}
		}
//		System.out.println("AtagNum:"+AtagNum);
		if(AtagNum==0)return false;
//		Collections.sort(allTagA);
		int ConcatNum=0;
		int mid=1;
		for(int i=1;i<allTagA.size();i++)
		{
//			System.out.println("\n----ffffffffff--------"+allTagA.get(i));
			if(allTagA.get(i)<=allTagA.get(i-1)+2&&allTagA.get(i)>allTagA.get(i-1))
			{
				mid++;
			}
			else
			{
				if(mid>ConcatNum)ConcatNum=mid;
				mid=1;
			}
		}
		if(mid>ConcatNum)ConcatNum=mid;
		System.out.println("ConcatNum:"+ConcatNum);
		System.out.println("pagenum:"+pagenum);
		System.out.println("firsthref:"+firsthref);
		System.out.println("HasHref:"+HasHref);
		System.out.println("preHref:"+preHref);
		System.out.println("pagehref:"+pagehref);
		System.out.println("IsSlowdpage:"+IsSlowdpage);
		System.out.println("IsFristPage:"+IsFristPage);
		System.out.println("IsHasPrevPage:"+IsHasPrevPage);
//		if(pagenum>=1&&IsSlowdpage==1)return 1;
		
		if(IsSlowdpage==1)return true;
		if(ConcatNum<1||ConcatNum<allTagA.size()/2)return false;
		if(allTagA.size()==1)
		{
			if(!allTagA.get(0).toString().matches("[\\[]{0,1}1[\\]]{0,1}"))return false;
		}
		if(pagenum==pagehref+1)
		{
			if(pagenum==1)return false;
			if(pagehref==1)
			{
				if(secondhref==1&&firsthref==0)return true;
				else return false;
			}
			if(pagenum>1&&firsthref==0)return true;
			if(pagenum>1&&firsthref==1)return false;
			if(pagenum>1&&!HasHref)return true;
			if(currentclassid==1)return true;
		}
		else if(pagenum>1&&pagenum==pagehref)
		{
			if(ConcatNum>=1&&ConcatNum<pagenum)return false;
			if(currentclassid==1)return true;
			if(pagehref==1)
			{
				if(secondhref==1&&firsthref==0)return true;
				else return false;
			}
			else if(!HasHref)return true;
			else if(IsFristPage==0)return true;
			else if(!HasHref&&pagenum>=1&&IsHasPrevPage==0)return true;
			else return false;
		}
		else if(pagenum>1)
		{
//			if(tagAhasherfNum!=pagenum-1&&tagApagenum==tagAhasherfNum+1)return false;
			if(IsFristPage==1||firsthref==1)return false;
			if(pagehref>1&&firsthref==0
					||(secondhref==1&&pagehref==1&&firsthref==0))return true;
			if(pagehref>=1&&firsthref==-1)
			{
				if(!HasHref)return true;
			}
			if(preHref==0)return true;
			if(pagehref>=1&&IsHasPrevPage==0)return true;
			if(pagehref>=1&&IsHasPrevPage==1)return false;
			if(currentclassid==1)return true;
		}
//		if(pagenum>1&&pagenum!=pagehref&&preHref==1)return true;
		return false;
	}
	
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		//System.out.println("ignorableWhitespace....");
	}


	
	public void processingInstruction(String target, String data) throws SAXException {
		// TODO Auto-generated method stub
		//System.out.println("processingInstruction....");
	}

	
	public void skippedEntity(String name) throws SAXException {
		// TODO Auto-generated method stub
		//System.out.println("skippedEntity....");
	}
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException, SAXException {
	}
} 
