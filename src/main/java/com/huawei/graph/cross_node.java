package com.huawei.graph;

public class cross_node {
	public int cross_id;
	public cross_node next_node;
	public cross_node up_node,right_node,down_node,left_node;
	public cross_node(int cross_id) {
		this.cross_id = cross_id;
		next_node=null;
		
		this.up_node = null;
		this.right_node = null;
		this.down_node = null;
		this.left_node = null;
	}
	
}
