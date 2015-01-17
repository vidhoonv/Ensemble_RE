package stackingm2;

import java.util.HashMap;
import java.util.Map;

public class FeatureExtractor {
	Map<String,Double> features =null;

	
	
	public FeatureExtractor(){
		features = new HashMap<String,Double>();
		
		
	}
	
	public void populateFeatures(DataExtractor de,String key,String relationName,Double conf1, Double conf2){
		//features.put("",);
		
		//relation ID
		features.put("E-relID",new Double(de.relationIDs.get(relationName)));
		
		//conf1
		features.put("A-conf1", conf1);
		//conf2
		features.put("B-conf2", conf2);
		
		//relation group ID
		features.put("F-groupID", new Double(de.relationGroupIDs.get(relationName)));

		//slot type
		if(de.singleValuedSlots.contains(relationName)){
			features.put("G-slotType",0.0);
		}
		else{
			features.put("G-slotType",1.0);
		}

		//entity type
		if(key.contains("per")){
			features.put("H-entType",0.0);
		}
		else{
			features.put("H-entType",1.0);
		}
		
		/*
		 * LINEAR FEATURES
		 * 
		 * 
		 * x2	y2	x3	y3	xy	x2y	xy2	x2y2	x3y	x3y2	x3y3	xy3	x2y3	s	d	s2	d2	s3	d3	sd	s2d	sd2	s2d2
		 * 	sd3	s2d3	s3d	s3d2	s3d3	sx	sy	dx	dy	s2x	s2y	d2x	d2y	s3x	s3y	d3x	d3y	sdx	sdy
		
		 */
		
		Double conf1_sq = new Double(conf1*conf1);
		Double conf1_cube = new Double(conf1_sq*conf1);
		
		Double conf2_sq = new Double(conf2*conf2);
		Double conf2_cube = new Double(conf2_sq*conf2);
		
		Double prod_conf = new Double(conf1*conf2);
		Double x2y = new Double(conf1_sq*conf2);
		Double xy2 = new Double(conf1*conf2_sq);
		Double x2y2 = new Double(conf1_sq*conf2_sq);
		Double x3y = new Double(conf1_cube*conf2);
		Double x3y2 = new Double(conf1_cube*conf2_sq);
		Double x3y3 = new Double(conf1_cube*conf2_cube);
		Double xy3 = new Double(conf1*conf2_cube);
		Double x2y3 = new Double(conf1_sq*conf2_cube);
		Double sum = new Double(conf1+conf2);
		Double diff = new Double(conf1-conf2);
		Double sum_sq = new Double(sum*sum);
		Double sum_cube = new Double(sum_sq*sum);
		Double diff_sq = new Double(diff*diff);
		Double diff_cube = new Double(diff_sq*diff);
		
		Double prod_sd = new Double(sum* diff);
		Double s2d = new Double(sum_sq*diff);
		Double sd2 = new Double(sum*diff_sq);
		Double s2d2 = new Double(sum_sq*diff_sq);
		
		Double sd3 = new Double(sum*diff_cube);
		Double s2d3 =  new Double(sum_sq*diff_cube);
		Double s3d =  new Double(sum_cube*diff);
		Double s3d2 =  new Double(sum_cube*diff_sq);
		Double s3d3 = new Double(sum_cube*diff_cube);
		
		Double sx =  new Double(sum*conf1);
		Double sy =  new Double(sum*conf2);
		Double dx = new Double(diff*conf1);
		Double dy = new Double(diff*conf2);
		Double s2x = new Double(sum_sq*conf1);
		Double s2y = new Double(sum*conf2);
		Double d2x = new Double(diff_sq*conf1);
		Double d2y = new Double(diff_sq*conf2);
		Double s3x =  new Double(sum_cube*conf1);
		Double s3y =  new Double(sum_cube*conf2);
		Double d3x =  new Double(diff_cube*conf1);
		Double d3y =  new Double(diff_cube*conf2);
		Double sdx =  new Double(sum*diff*conf1);
		Double sdy =  new Double(sum*diff*conf2);
		
		//target
		features.put("C-conf1_sq",conf1_sq);
		features.put("C-conf1_cube",conf1_cube);
		features.put("C-conf2_sq",conf2_sq);
		features.put("C-conf2_cube",conf2_cube);
		features.put("C-prod_conf",prod_conf);
		features.put("C-x2y",x2y);
		features.put("C-xy2",xy2);
		features.put("C-x2y2",x2y2);
		features.put("C-x3y",x3y);
		features.put("C-x3y2",x3y2);
		features.put("C-x3y3",x3y3);
		
		features.put("C-xy3",xy3);
		features.put("C-x2y3",x2y3);
		features.put("D-sum",sum);
		features.put("D-diff",diff);
		features.put("D-sum_sq",sum_sq);
		features.put("D-sum_cube",sum_cube);
		features.put("D-diff_sq",diff_sq);
		features.put("D-diff_cube",diff_cube);
		features.put("D-prod_sd",prod_sd);
		features.put("D-s2d",s2d);
		features.put("D-sd2",s2d);
		
		features.put("D-s2d2",s2d2);
		features.put("D-sd3",sd3);
		features.put("D-s2d3",s2d3);
		features.put("D-s3d",s3d);
		features.put("D-s3d2",s3d2);
		features.put("D-s3d3",s3d3);
		features.put("D-sx",sx);
		features.put("D-sy",sy);
		features.put("D-dx",dx);
		features.put("D-dy",dy);
		
		features.put("D-s2x",s2x);
		features.put("D-s2y",s2y);
		features.put("D-d2x",d2x);
		features.put("D-d2y",d2y);
		features.put("D-s3x",s3x);
		features.put("D-s3y",s3y);
		features.put("D-d3x",d3x);
		features.put("D-d3y",d3y);
		features.put("D-sdx",sdx);
		features.put("D-sdy",sdy);
		
		
	}
	


}
