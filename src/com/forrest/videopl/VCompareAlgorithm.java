package com.forrest.videopl;

import java.util.Comparator;

public class VCompareAlgorithm implements Comparator<String> {
	public int compare(String obj1, String obj2) {
			return obj1.compareTo(obj2);
		}
}
