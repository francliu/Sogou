package com.sogou.pa.tools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.hadoop.io.Text;
public class RandomExtractUrlLocal{
	
	public static void main(String[] args) throws Exception
	{
		int num=0;
		File file = new File("D:\\JavaWorkplace\\sogou\\src\\com\\sogou\\tools\\FinalTurnPageUrl.txt");
		BufferedReader reader = null;
		HashMap<Integer,String> map = new HashMap<Integer,String>(); 
		HashMap<Integer,Integer> mapPos = new HashMap<Integer,Integer>(); 
		
		try {
			System.out.println("hhhhhhhh");
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int i=1;
			while ((tempString = reader.readLine()) != null) {
		        Random random = new Random();
		        //String tmp = tempString.split("\\s")[0];
		        String Url = tempString.split("\\|")[1];
		        int s = 0;
//		        if(i%100==30)map.put(i,tempString);
		        if(i<=1000)
		        {
		        	map.put(i,Url);
		        	mapPos.put(i, i);
		        }
		        else
		        {
		        	s = random.nextInt(i)+1;		        
			       // if(i>10000)break;
			        if(s<=7000)
			        {
			        	map.put(s,Url);
			        	//System.out.println(s+"-------------------------"+i+"------------------"+map.size());
			        }
		        }
		        i++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		File RandomFile = new File("D:\\JavaWorkplace\\sogou\\src\\com\\sogou\\tools\\1000NewTurnPage_05_18.txt");
		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter(RandomFile));
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			writer.write(val+"\n");
		}
		writer.flush();
		writer.close();
	}
}
