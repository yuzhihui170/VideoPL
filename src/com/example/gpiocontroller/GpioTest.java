package com.example.gpiocontroller;

import android.util.Log;


public class GpioTest {

//	public static boolean loadGpio() {
//		try {
//			//System.load("/system/lib/libcom_android_power_myself.so");
//			System.load("/system/lib/libsoundCardOp.so");
//			return true;
//		} catch (UnsatisfiedLinkError e) {
//		}
//		return false;
//	}
	static {
		try {
		System.load("libsoundCardOp.so");
		}catch(Exception e) {
			Log.e("yzh",e.toString());
			e.printStackTrace();
		}
	}
	public static native int getGpio();
	public static native int writeGpio(char cmd);
}