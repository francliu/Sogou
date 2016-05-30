package com.sogou.cm.pa.pagecluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
class SiteQuota {
	String site;
	long quota;
	long start;
	SiteQuota() {
		site = "";
		quota = 0;
		start = 0;
	}
}

public class SampleSite {

	public static void main(String[] args) throws Exception {
		ArrayList<SiteQuota> site_quota = new ArrayList<SiteQuota>();
		long t = 0;
		HashMap<String, Long> site2info	 = new HashMap<String, Long>();
		BufferedReader reader = new BufferedReader(new FileReader(new File("domain_info.txt")));
		
		String line;
		SiteQuota last_sq = null;
		while ((line = reader.readLine()) != null) {
			String[] segs = line.split("\t");
			if (segs.length == 2) {
				SiteQuota sq = new SiteQuota();
				sq.site = "domain\t" + segs[0];
				sq.quota = new Long(segs[1]);
				if (last_sq == null) {
					sq.start = 0;
				} else {
					sq.start = last_sq.start + last_sq.quota;
				}
				site_quota.add(sq);
				last_sq = sq;
			}
		}
		reader.close();
		
		BufferedReader reader2 = new BufferedReader(new FileReader(new File("site_info.txt")));
		
		while ((line = reader2.readLine()) != null) {
			String[] segs = line.split("\t");
			if (segs.length == 2) {
				SiteQuota sq = new SiteQuota();
				sq.site = "site\t" + segs[0];
				sq.quota = new Long(segs[1]);
				if (last_sq == null) {
					sq.start = 0;
				} else {
					sq.start = last_sq.start + last_sq.quota;
				}
				site_quota.add(sq);
				last_sq = sq;
			}
		}
		reader2.close();
		System.out.println("read finish.");
		/*
		for (SiteQuota ss: site_quota) {
			System.out.println(ss.site + "\t" + ss.start + "\t" + ss.quota);
		}
		*/
		long size = last_sq.start + last_sq.quota;
		Random ran = new Random();
		HashSet<String> sample_result = new HashSet<String>();
		int sample_num = 100;
		while (sample_result.size() < sample_num) {
			long n = ran.nextLong();
			if (n < 0) {
				n = n*-1;
			}
			n = n%size;
			int l = 0, r = site_quota.size()-1;
			int find = -1;
			while (l <= r) {
				int mid = (l+r)>>1;
				SiteQuota temp = site_quota.get(mid);
				if (temp.start <= n && temp.start+temp.quota > n) {
					find = mid;
					break;
				} else if (n < temp.start) {
					r = mid-1;
				} else {
					l = mid+1;
				}
			}
			if (find == -1) {
				System.out.println("here: " + n + "\t" + l + "\t" + r);
				continue;
			}
		//	System.out.println(site_quota.get(find).site);
			sample_result.add(site_quota.get(find).site);
		}
		for (String site: sample_result) {
			System.out.println(site);
		}
		
	}
}
