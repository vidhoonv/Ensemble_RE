package stackingm2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Range;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;

public class StackedClassifier {

	/**
	 * @param args
	 * @throws Exception 
	 */
	
	String inTrainDataFile = null;
	String inTestDataFile = null;
	String trainDataFile = null;
	String testDataFile = null;
	
	String testPredictionsFile = null;
	
	Instances trainInstances=null,testInstances=null;
	Instances predictionInstances=null;
	AttributeSelectedClassifier classifier=null;
	boolean isTestingDataEmpty,isTrainingDataEmpty;
	
	public StackedClassifier(){
		inTrainDataFile = new String("/home/vidhoonv/workspace/RE_ensemble/weka_java_testing/2013.txt");
		inTestDataFile = new String("/home/vidhoonv/workspace/RE_ensemble/weka_java_testing/2014.txt");
		trainDataFile = new String("/home/vidhoonv/workspace/RE_ensemble/weka_java_testing/2013.arff");
		testDataFile = new String("/home/vidhoonv/workspace/RE_ensemble/weka_java_testing/2014.arff");
		testPredictionsFile = new String("/home/vidhoonv/workspace/RE_ensemble/weka_java_testing/2014-pred.csv");
		isTestingDataEmpty=false;
		isTrainingDataEmpty=false;
	}
	
	public StackedClassifier(String intrainfile, String trainfile, String intestfile, String testfile, String testoutfile){
		inTrainDataFile = intrainfile;
		inTestDataFile = intestfile;
		trainDataFile = trainfile;
		testDataFile = testfile;
		testPredictionsFile = testoutfile;
		isTestingDataEmpty=false;
		isTrainingDataEmpty=false;
	}
	
	public boolean loadCSVinput(String infile, String outfile, String year) throws Exception{
		String[] loaderOptions = new String[2];
		loaderOptions[0]="-F";
		loaderOptions[1]="\t";
		CSVLoader loader = new CSVLoader();
	    loader.setSource(new File(infile));
	    loader.setOptions(loaderOptions);
	    Instances data = loader.getDataSet();
	    
	    if(data.size()==0){
	    	return true;
	    }
	    //remove not needed attributes like run id, query id, slotfill etc.
	    Remove remove = new Remove();
	    if(year.equals("2013")){
	    	remove.setAttributeIndices("1,2,3,4,5,6,7,8");
	    }
	    else if(year.equals("2014")){
	    	remove.setAttributeIndices("1,2,3,4,5,6");
	    }
	    
	    remove.setInvertSelection(false);
	    remove.setInputFormat(data);
	    
	    Instances dataAttributes = Filter.useFilter(data, remove);	
	    // save ARFF
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(dataAttributes);
	    saver.setFile(new File(outfile));
	    saver.writeBatch();
	    
	    return false;
	}
	
	public void savePredictions(String outfile) throws Exception{
		String[] saverOptions = new String[2];
		saverOptions[0]="-F";
		saverOptions[1]="\t";
		CSVSaver saver = new CSVSaver();
		saver.setOptions(saverOptions);
		saver.setFile(new File(outfile));
		saver.setInstances(predictionInstances);
		saver.writeBatch();
		//System.out.println(saver.globalInfo());
	}
	public void loadPredictions(String infile, String outfile, String year) throws Exception{
		String[] loaderOptions = new String[2];
		loaderOptions[0]="-F";
		loaderOptions[1]="\t";
		CSVLoader loader = new CSVLoader();
		loader.setOptions(loaderOptions);
	    loader.setSource(new File(infile));
	    Instances data = loader.getDataSet();
	    
	    
	    //remove not needed attributes like run id, query id, slotfill etc.
	    Remove remove = new Remove();
	    if(year.equals("2014")){
	    	remove.setAttributeIndices("1,2,3,4,5,6,7,8,last");
	    }
	    remove.setInvertSelection(true);
	    remove.setInputFormat(data);
	    //System.out.println("num attributes :"+data.numAttributes());
	    
	    //remove feature attributes
	    predictionInstances = Filter.useFilter(data, remove);    
	   // System.out.println("num attributes :"+predictionInstances.numAttributes());
	    
	    //add predicted target attribute
	    Add addFilter = new Add();
	    addFilter.setAttributeIndex("9");
	    addFilter.setNominalLabels("0,2");
	    addFilter.setAttributeName("PredictedTarget");
	    addFilter.setInputFormat(predictionInstances);
        predictionInstances = Filter.useFilter(predictionInstances, addFilter);
        
        //convert target to nominal
        NumericToNominal nnfilter = new NumericToNominal();
		nnfilter.setAttributeIndices("last");
		nnfilter.setInputFormat(predictionInstances);
		predictionInstances = Filter.useFilter(predictionInstances,nnfilter);
	
	}
	
	public void preprocessData() throws Exception{
		
		//csvtoARFF conversion
		isTrainingDataEmpty = loadCSVinput(inTrainDataFile,trainDataFile,"2013");
		isTestingDataEmpty = loadCSVinput(inTestDataFile,testDataFile,"2014");
		
		if(isTrainingDataEmpty || isTestingDataEmpty){
			return;
		}
		//load predictions for writing predictions output
		loadPredictions(inTestDataFile,testPredictionsFile,"2014");
		
		//numericToNominal - BATCH
		DataSource trainSource = new DataSource(trainDataFile);
		Instances train = trainSource.getDataSet();
		DataSource testSource = new DataSource(testDataFile);
		Instances test = testSource.getDataSet();
		
		NumericToNominal nnfilter = new NumericToNominal();
		nnfilter.setAttributeIndices("45-last");
		nnfilter.setInputFormat(train);

		trainInstances = Filter.useFilter(train,nnfilter);
		testInstances = Filter.useFilter(test,nnfilter);
		
		trainInstances.setClassIndex(trainInstances.numAttributes()-1);
		testInstances.setClassIndex(testInstances.numAttributes()-1);

		
	}
	
	
	public void buildClassifier() throws Exception{
		//select attributes
		 classifier = new AttributeSelectedClassifier();
		 CfsSubsetEval eval = new CfsSubsetEval();
		 GreedyStepwise search = new GreedyStepwise();
		 search.setSearchBackwards(true);
		 
		 Logistic base = new Logistic();
		 classifier.setClassifier(base);
		 classifier.setEvaluator(eval);
		 classifier.setSearch(search);
		 
		 classifier.buildClassifier(trainInstances);

		 
		 // 10-fold cross-validation
		// Evaluation evaluation = new Evaluation(trainInstances);
		// evaluation.crossValidateModel(classifier, trainInstances, 10, new Random(1));
		 
		 System.out.println("Number of attributes selected : "+classifier.measureNumAttributesSelected());
		 Evaluation evals = new Evaluation(testInstances);
		 evals.evaluateModel(classifier, testInstances);		 
		 System.out.println(evals.toSummaryString("\nResults\n======\n", false));
		 
		 //add predicted target to predictions
		 //FastVector predictions = new FastVector();
		 
		 ArrayList<Prediction> predictions = evals.predictions();
		 int i=0;
		 for(Prediction p : predictions){
			 NominalPrediction np = (NominalPrediction) p;
			 
			 predictionInstances.instance(i).setValue(predictionInstances.numAttributes()-2, np.predicted());
			 i++;
			// System.out.println("actual: "+np.actual() + "\t pred: " + np.predicted());			 
		 }
		 
		
		//save predictions
		savePredictions(testPredictionsFile);    	
	}
	
	/*
	 * params:
	 * 
	 * args[0] - tab seperated file comprising of raw train instances
	 * args[1] - location to store ARFF training file
	 * args[2] - tab seperated file comprising of raw train instances
	 * args[3] - location to store ARFF testing file
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		StackedClassifier sc = new StackedClassifier();
		sc.preprocessData();
		if(sc.isTestingDataEmpty || sc.isTrainingDataEmpty){
			return;
		}
		else{
			sc.buildClassifier();
		}
		
		
	}

}
