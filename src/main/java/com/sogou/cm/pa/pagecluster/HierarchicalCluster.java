package com.sogou.cm.pa.pagecluster;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class HierarchicalCluster {

	ArrayList<ArrayList<HtmlPage> > data_t;
	double[][] dist;
	Random random = new Random();
	
	HierarchicalCluster() {
	}
	
	int cluster(ArrayList<HtmlPage> data) {
		dist = new double[data.size()][data.size()];
		int num = data.size();
		for (int i = 0; i < num; ++i) {
			for (int j = 0; j < i; ++j) {
				dist[i][j] = data.get(i).getDistance(data.get(j));
			//	System.out.print((int)(dist[i][j]*1000) + " ");
			}
		//	System.out.print("\n");
		}
		data_t = new ArrayList<ArrayList<HtmlPage> >();
		for (int i = 0; i < data.size(); ++i) {
			ArrayList<HtmlPage> temp = new ArrayList<HtmlPage>();
			temp.add(data.get(i));
			data_t.add(temp);
		}
		int i = data.size();
		for (; i >= 2; --i) {
			if (i%10 == 0) {
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
			if (dist[minx][miny] > 0.5) {
				break;
			}
			
			System.out.println("min dist: " + dist[minx][miny]);
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
				distance += s1.get(k).getDistance(s2.get(t));
			}
		}
		return distance/s1.size()/s2.size();
	}

}
