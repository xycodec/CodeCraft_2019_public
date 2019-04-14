package com.huawei.utils;

public class link_pair implements Cloneable{
	public int src_id,dst_id;
	public link_pair(int src_id, int dst_id) {
		super();
		this.src_id = src_id;
		this.dst_id = dst_id;
	}
	
	@Override
	public int hashCode() {
		return (src_id+11)*(dst_id+13)+src_id+dst_id+17;
	}
	
	@Override
	public boolean equals(Object obj) {
		link_pair lp=(link_pair)obj;
		if(lp.src_id==src_id&&lp.dst_id==dst_id) return true;
		else return false;
	}
	
	@Override
	public link_pair clone() {
		link_pair pos=null;
		try {
			pos=(link_pair)super.clone();
		}catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return pos;
	}
}
