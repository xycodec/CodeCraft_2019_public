package com.huawei.utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
public class CrossInfo {
	public static ArrayList<ArrayList<Integer>> Crosses=new ArrayList<ArrayList<Integer>>();
	public static ArrayList<ArrayList<Integer>> Cars=new ArrayList<ArrayList<Integer>>();
	public static ArrayList<ArrayList<Integer>> Roads=new ArrayList<ArrayList<Integer>>();
	public static ArrayList<ArrayList<Integer>> PreAns=new ArrayList<ArrayList<Integer>>();
	public static ArrayList<ArrayList<Integer>> get_info(String fname) throws Exception{
		FileReader fr=new FileReader(fname);
        BufferedReader br=new BufferedReader(fr);
        
        String line; 
        ArrayList<ArrayList<Integer>> crosses = new ArrayList<ArrayList<Integer>>();
        String[] sp; 
        int flag=0;
        while((line=br.readLine())!=null){ 
        	if(flag!=0) {
        		ArrayList<Integer> lineArray=new ArrayList<Integer>();
        		line=line.substring(1,line.length()-1);
        		sp=line.split("\\s*,\\s*");
        		//System.out.println(sp.length);
            	for(int i=0;i<sp.length;i++) {
            		if(sp[i]!="(" && sp[i]!=")") {
            			lineArray.add(Integer.parseInt(sp[i]));
            			//System.out.println(lineArray);
            		}
            	}
            	crosses.add(lineArray);
        	}
        	flag++;
	    }
        br.close();
		return crosses;
	}
	
	public static void read(String carPath,String roadPath,
			String crossPath,String preAnsPath)  {
		
		try {
			Crosses=get_info(crossPath);
			Cars=get_info(carPath);
			Roads=get_info(roadPath);
			PreAns=get_info(preAnsPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
        
}
