package com.sogou.pa.tools;
import java.io.BufferedReader;
import java.io.File;

public class Test {
	public static void Testa(String[] args) throws Exception
	{
		File file = new File("D:\\JavaWorkplace\\onlineHadoop\\DocsigUrl");
		BufferedReader reader = null;
		String value = "0015e77674b7604f-4ef3223d79529780-80bd73f8ff1cab03980b29ee69f86721      df266dfc166abae4|0800040000800115|0800000000000000|A|9|0|4385.15722|1457016786|1306828759|0|0|0|0|N|32|83c1670b|1:63781-2:32762-3:65532|-7.05569|6|84|9|0|0|http://www.redocn.com/network.php?uid=&do=blog&view=all&orderby=viewnum&day=7&page=4|1457016786|";
		String lines = value.toString();
		String[] tmp =  lines.split("\\|");
		for(int i=0;i<tmp.length;i++)
		{
			System.out.println(i+"|"+tmp[i]);
		}
		if(tmp.length>0)
		{
			String c = tmp[2];
			if(c.charAt(2)=='2')System.out.println(tmp[0].split("\\s")[0]);
			int one = tmp[2].charAt(2)-'0';
			int two = tmp[1].charAt(2)-'0';
			int turn = 1<<1;
			int onevale = one&turn;
			System.out.println(onevale+"|"+(two&turn)+"|"+turn);
			//context.write(new Text(tmp[tmp.length-1]), new Text("1"));
		}
	}
}
