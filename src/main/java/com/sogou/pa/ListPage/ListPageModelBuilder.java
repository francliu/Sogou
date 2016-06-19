package com.sogou.pa.ListPage;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Utils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.classifiers.trees.RandomForest;

import java.util.Random;

/**
 * Performs a single run of cross-validation.
 *
 * Command-line parameters:
 * <ul>
 *    <li>-t filename - the dataset to use</li>
 *    <li>-x int - the number of folds to use</li>
 *    <li>-s int - the seed for the random number generator</li>
 *    <li>-c int - the class index, "first" and "last" are accepted as well;
 *    "last" is used by default</li>
 *    <li>-W classifier - classname and options, enclosed by double quotes; 
 *    the classifier to cross-validate</li>
 * </ul>
 *
 * Example command-line:
 * <pre>
 * java CrossValidationSingleRun -t anneal.arff -c last -x 10 -s 1 -W "weka.classifiers.trees.J48 -C 0.25"
 * </pre>
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ListPageModelBuilder {

  /**
   * Performs the cross-validation. See Javadoc of class for information
   * on command-line parameters.
   *
   * @param args        the command-line parameters
   * @throws Excecption if something goes wrong
   */
  public static void main(String[] args) throws Exception {
    // loads data and set class index
	Remove rm = new Remove();
	rm.setAttributeIndices("1,2,3");
	Instances data_set_;
	data_set_ = DataSource.read("list_blockTrain.arff");
	data_set_.setClassIndex(data_set_.numAttributes() - 1);
	rm.setInputFormat(data_set_);
	Instances data = Filter.useFilter(data_set_, rm);

    // classifier
    String[] tmpOptions = {"-I", "30"};

    
    Classifier cls = new RandomForest();//(Classifier) Utils.forName(Classifier.class, "RandomForest", tmpOptions);
    cls.setOptions(tmpOptions);
    // other options
    int seed  = 0;
    int folds = 10;

    // randomize data
    Random rand = new Random(seed);
    Instances randData = new Instances(data);
    randData.randomize(rand);
    if (randData.classAttribute().isNominal())
      randData.stratify(folds);

    // perform cross-validation
    Classifier best = null;
    double best_rate = 1;
    for (int n = 0; n < folds; n++) {
      Instances train = randData.trainCV(folds, n);
      Instances test = randData.testCV(folds, n);
      // the above code is used by the StratifiedRemoveFolds filter, the
      // code below by the Explorer/Experimenter:
      // Instances train = randData.trainCV(folds, n, rand);

      // build and evaluate classifier
      Classifier clsCopy = Classifier.makeCopy(cls);
      clsCopy.buildClassifier(train);
      Evaluation eval = new Evaluation(randData);
      eval.evaluateModel(clsCopy, test);
      if (eval.errorRate() < best_rate) {
    	  best = clsCopy;
    	  best_rate = eval.errorRate();
      }
      System.out.println(eval.errorRate());
    }
    System.out.println("best: " + best_rate);
    SerializationHelper.write("list_block_cv.model", best);

    // output evaluation
    System.out.println();
    System.out.println("=== Setup ===");
    System.out.println("Classifier: " + cls.getClass().getName() + " " + Utils.joinOptions(cls.getOptions()));
    System.out.println("Dataset: " + data.relationName());
    System.out.println("Folds: " + folds);
    System.out.println("Seed: " + seed);
    System.out.println();
  //  System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));
  }
}