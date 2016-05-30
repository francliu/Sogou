package com.sogou.cm.pa.maincontent;

import java.util.List;

public class Tool {
	
	public static final int partitiony(List<Element> ads, int left, int right) {
		int i = left;
		int base = ads.get(right).top;
		for (int j = left; j < right; ++j) {
			if (ads.get(j).top < base) {
				Element e = ads.get(i);
				ads.set(i, ads.get(j));
				ads.set(j, e);
				++i;
			}
		}
		Element e = ads.get(i);
		ads.set(i, ads.get(right));
		ads.set(right, e);
		return i;
	}
	
	public static final int partitionx(List<Element> ads, int left, int right) {
		int i = left;
		int base = ads.get(right).left;
		for (int j = left; j < right; ++j) {
			if (ads.get(j).left < base) {
				Element e = ads.get(i);
				ads.set(i, ads.get(j));
				ads.set(j, e);
				++i;
			}
		}
		Element e = ads.get(i);
		ads.set(i, ads.get(right));
		ads.set(right, e);
		return i;
	}
	
	public static final void quickSortX(List<Element> ads, int left, int right) {
		if (right <= left) {
			return;
		}
		int i = partitionx(ads, left, right);
		quickSortX(ads, left, i - 1);
		quickSortX(ads, i+1, right);
	}
	
	public static final void quickSortY(List<Element> ads, int left, int right) {
		if (right <= left) {
			return;
		}
		int i = partitiony(ads, left, right);
		quickSortY(ads, left, i - 1);
		quickSortY(ads, i+1, right);
	}

}
