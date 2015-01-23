package MultipleSystems.stacking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataExtractor {

	/**
	 * @param args
	 */
	
	int numSystems;
	String[] REOutputs;
	Set<String>[] extractions;
	Map<String,String>[] outputs;
	Map<String,List<Double>> fextractions_confs;
	Map<String,Integer> fextractions_target;
	Map<String,String> fextractions_output;
	scorer2013[] scorers_2013;
	scorer2014[] scorers_2014;
	
	public DataExtractor(int nsys){
		numSystems = nsys;
		REOutputs = new String[numSystems];
//		extractions = new Set[numSystems];
//		for(Set extraction : extractions){
//			extraction = new HashSet<String>();
//		}
//		
//		outputs = new Map[numSystems];
//		for(Map mp : outputs){
//			mp = new HashMap<String,String>();
//		}
		fextractions_confs = new HashMap<String,List<Double>>();
		
		scorers_2013 = new scorer2013[numSystems];
		for(int i=0;i<numSystems;i++)
			scorers_2013[i] = new scorer2013();
		scorers_2014 = new scorer2014[numSystems];
		for(int i=0;i<numSystems;i++)
			scorers_2014[i] = new scorer2014();
		
		fextractions_target = new HashMap<String,Integer>();
		fextractions_output = new HashMap<String,String>();
	}
	
	public void getFiles(String path){
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
			int k=0;
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		        REOutputs[k] = path+listOfFiles[i].getName();
		        System.out.println(REOutputs[k]);
		        k++;
		      }
		    }
	}
	/*
	public void extractFillsFromFile(int k) throws IOException{
		BufferedReader fread = null;
		String inFile = REOutputs[k];
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
					continue; //skip NIL slot fills
				}
				else{
					slot_value=parts[4].toLowerCase().trim();
				}
				String key = query_id+"~"+slot_name+"~"+slot_value;				
				if(extractions[k].contains(key)==false){
					extractions[k].add(key);
					outputs[k].put(key, segs[3]);
				}
				else{
					System.out.println("repeated fill detected: "+key);
				}				
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
	
	*/
	public void getSlotsAndConfidences(String year){
		Map<String,Double> mp1=null,mp2=null;
		Map<String,Integer> t1=null,t2=null;
		Map<String,String> mpOut1=null,mpOut2=null;
		
		for(int i=0;i<numSystems;i++){
			if(year.equals("2013")){
				mp1=scorers_2013[i].mpConfidence;				
				t1=scorers_2013[i].mpTarget;
				mpOut1=scorers_2013[i].mpOutput;
			}
			else if(year.equals("2014")){
				mp1=scorers_2014[i].mpConfidence;				
				t1=scorers_2014[i].mpTarget;
				mpOut1=scorers_2014[i].mpOutput;
			}
			
			for(String mp1key : mp1.keySet()){
				if(fextractions_confs.containsKey(mp1key)){
					continue;
				}
				else{
					
					//add confidence for mp1
					ArrayList<Double> confs = new ArrayList<Double>();
					confs.add(mp1.get(mp1key));
					fextractions_target.put(mp1key, t1.get(mp1key));
					fextractions_output.put(mp1key, mpOut1.get(mp1key));
					
					for(int j=0;j<numSystems;j++){
						if(i==j){
							continue;
						}
						
						if(year.equals("2013")){
							mp2=scorers_2013[j].mpConfidence;				
							t2=scorers_2013[j].mpTarget;
							mpOut2=scorers_2013[j].mpOutput;
						}
						else if(year.equals("2014")){
							mp2=scorers_2014[j].mpConfidence;				
							t2=scorers_2014[j].mpTarget;
							mpOut2=scorers_2014[j].mpOutput;
						}
						
						
						if(mp2.containsKey(mp1key)){
							//common slot
							confs.add(mp2.get(mp1key));
							int target = t2.get(mp1key);
							String out = mpOut2.get(mp1key);
							if(target==1){
								if(fextractions_target.containsKey(mp1key)){
									if(fextractions_target.get(mp1key)==0){
										fextractions_target.remove(mp1key);
										fextractions_output.remove(mp1key);
									}									
								}
								fextractions_target.put(mp1key, target);
								fextractions_output.put(mp1key, out);
							}
						}
						else{
							//slot fill is not present 
							confs.add(0.0);
						}
						
					}
					fextractions_confs.put(mp1key, confs);
				}
			}			
		}
	}
	
	
	public void writeOutput(String outfile) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
		BufferedWriter bfeatures = new BufferedWriter(new FileWriter(outfile+".features"));
		
		String header = new String("");
		
		for(int i=0;i<numSystems;i++){
			int tmp=i+1;
			header += "conf_"+tmp+ "\t";
		}
		header += "relationtype";
		header += "\t" + "target";
		bfeatures.write(header+"\n");
		for(String key : fextractions_confs.keySet()){
			ArrayList<Double> confs = (ArrayList<Double>) fextractions_confs.get(key);
			String conf_str =  "";
			String delimiter = "\t";
			
			for(Double cf : confs){
				conf_str += cf + "\t";
			}
			conf_str = conf_str.trim();
			
			String[] parts = key.split("~");
			String relationType = parts[1];
			
			String out_str = fextractions_output.get(key);
			
			out_str += delimiter + conf_str;
			
			out_str += delimiter + fextractions_target.get(key);
			
			bw.write(out_str+"\n");
			bfeatures.write(conf_str+"\t"+relationType+"\t"+fextractions_target.get(key)+"\n");
		}
		
		bw.close();
		bfeatures.close();
	}
	/*
	 * args[0] = input files directory
	 * args[1] = output file
	 * args[2] = year			
			System.out.println("DEBUG: Repeated fills: "+repeatedFills);
			System.out.println("DEBUG: Nil count: "+nilCount);
			System.out.println("DEBUG: fill count: "+fillCount);

	 * args[3] = key file
	 * args[4] = number of systems
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String inputDir = new String(args[0]);
		String outputFile = new String(args[1]);
		String year = new String(args[2]);
		String key = new String(args[3]);
		Integer nsys = Integer.parseInt(args[4]);
		
		DataExtractor de = new DataExtractor(nsys);
		de.getFiles(inputDir);
		
		for(int i=0;i<nsys;i++){
			System.out.println("here");
			//run scorer
			String[] nargs=new String[3];
			nargs[0]=de.REOutputs[i];
			nargs[1]=key;
			nargs[2]= new String("anydoc");
			
			if(year.equals("2013")){
					de.scorers_2013[i].run(nargs);
			}
			else if(year.equals("2014")){
					de.scorers_2014[i].run(nargs);				
			}
			
		}
		
		de.getSlotsAndConfidences(year);
		de.writeOutput(outputFile);
		
		

	}

}
