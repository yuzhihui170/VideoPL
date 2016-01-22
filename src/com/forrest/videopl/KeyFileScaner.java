package com.forrest.videopl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

import android.util.Log;

public class KeyFileScaner {
	private final static String TAG = "KeyFileScaner";

	/**存放密码本的绝对路径*/
	private String mkeyPath ;
	private static final String rootPath = "/mnt";
	/** 默认不扫描的目录 */
	private static String[] notScanFileName = {"tencent","QQ","taobao","Music","Movies","BaiduCloudBackup"};
	
	/**sdcard根目录*/
	private static String[] sdcardDic = {"/storage/sdcard0","/storage/sdcard1"};
	
	public String getKeyPath() {
		return mkeyPath;
	}

	public void reset() {
		mkeyPath = null; 
	}

	// 扫描指定目录下的文件指定的文件类型
//	public void scanSDCardFile(String path, String type) {
//		reset();
//	}

	// 扫描指定目录下的文件指定,保存指定name的文件
	public void scanPathFile(String path,String name) {
		reset();
		scanFiles(path,name);
	}

	// 扫描指定路径下的文件
	public synchronized void scanFiles(String Path,String name) {
		File rootFile = new File(Path);
		/**标识当前目录是否扫描*/
		boolean scanDic = true;
		File[] files = rootFile.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				scanDic = true;
				File file = files[i];
				
				if (file.isFile()) {
					if(chooseVideo(file,name)) { //找到了password就退出扫描
						break;
					}
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
						scanFiles(file.getPath(),name);
					}
				}
			}
		}
	}

	/**将视频保存到容器中*/
	private void saveVideo(File file) {
		mkeyPath = file.getPath();
	}

	/**判断是否是视频文件并保存 */
	private synchronized boolean chooseVideo(File file,String name) {
		if (file == null) {
			return false;
		}
		String path = file.getPath().toLowerCase();
		if (path.contains(name)) {
			saveVideo(file);
			return true;
		}
		return false;
	}

	// 检查文件是否任然存在
	public boolean fileExist(String filePath) {
		File file = new File(filePath);
		if (file.exists())
			return true;
		return false;
	}
	

}