package com.huawei.utils;

public class NodePair implements Cloneable{
	public int src_id,dst_id;
	public NodePair(int src_id, int dst_id) {
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
		NodePair np=(NodePair)obj;
		if(np.src_id==src_id&&np.dst_id==dst_id) return true;
		else return false;
	}
	
	@Override
	public NodePair clone() {
		NodePair pos=null;
		try {
			pos=(NodePair)super.clone();
		}catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return pos;
	}

}
