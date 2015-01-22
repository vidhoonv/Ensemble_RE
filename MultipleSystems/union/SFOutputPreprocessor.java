package MultipleSystems.union;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SFOutputPreprocessor {

	/**
	 * @param args
	 */
	Map<String,Boolean> slotfills = null;
	Set<String> perSlots = null;
	Set<String> orgSlots = null;
	
	Map<String,String> outputs;
	Set<String> extractions;
	Set<String> nilExtractions;
	String typeStr=null;
	String outFile=null;
	SFScore2014 scorer=null;

	int repeatedFills,nilCount, fillCount;
	
	Integer numSystems=null;
	String[] REOutput;
	
	public SFOutputPreprocessor(int nsystems){
		numSystems = nsystems;
		REOutput = new String[numSystems];
		
		extractions = new HashSet<String>();
		nilExtractions = new HashSet<String>();
		outputs = new HashMap<String,String>();
		
		repeatedFills=0;
		nilCount=0;
		fillCount=0;
		
		scorer = new SFScore2014();		
		slotfills = new HashMap<String,Boolean>();
		perSlots = new HashSet<String>();
		orgSlots = new HashSet<String>();
		
		typeStr = new String("");
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
	
	public void extractFillsFromFile(String inFile) throws IOException{
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
				typeStr = parts[2];
				if(parts[3].equals("NIL")){					
					slot_value="NIL";					
					nilCount++;
				}
				else{
					slot_value=parts[4].toLowerCase().trim();
					fillCount++;
				}
				String key = query_id+"~"+slot_name+"~"+slot_value;				
				if(extractions.contains(key)==false){
					extractions.add(key);
					outputs.put(key, segs[3]);
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
	
	public void getFiles(String path){
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
			int k=0;
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        REOutput[k] = path+listOfFiles[i].getName();
		        System.out.println(REOutput[k]);
		        k++;
		      }
		    }
	}
	
	public void writeUnionSystemsSlots() throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		for(String fillEntry : extractions){
			String[] parts = fillEntry.split("~");
			String queryId = new String(parts[0]);
			String slotName = new String(parts[1]);
			String slotFill = new String(parts[2]);
			String line = queryId+"\t"+slotName+"\t"+typeStr+"\t";


			//if it is list valued slot then add the extraction for this slot 
			line += outputs.get(fillEntry)+"\n";
			bw.write(line);
		}

		int nc=0;
		for(String nilEntry : nilExtractions){
			bw.write(nilEntry+"\n");
			nc++;
		}
		System.out.println("nil entries written: "+nc);

		bw.close();
	
	}
	/*
	 * args[0] = path of all SF system outputs
	 * args[1] = output path
	 * args[2] = results summary file of all SF systems
	 * args[3] = key file
	 * 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String inputFilesPath = new String(args[0]);
		String outputPath = new String(args[1]);
		String summaryPath = new String(args[2]);
		String keyPath = new String(args[3]);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(summaryPath));
		
		SFOutputPreprocessor sfp= new SFOutputPreprocessor(65);
		sfp.getFiles(inputFilesPath);
		sfp.populateSlotFills();
		
		bw.write("System ID \t Precision \t Recall \t F1\n");
		for(String inFile : sfp.REOutput){
			//extract fills from output file of SF System
			sfp.extractFillsFromFile(inFile);
			
			//add NIL fillers for missing slot fills
			sfp.addNILFills();
		
			//write output with NIL
			sfp.outFile = outputPath+sfp.typeStr+".txt";
			sfp.writeUnionSystemsSlots();
			
			//run scorer
			String[] nargs = new String[3];
			nargs[0] = sfp.outFile;
			nargs[1] = keyPath;
			nargs[2]= new String("anydoc");
			sfp.scorer.run(nargs);
			
			bw.write(sfp.typeStr+"\t"+sfp.scorer.precision+"\t"+sfp.scorer.recall+"\t"+sfp.scorer.F+"\n");
			//clear everything
			sfp.extractions.clear();
			sfp.outputs.clear();
			sfp.nilExtractions.clear();
			sfp.slotfills.clear();
			sfp.repeatedFills=0;
			sfp.nilCount=0;
			sfp.fillCount=0;
			sfp.typeStr = new String("");
			sfp.outFile = new String("");
			sfp.scorer = new SFScore2014();
			
		}
		
		
		bw.close();
	}

}
