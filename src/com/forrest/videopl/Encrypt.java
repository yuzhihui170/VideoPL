package com.forrest.videopl;

public class Encrypt {
	static {
		System.loadLibrary("encrypt");
	}
	/**����1:��ȷ,���ظ���������*/
	public native static int native_encrypt(String plainFile, String keyStr, String cipherFile);
	public native static int native_decrypt(String cipherFile, String keyStr, String plainFile);
}
