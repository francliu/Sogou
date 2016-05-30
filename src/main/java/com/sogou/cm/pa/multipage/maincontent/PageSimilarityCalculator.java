package com.sogou.cm.pa.multipage.maincontent;

public class PageSimilarityCalculator {
	static int length = 10000;
	static int[][] d = new int[2][length];
	public static double minEditDistance(String s1, String s2) {
		int len1 = s1.length();
		int len2 = s2.length();
		if (len1 == 0 && len2 == 0) {
			return 0.0;
		}
		if (len1 == 0 || len2 == 0) {
			return 0.0;
		}
		if (len1 + 1 > length) {
			d = new int[2][len1+1];
			length = len1 + 1;
		}
		for (int i = 0; i <= len1; ++i) {
			d[0][i] = i;
		}
		int flag = 1;
		for (int i = 1; i <= len2; ++i) {
			d[flag][0] = i;
			for (int j = 1; j <= len1; ++j) {
				int last = flag^1;
				int d1 = d[last][j]+1;
				int d2 = d[flag][j-1]+1;
				int d3 = d[last][j-1];
				if (s1.charAt(j-1) != s2.charAt(i-1)) {
					++d3;
				}
				d[flag][j] = d1 > d2 ? d2 : d1;
				d[flag][j] = d[flag][j] > d3 ? d3 : d[flag][j];
			}
			flag = flag^1;
		}

		int min_len = len1 < len2 ? len1 : len2;
		int max_len = len1 > len2 ? len1 : len2;
		int diff = Math.abs(len1-len2);
		double t = 1-((double)d[flag^1][len1]-diff)/(double)min_len;
		//System.out.println(t);
		if (t > 0.5) {
			t = t - diff/(double)max_len/2;
			if (t <= 0.5) {
				t = 0.51;
			}
		}
		return t;
	}

	public static double minEditDistance2(String s1, String s2) {
		int len1 = s1.length();
		int len2 = s2.length();
		if (len1 == 0 && len2 == 0) {
			return 0.0;
		}
		if (len1 == 0 || len2 == 0) {
			return 0.0;
		}
		if (len1 + 1 > length) {
			d = new int[2][len1+1];
			length = len1 + 1;
		}
		for (int i = 0; i <= len1; ++i) {
			d[0][i] = i;
		}
		int flag = 1;
		for (int i = 1; i <= len2; ++i) {
			d[flag][0] = i;
			for (int j = 1; j <= len1; ++j) {
				int last = flag^1;
				int d1 = d[last][j]+1;
				int d2 = d[flag][j-1]+1;
				int d3 = d[last][j-1];
				if (s1.charAt(j-1) != s2.charAt(i-1)) {
					++d3;
				}
				d[flag][j] = d1 > d2 ? d2 : d1;
				d[flag][j] = d[flag][j] > d3 ? d3 : d[flag][j];
			}
			flag = flag^1;
		}

		int min_len = len1 < len2 ? len1 : len2;
		int max_len = len1 > len2 ? len1 : len2;
		int diff = Math.abs(len1-len2);
		double t = 1-((double)d[flag^1][len1])/(double)max_len;

		return t;
	}
	
	public static double minEditDistance3(String s1, String s2) {
		int len1 = s1.length();
		int len2 = s2.length();
		if (len1 == 0 && len2 == 0) {
			return 0.0;
		}
		if (len1 == 0 || len2 == 0) {
			return 0.0;
		}
		if (len1 + 1 > length) {
			d = new int[2][len1+1];
			length = len1 + 1;
		}
		for (int i = 0; i <= len1; ++i) {
			d[0][i] = i;
		}
		int flag = 1;
		for (int i = 1; i <= len2; ++i) {
			d[flag][0] = i;
			for (int j = 1; j <= len1; ++j) {
				int last = flag^1;
				int d1 = d[last][j]+1;
				int d2 = d[flag][j-1]+1;
				int d3 = d[last][j-1];
				if (s1.charAt(j-1) != s2.charAt(i-1)) {
					++d3;
				}
				d[flag][j] = d1 > d2 ? d2 : d1;
				d[flag][j] = d[flag][j] > d3 ? d3 : d[flag][j];
			}
			flag = flag^1;
		}

		int min_len = len1 < len2 ? len1 : len2;
		int max_len = len1 > len2 ? len1 : len2;
		int diff = Math.abs(len1-len2);
		double t = 1-((double)d[flag^1][len1]-diff)/(double)min_len;
		//System.out.println(t);
		
		if (t > 0.5) {
			t = t - diff/(double)max_len/2;
			if (min_len < 5 && max_len > 10) {
				t = t - 0.1;
			}
			if (t <= 0.5) {
				t = 0.51;
			}
		}
		return t;
	}
	
	public static int LCS(String s1, String s2) {
//		System.out.println("s1:  "+s1);
//		System.out.println("s2:  "+s2);
		int m = s1.length();
		int n = s2.length();
		if (m <= 0 || n <= 0) {
			return 0;
		}
		Integer [][] lcs = new Integer[2][n+1];
		for (int i = 0; i <=1; ++i) {
			lcs[i][0] = 0;
		}
		for (int i = 0; i <=n; ++i) {
			lcs[0][i] = 0;
		}
		int flag = 1;
		for (int i = 1; i <=m; ++i)	{
			int index = flag^0x01;
			for (int j = 1; j <=n; ++j) {
				if (s1.charAt(i-1) == s2.charAt(j-1)) {
					lcs[flag][j] = lcs[index][j-1] + 1;
				} else {
					int t1 = lcs[index][j];
					int t2 = lcs[flag][j-1];
					lcs[flag][j] = t1 > t2? t1: t2;
				}
			}
			flag = index;
		}
		return lcs[flag^0x01][n];
	}
	
	public static void main(String[] args) {
		String s1 = "《我的世界》1.6.4永恒之冰霜世界mod";
		String s2 = "我的世界1.6.4永恒之冰霜世界mod_我的";
		int comm = PageSimilarityCalculator.LCS(s1, s2);
		System.out.println(comm);
	}

}
