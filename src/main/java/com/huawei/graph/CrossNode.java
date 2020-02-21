package com.huawei.graph;

public class CrossNode {
	public int cross_id;
	public CrossNode next_node;
	public CrossNode up_node,right_node,down_node,left_node;
	public CrossNode(int cross_id) {
		this.cross_id = cross_id;
		next_node=null;
		
		this.up_node = null;
		this.right_node = null;
		this.down_node = null;
		this.left_node = null;
	}
	
}
