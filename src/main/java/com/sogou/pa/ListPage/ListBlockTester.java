package com.sogou.pa.ListPage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class ListBlockTester {
	public static void main(String[] args) throws Exception {
		String model_path = "list_block_cv.model";
		Classifier cls_;
		cls_ = (Classifier) SerializationHelper.read(model_path);
		Remove rm = new Remove();
		rm.setAttributeIndices("1,2,3");
		Instances data_set_;
		data_set_ = DataSource.read("list_block2.arff");
		data_set_.setClassIndex(data_set_.numAttributes() - 1);
		rm.setInputFormat(data_set_);
		Instances new_dataset = Filter.useFilter(data_set_, rm);
		Random random = new Random();
		HashSet<Integer> samples = new HashSet<Integer>();
		for (int i = 0; i < 20; i++) {
			int ran = random.nextInt(172);
			while (samples.contains(ran)) {
				ran = random.nextInt(172);
			}
			samples.add(ran);
		}
		int err = 0;
		int all = 0;
		for (int i = 0; i < new_dataset.numInstances(); i++) {
			try {
				Instance instance = new_dataset.instance(i);
				double label = cls_.classifyInstance(instance);
			//	if (label == 1) {
					all++;
			//	}
				if (label != instance.classValue()) {
					String[] segs = data_set_.instance(i).toString().split(",");
					/*
					Instance ins = data_set_.instance(i);
					for (int j = 0; j < ins.numAttributes(); j++) {
						System.out.print(ins.attribute(j).name() + "\t");
					}
					System.out.println();
					return;
					*/
					/*
					if (Math.tan(Double.valueOf(segs[5])) < 100 && Math.tan(Double.valueOf(segs[8])) >= 800 && Math.tan(Double.valueOf(segs[7])) >= 100) {
						System.out.println(segs[1] + "\t" + segs[2] + "\t" + segs[3] + "\t" + segs[segs.length-1]);
						
					}
					*/
				//	if (samples.contains(err)) {
				//		System.out.println();
				//		System.out.println(segs[1] + "\t" + segs[2] + "\t" + segs[3] + "\t" + segs[segs.length-1]);
				//	}
			//		System.out.println("adfs  " + data_set_.instance(i).toString());
					err++;
				} else if (label == 0) {
					System.out.println(data_set_.instance(i).toString());
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		System.out.println(all + "\t" + err + "\t" + (double)err/all);
	}
}
