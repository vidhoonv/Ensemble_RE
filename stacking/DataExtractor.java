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
	
	scorer2013 s1_2013=new scorer2013();
	scorer2013 s2_2013= new scorer2013();
	
	scorer2014 s1_2014=new scorer2014();
	scorer2014 s2_2014= new scorer2014();
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String fname1 = new String(args[0]);
		String fname2 = new String(args[1]);
		
		String key_file = new String(args[2]);
		String year = new String(args[3]);
		DataExtractor de = new DataExtractor();
		
		
		String[] nargs=new String[2];
		nargs[0]=fname1;
		nargs[1]=key_file;
		
		Map<String,Double> mp1=null,mp2=null;
		Map<String,Integer> t1=null,t2=null;
		Map<String,String> mpOut1=null,mpOut2=null;

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
			
			
			mp1=de.s1_2013.mpConfidence;
			mp2=de.s2_2013.mpConfidence;
			
			t1=de.s1_2013.mpTarget;
			t2=de.s2_2013.mpTarget;
			
			mpOut1=de.s1_2013.mpOutput;
			mpOut2=de.s2_2013.mpOutput;
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
			
			
			mp1=de.s1_2014.mpConfidence;
			mp2=de.s2_2014.mpConfidence;
			
			t1=de.s1_2014.mpTarget;
			t2=de.s2_2014.mpTarget;

			mpOut1=de.s1_2014.mpOutput;
			mpOut2=de.s2_2014.mpOutput;

		}
		
		
		BufferedWriter bw = null;		
		bw = new BufferedWriter(new FileWriter(year+"-data.txt"));
		
		
		/*
		 * 
		 * add for unique entries and common entries in output file of extractor 1
		 * 
		 */
		 
		int counter=0;
		for(String key : mp1.keySet()){
			Double conf1, conf2;
			Integer target;
			String output1;
			conf1=mp1.get(key);
			target=t1.get(key);
			output1=mpOut1.get(key);
			if(mp2.containsKey(key)){
				counter+=1;
				conf2=mp2.get(key);
			}
			else{
				conf2=0.0;
			}
			
			bw.write(output1+"\t"+conf1+"\t"+conf2+"\t"+target+"\n");
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
			String output2;
			if(mp1.containsKey(key)==false){
				conf2=mp2.get(key);
				target=t2.get(key);
				conf1=0.0;
				output2=mpOut2.get(key);
				bw.write(output2+"\t"+conf1+"\t"+conf2+"\t"+target+"\n");
			}
			
		}	
		bw.close();
		System.out.println("common count : "+counter);
	}

}
