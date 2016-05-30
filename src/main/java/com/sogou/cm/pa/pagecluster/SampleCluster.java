package com.sogou.cm.pa.pagecluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

class ClusterQuota {
	String key;
	long quota;
	long start;
	long num;
	ArrayList<String> urls;
	ClusterQuota() {
		key = "";
		quota = 0;
		start = 0;
		num = 0;
		urls = new ArrayList<String>();
	}
}

public class SampleCluster {
	
	static int getSampleNum(long num) {
		if (num > 1000000) {
			return 10000;
		} else if (num > 100000) {
			return 5000;
		} else if (num > 10000) {
			return 2000;
		} else {
			return 1000;
		}
	}

	
	public static void main(String[] args) throws Exception {
		HashMap<String, Long> site2info = new HashMap<String, Long>();
		
		
		BufferedReader reader2 = new BufferedReader(new FileReader(new File("site_info.txt")));
		String line2;
		while ((line2 = reader2.readLine()) != null) {
			String[] segs = line2.split("\t");
			if (segs.length != 2) {
				continue;
			}
			site2info.put("site\t"+segs[0], new Long(segs[1]));
		}
		reader2.close();
		reader2 = new BufferedReader(new FileReader(new File("domain_info.txt")));
		while ((line2 = reader2.readLine()) != null) {
			String[] segs = line2.split("\t");
			if (segs.length != 2) {
				continue;
			}
			site2info.put("domain\t"+segs[0], new Long(segs[1]));
		}
		reader2.close();
		
		ArrayList<ClusterQuota> cqs = new ArrayList<ClusterQuota>();
		BufferedReader reader = new BufferedReader(new FileReader(new File("predictor_result.txt")));
		String line;
		
		while ((line = reader.readLine()) != null) {
			String[] segs = line.split("\t");
			if (segs.length < 3) {
				continue;
			}
			String key = segs[0] + "\t" + segs[1];
		//	System.out.println(key);
			if (!site2info.containsKey(key)) {
			//	System.out.println("here.");
				continue;
			}
			long site_num = site2info.get(key);
			int site_sample_num = getSampleNum(site_num);
			cqs.clear();
			ClusterQuota last_cq = null;
			for (int i = 2; i < segs.length; ++i) {
				String[] segs2 = segs[i].split(" ");
				if (segs2[0].equals("null") || segs2.length < 8) {
					continue;
				}
				ClusterQuota cq = new ClusterQuota();
				cq.key = segs2[0] + " " + segs2[1] + " " + segs2[2];
				cq.quota = segs2.length - 3;
				cq.num = cq.quota*site_num/site_sample_num;
				if (last_cq == null) {
					cq.start = 0;
				} else {
					cq.start = last_cq.start + last_cq.quota;
				}
				last_cq = cq;
				Random random = new Random();
				int sample_num = 10;
				int cnt = 0;
				for (int j = 3; j < segs2.length; ++j) {
					++cnt;
					if (cnt <= sample_num) {
						cq.urls.add(segs2[j]);
					} else {
						int index = random.nextInt(cnt);
						if (index < sample_num) {
							cq.urls.set(index, segs2[j]);
						}
					}
				}
				cqs.add(cq);
			}
			if (last_cq == null) {
				continue;
			}
			long size = last_cq.start + last_cq.quota;
			Random ran = new Random();
			HashSet<String> sample_result = new HashSet<String>();
			int sample_num = 3;
			if (cqs.size() < 3) {
				sample_num = cqs.size();
			}
			while (sample_result.size() < sample_num) {
				long n = ran.nextLong();
				if (n < 0) {
					n = n*-1;
				}
				n = n%size;
				int l = 0, r = cqs.size()-1;
				int find = -1;
				while (l <= r) {
					int mid = (l+r)>>1;
					ClusterQuota temp = cqs.get(mid);
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
				sample_result.add(cqs.get(find).key);
			}
			for (ClusterQuota cq: cqs) {
				if (sample_result.contains(cq.key)) {
					System.out.print(cq.key + "\t" + cq.num);
					for (int k = 0; k < cq.urls.size(); ++k) {
						System.out.print("\t" + cq.urls.get(k));
					}
					System.out.print("\n");
				}
			}
		}
	}
}
