package com.sogou.pa.turnpage;

import java.io.BufferedWriter;
import java.io.IOException;

public class StoreFeathers {

	public static void storeNames(BufferedWriter w) throws IOException
	{
		String s="";
		s+="FirstPageIsAtagHerf,";
		s+="FirstPageIsAtagClass,";
		s+="FirstPageIsAtagid,";
		s+="FirstPageIsAtagColor,";
		s+="FirstPageIsAtagBold,";
		s+="FirstPageNotAtagHerf,";
		s+="FirstPageNotAtagClass,";
		s+="FirstPageNotAtagid,";
		s+="FirstPageNotAtagColor,";
		s+="FirstPageNotAtagBold,";
		s+="FirstPageIsStrong,";
		s+="IsFirstPage,";
		s+="IsSlowPage,";
//		s+="PageWordIsExist+",";//暂时不用
		s+="PrevPageHasHref,";
		s+="PrevPageIsExist,";
		s+="NextPageIsExist,";
		s+="SecondPageIsAtagHerf,";
		s+="SecondPageIsAtagClass,";
		s+="SecondPageIsAtagid,";
		s+="SecondPageIsAtagColor,";
		s+="SecondPageIsAtagBold,";
		s+="SecondPageNotAtagHerf,";
		s+="SecondPageNotAtagClass,";
		s+="SecondPageNotAtagid,";
		s+="SecondPageNotAtagColor,";
		s+="SecondPageNotAtagBold,";
		s+="SecondPageIsStrong,";
		s+="IsSecondPage,";
		s+="OtherPageIsAtagHerf,";
		s+="OtherPageIsAtagClass,";
		s+="OtherPageIsAtagid,";
		s+="OtherPageIsAtagColor,";
		s+="OtherPageIsAtagBold,";
		s+="OtherPageNotAtagHerf,";
		s+="OtherPageNotAtagClass,";
		s+="OtherPageNotAtagid,";
		s+="OtherPageNotAtagColor,";
		s+="OtherPageNotAtagBold,";
		s+="OtherPageIsStrong,";
		s+="IsOtherPage,";
		s+="IsHasTurnArea,";
		s+="TurnAreaNum,";
		s+="AtagPageHasHrefNum,";
		s+="AtagNumIsZero,";
		s+="AtagNum,";
		s+="AtagContinuousNum,";
		s+="pagenum,";
		s+="pageHasHrefNum,";
		s+="AtagCoutinuousNumsLesshalfAtagNums,";
		s+="AtagNumsEqualOneAndValueOne,";
		s+="FristpageNumEqualPageHrefAddOne,";
		s+="pageNumFirstStituationOne,";
		s+="pageNumFirstStituationTwo,";
		s+="pageNumFirstStituationThree,";
		s+="pageNumFirstStituationFour,";
		s+="pageNumFirstStituationFive,";
		s+="pageNumFirstStituationSix,";
		s+="pageNumFirstStituationSeven,";
		s+="SecondpageNumEqualPageHref,";
		s+="pageNumSecondStituationOne,";
		s+="pageNumSecondStituationTwo,";
		s+="pageNumSecondStituationThree,";
		s+="pageNumSecondStituationFour,";
		s+="pageNumSecondStituationFive,";
		s+="pageNumSecondStituationSix,";
		s+="pageNumSecondStituationSeven,";
		s+="pageNumSecondStituationEight,";
		s+="ThridpageNum,";
		s+="pageNumThridStituationOne,";
		s+="pageNumThridStituationTwo,";
		s+="pageNumThridStituationThree,";
		s+="pageNumThridStituationFour,";
		s+="pageNumThridStituationFive,";
		s+="pageNumThridStituationSix,";
		s+="pageNumThridStituationSeven,";
		s+="Forthpagenum,";
//		s+=TurnPageFeathers.pageUrl,";
		s+="FifthPageUrl,";
		s+="result\n";
		w.write(s);
	}
	public static void storeFeathers(BufferedWriter w) throws IOException
	{
		String s="";
		s+=TurnPageFeathers.FirstPageIsAtagHerf+",";
		s+=TurnPageFeathers.FirstPageIsAtagClass+",";
		s+=TurnPageFeathers.FirstPageIsAtagid+",";
		s+=TurnPageFeathers.FirstPageIsAtagColor+",";
		s+=TurnPageFeathers.FirstPageIsAtagBold+",";
		s+=TurnPageFeathers.FirstPageNotAtagHerf+",";
		s+=TurnPageFeathers.FirstPageNotAtagClass+",";
		s+=TurnPageFeathers.FirstPageNotAtagid+",";
		s+=TurnPageFeathers.FirstPageNotAtagColor+",";
		s+=TurnPageFeathers.FirstPageNotAtagBold+",";
		s+=TurnPageFeathers.FirstPageIsStrong+",";
		s+=TurnPageFeathers.IsFirstPage+",";
		s+=TurnPageFeathers.IsSlowPage+",";
//		s+=TurnPageFeathers.PageWordIsExist+",";//暂时不用
		s+=TurnPageFeathers.PrevPageHasHref+",";
		s+=TurnPageFeathers.PrevPageIsExist+",";
		s+=TurnPageFeathers.NextPageIsExist+",";
		s+=TurnPageFeathers.SecondPageIsAtagHerf+",";
		s+=TurnPageFeathers.SecondPageIsAtagClass+",";
		s+=TurnPageFeathers.SecondPageIsAtagid+",";
		s+=TurnPageFeathers.SecondPageIsAtagColor+",";
		s+=TurnPageFeathers.SecondPageIsAtagBold+",";
		s+=TurnPageFeathers.SecondPageNotAtagHerf+",";
		s+=TurnPageFeathers.SecondPageNotAtagClass+",";
		s+=TurnPageFeathers.SecondPageNotAtagid+",";
		s+=TurnPageFeathers.SecondPageNotAtagColor+",";
		s+=TurnPageFeathers.SecondPageNotAtagBold+",";
		s+=TurnPageFeathers.SecondPageIsStrong+",";
		s+=TurnPageFeathers.IsSecondPage+",";
		s+=TurnPageFeathers.OtherPageIsAtagHerf+",";
		s+=TurnPageFeathers.OtherPageIsAtagClass+",";
		s+=TurnPageFeathers.OtherPageIsAtagid+",";
		s+=TurnPageFeathers.OtherPageIsAtagColor+",";
		s+=TurnPageFeathers.OtherPageIsAtagBold+",";
		s+=TurnPageFeathers.OtherPageNotAtagHerf+",";
		s+=TurnPageFeathers.OtherPageNotAtagClass+",";
		s+=TurnPageFeathers.OtherPageNotAtagid+",";
		s+=TurnPageFeathers.OtherPageNotAtagColor+",";
		s+=TurnPageFeathers.OtherPageNotAtagBold+",";
		s+=TurnPageFeathers.OtherPageIsStrong+",";
		s+=TurnPageFeathers.IsOtherPage+",";
		s+=TurnPageFeathers.IsHasTurnArea+",";
		s+=TurnPageFeathers.TurnAreaNum+",";
		s+=TurnPageFeathers.AtagPageHasHrefNum+",";
		s+=TurnPageFeathers.AtagNumIsZero+",";
		s+=TurnPageFeathers.AtagNum+",";
		s+=TurnPageFeathers.AtagContinuousNum+",";
		s+=TurnPageFeathers.pagenum+",";
		s+=TurnPageFeathers.pageHasHrefNum+",";
		s+=TurnPageFeathers.AtagCoutinuousNumsLesshalfAtagNums+",";
		s+=TurnPageFeathers.AtagNumsEqualOneAndValueOne+",";
		s+=TurnPageFeathers.FristpageNumEqualPageHrefAddOne+",";
		s+=TurnPageFeathers.pageNumFirstStituationOne+",";
		s+=TurnPageFeathers.pageNumFirstStituationTwo+",";
		s+=TurnPageFeathers.pageNumFirstStituationThree+",";
		s+=TurnPageFeathers.pageNumFirstStituationFour+",";
		s+=TurnPageFeathers.pageNumFirstStituationFive+",";
		s+=TurnPageFeathers.pageNumFirstStituationSix+",";
		s+=TurnPageFeathers.pageNumFirstStituationSeven+",";
		s+=TurnPageFeathers.SecondpageNumEqualPageHref+",";
		s+=TurnPageFeathers.pageNumSecondStituationOne+",";
		s+=TurnPageFeathers.pageNumSecondStituationTwo+",";
		s+=TurnPageFeathers.pageNumSecondStituationThree+",";
		s+=TurnPageFeathers.pageNumSecondStituationFour+",";
		s+=TurnPageFeathers.pageNumSecondStituationFive+",";
		s+=TurnPageFeathers.pageNumSecondStituationSix+",";
		s+=TurnPageFeathers.pageNumSecondStituationSeven+",";
		s+=TurnPageFeathers.pageNumSecondStituationEight+",";
		s+=TurnPageFeathers.ThridpageNum+",";
		s+=TurnPageFeathers.pageNumThridStituationOne+",";
		s+=TurnPageFeathers.pageNumThridStituationTwo+",";
		s+=TurnPageFeathers.pageNumThridStituationThree+",";
		s+=TurnPageFeathers.pageNumThridStituationFour+",";
		s+=TurnPageFeathers.pageNumThridStituationFive+",";
		s+=TurnPageFeathers.pageNumThridStituationSix+",";
		s+=TurnPageFeathers.pageNumThridStituationSeven+",";
		s+=TurnPageFeathers.Forthpagenum+",";
//		s+=TurnPageFeathers.pageUrl+",";
		s+=TurnPageFeathers.FifthPageUrl;
		w.write(s);
	}
	public static void SetFristFeathers(Element e)
	{
		if(e.tag.compareTo("a")==0)
		{
			if(e.href.length()>0)TurnPageFeathers.FirstPageIsAtagHerf=1;
			if(e.classes.length()>0)TurnPageFeathers.FirstPageIsAtagClass=1;
			if(e.ids.length()>0)TurnPageFeathers.FirstPageIsAtagid=1;
			if(e.color.length()>0)TurnPageFeathers.FirstPageIsAtagColor=1;
//			if(e.is_bold)TurnPageFeathers.FirstPageIsAtagBold=1;
		}
		else
		{
			if(e.href.length()>0)TurnPageFeathers.FirstPageNotAtagHerf=1;
			if(e.classes.length()>0)TurnPageFeathers.FirstPageNotAtagClass=1;
			if(e.ids.length()>0)TurnPageFeathers.FirstPageNotAtagid=1;	
			if(e.color.length()>0)TurnPageFeathers.FirstPageNotAtagColor=1;
//			if(e.is_bold)TurnPageFeathers.FirstPageNotAtagBold=1;
		}
		if(e.href.length()==0||e.href==null)TurnPageFeathers.IsFirstPage=1;
	}
	public static void SetSecondFeathers(Element e)
	{
		if(e.tag.compareTo("a")==0)
		{
			if(e.href.length()>0)TurnPageFeathers.SecondPageIsAtagHerf=1;
			if(e.classes.length()>0)TurnPageFeathers.SecondPageIsAtagClass=1;
			if(e.ids.length()>0)TurnPageFeathers.SecondPageIsAtagid=1;
			if(e.color.length()>0)TurnPageFeathers.SecondPageIsAtagColor=1;
//			if(e.is_bold)TurnPageFeathers.SecondPageIsAtagBold=1;
		}
		else
		{
			if(e.href.length()>0)TurnPageFeathers.SecondPageNotAtagHerf=1;
			if(e.classes.length()>0)TurnPageFeathers.SecondPageNotAtagClass=1;
			if(e.ids.length()>0)TurnPageFeathers.SecondPageNotAtagid=1;	
			if(e.color.length()>0)TurnPageFeathers.SecondPageNotAtagColor=1;
//			if(e.is_bold)TurnPageFeathers.SecondPageNotAtagBold=1;
		}
		if(e.href.length()==0||e.href==null)TurnPageFeathers.IsSecondPage=1;
	}
	public static void SetOtherFeathers(Element e)
	{
		if(e.tag.compareTo("a")==0)
		{
			if(e.href.length()>0)TurnPageFeathers.OtherPageIsAtagHerf+=1;
			if(e.classes.length()>0)TurnPageFeathers.OtherPageIsAtagClass+=1;
			if(e.ids.length()>0)TurnPageFeathers.OtherPageIsAtagid+=1;
			if(e.color.length()>0)TurnPageFeathers.OtherPageIsAtagColor+=1;
//			if(e.is_bold)TurnPageFeathers.OtherPageIsAtagBold+=1;
		}
		else
		{
			if(e.href.length()>0)TurnPageFeathers.OtherPageNotAtagHerf+=1;
			if(e.classes.length()>0)TurnPageFeathers.OtherPageNotAtagClass+=1;
			if(e.ids.length()>0)TurnPageFeathers.OtherPageNotAtagid+=1;	
			if(e.color.length()>0)TurnPageFeathers.OtherPageNotAtagColor+=1;
//			if(e.is_bold)TurnPageFeathers.OtherPageNotAtagBold+=1;
		}
		if(e.href.length()==0||e.href==null)TurnPageFeathers.IsOtherPage+=1;
	}
	public static void SetStrongFeathers(int flag)
	{
		if(flag==1)TurnPageFeathers.FirstPageIsStrong=1;
		if(flag==2)TurnPageFeathers.SecondPageIsStrong=1;
		if(flag>2)TurnPageFeathers.OtherPageIsStrong=1;
	}
}
