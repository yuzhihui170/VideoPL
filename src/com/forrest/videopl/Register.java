package com.forrest.videopl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

public class Register {
//	private final static String cidPath = "/sys/block/mmcblk1/device/cid"; //系统cid路径   /sys/block/mmcblk0/device
	//275048534431364730da94d58d00e9b9  02544d5341303247039c5ff20f00999f 1247542020202020100000530e00b4b1
	private final static String usbModelPath = "/sys/block/sda/device/model";
	private final static String matchUsbModel = "Cruzer Fit"; //指定U盘型号
	private final static String matchUsbModel1 = "Cruzer"; //指定U盘型号
//	private String matchCidPath = "/mnt/external_sd/id"; //sd卡cid路径
	private final static String password = "password";
	private String passwordPath;
	private String mingmaPath_ttt;   //保存解密后的文件路径
	private String mingmaPath_mmm;//保存解密后的又修改了的明码文件,mmm
	private String mingmaPath_directory ; //密码目录
	private BufferedReader in;
	private BufferedWriter out;
	private boolean mHaspassword = true;
	
	public boolean readCID(String matchCidPath) {
		BufferedReader in_system;
		BufferedReader in_sdcard;
		String cid_sys;
		String cid_sdcard;
		try {
//			in_system = new BufferedReader(new FileReader(cidPath)); //系统cid
//			in_sdcard = new BufferedReader(new FileReader(matchCidPath)); //sd cid
//			cid_sys =  in_system.readLine();
//			cid_sdcard = in_sdcard.readLine();
//			Log.v("yzh","cid_sys = " + cid_sys);
//			Log.v("yzh","cid_sdcard = " + cid_sdcard);
//			if(cid_sys.equals(cid_sdcard)) {
//				return true;
//			}
			in_system = new BufferedReader(new FileReader(usbModelPath)); //u盘型号
			cid_sys =  in_system.readLine();
			Log.d("yzh","USB Model = " + cid_sys + "xxx");
			if(cid_sys.trim().equals(matchUsbModel) || cid_sys.trim().equals(matchUsbModel1)) {
				return true;
			}
		} catch (IOException e) {
			Log.v("yzh",e.toString());
			e.printStackTrace();
			return false;
		}
		return false;
	}
	/**-2:没有使用指定sd卡,  返回-1:没有密码本,  0注册失败,   1注册成功
	 *  -3:已经使用过的注册码   
	 *  -4:解密出错
	 *  */
	public synchronized int register(String key_t) {
		
		if(key_t.equals("s500")) {  //内部测试使用的注册码
			return 1;
		}
		int ret = 0;
		KeyFileScaner keyFileScaner = new KeyFileScaner();
//		keyFileScaner.scanPathFile("/mnt/external_sd",password);
		keyFileScaner.scanPathFile("/mnt/usb_storage",password);
		passwordPath = keyFileScaner.getKeyPath();
		if(passwordPath == null) {
			return -1;
		}
		Log.v("yzh","passwordPath = " + passwordPath);
		int lastIndex = passwordPath.lastIndexOf('/'); 
		mingmaPath_directory = passwordPath.substring(0, lastIndex); //不包含'/'
		mingmaPath_ttt = mingmaPath_directory + "/ttt"; //获取ttt的路径
		mingmaPath_mmm = mingmaPath_directory + "/mmm";
//		matchCidPath = mingmaPath_directory + "/id";
		Log.v("yzh","mingmaPath_ttt = " + mingmaPath_ttt);
		Log.v("yzh","mingmaPath_mmm = " + mingmaPath_mmm);
//		Log.v("yzh","matchCidPath = " + matchCidPath);
		
		Log.v("yzh","key_t =" + key_t);
		if(key_t.length() == 0 || key_t == null) {
			return 0;
		}
		
//		if(!readCID(usbModelPath)) {
//			return -2;
//		}
		
		/**调用c代码进行password文件解密,并且删除password,解密生成的文件名为ttt*/
		int tmp;
		tmp = Encrypt.native_decrypt(passwordPath, "helloworld", mingmaPath_ttt);
		if(tmp != 1 ) {
			return -4;
		}
//		Log.v("yzh","tmp = " + tmp);
		
		File passwordFile = new File(passwordPath);
		if(passwordFile.exists()) {
			passwordFile.delete();
			Log.v("yzh","old password has be deleted!");
		}
		
		try {
			in = new BufferedReader(new FileReader(mingmaPath_ttt));
			out = new BufferedWriter(new FileWriter(new File(mingmaPath_mmm))); //创建临时文件mmm
		} catch (IOException e) {
			Log.v("yzh",e.toString());
			e.printStackTrace();
		}
		if(in == null || out == null) {
			return 0;
		}
		Log.v("yzh","----");
		String readCode;
		try {
			while((readCode = in.readLine()) != null) {
//				Log.v("yzh","readCode =" + readCode );
				if(readCode.equals(key_t)) { //注册后的密码加上#*#*
					out.write(readCode + "#*#*");
					out.newLine();
					ret = 1;
				}else if(readCode.equals(key_t+"#*#*")) {
					out.write(readCode);
					out.newLine();
					ret = -3; //已经使用过的注册码
				}else {
					out.write(readCode); //保存密码本
					out.newLine();
					continue;
				}
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//删除明码文件ttt
		File mingmaFile_ttt = new File(mingmaPath_ttt);
		if(mingmaFile_ttt.exists()) {
			mingmaFile_ttt.delete();
			Log.d("yzh","ttt has be deleted!");
		}
		//加密修改后的明码文件mmm
		Encrypt.native_encrypt(mingmaPath_mmm, "helloworld", passwordPath);
		//删除明码文件mmm
		File mingmaFile_mmm = new File(mingmaPath_mmm);
		if(mingmaFile_mmm.exists()) {
			mingmaFile_mmm.delete();
			Log.d("yzh","mmm has be deleted!");
		}
		return ret; 
	}
	
}
