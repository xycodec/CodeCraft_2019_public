package com.huawei.vehicle;

import java.util.Comparator;

public class car_comparator implements Comparator<car>{

	@Override
	public int compare(car c1, car c2) {
		if(c1.real_time>c2.real_time) return 1;
		else if(c1.real_time>c2.real_time) return -1;
		else {
			return 0;
		}
	}
	
}
