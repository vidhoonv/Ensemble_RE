package stacking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class postProcessor {

	/**
	 * @param args
	 */
	Map<String,String> mpOutput=new HashMap<String,String>();
	Map<String,Double> mpConfidence=new HashMap<String,Double>();
	Set<String> singleValuedSlots= new HashSet<String>();
	Set<String> filledSlots = new HashSet<String>(); //only tracks single valued slots
	
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
	
	public void getExtractions(String file){
		
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
		
		out.close();
		
	}
	public void processClassifierOutput(String infile) throws IOException{
		int nLines=0, nSkipped=0;
		BufferedReader csv = null;
		csv = new BufferedReader(new FileReader(infile));
		String line;
		csv.readLine(); //skip header
		while ((line = csv.readLine()) != null) {
			nLines++;
			String[] data = line.split("\t");
			if(data.length<11)
				continue;
			if(data[10].equals("0")){
				//classifer predicted that this is not a good extraction
				System.out.println("skipping line");
				nSkipped++;
				continue;
			}
			
			//good extraction
			String query_id = data[1] + "~" + data[2] + "~" + data[5];
			String output_string = new String("");
			if(data[5].equals("NIL")){
				output_string +=  data [1] + "\t" + data [2] + "\t" + data [3] + "\t" + data [5];
			}
			else{
				output_string +=  data [1] + "\t" + data [2] + "\t" + data [3] + "\t" + data [4];
				output_string += "\t" + data [5] + "\t" + data [6] ;
			}
			
			Double conf1=0.0, conf2=0.0;
			conf1=Double.parseDouble(data[7]);
			conf2=Double.parseDouble(data[8]);
			Double diff=conf1-conf2;
			
			String key = data[1] + "~" + data[2];
			if(singleValuedSlots.contains(data[1])){				
				//chose the highest confidence value for extraction				
				if(filledSlots.contains(key)){
					//find which extraction to keep
					Double econf = mpConfidence.get(query_id);
					if(econf-Double.max(conf1,conf2)<0){
						mpConfidence.remove(query_id);
						mpOutput.remove(query_id);
						mpConfidence.put(query_id, Double.max(conf1,conf2));
						mpOutput.put(query_id, output_string);
					}
				}
				else{
					
					if(diff<0){
						if(data[5].equals("NIL")==false)
							output_string += "\t" + conf2;
						mpConfidence.put(query_id,conf2);
					}
					else{
						if(data[5].equals("NIL")==false)
							output_string += "\t" + conf1;
						mpConfidence.put(query_id,conf1);
					}
					mpOutput.put(query_id, output_string);
					filledSlots.add(key);
				}
			}
			else{
				//add the extraction
				//NIL comes after some slot fill - skip NIL
				if(filledSlots.contains(key) && data[5].equals("NIL")){
					continue;
				}
				
				//slot fill comes after NIL - remove previous NIL
				if(filledSlots.contains(key)){
					System.out.println("here " + key );
					if(mpOutput.containsKey(key+"~"+"NIL")){						
						mpOutput.remove(key+"~"+"NIL");
						mpConfidence.remove(key+"~"+"NIL");
					}						
				}
				
				if(diff<0){
					if(data[5].equals("NIL")==false)
						output_string += "\t" + conf2;
					mpConfidence.put(query_id,conf2);
				}
				else{
					if(data[5].equals("NIL")==false)
						output_string += "\t" + conf1;
					mpConfidence.put(query_id,conf1);
				}
				mpOutput.put(query_id, output_string);
				filledSlots.add(key);
			}
		}
		
		csv.close();
		System.out.println("Total lines: "+nLines);
		System.out.println("Skipped lines: "+nSkipped);
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String fname=new String(args[0]);
		String outFile=new String(args[1]);
		postProcessor pp = new postProcessor();
		pp.processClassifierOutput(fname);
		pp.writeOutputFile(outFile);
	}

}
