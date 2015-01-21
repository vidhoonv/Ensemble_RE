package stackingm2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Uses basic rules to combine slot fills
 * from  different relation extractor outputs
 * 
 * This method uses simple rules on union output 
 * based on slot type and precision and recall 
 * of the systems being combined to decide on 
 * how the systems should be ensembled.
 * 
 * All rules are included in comments
 * 
 */
public class RuleEnsembler {

	/**
	 * @param args
	 */
	
	Map<String,String> mpOutput=new HashMap<String,String>();
	Map<String,Double> mpConfidence=new HashMap<String,Double>();
	Set<String> singleValuedSlots= new HashSet<String>();
	Set<String> filledSlots = new HashSet<String>(); //only tracks single valued slots
	Map<String,Boolean> slotfills = new HashMap<String,Boolean>();
	Map<String,Boolean> isSlotfillCommon = new HashMap<String,Boolean>();
	Map<String,Integer> slotfillExtID = new HashMap<String,Integer>();
	Set<String> perSlots = new HashSet<String>();
	Set<String> orgSlots = new HashSet<String>();
	String runid = new String("");
	
	
	
	Map<String,Double> sys1_precision = new HashMap<String,Double>();
	Map<String,Double> sys1_recall = new HashMap<String,Double>();
	Map<String,Double> sys1_f1 = new HashMap<String,Double>();
	
	Map<String,Double> sys2_precision = new HashMap<String,Double>();
	Map<String,Double> sys2_recall = new HashMap<String,Double>();
	Map<String,Double> sys2_f1 = new HashMap<String,Double>();
	
	Map<String,Double> comb_precision = new HashMap<String,Double>();
	Map<String,Double> comb_recall = new HashMap<String,Double>();
	Map<String,Double> comb_f1 = new HashMap<String,Double>();
	
	
	Map<String,Boolean> iscombineImprovedF1 = new HashMap<String,Boolean>();
	
	/*
	 *  this function is keep track of what slots have fills or some entry
	 *  from classifier result. The KBP mandates to have NIL slot fill if
	 *  no suitable fill is found. So, using this book keeping, we can append
	 *  NIL slot fills for slots with no fills from classifier results.
	 *  
	 */
	
	public void loadExtStats(String statsfile) throws IOException{
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(statsfile));
		String line;
		String delimiter = new String("\t");
		br.readLine(); //skip header
		
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(delimiter);
			
			String slot_name = new String(parts[0].trim());
			
			Double sys1_prec = new Double(Double.parseDouble(parts[1].trim()));
			Double sys1_rec  = new Double(Double.parseDouble(parts[2].trim()));
			Double sys1_f = new Double(Double.parseDouble(parts[3].trim()));
			
			Double sys2_prec = new Double(Double.parseDouble(parts[4].trim()));
			Double sys2_rec = new Double(Double.parseDouble(parts[5].trim()));
			Double sys2_f = new Double(Double.parseDouble(parts[6].trim()));
			
			Double comb_prec = new Double(Double.parseDouble(parts[7].trim()));
			Double comb_rec  = new Double(Double.parseDouble(parts[8].trim()));
			Double comb_f = new Double(Double.parseDouble(parts[9].trim()));
			
			Integer iscombImp = new Integer(Integer.parseInt(parts[10].trim()));
			
			sys1_precision.put(slot_name,sys1_prec);
			sys1_recall.put(slot_name,sys1_rec);
			sys1_f1.put(slot_name,sys1_f);
			
			sys2_precision.put(slot_name,sys2_prec);
			sys2_recall.put(slot_name,sys2_rec);
			sys2_f1.put(slot_name,sys2_f);
			
			comb_precision.put(slot_name,comb_prec);
			comb_recall.put(slot_name, comb_rec);
			comb_f1.put(slot_name, comb_f);
			
			
			if(iscombImp.equals(1)){
				iscombineImprovedF1.put(slot_name, true);
			}
			else if(iscombImp.equals(0)){
				iscombineImprovedF1.put(slot_name, false);
			}
			else{
				System.out.println("ERR: invalid comb improve field value");
			}
		}
		
		br.close();
		
	}
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
					isSlotfillCommon.put(key,false);
				}
			}
			else{
				//org type
				for(String slotName : orgSlots){
					String key = new String(queryID+"~"+slotName);
					slotfills.put(key, false);
					isSlotfillCommon.put(key,false);
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
	public Boolean isMeetingCombineCondition(String relation_name){
		Boolean result = false;
		
		Double sys1_p = sys1_precision.get(relation_name),sys1_r = sys1_recall.get(relation_name);
		Double sys2_p = sys2_precision.get(relation_name),sys2_r = sys2_recall.get(relation_name);
		
		Double max_p = Double.max(sys1_p,sys2_p);
		Double max_r = Double.max(sys1_r, sys2_r);
		
		Double min_p = Double.min(sys1_p,sys2_p);
		Double min_r = Double.min(sys1_r, sys2_r);
		
		if( min_p > max_p){
			result = true;
		}		
		return result;
	}
	public void processClassifierOutput(String infile) throws IOException{
		int nLines=0, nSkipped=0;
		BufferedReader csv = null;
		csv = new BufferedReader(new FileReader(infile));
		String line;
		csv.readLine(); //skip header
		int expectedNumFields=10,predictedTargetFieldIndex=8,fillIndex=4;
		int conf1Index=6, conf2Index=7;
		while ((line = csv.readLine()) != null) {
			nLines++;
			//System.out.println("processing line "+nLines);
			String[] data = line.split("\t");
			for(String d : data){
				d=d.trim();
			}
			runid=data[2];
			if(data.length<expectedNumFields)
				continue;
			if(data[predictedTargetFieldIndex].trim().equals("0")){
				//classifer predicted that this is not a good extraction
				System.out.println("skipping line");
				nSkipped++;
				continue;
			}
			
			//good extraction
			String query_id = data[0] + "~" + data[1] + "~" + data[fillIndex];
			String output_string = new String("");
			String relation_name = data[1];
			if(data[fillIndex].equals("NIL")){
				output_string +=  data [0] + "\t" + data [1] + "\t" + data [2] + "\t" + data [fillIndex];
			}
			else{
				output_string +=  data [0] + "\t" + data [1] + "\t" + data [2] + "\t" + data [3];
				output_string += "\t" + data [fillIndex] + "\t" + data [5] ;
			}
			
			Double conf1=0.0, conf2=0.0;
			conf1=Double.parseDouble(data[conf1Index]);
			conf2=Double.parseDouble(data[conf2Index]);
			Double sum_conf=new Double(conf1+conf2);
			Boolean iscommonExt=false;
			if(conf1.compareTo(0.0)>0 && conf2.compareTo(0.0)>0){
				iscommonExt=true;				
			}
			Double diff=conf1-conf2;
			
			String key = data[0] + "~" + data[1];
			if(singleValuedSlots.contains(relation_name)){
				/*
				 * RULE for single valued slots
				 * 
				 * 1) Choose common extraction over unique extraction
				 * 2) Choose unique extraction from extractor of higher precision for the relation
				 */
				if(filledSlots.contains(key)){
					//find which extraction to keep
					/*
					 * 
					 * get key and confidence of existing slot fill
					 * 
					 */
					Double econf = new Double("0.0");
					String oldkey = new String("");
					for(String k : mpConfidence.keySet()){
						if(k.contains(key)){
							econf = mpConfidence.get(k);
							oldkey = k;
							break;
						}
					}
					Boolean isFillCommon = false;
					if(isSlotfillCommon.containsKey(oldkey)){
						isFillCommon = isSlotfillCommon.get(oldkey);
					}
					
					if(isFillCommon == true && iscommonExt==false){
						/*
						 * if existing fill for the slot is from a common extraction 
						 * and new slot fill is not common then do not consider it
						 * 
						 */
						System.out.println("SINGLE: existing common, new fill not common - skipping");
						
						continue; //skipping
					}
					/*
					else if(isFillCommon==false && iscommonExt==true){
						
						 // if existing fill is not common extraction
						 //and new fill is common then replace old fill
						 // with new one
						 // NOT POSSIBLE CASE
						 //
						System.out.println("SINGLE: existing not common, new fill common - replacing");
						mpConfidence.remove(oldkey);
						mpOutput.remove(oldkey);
						mpConfidence.put(query_id, sum_conf);
						mpOutput.put(query_id, output_string);
						isSlotfillCommon.remove(key); 
						isSlotfillCommon.put(key, iscommonExt); //update fill type (common or unique)
						Integer newfillextID = conf1.compareTo(0.0) > 0 ? 1 : 2 ; 
						slotfillExtID.remove(oldkey);
						slotfillExtID.put(query_id,newfillextID);
					}				
					else if(isFillCommon == true && iscommonExt==true ){
						//
						 // if both existing and new fills are from 
						 // common extractions, then choose the extraction
						 // with greater sum confidence.
						 // NOT POSSIBLE CASE - cant have multilple commn ext for single valued slot
						 //
						
						
						if(Double.compare(econf, sum_conf)<0){
							System.out.println("SINGLE: existing common, new fill common - choosing greater sum_conf fill");
							mpConfidence.remove(oldkey);
							mpOutput.remove(oldkey);
							mpConfidence.put(query_id, sum_conf);
							mpOutput.put(query_id, output_string);
							isSlotfillCommon.remove(key); 
							isSlotfillCommon.put(key, iscommonExt); //update fill type (common or unique)
							Integer newfillextID = conf1.compareTo(0.0) > 0 ? 1 : 2 ; 
							slotfillExtID.remove(oldkey);
							slotfillExtID.put(query_id,newfillextID);
						}
						
					}*/					
					else if(isFillCommon==false && iscommonExt==false ){
						/*
						 * if both fills are not common extractions
						 * then choose the one coming from extractor of
						 * greater precision
						 * 
						 */
						
						Integer newfillextID = conf1.compareTo(0.0) > 0 ? new Integer(1) : new Integer(2) ; 
						Integer efillextID = slotfillExtID.get(oldkey);
						//System.out.println(oldkey);
						
						Double efillerPrec = 0.0;
						Double newfillerPrec = 0.0;
						
						if(efillextID.equals(1)){
							efillerPrec = sys1_precision.get(relation_name);
						}
						else if(efillextID.equals(2)){
							efillerPrec = sys2_precision.get(relation_name);
						}
						
						if(newfillextID.equals(1)){
							newfillerPrec = sys1_precision.get(relation_name);
						}
						else if(newfillextID.equals(2)){
							newfillerPrec = sys2_precision.get(relation_name);
						}
						
						/*
						 * replace fill if new fill extractor has
						 * higher precision for that slot
						 * 
						 */
						if(newfillerPrec.compareTo(efillerPrec) > 0){
							System.out.println("SINGLE: existing not common , new fill not common - choosing new fill");
							System.out.println(newfillerPrec+"\t"+efillerPrec);
							mpConfidence.remove(oldkey);
							mpOutput.remove(oldkey);
							mpConfidence.put(query_id, sum_conf);
							mpOutput.put(query_id, output_string);
							isSlotfillCommon.remove(key); 
							isSlotfillCommon.put(key, iscommonExt); //update fill type (common or unique)
							slotfillExtID.remove(oldkey);
							slotfillExtID.put(query_id,newfillextID);
						}
						else{
							System.out.println("SINGLE: existing not common , new fill not common - choosing existing fill");
						}
						
						
					}
					else{
						System.out.println("Err: what case is this?");
					}
					
				}
				else{
					if(data[fillIndex].equals("NIL")==false)
						output_string += "\t" + sum_conf;
					
					mpConfidence.put(query_id,sum_conf);
					mpOutput.put(query_id, output_string);
					filledSlots.add(key);
					if(slotfills.containsKey(key)){
						slotfills.remove(key);
						slotfills.put(key,true); //update fill
						isSlotfillCommon.remove(key); 
						isSlotfillCommon.put(key, iscommonExt); //update fill type (common or unique)
						Integer newfillextID = conf1.compareTo(0.0) > 0 ? new Integer(1) : new Integer(2) ; 
						slotfillExtID.put(query_id,newfillextID); //update extractor id
						
					}
					else{
						
						System.out.println("Err: what case is this?");
					}
					
					
					 
					
				}
			}
			else{
				
				
				/*RULE for list valued slots
				 * 
				 * if it is a common extraction include it
				 * 
				 *  if it is a unique extraction, check if union improves.
				 *  If yes, then dont care about Ext ID and just include fill.
				 *  If no, include fill only if it is from better F1 Extractor.
				 */
				Boolean includeFill = false;
				if(iscommonExt == true){
					//System.out.println("common list ext");
					includeFill = true;
				}
				
				Integer newfillextID = conf1.compareTo(0.0) > 0 ? new Integer(1) : new Integer(2) ; 
				
				if(isMeetingCombineCondition(relation_name) == true){
					//System.out.println("improved combined f1");
					includeFill = true;
				}			
				else{
					Double ext1_f1 = sys1_f1.get(relation_name);
					Double ext2_f1 = sys2_f1.get(relation_name);
					Integer pref_ext = 0;
					if(ext1_f1.compareTo(ext2_f1) > 0){
						pref_ext = 1;
					}
					else{
						pref_ext = 2; 
					}
					
					if(newfillextID.equals(pref_ext)){
						//System.out.println("ext id greater f1 perf");
						includeFill = true;
					}
				}
				
				
				if(includeFill == true /*|| includeFill==false*/){
					if(data[fillIndex].equals("NIL")==false)
						output_string += "\t" + sum_conf;
					
					mpConfidence.put(query_id,sum_conf);
					mpOutput.put(query_id, output_string);
					filledSlots.add(key);
					if(slotfills.containsKey(key)){
						slotfills.remove(key);
						slotfills.put(key,true);
					}
				}
				else{
					nSkipped++;
				}
				
				
			}
		}
		csv.close();
		System.out.println("Total lines: "+nLines);
		System.out.println("Skipped lines: "+nSkipped);
	}
	
	/*
	 * args[0]	-	input file comprising of the slot fills and classifier result 
	 * args[1]	-	output file to write slot fills in KBP format based on classifier result				
	 * args[2]  -   extractor stats file for training year (2013)
	 * 
	 */
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		String inFile = new String(args[0]);
		String outFile = new String(args[1]);
		String statsFile = new String(args[2]);
		
		
		RuleEnsembler re = new RuleEnsembler();
		
		re.loadExtStats(statsFile);
		re.populateSingleValuedSlots();
		re.populateSlotFills();
		re.processClassifierOutput(inFile);
		re.writeOutputFile(outFile);
		

	}

}
