package stackingm2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/*
 * DataExtractor class:
 * 
 * Extract training data for classifiers from 
 * output file of extractors and key file.
 * Can be configured to extract slot fills by
 * relation type or even group common and unique 
 * extractions. 
 * 
 */
public class DataExtractor {

	/**
	 * @param args
	 */
	
	
	scorer2013 s1_2013=new scorer2013();
	scorer2013 s2_2013= new scorer2013();
	
	scorer2014 s1_2014=new scorer2014();
	scorer2014 s2_2014= new scorer2014();
	
	Map<String,Integer> relationIDs = new HashMap<String,Integer>();
	Map<String,Integer> relationGroupIDs = new HashMap<String,Integer>();
	Set<String> singleValuedSlots= new HashSet<String>();
	
	//map to track slot type -> fillscount 
	Map<String,Integer> fillsCount = new HashMap<String,Integer>();
	public DataExtractor(){
	
		//insert relation IDs - an integer value for each relation type to use as feature for classification
		relationIDs.put("per:alternate_names",0);
		relationIDs.put("per:date_of_birth",1);
		relationIDs.put("per:age",2);
		relationIDs.put("per:country_of_birth",3);
		relationIDs.put("per:stateorprovince_of_birth",4);
		relationIDs.put("per:city_of_birth",5);
		relationIDs.put("per:origin",6);
		relationIDs.put("per:date_of_death",7);
		relationIDs.put("per:country_of_death",8);
		relationIDs.put("per:stateorprovince_of_death",9);
		relationIDs.put("per:city_of_death",10);
		relationIDs.put("per:cause_of_death",11);
		relationIDs.put("per:countries_of_residence",12);
		relationIDs.put("per:statesorprovinces_of_residence",13);
		relationIDs.put("per:cities_of_residence",14);
		relationIDs.put("per:schools_attended",15);
		relationIDs.put("per:title",16);
		relationIDs.put("per:employee_or_member_of",17);
		relationIDs.put("per:religion",18);
		relationIDs.put("per:spouse",19);
		relationIDs.put("per:children",20);
		relationIDs.put("per:parents",21);
		relationIDs.put("per:siblings",22);
		relationIDs.put("per:other_family",23);
		relationIDs.put("per:charges",24);
		relationIDs.put("org:alternate_names",25);
		relationIDs.put("org:political_religious_affiliation",26);
		relationIDs.put("org:top_members_employees",27);
		relationIDs.put("org:number_of_employees_members",28);
		relationIDs.put("org:members",29);
		relationIDs.put("org:member_of",30);
		relationIDs.put("org:subsidiaries",31);
		relationIDs.put("org:parents",32);
		relationIDs.put("org:founded_by",33);
		relationIDs.put("org:date_founded",34);
		relationIDs.put("org:date_dissolved",35);
		relationIDs.put("org:country_of_headquarters",36);
		relationIDs.put("org:stateorprovince_of_headquarters",37);
		relationIDs.put("org:city_of_headquarters",38);
		relationIDs.put("org:shareholders",39);
		relationIDs.put("org:website",40);
		
		//manually created groups of slots 
		relationGroupIDs.put("per:date_of_birth",1);
		relationGroupIDs.put("per:date_of_death",1);
		relationGroupIDs.put("org:date_founded",1);
		relationGroupIDs.put("org:date_dissolved",1);

		relationGroupIDs.put("per:country_of_birth",2);
		relationGroupIDs.put("per:stateorprovince_of_birth",2);
		relationGroupIDs.put("per:city_of_birth",2);
		relationGroupIDs.put("per:country_of_death",2);
		relationGroupIDs.put("per:stateorprovince_of_death",2);
		relationGroupIDs.put("per:city_of_death",2);
		relationGroupIDs.put("per:countries_of_residence",2);
		relationGroupIDs.put("per:statesorprovinces_of_residence",2);
		relationGroupIDs.put("per:cities_of_residence",2);
		relationGroupIDs.put("org:country_of_headquarters",2);
		relationGroupIDs.put("org:stateorprovince_of_headquarters",2);
		relationGroupIDs.put("org:city_of_headquarters",2);
		
		relationGroupIDs.put("per:alternate_names",3);
		relationGroupIDs.put("org:alternate_names",3);
		
		
		relationGroupIDs.put("per:spouse",4);
		relationGroupIDs.put("per:children",4);
		relationGroupIDs.put("per:parents",4);
		relationGroupIDs.put("per:siblings",4);
		relationGroupIDs.put("per:other_family",4);
		
		//a misc group for all other per slot types
		relationGroupIDs.put("per:age",5);
		relationGroupIDs.put("per:origin",5);
		relationGroupIDs.put("per:cause_of_death",5);
		relationGroupIDs.put("per:schools_attended",5);
		relationGroupIDs.put("per:religion",5);
		relationGroupIDs.put("per:charges",5);
		
		
		relationGroupIDs.put("per:title",6);
		
		relationGroupIDs.put("per:employee_or_member_of",7);
		
		relationGroupIDs.put("org:top_members_employees",8);
		
		
		relationGroupIDs.put("org:number_of_employees_members",9);
		relationGroupIDs.put("org:political_religious_affiliation",9);
		relationGroupIDs.put("org:members",9);
		relationGroupIDs.put("org:member_of",9);
		relationGroupIDs.put("org:subsidiaries",9);
		relationGroupIDs.put("org:parents",9);
		relationGroupIDs.put("org:founded_by",9);
		relationGroupIDs.put("org:shareholders",9);
		relationGroupIDs.put("org:website",9);
		
		
		singleValuedSlots.add("per:date_of_birth");
		singleValuedSlots.add("per:age");
		singleValuedSlots.add("per:country_of_birth");
		singleValuedSlots.add("per:stateorprovince_of_birth");
		singleValuedSlots.add("per:city_of_birth");
		singleValuedSlots.add("per:date_of_death");
		singleValuedSlots.add("per:country_of_death");
		singleValuedSlots.add("per:stateorprovince_of_death");
		singleValuedSlots.add("per:city_of_death");
		singleValuedSlots.add("per:cause_of_death");
		singleValuedSlots.add("per:religion");
		
		singleValuedSlots.add("org:number_of_employees_members");
		singleValuedSlots.add("org:website");
		singleValuedSlots.add("org:city_of_headquarters");
		singleValuedSlots.add("org:stateorprovince_of_headquarters");
		singleValuedSlots.add("org:country_of_headquarters");
		singleValuedSlots.add("org:date_dissolved");
		singleValuedSlots.add("org:date_founded");
		
		
		
		//fillscount initialize
		fillsCount.put("per:alternate_names",0);
		fillsCount.put("per:date_of_birth",0);
		fillsCount.put("per:age",0);
		fillsCount.put("per:country_of_birth",0);
		fillsCount.put("per:stateorprovince_of_birth",0);
		fillsCount.put("per:city_of_birth",0);
		fillsCount.put("per:origin",0);
		fillsCount.put("per:date_of_death",0);
		fillsCount.put("per:country_of_death",0);
		fillsCount.put("per:stateorprovince_of_death",0);
		fillsCount.put("per:city_of_death",0);
		fillsCount.put("per:cause_of_death",0);
		fillsCount.put("per:countries_of_residence",0);
		fillsCount.put("per:statesorprovinces_of_residence",0);
		fillsCount.put("per:cities_of_residence",0);
		fillsCount.put("per:schools_attended",0);
		fillsCount.put("per:title",0);
		fillsCount.put("per:employee_or_member_of",0);
		fillsCount.put("per:religion",0);
		fillsCount.put("per:spouse",0);
		fillsCount.put("per:children",0);
		fillsCount.put("per:parents",0);
		fillsCount.put("per:siblings",0);
		fillsCount.put("per:other_family",0);
		fillsCount.put("per:charges",0);
		fillsCount.put("org:alternate_names",0);
		fillsCount.put("org:political_religious_affiliation",0);
		fillsCount.put("org:top_members_employees",0);
		fillsCount.put("org:number_of_employees_members",0);
		fillsCount.put("org:members",0);
		fillsCount.put("org:member_of",0);
		fillsCount.put("org:subsidiaries",0);
		fillsCount.put("org:parents",0);
		fillsCount.put("org:founded_by",0);
		fillsCount.put("org:date_founded",0);
		fillsCount.put("org:date_dissolved",0);
		fillsCount.put("org:country_of_headquarters",0);
		fillsCount.put("org:stateorprovince_of_headquarters",0);
		fillsCount.put("org:city_of_headquarters",0);
		fillsCount.put("org:shareholders",0);
		fillsCount.put("org:website",0);
	}
	
	public void writeSlotsTogether(String year,String key_file,String cu_opt) throws IOException{
		String delimiter = new String("\t");
		
		Map<String,Double> mp1=null,mp2=null;
		Map<String,Integer> t1=null,t2=null;
		Map<String,String> mpOut1=null,mpOut2=null;
		
		
		if(year.equals("2013")){
			mp1=s1_2013.mpConfidence;
			mp2=s2_2013.mpConfidence;
			
			t1=s1_2013.mpTarget;
			t2=s2_2013.mpTarget;
			
			mpOut1=s1_2013.mpOutput;
			mpOut2=s2_2013.mpOutput;
		}
		else if(year.equals("2014")){
			mp1=s1_2014.mpConfidence;
			mp2=s2_2014.mpConfidence;
			
			t1=s1_2014.mpTarget;
			t2=s2_2014.mpTarget;

			mpOut1=s1_2014.mpOutput;
			mpOut2=s2_2014.mpOutput;
		}
		
		
		
		BufferedWriter bw = null,bw_unique=null;		
		bw = new BufferedWriter(new FileWriter("run_out/"+year+"-data.txt"));
		bw_unique = new BufferedWriter(new FileWriter("run_out/unique/"+year+"-unique-data.txt"));
		
		//write header
		//create dummy extractor
		FeatureExtractor fe_dumm = new FeatureExtractor();
		fe_dumm.populateFeatures(this,"A:B:C","per:origin",0.0,0.0);
		String headerStr = new String("");
		Set<String> featureSet = new TreeSet<String>(fe_dumm.features.keySet());
		for(String feature_key : featureSet){
			headerStr += feature_key+delimiter;
		}
		if(year.equals("2013")){			
			bw.write("queryid"+delimiter+"relationtype"+delimiter+"extractorID"+delimiter+"docID"+delimiter+"slotfill"+delimiter+"prov1"+delimiter+"prov2"+delimiter+"prov3"+delimiter+headerStr.trim()+delimiter+"target"+"\n");
			bw_unique.write("queryid"+delimiter+"relationtype"+delimiter+"extractorID"+delimiter+"docID"+delimiter+"slotfill"+delimiter+"prov1"+delimiter+"prov2"+delimiter+"prov3"+delimiter+headerStr.trim()+delimiter+"target"+"\n");
		}
		else if(year.equals("2014")){
			bw.write("queryid"+delimiter+"relationtype"+delimiter+"extractorID"+delimiter+"prov1"+delimiter+"slotfill"+delimiter+"prov2"+delimiter+headerStr.trim()+delimiter+"target"+"\n");
			bw_unique.write("queryid"+delimiter+"relationtype"+delimiter+"extractorID"+delimiter+"prov1"+delimiter+"slotfill"+delimiter+"prov2"+delimiter+headerStr.trim()+delimiter+"target"+"\n");
		}
		
		
		/*
		 * 
		 * add for unique entries and common entries in output file of extractor 1
		 * 
		 */
		 
		int counter=0;
		for(String key : mp1.keySet()){
			Double conf1, conf2;
			Boolean isunique=false;
			Integer target;
			String output1;
			conf1=mp1.get(key);
			target=t1.get(key);
			if(key.contains("NIL")){
				//ignore all NIL fills
				continue;
			}		
			
			output1=mpOut1.get(key);
			String[] parts=output1.split(delimiter);
			
			//book keeping
			int count = fillsCount.get(parts[1]);
			fillsCount.remove(parts[1]);
			fillsCount.put(parts[1],count+1);
			
			if(mp2.containsKey(key)){
				counter+=1;
				conf2=mp2.get(key);
			}
			else{
				conf2=0.0;
				isunique=true;
			}
			
			/*
			 * extract and add features
			 */
			//init feature extractor
			FeatureExtractor fe = new FeatureExtractor();
			fe.populateFeatures(this,key,parts[1],conf1,conf2);
			
			String featureStr = new String("");			
			for(String feature_key : featureSet){
				Double featureVal = fe.features.get(feature_key);
				featureStr += featureVal + delimiter;
			}
			
			
			if(cu_opt.equals("cusep")){
				if(isunique){
					bw_unique.write(output1+delimiter+featureStr.trim()+delimiter+target+"\n");
				}
				else{
					bw.write(output1+delimiter+featureStr.trim()+delimiter+target+"\n");
				}
			}
			else{
				bw.write(output1+delimiter+featureStr.trim()+delimiter+target+"\n");
			}
			
			//System.out.println(key+delimiter+conf1+delimiter+conf2+delimiter+target);
		}
		/*
		 * 
		 * add for unique entries in output file of extractor 2
		 * 
		 */
		for(String key : mp2.keySet()){
			Double conf1, conf2;
			Integer target;
			String output2;
			
			if(mp1.containsKey(key)==false){
				if(key.contains("NIL")){
					//ignore all NIL fills
					continue;
				}
				conf2=mp2.get(key);
				target=t2.get(key);
				conf1=0.0;
				output2=mpOut2.get(key);
				String[] parts=output2.split(delimiter);
				
				//book keeping
				int count = fillsCount.get(parts[1]);
				fillsCount.remove(parts[1]);
				fillsCount.put(parts[1],count+1);
				
				/*
				 * extract and add features
				 */
				//init feature extractor
				FeatureExtractor fe = new FeatureExtractor();
				fe.populateFeatures(this,key,parts[1],conf1,conf2);
				
				String featureStr = new String("");				
				for(String feature_key : featureSet){
					Double featureVal = fe.features.get(feature_key);
					featureStr += featureVal + delimiter;
				}
				if(cu_opt.equals("cusep")){
					bw_unique.write(output2+delimiter+featureStr.trim()+delimiter+target+"\n");
				}
				else{
					bw.write(output2+delimiter+featureStr.trim()+delimiter+target+"\n");
				}
				
				
			}
			
		}	
		bw.close();
		bw_unique.close();
		System.out.println("common count : "+counter);
	}
	
	public void writeSlotsSeperate(String year, String key_file,String cu_opt) throws IOException{
		String delimiter = new String("\t");
		Integer num_slots = relationIDs.size();
		Map<String,Double> mp1=null,mp2=null;
		Map<String,Integer> t1=null,t2=null;
		Map<String,String> mpOut1=null,mpOut2=null;
		
		

		if(year.equals("2013")){
			mp1=s1_2013.mpConfidence;
			mp2=s2_2013.mpConfidence;
			
			t1=s1_2013.mpTarget;
			t2=s2_2013.mpTarget;
			
			mpOut1=s1_2013.mpOutput;
			mpOut2=s2_2013.mpOutput;
		}
		else if(year.equals("2014")){
			mp1=s1_2014.mpConfidence;
			mp2=s2_2014.mpConfidence;
			
			t1=s1_2014.mpTarget;
			t2=s2_2014.mpTarget;

			mpOut1=s1_2014.mpOutput;
			mpOut2=s2_2014.mpOutput;
		}
		

		//create bufferedwriter for all slot types and init to output files
		BufferedWriter[] bw= new BufferedWriter[num_slots];
		BufferedWriter[] bw_unique= new BufferedWriter[num_slots];
		Set<String> featureSet = null;
		for(String slot_name  : relationIDs.keySet()){
			String outfilename1 = new String("run_out/"+year+"-"+slot_name+"-data.txt");
			Integer slotid= relationIDs.get(slot_name);
			bw[slotid] = new BufferedWriter(new FileWriter(outfilename1));
			
			String outfilename2 = new String("run_out/unique/"+year+"-"+slot_name+"-unique-data.txt");
			bw_unique[slotid] = new BufferedWriter(new FileWriter(outfilename2));
			
			//write header
			//create dummy extractor
			FeatureExtractor fe_dumm = new FeatureExtractor();
			fe_dumm.populateFeatures(this,"A:B:C","per:origin",0.0,0.0);
			String headerStr = new String("");
			featureSet = new TreeSet<String>(fe_dumm.features.keySet());
			for(String feature_key : featureSet){
				headerStr += feature_key+delimiter;
			}
		
			if(year.equals("2013")){			
				bw[slotid].write("queryid"+delimiter+"relationtype"+delimiter+"extractorID"+delimiter+"docID"+delimiter+"slotfill"+delimiter+"prov1"+delimiter+"prov2"+delimiter+"prov3"+delimiter+headerStr.trim()+delimiter+"target"+"\n");
				bw_unique[slotid].write("queryid"+delimiter+"relationtype"+delimiter+"extractorID"+delimiter+"docID"+delimiter+"slotfill"+delimiter+"prov1"+delimiter+"prov2"+delimiter+"prov3"+delimiter+headerStr.trim()+delimiter+"target"+"\n");
			}
			else if(year.equals("2014")){
				bw[slotid].write("queryid"+delimiter+"relationtype"+delimiter+"extractorID"+delimiter+"prov1"+delimiter+"slotfill"+delimiter+"prov2"+delimiter+headerStr.trim()+delimiter+"target"+"\n");
				bw_unique[slotid].write("queryid"+delimiter+"relationtype"+delimiter+"extractorID"+delimiter+"prov1"+delimiter+"slotfill"+delimiter+"prov2"+delimiter+headerStr.trim()+delimiter+"target"+"\n");
			}
		}
			
		/*
		 * 
		 * add for unique entries and common entries in output file of extractor 1
		 * 
		 */
		 
		int counter=0;
		for(String key : mp1.keySet()){
			Double conf1, conf2;
			Boolean isunique=false;
			Integer target;
			String output1;
			conf1=mp1.get(key);
			target=t1.get(key);
			if(key.contains("NIL")){
				//ignore all NIL fills
				continue;
			}		
			
			output1=mpOut1.get(key);
			String[] parts=output1.split(delimiter);
			
			//book keeping
			int count = fillsCount.get(parts[1]);
			fillsCount.remove(parts[1]);
			fillsCount.put(parts[1],count+1);
			
			
			
			Integer relID = relationIDs.get(parts[1]);
			if(mp2.containsKey(key)){
				counter+=1;
				conf2=mp2.get(key);
			}
			else{
				isunique=true;
				conf2=0.0;
			}
			
			/*
			 * extract and add features
			 */
			//init feature extractor
			FeatureExtractor fe = new FeatureExtractor();
			fe.populateFeatures(this,key,parts[1],conf1,conf2);
			
			String featureStr = new String("");			
			for(String feature_key : featureSet){
				Double featureVal = fe.features.get(feature_key);
				featureStr += featureVal + delimiter;
			}
			
			if(cu_opt.equals("cusep")){
				if(isunique){
					bw_unique[relID].write(output1+delimiter+featureStr.trim()+delimiter+target+"\n");
				}
				else{
					bw[relID].write(output1+delimiter+featureStr.trim()+delimiter+target+"\n");
				}
			}
			else{
				bw[relID].write(output1+delimiter+featureStr.trim()+delimiter+target+"\n");
			}
			
			//System.out.println(key+delimiter+conf1+delimiter+conf2+delimiter+target);
		}
		/*
		 * 
		 * add for unique entries in output file of extractor 2
		 * 
		 */
		for(String key : mp2.keySet()){
			Double conf1, conf2;
			Integer target;
			String output2;

			if(mp1.containsKey(key)==false){
				if(key.contains("NIL")){
					//ignore all NIL fills
					continue;
				}
				conf2=mp2.get(key);
				target=t2.get(key);
				conf1=0.0;
				output2=mpOut2.get(key);
				String[] parts=output2.split(delimiter);
				Integer relID = relationIDs.get(parts[1]);
				
				
				//book keeping
				int count = fillsCount.get(parts[1]);
				fillsCount.remove(parts[1]);
				fillsCount.put(parts[1],count+1);
				
				
				
				/*
				 * extract and add features
				 */
				//init feature extractor
				FeatureExtractor fe = new FeatureExtractor();
				fe.populateFeatures(this,key,parts[1],conf1,conf2);
				
				String featureStr = new String("");				
				for(String feature_key : featureSet){
					Double featureVal = fe.features.get(feature_key);
					featureStr += featureVal + delimiter;
				}
				if(cu_opt.equals("cusep")){
					bw_unique[relID].write(output2+delimiter+featureStr.trim()+delimiter+target+"\n");
				}
				else{
					bw[relID].write(output2+delimiter+featureStr.trim()+delimiter+target+"\n");
				}
				
			}
			
		}
		
		for(String slot_name  : relationIDs.keySet()){
			Integer slotid= relationIDs.get(slot_name);
			bw[slotid].close();
			bw_unique[slotid].close();
		}
		System.out.println("common count : "+counter);
	}
	
	public void printStats(){
		//print fills count
		
		System.out.println("RUN SUMMARY");
		
		System.out.println("Slot type \t Number of fills");
		for(String key : fillsCount.keySet()){
			System.out.println(key+"\t"+fillsCount.get(key));
		}
	}
	
	/*
	 * Command line args
	 * 
	 * @args[0] Relation extractor output 1
	 * @args[1] Relation extractor output 2
	 * @args[2] key file 
	 * @args[3] year
	 * @args[4] slots option : "sep" (seperate file for slot fills of each slot type) or "all"
	 * @args[5] common/unique option : "cusep" (seperate file for common & unique slot fills) or "<anyotherstring> 
	 *  
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String fname1 = new String(args[0]);
		String fname2 = new String(args[1]);
		
		String key_file = new String(args[2]);
		String year = new String(args[3]);
		DataExtractor de = new DataExtractor();
		
		
		String opt = new String(args[4]);
		String cu_opt = new String(args[5]);
				
		String[] nargs=new String[3];
		nargs[0]=fname1;
		nargs[1]=key_file;
		nargs[2]= new String("anydoc");
		
		if(year.equals("2013")){
			try {
				de.s1_2013.run(nargs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nargs[0]=fname2;
			try {
				de.s2_2013.run(nargs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(year.equals("2014")){
			try {
				de.s1_2014.run(nargs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nargs[0]=fname2;
			try {
				de.s2_2014.run(nargs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(opt.equals("sep")){
			de.writeSlotsSeperate(year,key_file,cu_opt);
		}
		else if(opt.equals("all")){
			de.writeSlotsTogether(year,key_file,cu_opt);
		}

		de.printStats();
	}

}
