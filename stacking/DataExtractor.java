package stacking;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/*
 * Extract training data for classifiers from 
 * output file of extractors and key file.
 * 
 * 
 * 
 */
public class DataExtractor {

	/**
	 * @param args
	 */
	
	scorer s1=new scorer();
	scorer s2= new scorer();
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String fname1 = new String(args[0]);
		String fname2 = new String(args[1]);
		
		String key_file = new String(args[2]);
		DataExtractor de = new DataExtractor();
		
		String[] nargs=new String[2];
		nargs[0]=fname1;
		nargs[1]=key_file;
		try {
			de.s1.run(nargs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nargs[0]=fname2;
		try {
			de.s2.run(nargs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Map<String,Double> mp1=de.s1.mpConfidence;
		Map<String,Double> mp2=de.s2.mpConfidence;
		
		Map<String,Integer> t1=de.s1.mpTarget;
		Map<String,Integer> t2=de.s2.mpTarget;
		
		BufferedWriter bw = null;		
		bw = new BufferedWriter(new FileWriter("training-data.txt"));
		
		
		/*
		 * 
		 * add for unique entries and common entries in output file of extractor 1
		 * 
		 */
		 
		for(String key : t1.keySet()){
			System.out.println(key);
		}
		for(String key : mp1.keySet()){
			Double conf1, conf2;
			Integer target;
			conf1=mp1.get(key);
			target=t1.get(key);
			if(mp2.containsKey(key)){
				conf2=mp2.get(key);
			}
			else{
				conf2=0.0;
			}
			
			bw.write(key+"\t"+conf1+"\t"+conf2+"\t"+target+"\n");
			//System.out.println(key+"\t"+conf1+"\t"+conf2+"\t"+target);
		}
		/*
		 * 
		 * add for unique entries in output file of extractor 2
		 * 
		 */
		for(String key : mp2.keySet()){
			Double conf1, conf2;
			Integer target;
			if(mp1.containsKey(key)==false){
				conf2=mp2.get(key);
				target=t2.get(key);
				conf1=0.0;
				bw.write(key+"\t"+conf1+"\t"+conf2+"\t"+target+"\n");
			}
			
		}
		
		
		bw.close();
		 
	}

}
