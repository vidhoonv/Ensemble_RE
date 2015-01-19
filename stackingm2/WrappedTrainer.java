package stackingm2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WrappedTrainer {

	/**
	 * @param args
	 */
	
	String trainDir,testDir,trainPrefix,testPrefix,fileExt;
	Set<String> perSlots = new HashSet<String>();
	Set<String> orgSlots = new HashSet<String>();
	
	
	public WrappedTrainer(String traindir, String testdir, String trainpfix, String testpfix,String fext){
		
		trainDir = new String(traindir);
		testDir = new String(testdir);
		trainPrefix = new String(trainpfix);
		testPrefix = new String(testpfix);
		fileExt = new String(fext);
		
		//add per slots
		perSlots.add("per:alternate_names");
		//perSlots.add("per:date_of_birth");
		perSlots.add("per:age");
		perSlots.add("per:country_of_birth");
		perSlots.add("per:stateorprovince_of_birth");
		
		perSlots.add("per:city_of_birth");
		perSlots.add("per:origin");
		perSlots.add("per:date_of_death");
		perSlots.add("per:country_of_death");
		perSlots.add("per:stateorprovince_of_death");
		
		perSlots.add("per:city_of_death");
		perSlots.add("per:cause_of_death");
		perSlots.add("per:countries_of_residence");
		perSlots.add("per:statesorprovinces_of_residence");
		perSlots.add("per:cities_of_residence");
		
		perSlots.add("per:schools_attended");
		perSlots.add("per:title");
		//perSlots.add("per:employee_or_member_of");
		perSlots.add("per:religion");
		perSlots.add("per:spouse");
		
		perSlots.add("per:children");
		perSlots.add("per:parents");
		perSlots.add("per:siblings");
		//perSlots.add("per:other_family");
		perSlots.add("per:charges");
		
		
		//add org slots
		orgSlots.add("org:alternate_names");
		orgSlots.add("org:political_religious_affiliation");
		orgSlots.add("org:top_members_employees");
		orgSlots.add("org:number_of_employees_members");
		//orgSlots.add("org:members");
		
		orgSlots.add("org:member_of");
		orgSlots.add("org:subsidiaries");
		//orgSlots.add("org:parents");
		orgSlots.add("org:founded_by");
		orgSlots.add("org:date_founded");
		
		orgSlots.add("org:date_dissolved");
		orgSlots.add("org:country_of_headquarters");
		orgSlots.add("org:stateorprovince_of_headquarters");
		orgSlots.add("org:city_of_headquarters");
		orgSlots.add("org:shareholders");
		orgSlots.add("org:website");
	
	}
	
	public void buildClassifierForAllSlots() throws Exception{
		int num_succ=0;
		for(String slotType : perSlots){
			String inTrainDataFile = new String(trainDir+trainPrefix+slotType+fileExt);
			String inTestDataFile = new String(testDir+testPrefix+slotType+fileExt);
			String trainDataFile = new String(trainDir+trainPrefix+slotType+".arff");
			String testDataFile = new String(testDir+testPrefix+slotType+".arff");
			String testPredictionsFile = new String("/home/vidhoonv/workspace/RE_ensemble/predictions/"+testPrefix+slotType+".txt");
			
			System.out.println(inTrainDataFile);
//			System.out.println(inTestDataFile);
//			System.out.println(trainDataFile);
//			System.out.println(testDataFile);
			StackedClassifier sc = new StackedClassifier(inTrainDataFile,trainDataFile,inTestDataFile,testDataFile,testPredictionsFile);
			sc.preprocessData();
			if(sc.isTrainingDataEmpty || sc.isTestingDataEmpty){
				System.out.println("No data for "+slotType);
			}
			else{
				sc.buildClassifier();
				num_succ++;
			}
			
		}
		
		
		for(String slotType : orgSlots){
			String inTrainDataFile = new String(trainDir+trainPrefix+slotType+fileExt);
			String inTestDataFile = new String(testDir+testPrefix+slotType+fileExt);
			String trainDataFile = new String(trainDir+trainPrefix+slotType+".arff");
			String testDataFile = new String(testDir+testPrefix+slotType+".arff");
			String testPredictionsFile = new String("/home/vidhoonv/workspace/RE_ensemble/predictions/"+testPrefix+slotType+".txt");
			
			System.out.println(inTrainDataFile);
//			System.out.println(inTestDataFile);
//			System.out.println(trainDataFile);
//			System.out.println(testDataFile);
			StackedClassifier sc = new StackedClassifier(inTrainDataFile,trainDataFile,inTestDataFile,testDataFile,testPredictionsFile);
			sc.preprocessData();
			if(sc.isTrainingDataEmpty || sc.isTestingDataEmpty){
				System.out.println("No data for "+slotType);
			}
			else{
				sc.buildClassifier();
				num_succ++;
			}
			
		}
		
		System.out.println("Number of models built : "+num_succ);
	}
	
	/*
	 * args[0] - training data files location directory path for all slots
	 * args[1] - testing data files location directory path for all slots
	 * args[2] - training data files prefix
	 * args[3] - testing data files prefix
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String fileSuffix = new String(".txt"); //default assumption
		String trainPrefix = new String("2013-");
		String testPrefix = new String("2014-");
		
		String trainDir = new String("/home/vidhoonv/workspace/RE_ensemble/run_out_2013/unique/");
		String testDir = new String("/home/vidhoonv/workspace/RE_ensemble/run_out_2014/unique/");
		
		WrappedTrainer wt = new WrappedTrainer(trainDir,testDir,trainPrefix,testPrefix,fileSuffix);
		wt.buildClassifierForAllSlots();
		
	}

}
