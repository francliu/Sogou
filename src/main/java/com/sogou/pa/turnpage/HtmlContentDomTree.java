package com.sogou.pa.turnpage;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;



public class HtmlContentDomTree implements ContentHandler {

	Locator locator;
	public static double TurnPageNum = 0;
	public static double GetTurnPageNum = 0;
	public static int preHref=-1;
	public static int IsSlowdpage=-1;
	public static int IsFristPage=-1;
	public static int IsHasPrevPage=-1;
	public static LinkedList<Element> dom;
	public static int CurrentPage=-1;
	public static int pageNum=-1;
	public static boolean is_abnormal_page = false;
	int tagnum;
	StringBuffer xpath;
	LinkedList<Element> Allpath;
	LinkedList<Element> domtrees;
	HashMap<Element,Double> IshasDomtree;
	public static String url="";
	public HtmlContentDomTree() {
		dom = new LinkedList<Element>();
		Allpath = new LinkedList<Element>();
		domtrees = new LinkedList<Element>();
		IshasDomtree = new HashMap<Element,Double>();
		xpath = new StringBuffer();
		tagnum=0;
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
		CurrentPage = -1;
		pageNum=-1;
		xpath.setLength(0);
		xpath.trimToSize();
		is_abnormal_page = false;
		tagnum=0;
	}
	
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}
	
	public void startDocument() throws SAXException {
//		System.out.println("start");
		clear();
	}
	
	public void endDocument() throws SAXException {
		System.out.println("end");
	}
	
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
	}
	
	public void endPrefixMapping(String prefix) throws SAXException {
	}
	public void setFeatureFromAtts(Attributes atts, Element e) {
		String id = atts.getValue("id");
		e.ids=id;
		String class_attr = atts.getValue("class");
		e.classes=class_attr;
		String href = atts.getValue("href");
		if(href!=null)
		{
			if (href.length() > 0) {
				e.href = href;
			}
		}
		else href="";
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
			if(class_attr!=null&&class_attr.toLowerCase().contains("page")
					||id != null&&id.toLowerCase().contains("page"))
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
				if (sub_segs[0].equalsIgnoreCase("color")) {
					e.color = sub_segs[1];
					break;
				} 
			}
		}
	}

	
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		if (xpath.length() > 2000) {
			is_abnormal_page = true;
			return;
		}
		if(tagnum>100000)
		{
			is_abnormal_page=true;
			return;
		}
		tagnum++;
//		System.out.println(qName);
		xpath.append(qName + "/");
		Element e = new Element();
		e.tag = qName;
		setFeatureFromAtts(atts, e);
		//建立整个html页面的Dom树
		Element last = Allpath.peekLast();
		if(last!=null)last.children.add(e);
		Allpath.add(e);
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		String s = String.valueOf(ch, start, length);
//		System.out.println(s);
		if(s.length()>2000)
		{
			is_abnormal_page = true;
			return;
		}
		Element a = Allpath.peekLast();
        if(a.content.length()==0)a.content+=s;
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (is_abnormal_page) {
			return;
		}
		Element e = Allpath.peekLast();
		Allpath.pollLast();
		String content=e.content.toLowerCase();
		//判断当前标签是否为翻页区域
		if(content.length()<30&&!e.tag.equalsIgnoreCase("script") && !e.tag.equalsIgnoreCase("style") && !e.tag.equalsIgnoreCase("link"))
		{
			if(TagContentTool.HasPrevTurnArea(content))
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
		xpath.delete(xpath.lastIndexOf("/"), xpath.length());
		if (xpath.lastIndexOf("/")>=0) {
			xpath.delete(xpath.lastIndexOf("/")+1, xpath.length());
		} else {
			xpath.delete(0, xpath.length());
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
			dom.clear();
			TagContentTool.ExtractDomTree(domtrees.get(i));
			boolean pagenum=IsTurnPage(dom);
			if(pagenum)return true;
		}
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
		int value = 0;
		for(int i=0;i<tree.size();i++)
		{
			if(tree.get(i).tag.compareTo("a")==0)
			{
				value = TagContentTool.gettagContent(tree.get(i));
				if(value!=-1)
				{
					AtagNum++;
					allTagA.add(value);
				}
				if(TagContentTool.IsPageDigit(tree.get(i),"1"))
				{
					if(TagContentTool.IsHrefNull(tree.get(i).href))IsFristPage=1;
				}
				else if((value=TagContentTool.IsPageDigit(tree.get(i)))>1)
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
				if(firsthref!=-1)return false; //很重要不能忽略
				if(href.length()>0&&href.compareTo("#")!=0&&href.compareTo("javascript:;")!=0)
				{
					pagehref++;
					if(firsthref!=0)firsthref=0;//注意
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
			}
			else if((value=TagContentTool.IsPageDigit(tree.get(i)))>1)
			{
				if(TagContentTool.IsSecondPageDigit(tree.get(i)))secondhref=1;
				String href = TagContentTool.getOneHref(tree.get(i));
				if(TagContentTool.IsHrefNull(tree.get(i).href)
						||(i+1<tree.size()&&(tree.get(i+1).classes==null||tree.get(i+1).classes.length()==0)
								||i-1>=0&&(tree.get(i-1).classes==null||tree.get(i-1).classes.length()==0)
								)&&(tree.get(i).classes!=null&&tree.get(i).classes.length()>0)
						||(i+1<tree.size()&&(tree.get(i+1).ids==null||tree.get(i+1).ids.length()==0)
								||i-1>=0&&(tree.get(i-1).ids==null||tree.get(i-1).ids.length()==0)
								)&&(tree.get(i).ids!=null&&tree.get(i).ids.length()>0)
						)
				{
					if(tree.get(i).tag.compareTo("a")==0
							||i+1<tree.size()&&tree.get(i+1).tag.compareTo("a")==0
							&&TagContentTool.IsPageDigit(tree.get(i+1))==value+1
							||i-1>0&&tree.get(i-1).tag.compareTo("a")==0
							&&TagContentTool.IsPageDigit(tree.get(i-1))==value-1)
					{
						HtmlContentDomTree.CurrentPage=value;
					}
					if(tree.get(i).tag.toLowerCase().compareTo("strong")==0)
	 				{
	 					HtmlContentDomTree.pageNum = value;
	 				}
					if(tree.get(i).color!=null&&tree.get(i).color.length()>1)
					{
						if(i+1<tree.size()&&tree.get(i+1).tag.compareTo("a")==0
							&&TagContentTool.IsPageDigit(tree.get(i+1))==value+1
							||i-1>0&&tree.get(i-1).tag.compareTo("a")==0
							&&TagContentTool.IsPageDigit(tree.get(i-1))==value-1)
						{
							HtmlContentDomTree.CurrentPage=value;
						}
					}
				}
				if(TagContentTool.IsHrefNull(href))HasHref = false;
				else pagehref++;
				pagenum++;
			}
		}
		if(AtagNum==0)return false;
		int ConcatNum=0;
		int mid=1;
		for(int i=1;i<allTagA.size();i++)
		{
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
		if(IsSlowdpage==1)return true;
		if(ConcatNum<1||ConcatNum<allTagA.size()/2)return false;
		if(allTagA.size()==1)
		{
			if(!allTagA.get(0).toString().matches("[\\[]{0,1}1[\\]]{0,1}"))
			{
				return false;
			}
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
			if(preHref==1)return false;
			if(currentclassid==1)return true;
			if(pagehref==1)
			{
				if(secondhref==1&&firsthref==0)return true;
				else return false;
			}
			else if(!HasHref)return true;
			else if(IsFristPage==0)return true;
			else if(!HasHref&&pagenum>=1&&IsHasPrevPage==0)return true;
			else if(firsthref==-1)return true;
			else return false;
		}
		else if(pagenum>1)
		{
			if(IsFristPage==1||firsthref==1)return false;
			if(pagehref>1&&firsthref==0
					||(secondhref==1&&pagehref==1&&firsthref==0))
			{
				return true;
			}
			if(pagehref>=1&&firsthref==-1)
			{
				if(!HasHref)return true;
			}
			if(preHref==0)return true;
			if(pagehref>=1&&IsHasPrevPage==0)return true;
			if(pagehref>=1&&IsHasPrevPage==1)return false;
			if(currentclassid==1)return true;
		}
		return false;
	}
	
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
	}
	
	public void processingInstruction(String target, String data) throws SAXException {
	}
	
	public void skippedEntity(String name) throws SAXException {
	}
} 
