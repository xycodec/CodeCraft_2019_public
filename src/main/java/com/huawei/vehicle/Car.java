package com.huawei.vehicle;

import com.huawei.utils.Path;

public class Car implements Comparable{
	public int car_id;
	public int car_length;//车身长度,默认为1
	public int max_speed;//车子的最大速度
	public int start_time;//计划启动时间
	public boolean is_hard;//是否是频繁车子
	public int priority;
	public int preset;
	public int real_time;//实际启动时间
	
	public int from,to;//起点,终点
	
	public Path path;//车子的规划路线
	
	public Car() {
		car_length=1;
		is_hard=false;
	}


	@Override
	public int compareTo(Object o) {
		Car c=(Car)o;
		if(this.start_time>c.start_time) return 1;//start_time从小到大
		else if(this.start_time<c.start_time) return -1;
		else {
			if(this.max_speed>c.max_speed) return 1;//max_speed从大到小???慢的车先走更优化???
			else if(this.max_speed<c.max_speed) return -1;
			else return 0;
		}
	}
}







