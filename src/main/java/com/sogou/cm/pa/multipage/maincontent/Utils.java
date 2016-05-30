package com.sogou.cm.pa.multipage.maincontent;

public class Utils {
	static int[][] dist = new int[201][201];
	public static String getLCS(String s1, String s2) {
		for (int i = 0;i<=s1.length(); ++i) {
			dist[i][0]=0;
		}
		for (int i = 0;i<=s2.length();++i) {
			dist[0][i] = 0;
		}
		int x = 0,y = 0;
		for (int i = 1; i<= s1.length();++i) {
			for (int j = 1; j <= s2.length();++j) {
				if (s1.charAt(i-1)==s2.charAt(j-1)) {
					dist[i][j] = dist[i-1][j-1]+1;
				} else {
					dist[i][j] = 0;
				}
			//	System.out.println(i + "\t" + j + "\t" + dist[i][j]);
				if (dist[i][j] > dist[x][y]) {
					x = i;
					y = j;
				}
			}
		}
	//	System.out.println(x + "\t" + dist[x][y]);
		return s1.substring(x-dist[x][y], x);
	}
	
	public static void main(String[] args) {
		String s1 = "assssdfghjj";
		String s2 = "ssssadfgjjj";
		System.out.println(Utils.getLCS(s1, s2));
	}
}
