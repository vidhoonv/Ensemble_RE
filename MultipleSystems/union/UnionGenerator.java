package MultipleSystems.union;

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

public class UnionGenerator {

	/**
	 * @param args
	 */
	
	/*
	 * This class generates a union output from output files of
	 * different relation extraction systems. The output files 
	 * from SF Validation task do not contain NIL slot fills.
	 * 
	 */
	
	Integer numSystems=null;
	String[] REOutput;
	int commonCount;

	int repeatedFills,nilCount, fillCount;
	Map<String,String> uniqueOutputs;
	Set<String> uniqueExtractions;
	Set<String> nilExtractions;
	Set<String> singleValuedSlots;
	Set<String> filledSingleValuedSlots;
	//SFScore[] scorers;
	String typeStr=null;
	String outFile=null;
	
	Map<String,Boolean> slotfills = null;
	Set<String> perSlots = null;
	Set<String> orgSlots = null;
	
	public UnionGenerator(int nsystems){
		numSystems = nsystems;
		REOutput = new String[numSystems];
	
		uniqueExtractions = new HashSet<String>();
		nilExtractions = new HashSet<String>();
		uniqueOutputs = new HashMap<String,String>();
		

		commonCount = 0;
		repeatedFills=0;
		nilCount=0;
		fillCount=0;
	
		singleValuedSlots = new HashSet<String>();
		filledSingleValuedSlots = new HashSet<String>();
	/*
	  scorers = new SFScore[numSystems];
	 
		
		for(SFScore sf : scorers){
			sf = new SFScore();
		}
	*/	
		typeStr = new String("UnionGen1");
		outFile = new String("unionGeneratorOutput.txt");
		
		slotfills = new HashMap<String,Boolean>();
		perSlots = new HashSet<String>();
		orgSlots = new HashSet<String>();
		populateSingleValuedSlots();
		populateSlotFills();
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
	
	public void extractUnionFromFile(String inFile) throws IOException{
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

				if(parts[3].equals("NIL")){					
					slot_value="NIL";					
					nilCount++;
				}
				else{
					slot_value=parts[4].toLowerCase().trim();
					fillCount++;
				}
				String key = query_id+"~"+slot_name+"~"+slot_value;				
				if(uniqueExtractions.contains(key)==false){
					uniqueExtractions.add(key);
					uniqueOutputs.put(key, segs[3]);
					slotfills.put(query_id+"~"+slot_name, true);
				}
				else{
					System.out.println("repeated fill detected: "+key);
					repeatedFills++;
				}				
			}
			
			System.out.println("DEBUG: Repeated fills: "+repeatedFills);
			System.out.println("DEBUG: Nil count: "+nilCount);
			System.out.println("DEBUG: fill count: "+fillCount);
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fread != null) {
				fread.close();
			}
		}
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
			
	}
	
	public void addNILFills(){
		String queryPrefix = new String("SF14_ENG_");
		String delimiter = new String("~");
		
		for(int i=1;i<=100;i++){
			String queryID = queryPrefix+String.format("%03d", i);
			
			if(i<=50){
				//per type
				for(String slotName : perSlots){
					String key = new String(queryID+"~"+slotName);
					if(slotfills.containsKey(key)==false){
						//no slot fill
						nilExtractions.add(queryID+"\t"+slotName+"\t"+typeStr+"\t"+"NIL");
					}
				}
			}
			else{
				//org type
				for(String slotName : orgSlots){
					String key = new String(queryID+"~"+slotName);
					if(slotfills.containsKey(key)==false){
						//no slot fill
						nilExtractions.add(queryID+"\t"+slotName+"\t"+typeStr+"\t"+"NIL");
					}
				}
			}
		
		}	
	}
	public String gethighestConfidenceFill(String key, String output){
		
		String chosenSlotFill = output, chosenKey=null;
		Double conf1=0.0,conf2=0.0;
		String[] outputParts = output.split("\t");
		if(outputParts.length > 1){
			conf1 = Double.parseDouble(outputParts[3]);
		}	
		
		String[] parts = key.split("~");		
		String eKey = new String(parts[0]+"~"+parts[1]);
		
		for(String entry : uniqueExtractions){
			if(entry.contains(eKey)){
				//slot fill for the same slot
				if(entry.equals(key) == false){
					//slot fill is not same
					String newFill = uniqueOutputs.get(entry);
					String[] parts2 = newFill.split("\t");
					if(parts2.length > 1){
						conf2=Double.parseDouble(parts2[3]);
						if(conf2.compareTo(conf1)>0){
							chosenSlotFill = newFill;
							chosenKey = entry;
						}
						
					}
				}
			}
		}
				
		return chosenSlotFill;

	}
	
	public void writeUnionSystemsSlots() throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		for(String fillEntry : uniqueExtractions){
			String[] parts = fillEntry.split("~");
			String queryId = new String(parts[0]);
			String slotName = new String(parts[1]);
			String slotFill = new String(parts[2]);
			String line = queryId+"\t"+slotName+"\t"+typeStr+"\t";
			
			if(singleValuedSlots.contains(slotName)){
				if(filledSingleValuedSlots.contains(queryId+"\t"+slotName)){
					//already filled slot with highest confidence fill
					continue;
				}
				//if it is a single valued slot then get highest confidence entry among two extractors
				String slot_fill_entry = gethighestConfidenceFill(fillEntry,uniqueOutputs.get(fillEntry));
				line += slot_fill_entry+"\n";
				bw.write(line);
				filledSingleValuedSlots.add(queryId+"\t"+slotName);
			}				
			else{
				//if it is list valued slot then add the extraction for this slot 
				line += uniqueOutputs.get(fillEntry)+"\n";
				bw.write(line);
			}	
		}
		int nc=0;
		for(String nilEntry : nilExtractions){
			bw.write(nilEntry+"\n");
			nc++;
		}
		System.out.println("nil entries written: "+nc);
		
		bw.close();
	
	}
	public void getExtractions() throws IOException{
		
		for(int i=0;i<numSystems;i++){
			System.out.println("Processing file "+REOutput[i]);
			extractUnionFromFile(REOutput[i]);
		}
	}
	
	public void getFiles(String path){
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
			int k=0;
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        REOutput[k] = path+"/"+listOfFiles[i].getName();
		        k++;
		      }
		    }
	}
	/*
	 * args[0]	=	path of all the system files
	 * args[1]  =   number of systems
	 * 
	 * 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		String systemFilesPath = new String(args[0]);
		Integer numSys = Integer.parseInt(args[1]);
		UnionGenerator mygen = new UnionGenerator(numSys);
		
		mygen.getFiles(systemFilesPath);
		mygen.getExtractions();
		mygen.addNILFills();
		mygen.writeUnionSystemsSlots();
		

	}

}
