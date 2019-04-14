package com.huawei.utils;

public class node_pair implements Cloneable{
	public int src_id,dst_id;
	public node_pair(int src_id, int dst_id) {
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
		node_pair np=(node_pair)obj;
		if(np.src_id==src_id&&np.dst_id==dst_id) return true;
		else return false;
	}
	
	@Override
	public node_pair clone() {
		node_pair pos=null;
		try {
			pos=(node_pair)super.clone();
		}catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return pos;
	}

}
