package com.forrest.videopl;

public class Encrypt {
	static {
		System.loadLibrary("encrypt");
	}
	/**返回1:正确,返回负数出错了*/
	public native static int native_encrypt(String plainFile, String keyStr, String cipherFile);
	public native static int native_decrypt(String cipherFile, String keyStr, String plainFile);
}
