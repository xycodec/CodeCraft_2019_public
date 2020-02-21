package com.huawei.utils;

public class LinkPair implements Cloneable{
	public int src_id,dst_id;
	public LinkPair(int src_id, int dst_id) {
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
		LinkPair lp=(LinkPair)obj;
		if(lp.src_id==src_id&&lp.dst_id==dst_id) return true;
		else return false;
	}
	
	@Override
	public LinkPair clone() {
		LinkPair pos=null;
		try {
			pos=(LinkPair)super.clone();
		}catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return pos;
	}
}
