package com.forrest.videopl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

import android.util.Log;

public class VideoFileScaner {
	private final static String TAG = "SdcardFileScaner";
	//分别存放视频文件路径容器
	private ArrayList<String> videoPathList = new ArrayList<String>();
	//分别存放APK文件路径容器
	private ArrayList<String> apkPathList = new ArrayList<String>();

	private static final String rootPath = "/mnt";
	/**第一次只扫描指定目录*/
//	private static final String firstPath = "/storage/sdcard0/3D视频";

	private static String[] videoTypes = {".mp4", ".3gp", ".avi", ".mpeg", ".flv", ".mkv", ".mov" ,".wmv", ".ts"};

	/** 默认不扫描的目录 */
	private static String[] notScanFileName = {"tencent","QQ","taobao","Music","Movies","BaiduCloudBackup"};
	
	/**sdcard根目录*/
	private static String[] sdcardDic = {"/storage/sdcard0","/storage/sdcard1"};
		
	public ArrayList<String> getVideoList() {
		return videoPathList;
	}
	
	public ArrayList<String> getApkList() {
		return apkPathList;
	}
	public void reset() {
		videoPathList.clear(); 
	}

	// 扫描指定目录下的文件指定的文件类型
//	public void scanSDCardFile(String path, String type) {
//		reset();
//	}

	// 扫描指定目录下的文件指定
	public void scanPathFile(String path) {
		reset();
		scanFiles(path);
		VCompareAlgorithm compare = new VCompareAlgorithm();
		Collections.sort(videoPathList, compare);
	}

	// 扫描指定路径下的文件
	public synchronized void scanFiles(String Path) {
		File rootFile = new File(Path);
		/**标识当前目录是否扫描*/
		boolean scanDic = true;
		File[] files = rootFile.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				scanDic = true;
				File file = files[i];
				if (file.isFile()) {
					chooseVideo(file);
					chooseAPK(file); //保存APK
				}else if (file.isDirectory() && file.getPath().indexOf("/.") == -1){ // 必须排除本目录和上层目录   (同时排除了以.开头的文件和目录的扫描)
					//如果是父目录是 /mnt/sdcard 或者 /mnt/sdcard2 就进行判断目录名是否在不扫描目录之列,
					if(file.getParent().equals(sdcardDic[0]) || file.getParent().equals(sdcardDic[1]) ) { 
						for(int j=0; j<notScanFileName.length; j++) {     //判断目录名是在不扫描目录之列
							if(notScanFileName[j].equals(file.getName())) {
								scanDic = false;
								Log.v(TAG,"not scan DicName = " + file.getPath());
								break;
							}
						}
					}
					if(scanDic) {
						scanFiles(file.getPath());
					}
				}
			}
		}
	}

	/**将视频保存到容器中*/
	private void saveVideo(File file) {
		videoPathList.add(file.getPath());
	}

	/**判断是否是视频文件并保存 */
	private synchronized void chooseVideo(File file) {
		if (file == null) {
			return;
		}
		String path = file.getPath().toLowerCase();
		for (String suffix : videoTypes) {
			if (path.endsWith(suffix) && chooseVideoName(path)) {
				saveVideo(file);
			}
		}
	}

	/**判断视频是否以v0 或者 v1 V0 V1*/
	private synchronized boolean chooseVideoName(String videoName) {
		Log.d("yzh","videoName = " + videoName);
		char ch0;
		char ch1;
		int index = 0;
		index = videoName.lastIndexOf('/');
		if( (videoName.charAt(index+1) == '0' || videoName.charAt(index+1) == '1')) {
			return true;
		}else {
			return false;
		}
	}
	
	/**判断是否是apk文件并保存 */
	private synchronized void chooseAPK(File file) {
		if (file == null) {
			return;
		}
		String path = file.getPath().toLowerCase();
			if (path.endsWith("apk")) {
				saveAPK(file);
		}
	}
	
	/**将APK保存到容器中*/
	private void saveAPK(File file) {
		apkPathList.add(file.getPath());
	}
	
	// 检查文件是否任然存在
	public boolean fileExist(String filePath) {
		File file = new File(filePath);
		if (file.exists())
			return true;
		return false;
	}
	

}