package com.sogou.pa.ListPage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class CorrectData {
	public static void main(String[] args) throws Exception {
		Instances data_set_;
		data_set_ = DataSource.read("list_block1.arff");
		data_set_.setClassIndex(data_set_.numAttributes() - 1);
		BufferedReader reader = new BufferedReader(new FileReader(new File("correct_data2.txt")));
		HashSet<String> err_set = new HashSet<String>();
		HashSet<String> remove_set = new HashSet<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			String[] segs = line.split("\t");
			if (segs.length == 5) {
				if (segs[4].equals("1")) {
					err_set.add(segs[0]+" "+segs[1]);
				} else if (segs[4].equals("2")) {
					remove_set.add(segs[0]+" "+segs[1]);
				}
			}
		}
		reader.close();
		for (int i = 0; i < data_set_.numInstances(); i++) {
			Instance ins = data_set_.instance(i);
			String key = (int) (ins.value(1)) + " " + ins.stringValue(2) ;
		//	System.out.println(key);
			if (err_set.contains(key)) {
		//		System.out.println(key);
				int val = (int) ins.value(ins.numAttributes()-1);
				if (val == 0)
					ins.setValue(ins.numAttributes()-1, 1);
				else
					ins.setValue(ins.numAttributes()-1, 0);
			}
			if (remove_set.contains(key)) {
				data_set_.delete(i);
			}
		}
		System.out.println(data_set_.toString());
	}
}
