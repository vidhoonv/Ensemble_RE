package MultipleSystems.aliasing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/*
 * this class is used to extract unique 
 * fills by slot type and put them in 
 * separate files in sorted order to get 
 * an overall idea of what kind of fills are 
 * produced and what kind of filters need
 * to be employed for them.
 */
public class FillAnalysis {

	String fillsFile = null;
	String outputDir = null;
	Map<String,SortedSet<String>> mp = null;
	Set<String> slots = new HashSet<String>();
	
	public FillAnalysis(String inFile, String outDir){
		//add slot types
		slots.add("per:alternate_names");
		slots.add("per:date_of_birth");
		slots.add("per:age");
		slots.add("per:country_of_birth");
		slots.add("per:stateorprovince_of_birth");

		slots.add("per:city_of_birth");
		slots.add("per:origin");
		slots.add("per:date_of_death");
		slots.add("per:country_of_death");
		slots.add("per:stateorprovince_of_death");

		slots.add("per:city_of_death");
		slots.add("per:cause_of_death");
		slots.add("per:countries_of_residence");
		slots.add("per:statesorprovinces_of_residence");
		slots.add("per:cities_of_residence");

		slots.add("per:schools_attended");
		slots.add("per:title");
		slots.add("per:employee_or_member_of");
		slots.add("per:religion");
		slots.add("per:spouse");

		slots.add("per:children");
		slots.add("per:parents");
		slots.add("per:siblings");
		slots.add("per:other_family");
		slots.add("per:charges");


		//add org slots
		slots.add("org:alternate_names");
		slots.add("org:political_religious_affiliation");
		slots.add("org:top_members_employees");
		slots.add("org:number_of_employees_members");
		slots.add("org:members");

		slots.add("org:member_of");
		slots.add("org:subsidiaries");
		slots.add("org:parents");
		slots.add("org:founded_by");
		slots.add("org:date_founded");

		slots.add("org:date_dissolved");
		slots.add("org:country_of_headquarters");
		slots.add("org:stateorprovince_of_headquarters");
		slots.add("org:city_of_headquarters");
		slots.add("org:shareholders");
		slots.add("org:website");
		
		
		fillsFile = new String(inFile);
		outputDir = new String(outDir);
		mp = new HashMap<String,SortedSet<String>>();
		
		for(String slotType : slots){
			mp.put(slotType, new TreeSet<String>());
		}
	}
	
	public void collectFills() throws IOException{
		BufferedReader fread = new BufferedReader(new FileReader(fillsFile));
		String line;
		int fillIndex = 4;
		int slotIndex = 1;
		while ((line = fread.readLine()) != null){
			String[] parts =  line.split("\t");
			if(parts.length<7)
				continue;
			
			SortedSet<String> stSet = mp.get(parts[slotIndex]);
			stSet.add(parts[fillIndex]);			
		}		
		fread.close();		
	}
	
	public void writeFills() throws IOException{
		for(String s : slots){
			String fname = new String(outputDir+s+".txt");
			BufferedWriter fwrite = new BufferedWriter(new FileWriter(fname));
			SortedSet<String> stSet = mp.get(s);
			
			for(String fill : stSet){
				fwrite.write(fill+"\n");
			}
			
			fwrite.close();
		}
	}
	
	/**
	 * @param args
	 * args[0] - a file that can be used by scorer
	 * args[1] -  output directory to store files for each slot type
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		String inputFile = new String("/home/vidhoonv/workspace/RE_ensemble/experiments/aliasing/ensemble_alias.txt");
		String outputDir = new String("/home/vidhoonv/workspace/RE_ensemble/experiments/aliasing/fill_analysis/");
		
		FillAnalysis fa = new FillAnalysis(inputFile,outputDir);
		fa.collectFills();
		fa.writeFills();
		//FillAnalysis fa = new FillAnalysis(args[0],args[1]);
	}

}
