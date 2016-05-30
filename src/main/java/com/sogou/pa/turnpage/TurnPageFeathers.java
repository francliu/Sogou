package com.sogou.pa.turnpage;


public class TurnPageFeathers {
	
	//标签值为数字的特征
	public static int  FirstPageIsAtagHerf;
	public static int  FirstPageNotAtagHerf;
	public static int  FirstPageIsAtagClass;
	public static int  FirstPageNotAtagClass;
	public static int  FirstPageIsAtagid;
	public static int  FirstPageNotAtagid;
	public static int  FirstPageIsStrong;
	public static int  FirstPageIsAtagColor;
	public static int  FirstPageNotAtagColor;
	public static int  FirstPageIsAtagBold;
	public static int  FirstPageNotAtagBold;
	public static int  IsFirstPage;
	public static int  IsSlowPage;
	public static int  PageWordIsExist;//暂时不用
	public static int  PrevPageHasHref;
	public static int  PrevPageIsExist;
	public static int  NextPageIsExist;
	public static int  SecondPageIsAtagHerf;
	public static int  SecondPageNotAtagHerf;
	public static int  SecondPageIsAtagClass;
	public static int  SecondPageNotAtagClass;
	public static int  SecondPageIsAtagid;
	public static int  SecondPageNotAtagid;
	public static int  SecondPageIsStrong;
	public static int  SecondPageIsAtagColor;
	public static int  SecondPageNotAtagColor;
	public static int  SecondPageIsAtagBold;
	public static int  SecondPageNotAtagBold;
	public static int  IsSecondPage;
	public static int  OtherPageIsAtagHerf;
	public static int  OtherPageNotAtagHerf;
	public static int  OtherPageIsAtagClass;
	public static int  OtherPageNotAtagClass;
	public static int  OtherPageIsAtagid;
	public static int  OtherPageNotAtagid;
	public static int  OtherPageIsStrong;
	public static int  OtherPageIsAtagColor;
	public static int  OtherPageNotAtagColor;
	public static int  OtherPageIsAtagBold;
	public static int  OtherPageNotAtagBold;
	public static int  IsOtherPage;
	//针对翻页区域的特征整合
	public static int  IsHasTurnArea;
	public static int  TurnAreaNum;
	
	public static int  AtagPageHasHrefNum;
	public static int  AtagNumIsZero;
	public static int  AtagNum;
	public static int  AtagContinuousNum;
	public static int  pagenum;
	public static int  pageHasHrefNum;
	public static int  AtagCoutinuousNumsLesshalfAtagNums;
	public static int  AtagNumsEqualOneAndValueOne;
	//a标签的个数等于总标签个数减一情况
	public static int  FristpageNumEqualPageHrefAddOne; //first stiuation
	public static int  pageNumFirstStituationOne;
	public static int  pageNumFirstStituationTwo;
	public static int  pageNumFirstStituationThree;
	public static int  pageNumFirstStituationFour;
	public static int  pageNumFirstStituationFive;
	public static int  pageNumFirstStituationSix;
	public static int  pageNumFirstStituationSeven;
	//a标签的个数等于总标签个数情况
	public static int  SecondpageNumEqualPageHref; //Second stiuation
	public static int  pageNumSecondStituationOne;
	public static int  pageNumSecondStituationTwo;
	public static int  pageNumSecondStituationThree;
	public static int  pageNumSecondStituationFour;
	public static int  pageNumSecondStituationFive;
	public static int  pageNumSecondStituationSix;
	public static int  pageNumSecondStituationSeven;
	public static int  pageNumSecondStituationEight;
	//a标签的个数比总标签个数少多于1的情况
	public static int  ThridpageNum; //Thrid stiuation
	public static int  pageNumThridStituationOne;
	public static int  pageNumThridStituationTwo;
	public static int  pageNumThridStituationThree;
	public static int  pageNumThridStituationFour;
	public static int  pageNumThridStituationFive;
	public static int  pageNumThridStituationSix;
	public static int  pageNumThridStituationSeven;
	//其他情况
	public static int  Forthpagenum; //Forth stiuation
	public static int  pageUrl;
	public static int  FifthPageUrl; //Fifth stiuation
	TurnPageFeathers(){
		FirstPageIsAtagHerf=0;
		FirstPageNotAtagHerf=0;
		FirstPageIsAtagClass=0;
		FirstPageNotAtagClass=0;
		FirstPageIsAtagid=0;
		FirstPageNotAtagid=0;
		FirstPageIsStrong=0;
		FirstPageIsAtagColor=0;
		FirstPageNotAtagColor=0;
		FirstPageIsAtagBold=0;
		FirstPageNotAtagBold=0;
		IsFirstPage=0;
		IsSlowPage=0;
		PageWordIsExist=0;
		PrevPageHasHref=0;
		PrevPageIsExist=0;
		NextPageIsExist=0;
		SecondPageIsAtagHerf=0;
		SecondPageNotAtagHerf=0;
		SecondPageIsAtagClass=0;
		SecondPageNotAtagClass=0;
		SecondPageIsAtagid=0;
		SecondPageNotAtagid=0;
		SecondPageIsStrong=0;
		SecondPageIsAtagColor=0;
		SecondPageNotAtagColor=0;
		SecondPageIsAtagBold=0;
		SecondPageNotAtagBold=0;
		IsSecondPage=0;
		OtherPageIsAtagHerf=0;
		OtherPageNotAtagHerf=0;
		OtherPageIsAtagClass=0;
		OtherPageNotAtagClass=0;
		OtherPageIsAtagid=0;
		OtherPageNotAtagid=0;
		OtherPageIsStrong=0;
		OtherPageIsAtagColor=0;
		OtherPageNotAtagColor=0;
		OtherPageIsAtagBold=0;
		OtherPageNotAtagBold=0;
		IsOtherPage=0;
		AtagNum=0;
		AtagContinuousNum=0;
		AtagCoutinuousNumsLesshalfAtagNums=0;
		AtagNumsEqualOneAndValueOne=0;
		IsHasTurnArea=0;
		TurnAreaNum=0;
		pagenum=0;
		pageHasHrefNum=0;
		AtagNumIsZero=0;
		FristpageNumEqualPageHrefAddOne=0; //first stiuation
		pageNumFirstStituationOne=0;
		pageNumFirstStituationTwo=0;
		pageNumFirstStituationThree=0;
		pageNumFirstStituationFour=0;
		pageNumFirstStituationFive=0;
		pageNumFirstStituationSix=0;
		pageNumFirstStituationSeven=0;
		SecondpageNumEqualPageHref=0; //Second stiuation
		pageNumSecondStituationOne=0;
		pageNumSecondStituationTwo=0;
		pageNumSecondStituationThree=0;
		pageNumSecondStituationFour=0;
		pageNumSecondStituationFive=0;
		pageNumSecondStituationSix=0;
		pageNumSecondStituationSeven=0;
		pageNumSecondStituationEight=0;
		ThridpageNum=0; //Thrid stiuation
		pageNumThridStituationOne=0;
		pageNumThridStituationTwo=0;
		pageNumThridStituationThree=0;
		pageNumThridStituationFour=0;
		pageNumThridStituationFive=0;
		pageNumThridStituationSix=0;
		pageNumThridStituationSeven=0;
		Forthpagenum=0; //Forth stiuation
		AtagPageHasHrefNum=0;
		pageUrl=0;
		FifthPageUrl=0; //Fifth stiuation
	}
	public static void clear() {
		FirstPageIsAtagHerf=0;
		FirstPageNotAtagHerf=0;
		FirstPageIsAtagClass=0;
		FirstPageNotAtagClass=0;
		FirstPageIsAtagid=0;
		FirstPageNotAtagid=0;
		FirstPageIsStrong=0;
		FirstPageIsAtagColor=0;
		FirstPageNotAtagColor=0;
		FirstPageIsAtagBold=0;
		FirstPageNotAtagBold=0;
		IsFirstPage=0;
		IsSlowPage=0;
		PageWordIsExist=0;
		PrevPageHasHref=0;
		PrevPageIsExist=0;
		NextPageIsExist=0;
		SecondPageIsAtagHerf=0;
		SecondPageNotAtagHerf=0;
		SecondPageIsAtagClass=0;
		SecondPageNotAtagClass=0;
		SecondPageIsAtagid=0;
		SecondPageNotAtagid=0;
		SecondPageIsStrong=0;
		SecondPageIsAtagColor=0;
		SecondPageNotAtagColor=0;
		SecondPageIsAtagBold=0;
		SecondPageNotAtagBold=0;
		IsSecondPage=0;
		OtherPageIsAtagHerf=0;
		OtherPageNotAtagHerf=0;
		OtherPageIsAtagClass=0;
		OtherPageNotAtagClass=0;
		OtherPageIsAtagid=0;
		OtherPageNotAtagid=0;
		OtherPageIsStrong=0;
		OtherPageIsAtagColor=0;
		OtherPageNotAtagColor=0;
		OtherPageIsAtagBold=0;
		OtherPageNotAtagBold=0;
		IsOtherPage=0;
		AtagNum=0;
		AtagContinuousNum=0;
		AtagCoutinuousNumsLesshalfAtagNums=0;
		AtagNumsEqualOneAndValueOne=0;
		IsHasTurnArea=0;
		TurnAreaNum=0;
		pagenum=0;
		pageHasHrefNum=0;
		AtagNumIsZero=0;
		FristpageNumEqualPageHrefAddOne=0; //first stiuation
		pageNumFirstStituationOne=0;
		pageNumFirstStituationTwo=0;
		pageNumFirstStituationThree=0;
		pageNumFirstStituationFour=0;
		pageNumFirstStituationFive=0;
		pageNumFirstStituationSix=0;
		pageNumFirstStituationSeven=0;
		SecondpageNumEqualPageHref=0; //Second stiuation
		pageNumSecondStituationOne=0;
		pageNumSecondStituationTwo=0;
		pageNumSecondStituationThree=0;
		pageNumSecondStituationFour=0;
		pageNumSecondStituationFive=0;
		pageNumSecondStituationSix=0;
		pageNumSecondStituationSeven=0;
		pageNumSecondStituationEight=0;
		ThridpageNum=0; //Thrid stiuation
		pageNumThridStituationOne=0;
		pageNumThridStituationTwo=0;
		pageNumThridStituationThree=0;
		pageNumThridStituationFour=0;
		pageNumThridStituationFive=0;
		pageNumThridStituationSix=0;
		pageNumThridStituationSeven=0;
		Forthpagenum=0; //Forth stiuation
		AtagPageHasHrefNum=0;
		pageUrl=0;
		FifthPageUrl=0; //Fifth stiuation
	}
}