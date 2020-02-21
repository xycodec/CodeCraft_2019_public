package com.huawei.vehicle;

import java.util.Comparator;

public class CarComparator implements Comparator<Car>{

	@Override
	public int compare(Car c1, Car c2) {
		if(c1.real_time>c2.real_time) return 1;
		else if(c1.real_time>c2.real_time) return -1;
		else {
			return 0;
		}
	}
	
}
