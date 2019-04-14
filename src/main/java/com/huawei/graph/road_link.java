package com.huawei.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import com.huawei.utils.Direction;
import com.huawei.vehicle.car;

public class road_link {
	public static Random r=new Random();
	public int road_id;
	public int length;//道路长度
	public double cost;//道路花费
	public double cost_tmp;
	public int speed_limit;
	public int lane_num;//车道数
	public int src_id,dst_id;//源路口id,目的路口id
	public int used_cnt;//在某个时间段当前road_link被使用的次数
	public ArrayList<Integer> used_list;
	public double degree;
	public road_link(int road_id, int length, int speed_limit, int lane_num, int src_id, int dst_id) {
		super();
		this.road_id = road_id;
		this.length = length;
		this.speed_limit = speed_limit;
		this.lane_num = lane_num;
		this.src_id = src_id;
		this.dst_id = dst_id;
		this.used_cnt=0;
//		this.cost=Math.sqrt(Math.sqrt(length/(double)speed_limit/(double)lane_num));
		this.cost=Math.pow(length/Math.pow((double)speed_limit,0.8)/Math.pow((double)lane_num,1), 1.0/3);
		this.cost_tmp=this.cost;
		this.used_list=new ArrayList<>();
	}
	
	public road_link(int road_id) {
		this.road_id=road_id;
		this.used_cnt=0;
		this.used_list=new ArrayList<>();
	}
	
	public void update_cost(double car_num) {
		cost*=(1+44/(double)car_num);
	}
	
	public void update_cost2(double car_num) {
		if(used_list.size()<=500) cost*=(1+33/(double)car_num);
		else {
			int sum=0;
			for(int tmp:used_list.subList(used_list.size()-1-500, used_list.size()-1)) {
				sum+=tmp;
			}
			cost*=(1+33/(double)car_num+sum/((double)car_num*Math.exp(1)));
		}
	}
	
}
