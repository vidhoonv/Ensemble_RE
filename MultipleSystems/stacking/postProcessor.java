package MultipleSystems.stacking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import MultipleSystems.aliasing.AliasWrapper;

/*
 * postProcessor Class:
 * 
 * Used to create output file in KBP format for evaluation.
 * Feed the output predictions from classifier and postProcessor
 * throws an output file in KBP format that can be run against
 * their provided scorer. 
 * 
 * NOTE: 
 * Currently postProcessor produces output file in 2014 format.
 * The confidence values written are wrong - they were meant for 
 * 2 systems only
 * 
 */
public class postProcessor {

	/**
	 * @param args
	 */
	Map<String,String> mpOutput=new HashMap<String,String>();
	Map<String,Double> mpConfidence=new HashMap<String,Double>();
	Set<String> singleValuedSlots= new HashSet<String>();
	Set<String> filledSlots = new HashSet<String>(); //only tracks single valued slots
	Map<String,Boolean> slotfills = new HashMap<String,Boolean>();
	Set<String> perSlots = new HashSet<String>();
	Set<String> orgSlots = new HashSet<String>();
	String runid = new String("");
	AliasWrapper aw = null;
	boolean doAlias = false;
	public postProcessor(String aliasFlag, String wikiFilePath, String orgSuffixPath) throws IOException{
		if(aliasFlag.equals("true")){
			aw = new AliasWrapper(wikiFilePath,orgSuffixPath,10);
			doAlias=true;
		}
		
	}
	/*
	 *  this function is keep track of what slots have fills or some entry
	 *  from classifier result. The KBP mandates to have NIL slot fill if
	 *  no suitable fill is found. So, using this book keeping, we can append
	 *  NIL slot fills for slots with no fills from classifier results.
	 *  
	 */
	public void populateSlotFills(){
		
		//add per slots
		perSlots.add("per:alternate_names");
		perSlots.add("per:date_of_birth");
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
		perSlots.add("per:employee_or_member_of");
		perSlots.add("per:religion");
		perSlots.add("per:spouse");
		
		perSlots.add("per:children");
		perSlots.add("per:parents");
		perSlots.add("per:siblings");
		perSlots.add("per:other_family");
		perSlots.add("per:charges");
		
		
		//add org slots
		orgSlots.add("org:alternate_names");
		orgSlots.add("org:political_religious_affiliation");
		orgSlots.add("org:top_members_employees");
		orgSlots.add("org:number_of_employees_members");
		orgSlots.add("org:members");
		
		orgSlots.add("org:member_of");
		orgSlots.add("org:subsidiaries");
		orgSlots.add("org:parents");
		orgSlots.add("org:founded_by");
		orgSlots.add("org:date_founded");
		
		orgSlots.add("org:date_dissolved");
		orgSlots.add("org:country_of_headquarters");
		orgSlots.add("org:stateorprovince_of_headquarters");
		orgSlots.add("org:city_of_headquarters");
		orgSlots.add("org:shareholders");
		orgSlots.add("org:website");
	
		
		String queryPrefix = new String("SF14_ENG_");
		String delimiter = new String("~");
		
		for(int i=1;i<=100;i++){
			String queryID = queryPrefix+String.format("%03d", i);
			
			if(i<=50){
				//per type
				for(String slotName : perSlots){
					String key = new String(queryID+"~"+slotName);
					slotfills.put(key, false);
				}
			}
			else{
				//org type
				for(String slotName : orgSlots){
					String key = new String(queryID+"~"+slotName);
					slotfills.put(key, false);
				}
			}
		
		}
		
	}
	public void populateSingleValuedSlots(){
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
	}
	

	public void writeOutputFile(String file) throws IOException{
		BufferedWriter out = null;
		out = new BufferedWriter(new FileWriter(file));
		for(String key : mpOutput.keySet()){
			Double conf = mpConfidence.get(key);
			String output_str = mpOutput.get(key);
			
			String line = output_str +"\n";
			out.write(line);
			
		}
		
		
		for(String key : slotfills.keySet()){
			if(slotfills.get(key)==false){				
				String output_string = new String("");
				String[] data= key.split("~");
				output_string =  data [0] + "\t" + data [1] + "\t" + runid + "\t" + "NIL" + "\n";
				out.write(output_string);
			}
		}
		out.close();
		
	}
	public void processClassifierOutput(String infile, String year) throws IOException{
		int nLines=0, nSkipped=0, nAliasFillsSkipped=0;
		BufferedReader csv = null;
		csv = new BufferedReader(new FileReader(infile));
		String line;
		csv.readLine(); //skip header
		int expectedNumFields=10,predictedTargetFieldIndex=8,fillIndex=4;
		int conf1Index=6, conf2Index=7;
		int confIndexStart=0,confIndexEnd=0;
		
		if(year.equals("2013")){
			expectedNumFields=12;
			predictedTargetFieldIndex=10;
			fillIndex=4;
			conf1Index=8;
			conf2Index=9;
		}
		else if(year.equals("2014")){
			expectedNumFields=18;
			predictedTargetFieldIndex=16;
			fillIndex=4;
			confIndexStart=6;
			confIndexEnd=15;
		}
		else{
			System.out.println("ERR: Invalid year");
		}
		while ((line = csv.readLine()) != null) {
			nLines++;
			String[] data = line.split("\t");
			runid=data[2];
			
			if(data.length<expectedNumFields)
				continue;
			if(data[predictedTargetFieldIndex].trim().equals("0")){
				//classifer predicted that this is not a good extraction
				//System.out.println("skipping line");
				nSkipped++;
				continue;
			}
			
			//good extraction
			String query_id = data[0] + "~" + data[1] + "~" + data[fillIndex];
			String output_string = new String("");
			if(data[fillIndex].equals("NIL")){
				output_string +=  data [0] + "\t" + data [1] + "\t" + data [2] + "\t" + data [fillIndex];
			}
			else{
				output_string +=  data [0] + "\t" + data [1] + "\t" + data [2] + "\t" + data [3];
				output_string += "\t" + data [fillIndex] + "\t" + data [5] ;
			}
			
			Double conf1=0.0, conf2=0.0, sumConf=0.0;
			for(int k=confIndexStart;k<=confIndexEnd;k++){
				Double conf = Double.parseDouble(data[k]);
				sumConf += conf;				
			}
			
			
			String key = data[0] + "~" + data[1];
			if(singleValuedSlots.contains(data[1])){
				//chose the highest confidence value for extraction				
				if(filledSlots.contains(key)){
					//find which extraction to keep
					Double econf = new Double("0.0");
					String oldkey = new String("");
					for(String k : mpConfidence.keySet()){
						if(k.contains(key)){
							econf = mpConfidence.get(k);
							oldkey = k;
							break;
						}
					}
					
					
					if(Double.compare(econf, sumConf)<0){
						mpConfidence.remove(oldkey);
						mpOutput.remove(oldkey);
						mpConfidence.put(query_id, sumConf);
						mpOutput.put(query_id, output_string);
					}
				}
				else{
					if(data[fillIndex].equals("NIL")==false)
						output_string += "\t" + sumConf;
					mpConfidence.put(query_id,sumConf);
					mpOutput.put(query_id, output_string);
					filledSlots.add(key);
					if(slotfills.containsKey(key)){
						slotfills.remove(key);
						slotfills.put(key,true);
					}
					
				}
			}
			else{
				if(doAlias == true){
					//check for alias
					String newfill = new String(data[fillIndex]);
					List<String> existingFills = new ArrayList<String>();
					boolean aliasFound = false;

					for(String k : mpConfidence.keySet()){
						if(k.contains(key)){
							String[] parts = k.split("~");
							existingFills.add(parts[2]);
						}
					}

					for(String oldFill : existingFills){
						List<String> aliases = aw.getAliases(oldFill);
						for(String a : aliases){
							if(newfill.equals(a)){
								System.out.println("alias found for -"+newfill+" -from oldfill -"+oldFill);
								aliasFound = true;
							}
						}
					}

					if(aliasFound==true){
						//skip the fill
						nAliasFillsSkipped++;
						continue;
					}
				}
				
				if(data[fillIndex].equals("NIL")==false)
					output_string += "\t" + sumConf;
				mpConfidence.put(query_id,sumConf);

				mpOutput.put(query_id, output_string);
				filledSlots.add(key);
				if(slotfills.containsKey(key)){
					slotfills.remove(key);
					slotfills.put(key,true);
				}
			}
		}
		csv.close();
		System.out.println("Total lines: "+nLines);
		System.out.println("Skipped lines: "+nSkipped);
		System.out.println("Alias fills skipped: "+nAliasFillsSkipped);
	}
	/*
	 * args[0]	-	input file comprising of the slot fills and classifier result 
	 * args[1]	-	output file to write slot fills in KBP format based on classifier result				
	 * args[2]  -	year
	 * args[3]  -   aliasFlag
	 * args[4]  -   wiki links path
	 * args[5]  -   org suffixes path
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String fname=new String(args[0]);
		String outFile=new String(args[1]);
		String year = new String(args[2]);
		String aliasFlag = new String(args[3]);
		postProcessor pp = new postProcessor(aliasFlag, args[4],args[5]);
		pp.populateSingleValuedSlots();
		pp.populateSlotFills();
		pp.processClassifierOutput(fname,year);
		pp.writeOutputFile(outFile);
	}

}
