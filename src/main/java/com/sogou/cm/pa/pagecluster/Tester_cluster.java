package com.sogou.cm.pa.pagecluster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;

import org.xml.sax.SAXException;

import com.sogou.web.selector.urllib.URLUtils;

public class Tester_cluster {
	public static String getMemStat() {
		 SimpleDateFormat STANDARD_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 
	  Runtime runtime = Runtime.getRuntime();
	Date date = new Date(System.currentTimeMillis());
				StringBuilder sb = new StringBuilder(1024);
				sb.append("[").append(STANDARD_FORMAT.format(date)).append("]");
				sb.append("[").append(runtime.totalMemory() >> 20).append(":").append(runtime.freeMemory() >> 20)
						.append("] ");
				sb.append("");
				System.out.println(sb);
				return (sb.toString());
	}
	public static void main(String[] args) throws IOException, SAXException {
	//	ClusterPredictor predictor = new ClusterPredictor("site_clusters.txt");
		BufferedReader reader = new BufferedReader(new FileReader(new File("clusters.txt")));
	//	BufferedWriter writer = new BufferedWriter(new FileWriter(new File("tt.txt")));
		LinkedList<ClusterInfo> cis = new LinkedList<ClusterInfo>();
		String line;
		int i = 0;
		long ela = 0;
		while ((line = reader.readLine()) != null) {
			try {
				ClusterInfo ci = new ClusterInfo();
				long a = System.currentTimeMillis();
				ci.fromString(line);
				ela += System.currentTimeMillis() -a;
				cis.add(ci);
			//	System.out.println("sadf");
				i++;
				
				if (i%100 == 0) {
					getMemStat();
				}
				
			} catch (Exception e) {
				
			}
		}
		System.out.println(ela + "\t" + i);
		reader.close();
		
		getMemStat();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getMemStat();
		
	}
}
