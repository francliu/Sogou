package com.sogou.cm.pa.pagecluster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.xml.sax.SAXException;

import com.sogou.web.selector.urllib.URLUtils;

public class Tester2 {

	public static void main(String[] args) throws IOException, SAXException {
		BufferedReader reader2 = new BufferedReader(new FileReader(new File("C:\\Users\\sunjian\\Downloads\\part-r-00000")));
		String line;
		while ((line = reader2.readLine()) != null) {
			System.out.println(line);
			byte[] reb = line.getBytes("UTF-16LE");
			for (byte b: reb) {
				System.out.format("%x  ", b);
			}
			System.out.println();
		}
	}
}
