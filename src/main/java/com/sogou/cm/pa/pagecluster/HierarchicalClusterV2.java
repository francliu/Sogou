package com.sogou.cm.pa.pagecluster;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import org.apache.hadoop.mapreduce.Reducer.Context;

class XpathStat {
	int count;
	int above_limit_count;
	int all_len;
	int all_anchor_len;
	float average_before_max_node_len;
	float variance;
	XpathStat() {
		count = 0;
		above_limit_count = 0;
		all_len = 0;
		all_anchor_len = 0;
		average_before_max_node_len = 0;
		variance = 0;
	}
}

public class HierarchicalClusterV2 {

	ArrayList<ArrayList<HtmlPage> > data_t;
	double[][] dist_init;
	double[][] dist;
	Random random = new Random();
	HashMap<String, HashMap<Long, Integer>> xpath_count = new HashMap<String, HashMap<Long, Integer>>();
	HashMap<String, XpathStat> xpath_stat = new HashMap<String, XpathStat>();
	HashSet<String> sidebar_xpath = new HashSet<String>();
	Context context = null;
	
	HierarchicalClusterV2() {
	}
	
	public static String getMemStat() {
		 SimpleDateFormat STANDARD_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 
	  Runtime runtime = Runtime.getRuntime();
	Date date = new Date(System.currentTimeMillis());
				StringBuilder sb = new StringBuilder(1024);
				sb.append("[").append(STANDARD_FORMAT.format(date)).append("]");
				sb.append("[").append(runtime.totalMemory() >> 20).append(":").append(runtime.freeMemory() >> 20)
						.append("] ");
				sb.append("");
				return (sb.toString());
	}
	
	void getSideBarXpath(ArrayList<HtmlPage> data) {
		xpath_count.clear();
		xpath_stat.clear();
		sidebar_xpath.clear();
		for (HtmlPage hp: data) {
			Iterator iter = hp.xpath2info.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String xpath = (String) entry.getKey();
				XpathInfo xi = (XpathInfo) entry.getValue();
				HashMap<Long, Integer> code2count = xpath_count.get(xpath);
				if (code2count != null) {
					Integer count = code2count.get(xi.hash);
					if (count != null) {
						code2count.put(xi.hash, count+1);
					} else {
						code2count.put(xi.hash, 1);
					}
				} else {
					code2count = new HashMap<Long, Integer>();
					code2count.put(xi.hash, 1);
					xpath_count.put(xpath, code2count);
				}
				
				XpathStat xs = xpath_stat.get(xpath);
				if (xs != null) {
					xs.count++;
					xs.all_len += xi.num;
					xs.all_anchor_len += xi.anchor_len;
					xs.average_before_max_node_len += xi.before_max_node_len;
					xs.variance += xi.before_max_node_len*xi.before_max_node_len;
					if (xi.before_max_node_len > 200) {
						xs.above_limit_count++;
					}
				} else {
					xs = new XpathStat();
					xs.count = 1;
					xs.all_len = xi.num;
					xs.all_anchor_len = xi.anchor_len;
					xs.average_before_max_node_len = xi.before_max_node_len;
					xs.variance = xi.before_max_node_len*xi.before_max_node_len;
					if (xi.before_max_node_len > 200) {
						xs.above_limit_count++;
					}
					xpath_stat.put(xpath, xs);
				}
			}
		}
		
		Iterator iter = xpath_count.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String xpath = (String) entry.getKey();
			HashMap<Long, Integer> code2count = (HashMap<Long, Integer>) entry.getValue();
			int max_count = 0;
			int all_count = 0;
			int uniq_count = code2count.size();
			for (Integer n: code2count.values()) {
				all_count += n;
				if (max_count < n) {
					max_count = n;
				}
			}
			if (uniq_count*10<all_count || (uniq_count == 1 && all_count >= 5)) {
				sidebar_xpath.add(xpath);
			}
	//		 System.out.println(xpath + "\t" + max_count + "\t" + all_count + "\t" + uniq_count);
		}
		
		iter = xpath_stat.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String xpath = (String) entry.getKey();
			XpathStat xs = (XpathStat) entry.getValue();
			if (xs.all_len < 10) {
				continue;
			}
			float anchor_ratio = xs.all_anchor_len/(float)xs.all_len;
			if (anchor_ratio > 0.7 && xs.average_before_max_node_len/xs.count > 5) {
				xs.variance = xs.variance*xs.count/(float)xs.average_before_max_node_len/xs.average_before_max_node_len - 1;
				if (xs.variance > 3 || xs.above_limit_count/(float)xs.count > 0.1) {
					sidebar_xpath.add(xpath);
			//		System.out.println(xpath + "\t" + anchor_ratio + "\t" +  xs.above_limit_count/(float)xs.count + "\t" + xs.variance);				
				}
			}
//			System.out.println(xpath + "\t" + anchor_ratio + "\t" +  xs.above_limit_count/(float)xs.count + "\t" + xs.variance);				

		}
		
		for (HtmlPage hp: data) {
			Iterator iter2 = hp.xpath2info.entrySet().iterator();
			while (iter2.hasNext()) {
				Map.Entry entry = (Map.Entry) iter2.next();
				String xpath = (String) entry.getKey();
				XpathInfo xi = (XpathInfo) entry.getValue();
				if (sidebar_xpath.contains(xpath)) {
			//		if (xpath.equals("html/body/div[2]/div(g-sidebar g-sidebar-init)/div(g-side-menu)/ul/li/")) {
			//			System.out.println("here");
			//		}
					xi.is_sidebar = true;
				}
			}
		}
		
	}
	
	void initCluster(ArrayList<HtmlPage> data) {
		getSideBarXpath(data);
		dist_init = new double[data.size()][data.size()];
		for (int i = 0; i < data.size(); ++i) {
			data.get(i).index = i;
		}
		System.err.println("1: " + getMemStat());
		if (context != null) {
			context.progress();
		}
		int num = data.size();
		for (int i = 0; i < num; ++i) {
			if (i%1000 == 0 && context != null) {
				System.err.println("11: " + getMemStat());
				context.progress();
			}
			for (int j = 0; j < i; ++j) {
				dist_init[i][j] = data.get(i).getDistance(data.get(j));
				dist_init[j][i] = dist_init[i][j];
			}

		//	System.out.print("\n");
		}
		System.err.println("2: " + getMemStat());
		if (context != null) {
			context.progress();
		}

		Integer[] cluster_index = new Integer[data.size()];
		for (int i = 0; i < cluster_index.length; ++i) {
			cluster_index[i] = -1;
		}
		for (int i = 0; i < cluster_index.length; ++i) {
			initClusterInternal(cluster_index, i, 0.0);
		}
		HashSet<Integer> init_cluster_num = new HashSet<Integer>();
		for (int i = 0 ; i < cluster_index.length; ++i) {
			init_cluster_num.add(cluster_index[i]);
		}
		System.err.println("3: " + getMemStat());
		if (context != null) {
			context.progress();
		}
		System.err.println("init cluster num: " + init_cluster_num.size());
		if (init_cluster_num.size() > 10000) {
			for (int i = 0; i < cluster_index.length; ++i) {
				cluster_index[i] = -1;
			}
			for (int i = 0; i < cluster_index.length; ++i) {
				initClusterInternal(cluster_index, i, 0.1);
			}
		}
		System.err.println("4: " + getMemStat());
		if (context != null) {
			context.progress();
		}
		HashMap<Integer, ArrayList<HtmlPage>> clusterindex2data = new HashMap<Integer, ArrayList<HtmlPage>>(); 
		for (int i = 0; i < cluster_index.length; ++i) {
			int index = cluster_index[i];
			if (clusterindex2data.containsKey(index)) {
				clusterindex2data.get(index).add(data.get(i));
			} else {
				ArrayList<HtmlPage> temp = new ArrayList<HtmlPage>();
				temp.add(data.get(i));
				clusterindex2data.put(index, temp);
			}
		}
		data_t = new ArrayList<ArrayList<HtmlPage> >();
		for (ArrayList<HtmlPage> val: clusterindex2data.values()) {
			data_t.add(val);
		}
		System.err.println("5: " + getMemStat());
		System.err.println(data_t.size());
		if (context != null) {
			context.progress();
		}
		dist = new double[data_t.size()][data_t.size()];
		for (int i = 0; i < data_t.size(); ++i) {
			if (i%10 == 0 && context != null) {
				System.err.println("55: " + getMemStat());
				context.progress();
			}
			for (int j = 0; j < i; ++j) {
				dist[i][j] = getDistance(i, j);
		//		dist[j][i] = dist[i][j];
			}
		}
		System.err.println("6: " + getMemStat());
	}
	
	void initClusterInternal(Integer[] cluster_index, int n, double parm) {
		if (cluster_index[n] == -1) {
			cluster_index[n] = n;
		}
		for (int i = 0; i < cluster_index.length; i++) {
			if (dist_init[n][i] <= parm) {
				if (cluster_index[n] != cluster_index[i]) {
					cluster_index[i] = cluster_index[n];
					initClusterInternal(cluster_index, i, parm);
				}
			}
		}
	}
	
	int cluster(ArrayList<HtmlPage> data, Context context_input) {
		this.context = context_input;
		return cluster(data);
	}
	
	int cluster(ArrayList<HtmlPage> data) {
		initCluster(data);
		//context.progress();
		int i = data_t.size();
		Double[] cluster_size = new Double[data_t.size()];
		for (int k = 0; k < cluster_size.length; ++k) {
			cluster_size[k] = 0.0;
		}
		System.out.println("init size:  " + i);
		double last_min_dist = 1.0;
		System.err.println("6: " + getMemStat());
		for (; i >= 2; --i) {
			if (i%10 == 0) {
				if (context != null) {
					context.progress();
				}
				System.out.println("process: " + i);
			}
			/*
			int jj = 0;
			for (int ii = 0; ii < i; ++ii) {
				System.out.print(data_t.get(ii).size() + " ");
				jj += data_t.get(ii).size();
			}
			System.out.print("\n");
			System.out.println(jj);
			*/
			int minx = 1, miny = 0;
			int len = i;
			for (int k = 0; k < len; ++k) {
				for (int t = 0; t < k; ++t) {
			//		System.out.println(k + " " + t);
					if (dist[k][t] < dist[minx][miny]) {
						minx = k;
						miny = t;
					}
				}
			}
			System.out.println("min dist: " + dist[minx][miny] + "\t" + cluster_size[minx] + "\t" + cluster_size[miny]);
	//		if (dist[minx][miny] > 0.5 || (i < 25 && dist[minx][miny] > 0.4) || (dist[minx][miny] > 0.3 && dist[minx][miny]  - last_min_dist > 0.16)) {
		//	if (dist[minx][miny] > 0.5 || (i < 10 && dist[minx][miny] > 0.3)) {
			if (dist[minx][miny] > 0.5) {
				break;
			}
			
			/*
			if (dist[minx][miny] > 0.4) {
				System.out.println("x");
				ArrayList<HtmlPage> temp = data_t.get(minx);
				for (int k = 0; k < temp.size(); ++k) {
					System.out.println(temp.get(k).url);
				}
				System.out.println("y");
				temp = data_t.get(miny);
				for (int k = 0; k < temp.size(); ++k) {
					System.out.println(temp.get(k).url);
				}
				System.out.println("");
			}
			*/
			
			last_min_dist = dist[minx][miny];
			cluster_size[minx] = dist[minx][miny];
			data_t.get(minx).addAll(data_t.get(miny));
			data_t.set(miny, data_t.get(len-1));
			for (int j = 0; j < len; ++j) {
				if (j < miny) {
					dist[miny][j] = dist[len-1][j];
				} else {
					dist[j][miny] = dist[len-1][j];
				}
			}
			updateDistance(len-1, minx);
		}
		return i;
		
	}
	
	void updateDistance(int len, int index) {
		for (int i = 0; i < len; ++i) {
			if (i < index) {
				dist[index][i] = getDistance(index, i);
			} else if (i > index) {
				dist[i][index] = getDistance(index, i);
			}
		}
	}
	
	double getDistance(int i, int j) {
		ArrayList<HtmlPage> c1 = data_t.get(i);
		ArrayList<HtmlPage> c2 = data_t.get(j);
		int sample_num = 10;
		ArrayList<HtmlPage> s1 = new ArrayList<HtmlPage>();
		for (int k = 0; k < c1.size(); ++k) {
			if (k < sample_num) {
				s1.add(c1.get(k));
			} else {
				int pos = random.nextInt(k);
				if (pos < sample_num) {
					s1.set(pos, c1.get(k));
				}
			}
		}
		
		ArrayList<HtmlPage> s2 = new ArrayList<HtmlPage>();
		for (int k = 0; k < c2.size(); ++k) {
			if (k < sample_num) {
				s2.add(c2.get(k));
			} else {
				int pos = random.nextInt(k);
				if (pos < sample_num) {
					s2.set(pos, c2.get(k));
				}
			}
		}
		
		double distance = 0.0;
		for (int k = 0; k < s1.size(); ++k) {
			for (int t = 0; t < s2.size(); ++t) {
			//	distance += s1.get(k).getDistance(s2.get(t));
				distance += dist_init[s1.get(k).index][s2.get(t).index];
			}
		}
		return distance/s1.size()/s2.size();
	}

}
