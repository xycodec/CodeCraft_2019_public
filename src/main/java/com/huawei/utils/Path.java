package com.huawei.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Path implements Comparable,Cloneable{
	public ArrayList<Integer> path;
	public double dist;
	public Path(ArrayList<Integer> path,double dist){
		this.path=path;
		this.dist=dist;
	}

	@Override
	public int compareTo(Object o) {
		Path p=(Path)o;
		if(this.dist>p.dist) return -1;
		else if(this.dist==p.dist) return 0;
		else return 1;
	}
	
	@Override
	public int hashCode() {
		return path.hashCode()+(int)dist*13;
	}
	
	@Override
	public boolean equals(Object obj) {
		Path p=(Path)obj;
		if(this.dist!=p.dist) return false;
		int len1=this.path.size();
		int len2=p.path.size();
		if(len1!=len2) return false;
		for(int i=0;i<len1;++i) {
			if(this.path.get(i)!=p.path.get(i)) return false;
		}
		return true;
			
	}
	
	@Override
	public Path clone(){
		Path tmp;
		try {
			tmp=(Path) super.clone();
		}catch(CloneNotSupportedException e) {
			throw new RuntimeException("This calss not implement Cloneable");
		}
		tmp.path=(ArrayList<Integer>)this.path.clone();
		tmp.dist=this.dist;
		return tmp;
	}
	
//	public static void main(String[] args) {
//		Map<Path, Boolean> dev_path_sql=new HashMap<Path, Boolean>();
//		ArrayList<Integer> v1= new ArrayList<>();
//		v1.add(1);
//		v1.add(2);
//		ArrayList<Integer> v2= new ArrayList<>();
//		v2.add(1);
//		v2.add(2);
//		Path p1=new Path(v1,10);
//		Path p2=new Path(v2,10);
//		dev_path_sql.put(p1.clone(), true);
//		System.out.println(dev_path_sql.containsKey(p2));
//		p1.path.add(3);
//		System.out.println(dev_path_sql.containsKey(p2));
//		dev_path_sql.put(new Path(v2,10), true);
//		System.out.println(dev_path_sql.size());
//		dev_path_sql.remove(new Path(v2,10));
//		System.out.println(dev_path_sql.size());
//	}
	
}
