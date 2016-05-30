package com.sogou.pa.regex;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class localregex {

	public static void main(String[] args) throws Exception
	{	
//		String line = "urllist:%23%E5%90%B1%E5%8F%A3%E4%BB%A4%23%E9%95%BF%E6%8C%89%E5%A4%8D%E5%88%B6%E6%AD%A4%E6%9D%A1%E6%B6%88%E6%81%AF%EF%BC%8C%E6%89%93%E5%BC%80%E6%94%AF%E4%BB%98%E5%AE%9D%E5%8D%B3%E5%8F%AF%E6%B7%BB%E5%8A%A0%E6%88%91%E4%B8%BA%E5%A5%BD%E5%8F%8Bwdpl3m13pz#c4db3f01-6d26-45b9-892a-90e9bd3932fe#ab735a258a90e8e1-6bee54fcbd896b2a-5a641e6336eee5fa2dbe455a93024fed       ab735a258a90e8e1-6bee54fcbd896b2a-9e60a7e03053e5413eb23091f9c9d898      ab735a258a90e8e1-6bee54fcbd896b2a-dc3c15d865943b0442ba83ba663d105d      ab735a258a90e8e1-6bee54fcbd896b2a-4130d5948467e7c807ab0a1a331b935a      ab735a258a90e8e1-6bee54fcbd896b2a-31a7cbae4237a454bb796a0360f28643      ab735a258a90e8e1-6bee54fcbd896b2a-4591311ad9bf4df783be4a068a33b458      ab735a258a90e8e1-6bee54fcbd896b2a-1b8adfcacaad99ac6c287151fd0e9569      ab735a258a90e8e1-6bee54fcbd896b2a-26d5a14d14020c75cec99d6dd5f03dcd      ab735a258a90e8e1-6bee54fcbd896b2a-cc6d8c0c5e6e66c9da5ececb2d5d2575      ab735a258a90e8e1-6bee54fcbd896b2a-0217751e6e17fd2876fe1cc72b787f0d";
//		Pattern p = Pattern.compile("^urllist:+([^\\\n]+)");
//	    Matcher m = p.matcher(line);
//	    ArrayList<String> strs = new ArrayList<String>();
//	    while (m.find()) {
//	    	String[] urls = m.group(1).split("\\s"); 
//	    	for(int i=0;i<urls.length;i++)
//	    	{
//	    		if(i==0)
//	    		{
//	    			String[] tmp = urls[i].split("#");
//	    			System.out.println(tmp[tmp.length-1]);
//	    		}
//	    		else if(urls[i].toString().length()>=2)System.out.println(urls[i]);
//	    	}
//	    	
//	    } 
		String line="&nbsp;10&nbsp;";
		
		Pattern digit = Pattern.compile("[&nbsp;]*?([\\d]+)[&nbsp;]*?");
	    Matcher m = digit.matcher(line);
	    String str="";
		while(m.find()){
			str+=m.group(1);
		}
		System.out.println("hhh:"+str);
	    System.out.println("end");
	}
}
