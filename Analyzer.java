import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/*
 *  OBJECTIVES OF ANALYSIS
 * 
 *  1) Find number of common extractions
 *  2) Find number of exclusive extractions
 *  3) How are the confidence values for common extractions
 *  
 *  
 *  OTHER:
 *  
 *  4) Provenance analysis for common extractions
 * 
 */

public class Analyzer {

	/**
	 * @param args
	 */
	String RE_output1, RE_output2;
	//slot name , extraction
	Map<String,Map<String,Set<String>>> extractions1,extractions2; //entity name, map of slotname => extraction
	int common_count,unique_count1,unique_count2;
	int repeated_fills,nil_count, fill_count;
	Map<String,String> output1,output2;
	Map<String,String> unique1,unique2,common,common1,common2;
	
	Set<String> singleValuedSlots;
	SFScore scorer_sys1;
	SFScore scorer_sys2;
	
	//book keeping
	Map<String,Integer> uniqueSlotWiseCount;
	
	Analyzer(String f1, String f2) throws IOException{
		
		common_count=0;
		unique_count1=0;
		unique_count2=0;
		
		repeated_fills=0;
		nil_count=0;
		fill_count=0;
		
		RE_output1 = new String(f1);
		RE_output2 = new String(f2);
		
		extractions1 = new HashMap<String, Map<String,Set<String>>>();
		extractions2 = new HashMap<String, Map<String,Set<String>>>();
		
		output1 = new HashMap<String,String>();
		output2 = new HashMap<String,String>();
		
		unique1 = new HashMap<String,String>();
		unique2 = new HashMap<String,String>();
		common = new HashMap<String,String>();
		common1 = new HashMap<String,String>();
		common2 = new HashMap<String,String>();
		
		singleValuedSlots= new HashSet<String>();
		
		scorer_sys1 = new SFScore();
		scorer_sys2 = new SFScore();
		
		uniqueSlotWiseCount = new HashMap<String,Integer>();
		populateSingleValuedSlots();
		getExtractions();
		
		System.out.println("First file: "+extractions1.size());
//		for(String key : extractions1.keySet()){
//			Map<String,Set<String>> mp = extractions1.get(key);
//			System.out.println(key+" "+mp.size());
//			for(String k : mp.keySet()){
//				Set<String> st=mp.get(k);
//				System.out.println("\t"+k+" "+st.size());
//			}
//		}
//		
		System.out.println("Second file: "+extractions2.size());
//		for(String key : extractions2.keySet()){
//			Map<String,Set<String>> mp = extractions2.get(key);
//			System.out.println(key+" "+mp.size());
//			for(String k : mp.keySet()){
//				Set<String> st=mp.get(k);
//				System.out.println("\t"+k+" "+st.size());
//			}
//		}
		
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
	public void extractForFile(String inFile, Map<String,Map<String,Set<String>>> extractions, Map<String,String> output) throws IOException{
		BufferedReader fread = null;
		try {
			fread = new BufferedReader(new FileReader(inFile));
			String line;
			while ((line = fread.readLine()) != null) {
				String[] parts=line.split("\t");
				String[] segs=line.split("\t",4);
				String query_id=parts[0];
				String slot_name=parts[1];
				String slot_value=null;
				
				
				Map<String,Set<String>> mp=null;
				Set<String> st=null;
				if(parts[3].equals("NIL")){					
					slot_value="NIL";					
					nil_count++;
				}
				else{
					slot_value=parts[4].toLowerCase().trim();
					fill_count++;
				}
				String kk = query_id+"~"+slot_name+"~"+slot_value;
				output.put(kk, segs[3]);
				if(extractions.containsKey(query_id)){
					mp=extractions.get(query_id);
					extractions.remove(query_id);
				}
				else{
					mp = new HashMap<String,Set<String>>();
				}
				
				if(mp.containsKey(slot_name)){
					st=mp.get(slot_name);
					mp.remove(slot_name);
				}
				else{
					st=new HashSet<String>();
				}
				
				if(st.contains(slot_value)==false){
					st.add(slot_value);
				}
				else{
					System.out.println("repeated fill detected: "+slot_value);
					repeated_fills++;
				}
				
				//put updated entries
				mp.put(slot_name, st);
				extractions.put(query_id, mp);
				
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fread != null) {
				fread.close();
			}
		}
	}
	public void getExtractions() throws IOException{
		extractForFile(RE_output1,extractions1,output1);
		extractForFile(RE_output2,extractions2,output2);
	}
	
	public void countCommonAndUniqueExtractions(){
		for(String key : extractions1.keySet()){
			Map<String,Set<String>> mp1 = extractions1.get(key);
			Map<String,Set<String>> mp2 = null;
			
			if(extractions2.containsKey(key)){
				mp2=extractions2.get(key);
			}
			else{
				//there are no results for this query in file2
				//add all extractions in file1 for this query to unique_count1
				for(String k : mp1.keySet()){
					Set<String> st1 = mp1.get(k);
					unique_count1+=st1.size();
					Iterator s=st1.iterator();
					while(s.hasNext()){
						String kk = key+"~"+k+"~"+s.next();
						unique1.put(kk, output1.get(kk));
					}
					
				}
				
				continue;
			}
			
			for(String k : mp1.keySet()){
				Set<String> st1 = mp1.get(k);
				Set<String> st2 = null;
				if(mp2.containsKey(k)){
					st2=mp2.get(k);
				}
				else{
					//there are no results for this slot in file2
					//add all extractions in file1 for this query:slot_type to unique_count1
					unique_count1+=st1.size();
					Iterator s=st1.iterator();
					while(s.hasNext()){
						String kk = key+"~"+k+"~"+s.next();
						unique1.put(kk, output1.get(kk));
					}
					continue;
				}
				
				Set<String> tmp = new HashSet<String>(st1); 
				tmp.retainAll(st2);
				common_count+=tmp.size();
				unique_count1+=st1.size()-tmp.size();
				unique_count2+=st2.size()-tmp.size();
				String kk = key+"~"+k+"~"+"NIL";
				if(tmp.contains("NIL")){
					common_count-=1;
					common.put(kk, "NIL");
					common1.put(kk, "NIL");
					common2.put(kk, "NIL");
				}
				else{
					if(st1.contains("NIL")){
						//unique1.put(kk,"NIL");
						unique_count1-=1;
					}
					if(st2.contains("NIL")){
						//unique2.put(kk,"NIL");
						unique_count2-=1;
					}
				}
				
				
				Iterator it=tmp.iterator();
				while(it.hasNext()){
					String kkk = key+"~"+k+"~"+it.next();
					common.put(kkk, output1.get(kkk));
					common1.put(kkk, output1.get(kkk));
					common2.put(kkk, output2.get(kkk));
				}
				Set<String> unique1_set = new HashSet<String>(st1); 
				unique1_set.removeAll(st2);
				it=unique1_set.iterator();
				while(it.hasNext()){
					String nxt=new String(it.next().toString());
					if(nxt.equals("NIL")){
						System.out.println("here1");
						continue;
					}
					String kkk = key+"~"+k+"~"+nxt;
					unique1.put(kkk, output1.get(kkk));
				}
				
				Set<String> unique2_set = new HashSet<String>(st2); 
				unique2_set.removeAll(st1);
				it=unique2_set.iterator();
				while(it.hasNext()){
					String nxt=new String(it.next().toString());
					if(nxt.equals("NIL")){
						System.out.println("here2");
						continue;
					}
					String kkk = key+"~"+k+"~"+nxt;
					unique2.put(kkk, output2.get(kkk));
				}
				
				//DEBUG
//				Set<String> temp = new HashSet<String>(st1);
//				temp.removeAll(st2);
//				Iterator it = temp.iterator();
//				while(it.hasNext()){
//					String dumm=it.next().toString();
//					if(dumm.equals("NIL")){
//						System.out.println("nil present");
//					}
//					
//				}
//				System.out.println("end");
				
			}
		}
		
		
		for(String key : extractions2.keySet()){
			Map<String,Set<String>> mp1 = null;
			Map<String,Set<String>> mp2 = extractions2.get(key);
			
			if(extractions1.containsKey(key)==false){
				for(String k : mp2.keySet()){
					Set<String> st2 = mp2.get(k);
					unique_count2+=st2.size();
				}
			}
			else{
				mp1 = extractions1.get(key);
				for(String k : mp2.keySet()){
					Set<String> st1 = null;
					Set<String> st2 = mp2.get(k);
				
				
					if(mp1.containsKey(k)==false){
						unique_count2+=st2.size();
					}
				}
				
			}
		
		}
		
		System.out.println("Unique count 1: "+unique_count1);
		System.out.println("Unique count 2: "+unique_count2);
		System.out.println("Common count: "+common_count);
		
		System.out.println("Unique 1 size: "+unique1.size());
		System.out.println("Unique 2 size: "+unique2.size());
		System.out.println("Common size: "+common.size());
		
		System.out.println("DEBUG: Repeated fills: "+repeated_fills);
		System.out.println("DEBUG: Nil count: "+nil_count);
		System.out.println("DEBUG: fill count: "+fill_count);
	}
	
	
	public double noisyAnd(String slot_type, double conf1, double conf2){
		double result=0.0;
		double p1=0.0,p2=0.0;
		
		if(scorer_sys1.mpSlotConfidence_precision.containsKey(slot_type)){
			p1=scorer_sys1.mpSlotConfidence_precision.get(slot_type);
		}
		if(scorer_sys2.mpSlotConfidence_precision.containsKey(slot_type)){
			p2=scorer_sys2.mpSlotConfidence_precision.get(slot_type);
		}
		

		result=p1*conf1*(1-p2)*(1-conf2);
		return result;
	}
	
	public double noisyOr(String slot_type, double conf1, double conf2){
		double result=0.0;
		double p1=0.0,p2=0.0;
		if(scorer_sys1.mpSlotConfidence_precision.containsKey(slot_type)){
			p1=scorer_sys1.mpSlotConfidence_precision.get(slot_type);
		}
		if(scorer_sys2.mpSlotConfidence_precision.containsKey(slot_type)){
			p2=scorer_sys2.mpSlotConfidence_precision.get(slot_type);
		}
		

		result=1-(p1*(1-conf1)*p2*(1-conf2));
		return result;
	}
	
	public String noisyAndOutputs(String slot_type,String output1){
		String result=new String("");
		double conf1=0.0,conf2=0.0;
		String[] pp1 = output1.split("\t");
		
		
		if(pp1.length > 1){
			conf1=Double.parseDouble(pp1[5]);
		}
		
		
		double resConf=noisyAnd(slot_type,conf1,conf2);
		int i=0;
		for(String elem : pp1){
			if(i==5){
				result=result.concat(resConf+"");
				break;
			}
			else{
				result=result.concat(elem);
				result=result.concat("\t");
			}
			i++;
			
		}
		return result;
	}
	
	public String noisyOrOutputs(String slot_type, String output1, String output2){
		String result=new String("");
		
		double conf1=0.0,conf2=0.0;
		String[] pp1 = output1.split("\t");
		
		
		if(pp1.length > 1){
			conf1=Double.parseDouble(pp1[5]);
		}
		
		String[] pp2 = output2.split("\t");
		
		
		if(pp2.length > 1){
			conf2=Double.parseDouble(pp2[5]);
		}
		
		
		double resConf=noisyOr(slot_type,conf1,conf2);
		int i=0;
		for(String elem : pp1){
			if(i==5){
				result=result.concat(resConf+"");
				break;
			}
			else{
				result=result.concat(elem);
				result=result.concat("\t");
			}
			i++;
		}
		return result;
	}
	public void writeCommonOutput(String typeStr,String fname) throws IOException{
		BufferedWriter bw = null;
		
			bw = new BufferedWriter(new FileWriter(fname));
		
			for(String key : common.keySet()){
				String[] parts = key.split("~");
				String query_id = parts[0];
				String slot_name = parts[1];
				String line = parts[0]+"\t"+parts[1]+"\t"+"e"+typeStr+"\t";
				line += common.get(key)+"\n";
				bw.write(line);
			}
			
			bw.close();
				
		
	}
	
	public void writeNoisyCommonOutput(String typeStr,String fname) throws IOException{
		BufferedWriter bw = null;
		
			bw = new BufferedWriter(new FileWriter(fname));
		
			for(String key : common.keySet()){
				String[] parts = key.split("~");
				String query_id = parts[0];
				String slot_name = parts[1];
				String line = parts[0]+"\t"+parts[1]+"\t"+"e"+typeStr+"\t";
				String slot_fill_entry = noisyOrOutputs(parts[1], common1.get(key), common2.get(key));
				line +=slot_fill_entry+"\n";
				bw.write(line);
			}
			
			bw.close();
				
		
	}


	public String gethighestConfidenceFill(String key, String query_id, String slot_name){
		String fkey = "";
		Double conf1=0.0,conf2=0.0;
		String[] pp1 = unique1.get(key).split("\t");
		if(pp1.length > 1){
			conf1=Double.parseDouble(pp1[5]);
		}
		if(extractions2.containsKey(query_id)){
			Map<String,Set<String>> mp = extractions2.get(query_id);
			
			if(mp.containsKey(slot_name)){
				Set<String> st=mp.get(slot_name); //since this is single valued slot, this should have only one element
				Iterator it=st.iterator();
				fkey= "";
				while(it.hasNext()){
					String slot_fill=it.next().toString();
					String new_key=query_id+"~"+slot_name+"~"+slot_fill;
					if(unique2.containsKey(new_key)){
						String[] pp2 = unique2.get(new_key).split("\t");
						if(pp2.length > 1){
							if(conf2<Double.parseDouble(pp2[5])){
								conf2=Double.parseDouble(pp2[5]);
								fkey=new_key;
							}
						}	
						
					}							
				}
			}								
		}
		//choosing the one with greater confidence if there are slot fills for the same slot
		//System.out.println(key+" "+conf1+" "+conf2);
		if(conf1>=conf2){
			return unique1.get(key);
		}
		else{
			return unique2.get(fkey);
		}
	}
	
	
	public void writeNoisyUnionOutput(String typeStr,String fname) throws IOException{
		BufferedWriter bw = null;
		
		bw = new BufferedWriter(new FileWriter(fname));
		//write all unique slot fills from extractor 1
		for(String key : unique1.keySet()){
			String[] parts = key.split("~");
			String query_id = parts[0];
			String slot_name = parts[1];
			String line = query_id+"\t"+slot_name+"\t"+"e"+typeStr+"\t";
			
			
			
			if(singleValuedSlots.contains(slot_name)){
				//if it is a single valued slot then get highest confidence entry among two extractors
				String slot_fill_entry = gethighestConfidenceFill(key,query_id,slot_name);
				slot_fill_entry = noisyAndOutputs(slot_name,slot_fill_entry); 
				line += slot_fill_entry+"\n";
				bw.write(line);
			}				
			else{
				//if it is list valued slot then add the extraction for this slot 
				String slot_fill_entry = noisyAndOutputs(slot_name,unique1.get(key)); 
				line += slot_fill_entry+"\n";
				bw.write(line);
			}				
		}
		bw.close();
		
		bw = new BufferedWriter(new FileWriter(fname,true));
		boolean repeat=false;
		//write all unique slot fills from extractor 2
		for(String key : unique2.keySet()){
			String[] parts = key.split("~");
			String query_id = parts[0];
			String slot_name = parts[1];
			String line = query_id+"\t"+slot_name+"\t"+"e"+typeStr+"\t";
			
			if(singleValuedSlots.contains(slot_name)){
				//check if this was selected as the best confidence slot before
				//if so a corresponding entry would be present in extractions1 for same query_id,slot_name
				if(extractions1.containsKey(query_id)){
					Map<String,Set<String>> mp = extractions1.get(query_id);
					if(mp.containsKey(slot_name)){
						Set<String> st=mp.get(slot_name); //since this single valued, this set should have only one value
						Iterator it=st.iterator();
						while(it.hasNext()){
							String slot_fill=it.next().toString();
							String new_key=query_id+"~"+slot_name+"~"+slot_fill;
							if(unique1.containsKey(new_key)){
								repeat=true;
								break;
							}
						}
						if(repeat){
							repeat=false;
							continue;
						}
					}
				}
				String slot_fill_entry = noisyAndOutputs(slot_name, unique2.get(key));
				line +=slot_fill_entry+"\n";
				bw.write(line);
			}
			else{
				//add all extractions for this slot
				String slot_fill_entry = noisyAndOutputs(slot_name, unique2.get(key));
				line +=slot_fill_entry+"\n";
				bw.write(line);
			}
		}
		
		//write all common slot fills from extractor 1 and 2
		for(String key : common.keySet()){
			String[] parts = key.split("~");
			String line = parts[0]+"\t"+parts[1]+"\t"+"e"+typeStr+"\t";
			String slot_fill_entry = noisyOrOutputs(parts[1], common1.get(key), common2.get(key));
			line +=slot_fill_entry+"\n";
			bw.write(line);
		}
		
		bw.close();
		
	}
	public void writeUnionOutput(String typeStr,String fname) throws IOException{
		BufferedWriter bw = null;
		
			bw = new BufferedWriter(new FileWriter(fname));
			//write all unique slot fills from extractor 1
			for(String key : unique1.keySet()){
				String[] parts = key.split("~");
				String query_id = parts[0];
				String slot_name = parts[1];
				String line = query_id+"\t"+slot_name+"\t"+"e"+typeStr+"\t";
				
				
				//book keeping unique slot wise counts
				if(uniqueSlotWiseCount.containsKey(slot_name)){
					uniqueSlotWiseCount.put(slot_name, uniqueSlotWiseCount.get(slot_name)+1);
				}
				else{
					uniqueSlotWiseCount.put(slot_name, 1);
				}
				
				if(singleValuedSlots.contains(slot_name)){
					//if it is a single valued slot then get highest confidence entry among two extractors
					String slot_fill_entry = gethighestConfidenceFill(key,query_id,slot_name);
					line += slot_fill_entry+"\n";
					bw.write(line);
				}				
				else{
					//if it is list valued slot then add the extraction for this slot 
					line += unique1.get(key)+"\n";
					bw.write(line);
				}				
			}
			bw.close();
			
			bw = new BufferedWriter(new FileWriter(fname,true));
			boolean repeat=false;
			//write all unique slot fills from extractor 2
			for(String key : unique2.keySet()){
				String[] parts = key.split("~");
				String query_id = parts[0];
				String slot_name = parts[1];
				String line = query_id+"\t"+slot_name+"\t"+"e"+typeStr+"\t";
				
				//book keeping unique slot wise counts
				if(uniqueSlotWiseCount.containsKey(slot_name)){
					uniqueSlotWiseCount.put(slot_name, uniqueSlotWiseCount.get(slot_name)+1);
				}
				else{
					uniqueSlotWiseCount.put(slot_name, 1);
				}
				
				if(singleValuedSlots.contains(slot_name)){
					//check if this was selected as the best confidence slot before
					//if so a corresponding entry would be present in extractions1 for same query_id,slot_name
					if(extractions1.containsKey(query_id)){
						Map<String,Set<String>> mp = extractions1.get(query_id);
						if(mp.containsKey(slot_name)){
							Set<String> st=mp.get(slot_name); //since this single valued, this set should have only one value
							Iterator it=st.iterator();
							while(it.hasNext()){
								String slot_fill=it.next().toString();
								String new_key=query_id+"~"+slot_name+"~"+slot_fill;
								if(unique1.containsKey(new_key)){
									repeat=true;
									break;
								}
							}
							if(repeat){
								repeat=false;
								continue;
							}
						}
					}
					
					line += unique2.get(key)+"\n";
					bw.write(line);
				}
				else{
					//add all extractions for this slot
					line += unique2.get(key)+"\n";
					bw.write(line);
				}
			}
			
			//write all common slot fills from extractor 1 and 2
			for(String key : common.keySet()){
				String[] parts = key.split("~");
				String line = parts[0]+"\t"+parts[1]+"\t"+"e"+typeStr+"\t";
				line += common.get(key)+"\n";
				bw.write(line);
			}
			
			bw.close();
			
			int ttl=0;
			
			for(String key : uniqueSlotWiseCount.keySet()){
				ttl+=uniqueSlotWiseCount.get(key);
				System.out.println("unique entries for "+key+" \t "+uniqueSlotWiseCount.get(key));
			}
			System.out.println("total unique count "+ttl);
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		String fname1= new String(args[0]);
		String fname2= new String(args[1]);		
		String key_file = new String(args[2]);
		
		Analyzer myanalyzer=new Analyzer(fname1,fname2);
		myanalyzer.countCommonAndUniqueExtractions();
		
		
		
		/*
		String[] nargs= new String[2];
		nargs[0]="/home/vidhoonv/workspace/RE_output_analysis/2013_output/lsv_output_2013";
		nargs[1]=key_file;
		myanalyzer.scorer_sys1.run(nargs);
		nargs[0]="//home/vidhoonv/workspace/RE_output_analysis/2013_output/Stanford1.output";
		myanalyzer.scorer_sys2.run(nargs);
		
		
		
		myanalyzer.writeNoisyUnionOutput("union1","ensemble_union1.txt");
		myanalyzer.writeNoisyCommonOutput("common1","ensemble_common1.txt");
		*/
		myanalyzer.writeUnionOutput("union2","ensemble_union2.txt");
		myanalyzer.writeCommonOutput("common2","ensemble_common2.txt");
		//myanalyzer.writeUnionOutput("union1","ensemble_union.txt");
		
		//myanalyzer.writeCommonOutput("common1","ensemble_common.txt");
		
		
	}

}
